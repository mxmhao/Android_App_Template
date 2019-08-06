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

        //这个是用来获取媒体的元信息的，无法获取其他MIME，只能获取音乐，视频等MIME
        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);//path文件要真实存在，远程、本地都可，否则报错
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);*/
        return mime;
    }
}
