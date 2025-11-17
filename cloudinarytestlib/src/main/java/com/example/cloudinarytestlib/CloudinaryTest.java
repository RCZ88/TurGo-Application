package com.example.cloudinarytestlib;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryTest {
    public static void main(String[] args) {
        String []banners = {"banner_blue.png", "banner_green.png", "banner_red.png"};
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "daccry0jr",
                "api_key", "987677119757127",
                "api_secret", "qHMUfR5ZZQjqJIldJqtViW7YDWw"));
//        for(String banner : banners){
            try{
                File file = new File("C:\\Users\\ASUS\\StudioProjects\\TurGo-Application\\app\\src\\main\\res\\drawable\\chalkboard_user.png");
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

                System.out.println("Upload Result:");
                System.out.println(uploadResult);

                // 🌐 Get the file URL
                String url = (String) uploadResult.get("secure_url");
                System.out.println("File URL: " + url);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//        }

    }
}