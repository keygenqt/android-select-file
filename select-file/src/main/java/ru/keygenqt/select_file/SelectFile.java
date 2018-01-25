package ru.keygenqt.select_file;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.pinxter.activity.ActivitySelect;
import me.pinxter.dialog.DialogCustom;
import rx.functions.Action1;

public class SelectFile {

    private SelectFileProperties properties;
    private ActivitySelect activity;
    private List<String> result = new ArrayList<>();
    private DialogCustom dialog;
    private Action1<List<String>> response;
    private Action1<String> error;

    private Adapter.OnClickFolderListener listener = (isFolder, path) -> {
        if (isFolder) {
            openFolder(path);
        } else {
            if (getResult().contains(path)) {
                getResult().remove(path);
            } else {
                File file = new File(path);
                if (properties.max_files_count < getResult().size()) {
                    error.call("Max count upload file: " + properties.max_files_count + ".");
                }
                else if (properties.max_files_size < file.length()) {
                    error.call("Max size file: " + Helper.humanReadableByteCount(properties.max_files_size, true) + "!");
                } else {
                    addResult(path);
                    if (properties.max_files_count <= 1) {
                        response.call(getResult());
                        dialog.dismiss();
                    }
                }
            }
        }
    };

    public SelectFile(ActivitySelect activity) {
        this.activity = activity;
        this.properties = new SelectFileProperties();
    }

    public SelectFile(ActivitySelect activity, SelectFileProperties properties) {
        this.activity = activity;
        this.properties = properties;
        result = properties.selected;
    }

    public void show(Action1<List<String>> response, Action1<String> error) {

        this.response = response;
        this.error = error;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point(); display.getSize(size);
        int height = size.y / 2 - (int) (65 * Resources.getSystem().getDisplayMetrics().density);

        if (dialog == null) {
            dialog = new DialogCustom(activity, R.layout.dialog_files);
            dialog.getView().findViewById(R.id.files_list).getLayoutParams().height = height;
            dialog.getView().findViewById(R.id.linearLayoutClose).setOnClickListener(v -> dialog.dismiss());
        }

        dialog
                .setOnBeforeShow(d -> openFolder("/"))
                .setAnim(DialogCustom.ANIM_BOTTOM_SLIDE).showBottom();
    }

    private void openFolder(String path) {

        ImageView close = (ImageView) dialog.getView().findViewById(R.id.files_close);

        close.setImageResource(R.drawable.select_file_img_file_update);
        close.setOnClickListener(null);

        new Timer().schedule(new TimerTask() {@Override public void run() {activity.runOnUiThread(() -> {

            List<Adapter.Item> list = new ArrayList<>();

            File directory = new File(path);
            File[] files = directory.listFiles();
            String dir = directory.getParent();

            if (path.equals("/") || path.equals("/storage")) {

                if (properties.choosePhoto) {
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_camera), "To make a photo",
                            v -> Helper.createImage(activity, p -> {
                                getResult().clear();
                                addResult(p);
                                response.call(getResult());
                                dialog.dismiss();
                            })));
                }

                list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_server), "Phone storage",
                        v -> openFolder(Environment.getExternalStorageDirectory().getAbsolutePath())));

                File storage = new File("/storage/");
                for (File file : storage.listFiles()) {
                    if (file.isHidden() || !file.canRead() || !file.isDirectory()) {
                        continue;
                    }
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_memory_external), "CD card",
                            v -> openFolder(file.getAbsolutePath())));
                }
            }
            else if (directory.canRead()) {

                if (dir != null) {
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_img_dir_open), "...", dir, true, false, false));
                }

                for (int i = 0; i < files.length; i++) {
                    if (files[i].isHidden()) {
                        continue;
                    }
                    String mime = Helper.getMimeType(Uri.fromFile(files[i]));
                    list.add(new Adapter.Item(
                            files[i].isDirectory() ? BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_img_dir_close) : Helper.getIcon(activity, mime, files[i]),
                            files[i].getName(),
                            files[i].getAbsolutePath(),
                            files[i].isDirectory(),
                            properties.selected.contains(files[i].getAbsolutePath()),
                            !properties.mimes.contains(mime)
                    ));
                }

            }
            else {
                if (dir != null) {
                    openFolder(dir);
                }
            }

            Adapter adapter = new Adapter(properties.max_files_count > 1 ? Adapter.TYPE_MULTI : Adapter.TYPE_ONE, list, listener);
            ((ListView) dialog.getView().findViewById(R.id.files_list)).setAdapter(adapter);
            close.setOnClickListener(v -> {
                response.call(getResult());
                dialog.dismiss();
            });

            if (properties.max_files_count > 1) {
                close.setVisibility(path.equals("/") || path.equals("/storage") ? View.GONE : View.VISIBLE);
                close.setImageResource(R.drawable.select_file_img_done);
            } else {
                close.setImageResource(R.drawable.select_file_img_close);
            }

        });}}, 10);
    }

    private void addResult(String val) {
        result.add(val);
    }

    private List<String> getResult() {
        return result;
    }
}
