package com.barlog.loyaltyapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    // Removed: @Value("${file.upload-dir}") private String uploadDir;

    private final Cloudinary cloudinary;

    // Constructor Injection for Cloudinary Configuration
    public FileStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        // Configure Cloudinary using the injected properties
        Map config = ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret);

        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Uploads the given MultipartFile to Cloudinary.
     *
     * @param file The image file to upload.
     * @return The Cloudinary Public ID, which is returned by Cloudinary and should be stored in the DB.
     */

    public String storeRawFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fișierul nu poate fi gol.");
        }

        try {
            // Specificăm resource_type: "raw" pentru fișiere non-media (APK)
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "raw",
                    "use_filename", true,
                    "unique_filename", false
            ));

            // Returnăm URL-ul complet securizat (https link direct)
            return (String) uploadResult.get("secure_url");

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Eroare la încărcarea fișierului pe Cloudinary.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }

        try {
            // Upload the file bytes to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Return the Public ID, which is the unique reference on Cloudinary
            return (String) uploadResult.get("public_id");

        } catch (IOException ex) {
            // Log the exception properly in a real application
            ex.printStackTrace();
            throw new RuntimeException("Could not store file on Cloudinary. Please try again!", ex);
        }
    }

    /**
     * Helper method to generate the secure, public URL from the Public ID
     * stored in your database.
     *
     * @param publicId The Public ID stored in the Product.imageUrl field.
     * @return The full, accessible URL for the image.
     */
    public String getImageUrlFromPublicId(String publicId) {
        // Generates a secure URL for the uploaded image
        return cloudinary.url()
                .secure(true) // Use HTTPS
                .publicId(publicId)
                .generate();
    }
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return; // Nu face nimic dacă ID-ul este gol
        }
        try {
            // Distruge resursele asociate public_id-ului
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Cloudinary resource destroyed for public_id: " + publicId);
        } catch (IOException e) {
            // Logare mai bună necesară în producție
            System.err.println("Failed to destroy Cloudinary resource with public_id: " + publicId);
            e.printStackTrace();
            // Nu aruncăm excepție pentru a nu bloca operațiunea principală (ex: ștergerea produsului)
        }
    }
    // (Optional: You might want to add a deleteFile method here too)
}