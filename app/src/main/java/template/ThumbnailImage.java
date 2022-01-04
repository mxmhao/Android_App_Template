package template;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ThumbnailImage extends Fragment {

    private Context mContext;
    ContentResolver cr;
    private ImageView imageView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        cr = mContext.getContentResolver();
        imageView = null;
    }

    //测试获取缩略图
    public void test() {
        File file = new File("");
        String path = file.getAbsolutePath();
        //1、根据文件路径获取缩略图的id
        if (isPhoto(path)) {
            long id = getMediaId(cr, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, path);

            LoadThumbnailsAsyncTask ltat = new LoadThumbnailsAsyncTask(cr);
            ltat.execute(imageView, id, 1);
        } else if (isVideo(path)) {
            long id = getMediaId(cr, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);

            LoadThumbnailsAsyncTask ltat = new LoadThumbnailsAsyncTask(cr);
            ltat.execute(imageView, id, 2);
        }
    }

    private static final String WHERE = MediaStore.MediaColumns.DATA + "=?";//文件的绝对路径
    private static final String[] COLUMNS = new String[]{MediaStore.MediaColumns._ID};//图片id

    private static long getMediaId(ContentResolver cr, Uri content_uri, String path) {
        Cursor cursor = cr.query(content_uri, COLUMNS, WHERE, new String[]{path}, null);
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }

        return -1;
    }

    public boolean isPhoto(String path) {
        return path.endsWith(".png");
    }

    public boolean isVideo(String path) {
        return path.endsWith(".mp4");
    }

    //异步获取缩略图
    static class LoadThumbnailsAsyncTask extends AsyncTask<Object, Void, Bitmap> {
        private ImageView imageView;
        ContentResolver cr;

        public LoadThumbnailsAsyncTask(ContentResolver cr) {
            this.cr = cr;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            //这是在后台子线程中执行的
            imageView = (ImageView) params[0];
            int id = (int) params[1];
            int type = (int) params[2];
            Bitmap bitmap = null;
            //2、根据缩略图id获取缩略图
            if (1 == type) {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            } else if (2 == type) {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //当任务执行完成是调用,在UI线程
            //取消后这里不会执行
            imageView.setImageBitmap(bitmap);
            imageView.setTag(null);
            imageView = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            imageView = null;
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            imageView = null;
        }
    }

    //根据base64图片创建缩略图
    public static void createThumbImage(Context context, String base64) {
        byte[] decode = Base64.decode(base64.getBytes(), Base64.DEFAULT);
        //缩略图
        if (decode.length > 32 * 1024) {
            Bitmap bmp = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Bitmap thumbBmp = null;
            int baseLength = 150;
            if (width > height) {
                thumbBmp = Bitmap.createScaledBitmap(bmp, baseLength, bmp.getHeight() / (bmp.getWidth() / baseLength), true);
            } else {
                thumbBmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / (bmp.getHeight() / baseLength), baseLength, true);
            }
            bmp.recycle();

            //缩略图转成字节数组
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            thumbBmp.compress(Bitmap.CompressFormat.PNG, 100, output);
            thumbBmp.recycle();
            byte[] result = output.toByteArray();
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
