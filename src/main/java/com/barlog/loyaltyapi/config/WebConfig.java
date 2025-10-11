package com.barlog.loyaltyapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Injectăm calea de upload din application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obținem calea absolută către folderul de upload
        // 'user.dir' este directorul rădăcină al proiectului
        String uploadPath = "file:" + System.getProperty("user.dir") + "/" + uploadDir + "/";

        // Măpăm URL-ul public la calea fizică de pe disc
        // Când o cerere vine la /uploads/images/nume_fisier.jpg,
        // Spring va căuta fișierul la C:/calea/proiectului/uploads/images/nume_fisier.jpg
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(uploadPath);
    }
}