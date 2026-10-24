package com.restaurante.app.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StorageService {

    private final Storage storage;

    @Value("${google.cloud.bucket.name}")
    private String bucketName;

    public StorageService(Storage storage) {
        this.storage = storage;
    }

    public String uploadFile(MultipartFile file, String caminhoCompleto) throws IOException {
        BlobId blobId = BlobId.of(bucketName, caminhoCompleto);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
                
        storage.create(blobInfo, file.getBytes());
        return "https://storage.googleapis.com/" + bucketName + "/" + caminhoCompleto;
    }

    public void deleteFile(String publicUrl) throws IOException {
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (publicUrl == null || !publicUrl.startsWith(prefix)) return;
        String objectName = publicUrl.substring(prefix.length());
        storage.delete(BlobId.of(bucketName, objectName));
    }
}