package ru.keygenqt.select_file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;
import com.rey.material.widget.RadioButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Adapter extends BaseAdapter {

    final static public String TAG = "Adapter";

    private Context context;
    private int maxCount;
    private List<Object> items;
    private List<String> result = new ArrayList<>();
    private OnOpenDir listener;
    private HashMap<String, RadioButton> buttons = new HashMap<>();

    public Adapter(int maxCount, List<String> selected, OnOpenDir listener) {
        this.maxCount = maxCount;
        this.result = selected;
        this.listener = listener;
    }

    public void updateItem(List<Object> val) {
        items = val;
        notifyDataSetChanged();
    }

    public List<String> getResult() {
        return result;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Holder holder;
        if (view == null) {
            context = viewGroup.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.dialog_files_item, viewGroup, false);
            holder = new Holder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        if (items.get(i).getClass() == File.class) {
            holder.initFile((File) items.get(i), listener);
        } else {
            holder.initItem((Item) items.get(i));
        }

        return view;
    }

    public interface OnOpenDir {
        void onOpenDir(String path);
    }

    public static class Item {

        private Bitmap icon;
        private String name;
        private View.OnClickListener listener;

        public Item(Bitmap icon, String name, View.OnClickListener listener) {
            this.icon = icon;
            this.name = name;
            this.listener = listener;
        }
    }

    public class Holder {

        private View block;
        private View block2;
        private ImageView icon;
        private TextView title;
        private CheckBox checkBox;
        private RadioButton radioButton;

        Holder(View view) {
            block = (View) view.findViewById(R.id.item_files_block);
            block2 = (View) view.findViewById(R.id.item_files_block2);
            icon = (ImageView) view.findViewById(R.id.item_files_icon);
            title = (TextView) view.findViewById(R.id.item_files_title);
            checkBox = (CheckBox) view.findViewById(R.id.item_list_files_checkbox);
            radioButton = (RadioButton) view.findViewById(R.id.item_list_files_radio);
        }

        private void initFile(File file, OnOpenDir l) {

            View.OnClickListener listener = null;

            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.GONE);

            icon.setImageBitmap(getIcon(file));
            title.setText(file.getName());

            if (file.isDirectory()) {
                listener = v -> l.onOpenDir(file.getAbsolutePath());
            } else {
                if (maxCount > 1) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setCheckedImmediately(result.contains(file.getAbsolutePath()));
                    listener = v -> {

                        int count = result.size();

                        if (checkBox.isChecked()) {
                            result.remove(file.getAbsolutePath());
                        } else {
                            if (result.size() >= maxCount) {
                                Log.e(TAG, "Max count upload file: " + maxCount + ".");
                            }
                            else {
                                result.add(file.getAbsolutePath());
                            }
                        }
                        if (count != result.size()) {
                            checkBox.toggle();
                        }
                    };
                } else {
                    radioButton.setVisibility(View.VISIBLE);
                    buttons.put(file.getAbsolutePath(), radioButton);
                    radioButton.setCheckedImmediately(result.contains(file.getAbsolutePath()));
                    listener = v -> {
                        if (result.isEmpty()) {
                            result.add(file.getAbsolutePath());
                            radioButton.setChecked(true);
                        } else if (result.get(0).equals(file.getAbsolutePath())) {
                            result.clear();
                            radioButton.setChecked(false);
                        } else {
                            buttons.get(result.get(0)).setChecked(false);
                            result.clear();
                            result.add(file.getAbsolutePath());
                            radioButton.setChecked(true);
                        }
                    };
                }
            }

            block.setOnClickListener(listener);
            block2.setOnClickListener(listener);
        }

        private void initItem(Item model) {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.GONE);
            icon.setImageBitmap(model.icon);
            title.setText(model.name);
            block.setOnClickListener(model.listener);
        }

        private Bitmap getIcon(File file) {
            return file.isDirectory() ?
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.select_file_img_dir_close) :
                    Helper.getIcon(context, Helper.getMimeType(Uri.fromFile(file)), file);
        }
    }
}
