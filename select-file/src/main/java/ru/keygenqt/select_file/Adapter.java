package ru.keygenqt.select_file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.system.Os;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Adapter extends BaseAdapter {

    final static public String TAG = "Adapter";

    final static public int TYPE_ONE = 0;
    final static public int TYPE_MULTI = 1;

    private Context context;
    private List<Item> items;
    private OnClickFolderListener onClickListener;
    private int type;

    public Adapter(int type, List<Item> items, OnClickFolderListener onClickListener) {
        this.type = type;
        this.items = items;
        this.onClickListener = onClickListener;
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

        holder.initItem(context, items.get(i), i);

        return view;
    }

    public interface OnClickFolderListener {
        void onClickFolderListener(boolean isFolder, String path);
    }

    public static class Item {

        private String name;
        private Bitmap icon;
        private String path;
        private boolean isFolder;
        private boolean isSelect;
        private boolean disable;
        private View.OnClickListener listener;

        public Item(Bitmap bitmap, String name, View.OnClickListener listener) {
            this.icon = bitmap;
            this.name = name;
            this.listener = listener;
        }

        public Item(Bitmap bitmap, String name, String path, boolean isFolder, boolean isSelect, boolean disable) {
            this.icon = bitmap;
            this.name = name;
            this.path = path;
            this.isFolder = isFolder;
            this.isSelect = isSelect;
            this.disable = disable;
        }
    }

    public class Holder {

        private View block;
        private View block2;
        private ImageView icon;
        private TextView title;
        private TextView subtitle;
        private CheckBox checkBox;

        Holder(View view) {
            block = (View) view.findViewById(R.id.item_files_block);
            block2 = (View) view.findViewById(R.id.item_files_block2);
            icon = (ImageView) view.findViewById(R.id.item_files_icon);
            title = (TextView) view.findViewById(R.id.item_files_title);
            subtitle = (TextView) view.findViewById(R.id.item_files_subtitle);
            checkBox = (CheckBox) view.findViewById(R.id.item_list_files_checkbox);
        }

        private void initItem(Context context, Item model, int position) {
            icon.setImageBitmap(model.icon);
            title.setText(model.name);
            if (!model.name.equals("...")) {
                String time = getDate(model.path);
                subtitle.setVisibility(time.equals("") ? View.GONE : View.VISIBLE);
                subtitle.setText(time);
            } else {
                subtitle.setVisibility(View.GONE);
            }

            checkBox.setVisibility(View.GONE);

            if (model.listener != null) {
                subtitle.setVisibility(View.GONE);
                block.setBackgroundColor(Color.WHITE);
                block.setOnClickListener(model.listener);
            } else {

                View.OnClickListener listener = view -> {
                    onClickListener.onClickFolderListener(model.isFolder, model.path);
                    if (type == TYPE_MULTI) {
                        checkBox.toggle();
                    }
                };

                if (!model.isFolder) {
                    if (model.disable) {
                        block.setBackgroundColor(Color.parseColor("#eeeeee"));
                        block.setOnClickListener(null);
                        checkBox.setVisibility(View.GONE);
                    } else {
                        if (type == TYPE_MULTI) {
                            checkBox.setVisibility(View.VISIBLE);
                            checkBox.setCheckedImmediately(model.isSelect);
                        }
                        block.setBackgroundColor(Color.WHITE);
                        block.setOnClickListener(listener);
                        block2.setOnClickListener(listener);
                    }
                } else {
                    block.setBackgroundColor(Color.WHITE);
                    block.setOnClickListener(listener);
                }
            }
        }

        private String getDate(String path) {
            long st_atime = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    st_atime = Os.lstat(path).st_atime;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Class<?> clazz = Class.forName("libcore.io.Libcore");
                    Field field = clazz.getDeclaredField("os");
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object os = field.get(null);
                    Method method = os.getClass().getMethod("lstat", String.class);
                    Object lstat = method.invoke(os, path);
                    field = lstat.getClass().getDeclaredField("st_atime");
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    st_atime = field.getLong(lstat);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (st_atime == 0L) {
                return "";
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/d/yyyy h:mm a", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date(st_atime);
            return dateFormat.format(date);
        }
    }
}
