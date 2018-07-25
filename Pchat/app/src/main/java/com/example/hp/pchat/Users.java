package com.example.hp.pchat;

/**
 * Created by hp on 18-12-2017.
 */
//this is the model class
public class Users {
    public String name,image,status,thumb_image;

    public Users(){
        //if we dont use this empty constructor the app may crash
    }

    public Users(String name, String image, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image=thumb_image;
    }


    public String getName() {
        return name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
