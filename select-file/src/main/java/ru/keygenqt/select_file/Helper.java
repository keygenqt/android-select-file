package ru.keygenqt.select_file;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.lang.reflect.Method;

import me.pinxter.activity.ActivitySelect;
import rx.functions.Action1;

public class Helper {

    public static String getMimeType(Uri uri) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type == null ? "" : type.replace("\"", "");
    }

    public static Bitmap getIcon(Activity activity, String mime, File file) {

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_file);

        switch (mime) {
            case SelectFileMime.MIME_DOC:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_doc);
                break;
            case SelectFileMime.MIME_PDF:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_pdf);
                break;
            case SelectFileMime.MIME_PPT:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_ppt);
                break;
            case SelectFileMime.MIME_TXT:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_txt);
                break;
            case SelectFileMime.MIME_XLS:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_xls);
                break;
            case SelectFileMime.MIME_ZIP:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_zip);
                break;
            case SelectFileMime.MIME_RAR:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_rar);
                break;
            case SelectFileMime.MIME_MP4:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_mp4);
                break;
            case SelectFileMime.MIME_MP3:
                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.select_file_mp3);
                break;
            case SelectFileMime.MIME_JPG:
                bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getPath()), 150, 150);
                break;
            case SelectFileMime.MIME_PNG:
                bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getPath()), 150, 150);
                break;
            default:
                Log.e("TAG", mime);
        }

        return bitmap;
    }

    public static void createImage(ActivitySelect activity, Action1<String> action1) {

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        File photo = new File(getPicturesDir(), System.currentTimeMillis() + ".jpg");
        Intent intentImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        activity.startActivityForResult(intentImage, 666);
        activity.removeActivityResult(666);
        activity.addActivityResult(666, (resultCode, intent) -> {
            if (resultCode == Activity.RESULT_OK) {
                action1.call(photo.getAbsolutePath());
            }
        });
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getPicturesDir() {
        String path = Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator;
        File file = new File(path);
        file.mkdirs();
        return path;
    }

}
