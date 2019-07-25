package utils;

import android.webkit.MimeTypeMap;

public class Utils {
    //根据文件路径获取MIME
    public static String getMIME(String path) {
        //方式一：
        //不建议使用getFileExtensionFromUrl方法，它总是返回空，应该是google的问题
        String extension = MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase();
        //这个还是可以用的
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        //方式二：
        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);*/

        return mime;
    }
}
