package com.example.android.videoplayerjava;

import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;

import java.util.ArrayList;
import java.util.List;

public class JavaMediaCatalog {
    private static JavaMediaCatalog instance = null;
    private List<MediaDescriptionCompat> list = new ArrayList<>();

    private JavaMediaCatalog() {
        list.add(new MediaDescriptionCompat.Builder()
                .setDescription("MP4 loaded over HTTP")
                .setMediaId("1")
                // License - https://peach.blender.org/download/
                .setMediaUri(Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"))
                .setTitle("Short film Big Buck Bunny")
                .setSubtitle("Streaming video")
                .build()
        );
        list.add(new MediaDescriptionCompat.Builder()
                .setDescription("MP4 loaded over HTTP")
                .setMediaId("2")
                // License - https://archive.org/details/ElephantsDream
                .setMediaUri(Uri.parse("https://archive.org/download/ElephantsDream/ed_hd.mp4"))
                .setTitle("Short film Elephants Dream")
                .setSubtitle("Streaming video")
                .build()
        );
        list.add(new MediaDescriptionCompat.Builder()
                .setDescription("MOV loaded over HTTP")
                .setMediaId("3")
                // License - https://mango.blender.org/sharing/
                .setMediaUri(Uri.parse("http://ftp.nluug.nl/pub/graphics/blender/demo/movies/ToS/ToS-4k-1920.mov"))
                .setTitle("Short film Tears of Steel")
                .setSubtitle("Streaming audio")
                .build()
        );
    }

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new JavaMediaCatalog();
        }
    }

    public static JavaMediaCatalog getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    public MediaDescriptionCompat getItemAt(int index){
        return list.get(index);
    }

    public List<MediaDescriptionCompat> getList() {
        return list;
    }
}
