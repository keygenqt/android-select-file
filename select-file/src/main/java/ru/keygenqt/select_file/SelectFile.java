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
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.pinxter.activity.ActivitySelect;
import me.pinxter.dialog.DialogCustom;
import rx.functions.Action1;

public class SelectFile {

    private final Adapter adapter;

    private SelectFileProperties properties;
    private ActivitySelect activity;
    private DialogCustom dialog;
    private Action1<List<String>> response;

    public SelectFile(ActivitySelect activity) {
        this.activity = activity;
        this.properties = new SelectFileProperties();
        adapter = new Adapter(properties.max_files_count, properties.selected, this::openFolder);
    }

    public SelectFile(ActivitySelect activity, SelectFileProperties properties) {
        this.activity = activity;
        this.properties = properties;
        adapter = new Adapter(properties.max_files_count, properties.selected, this::openFolder);
    }

    public void show(Action1<List<String>> response) {

        this.response = response;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point(); display.getSize(size);
        int height = size.y / 2 - (int) (65 * Resources.getSystem().getDisplayMetrics().density);

        if (dialog == null) {
            dialog = new DialogCustom(activity, R.layout.dialog_files);
            dialog.getView().findViewById(R.id.files_list).getLayoutParams().height = height;
            ((TextView) dialog.getView().findViewById(R.id.dialog_files_info)).setText(String.format("Max size: %s, Max count %s", Helper.humanReadableByteCount(properties.max_files_size, true), properties.max_files_count));
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

            List<Object> list = new ArrayList<>();

            File directory = new File(path);
            File[] files = directory.listFiles();
            String dir = directory.getParent();

            if (path.equals("/") || path.equals("/storage")) {

                if (properties.choosePhoto) {
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_camera), "To make a photo",
                            v -> Helper.createImage(activity, p -> {
                                response.call(new ArrayList<String>(){{
                                    add(p);
                                }});
                                dialog.dismiss();
                            })));
                }

                list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_server), "Phone storage",
                        v -> openFolder(Environment.getExternalStorageDirectory().getAbsolutePath())));

                File storage = new File("/storage/");
                for (File file : storage.listFiles()) {
                    if (file.isHidden() ||
                            !file.canRead() ||
                            !file.isDirectory() ||
                            file.getAbsolutePath().equals("/storage/emulated") ||
                            file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                        continue;
                    }
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_memory_external), file.getName(),
                            v -> openFolder(file.getAbsolutePath())));
                }
            }
            else if (directory.canRead()) {
                if (dir != null) {
                    list.add(new Adapter.Item(BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_img_dir_open), "...", v -> {
                        openFolder(dir);
                    }));
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isHidden()) {
                        continue;
                    }
                    String mime = Helper.getMimeType(Uri.fromFile(files[i]));
                    if (files[i].isDirectory() || properties.mimes.contains(mime)) {
                        if (properties.max_files_size >= files[i].length()) {
                            list.add(files[i]);
                        }
                    }
                }
            }
            else {
                if (dir != null) {
                    openFolder(dir);
                }
            }

            adapter.updateItem(list);
            ((ListView) dialog.getView().findViewById(R.id.files_list)).setAdapter(adapter);
            close.setOnClickListener(v -> {
                response.call(adapter.getResult());
                dialog.dismiss();
            });

            close.setVisibility(path.equals("/") || path.equals("/storage") ? View.GONE : View.VISIBLE);
            close.setImageResource(R.drawable.select_file_img_done);

        });}}, 10);
    }
}
