package com.zxt.dlna.dmr;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lantern on 2017/8/12.
 */

public class MediaType {
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String IMAGE = "image";
    public static final String UNKNOWN = "unknown";

    public static String fromUriPath(URI uri){
        return fromPath(uri.getPath());
    }

    public static String fromPath(String path){
        int i = path.lastIndexOf('.')+1;
        if(i == 0 || i>=path.length()){
            return UNKNOWN;
        }
        return fromExt(path.substring(i));
    }

    public static String fromMime(String mime){
        if(mime == null){
            return UNKNOWN;
        }
        int i = mime.indexOf('/');
        if(i > 0){
            String type = mime.substring(0, i);
            if(IMAGE.equals(type) || VIDEO.equals(type) || AUDIO.equals(type)){
                return type;
            }
        }
        return UNKNOWN;
    }

    public static String fromExt(String ext){
        return fromMime(EXT2MIME.get(ext));
    }

    public static String fromContentType(String contentType){
        if(contentType == null){
            return UNKNOWN;
        }
        contentType = contentType.trim();
        String mime = null;
        int i = contentType.indexOf(';');
        if(i == -1){
            mime = contentType;
        } else {
            mime = contentType.substring(0, i).trim();
        }

        return fromMime(mime);
    }

    private static final Map<String, String> EXT2MIME = new HashMap<String, String>(){
        {
            put("3ds","image/x-3ds");
            put("3g2","video/3gpp2");
            put("3gp","video/3gpp");
            put("3gpp","audio/3gpp");
            put("3gpp","video/3gpp");
            put("aac","audio/x-aac");
            put("adp","audio/adpcm");
            put("aif","audio/x-aiff");
            put("aifc","audio/x-aiff");
            put("aiff","audio/x-aiff");
            put("asf","video/x-ms-asf");
            put("asx","video/x-ms-asf");
            put("au","audio/basic");
            put("avi","video/x-msvideo");
            put("bmp","image/bmp");
            put("bmp","image/x-ms-bmp");
            put("btif","image/prs.btif");
            put("caf","audio/x-caf");
            put("cgm","image/cgm");
            put("cmx","image/x-cmx");
            put("djv","image/vnd.djvu");
            put("djvu","image/vnd.djvu");
            put("dra","audio/vnd.dra");
            put("dts","audio/vnd.dts");
            put("dtshd","audio/vnd.dts.hd");
            put("dvb","video/vnd.dvb.file");
            put("dwg","image/vnd.dwg");
            put("dxf","image/vnd.dxf");
            put("ecelp4800","audio/vnd.nuera.ecelp4800");
            put("ecelp7470","audio/vnd.nuera.ecelp7470");
            put("ecelp9600","audio/vnd.nuera.ecelp9600");
            put("eol","audio/vnd.digital-winds");
            put("f4v","video/x-f4v");
            put("fbs","image/vnd.fastbidsheet");
            put("fh","image/x-freehand");
            put("fh4","image/x-freehand");
            put("fh5","image/x-freehand");
            put("fh7","image/x-freehand");
            put("fhc","image/x-freehand");
            put("flac","audio/x-flac");
            put("fli","video/x-fli");
            put("flv","video/x-flv");
            put("fpx","image/vnd.fpx");
            put("fst","image/vnd.fst");
            put("fvt","video/vnd.fvt");
            put("g3","image/g3fax");
            put("gif","image/gif");
            put("h261","video/h261");
            put("h263","video/h263");
            put("h264","video/h264");
            put("ico","image/x-icon");
            put("ief","image/ief");
            put("jng","image/x-jng");
            put("jpe","image/jpeg");
            put("jpeg","image/jpeg");
            put("jpg","image/jpeg");
            put("jpgm","video/jpm");
            put("jpgv","video/jpeg");
            put("jpm","video/jpm");
            put("kar","audio/midi");
            put("ktx","image/ktx");
            put("lvp","audio/vnd.lucent.voice");
            put("m1v","video/mpeg");
            put("m2a","audio/mpeg");
            put("m2v","video/mpeg");
            put("m3a","audio/mpeg");
            put("m3u","audio/x-mpegurl");
            put("m4a","audio/mp4");
            put("m4a","audio/x-m4a");
            put("m4u","video/vnd.mpegurl");
            put("m4v","video/x-m4v");
            put("mdi","image/vnd.ms-modi");
            put("mid","audio/midi");
            put("midi","audio/midi");
            put("mj2","video/mj2");
            put("mjp2","video/mj2");
            put("mk3d","video/x-matroska");
            put("mka","audio/x-matroska");
            put("mks","video/x-matroska");
            put("mkv","video/x-matroska");
            put("mmr","image/vnd.fujixerox.edmics-mmr");
            put("mng","video/x-mng");
            put("mov","video/quicktime");
            put("movie","video/x-sgi-movie");
            put("mp2","audio/mpeg");
            put("mp2a","audio/mpeg");
            put("mp3","audio/mp3");
            put("mp3","audio/mpeg");
            put("mp4","video/mp4");
            put("mp4a","audio/mp4");
            put("mp4v","video/mp4");
            put("mpe","video/mpeg");
            put("mpeg","video/mpeg");
            put("mpg","video/mpeg");
            put("mpg4","video/mp4");
            put("mpga","audio/mpeg");
            put("mxu","video/vnd.mpegurl");
            put("npx","image/vnd.net-fpx");
            put("oga","audio/ogg");
            put("ogg","audio/ogg");
            put("ogv","video/ogg");
            put("pbm","image/x-portable-bitmap");
            put("pct","image/x-pict");
            put("pcx","image/x-pcx");
            put("pgm","image/x-portable-graymap");
            put("pic","image/x-pict");
            put("png","image/png");
            put("pnm","image/x-portable-anymap");
            put("ppm","image/x-portable-pixmap");
            put("psd","image/vnd.adobe.photoshop");
            put("pya","audio/vnd.ms-playready.media.pya");
            put("pyv","video/vnd.ms-playready.media.pyv");
            put("qt","video/quicktime");
            put("ra","audio/x-pn-realaudio");
            put("ra","audio/x-realaudio");
            put("ram","audio/x-pn-realaudio");
            put("ras","image/x-cmu-raster");
            put("rgb","image/x-rgb");
            put("rip","audio/vnd.rip");
            put("rlc","image/vnd.fujixerox.edmics-rlc");
            put("rmi","audio/midi");
            put("rmp","audio/x-pn-realaudio-plugin");
            put("s3m","audio/s3m");
            put("sgi","image/sgi");
            put("sid","image/x-mrsid-image");
            put("sil","audio/silk");
            put("smv","video/x-smv");
            put("snd","audio/basic");
            put("spx","audio/ogg");
            put("sub","image/vnd.dvb.subtitle");
            put("svg","image/svg+xml");
            put("svgz","image/svg+xml");
            put("tga","image/x-tga");
            put("tif","image/tiff");
            put("tiff","image/tiff");
            put("ts","video/mp2t");
            put("uva","audio/vnd.dece.audio");
            put("uvg","image/vnd.dece.graphic");
            put("uvh","video/vnd.dece.hd");
            put("uvi","image/vnd.dece.graphic");
            put("uvm","video/vnd.dece.mobile");
            put("uvp","video/vnd.dece.pd");
            put("uvs","video/vnd.dece.sd");
            put("uvu","video/vnd.uvvu.mp4");
            put("uvv","video/vnd.dece.video");
            put("uvva","audio/vnd.dece.audio");
            put("uvvg","image/vnd.dece.graphic");
            put("uvvh","video/vnd.dece.hd");
            put("uvvi","image/vnd.dece.graphic");
            put("uvvm","video/vnd.dece.mobile");
            put("uvvp","video/vnd.dece.pd");
            put("uvvs","video/vnd.dece.sd");
            put("uvvu","video/vnd.uvvu.mp4");
            put("uvvv","video/vnd.dece.video");
            put("viv","video/vnd.vivo");
            put("vob","video/x-ms-vob");
            put("wav","audio/wav");
            put("wav","audio/wave");
            put("wav","audio/x-wav");
            put("wax","audio/x-ms-wax");
            put("wbmp","image/vnd.wap.wbmp");
            put("wdp","image/vnd.ms-photo");
            put("weba","audio/webm");
            put("webm","video/webm");
            put("webp","image/webp");
            put("wm","video/x-ms-wm");
            put("wma","audio/x-ms-wma");
            put("wmv","video/x-ms-wmv");
            put("wmx","video/x-ms-wmx");
            put("wvx","video/x-ms-wvx");
            put("xbm","image/x-xbitmap");
            put("xif","image/vnd.xiff");
            put("xm","audio/xm");
            put("xpm","image/x-xpixmap");
            put("xwd","image/x-xwindowdump");
        }
    };
}
