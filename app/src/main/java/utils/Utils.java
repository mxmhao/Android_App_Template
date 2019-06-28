package utils;

import android.media.MediaMetadataRetriever;
import android.webkit.MimeTypeMap;

public class Utils {
    //根据文件路径获取MIME
    public static String getMIME(String path) {
        //方式一：
        String extension = MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase();
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        //方式二：
        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);*/

        return mime;
    }
}
