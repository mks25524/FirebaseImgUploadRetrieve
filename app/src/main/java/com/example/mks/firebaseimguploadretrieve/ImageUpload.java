package com.example.mks.firebaseimguploadretrieve;

/**
 * Created by mks on 10/23/2017.
 */

public class ImageUpload {
    private String name;
    private String url;

    public ImageUpload(String name, String url) {
        this.name = name;
        this.url = url;
    }
    public ImageUpload(){

    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
