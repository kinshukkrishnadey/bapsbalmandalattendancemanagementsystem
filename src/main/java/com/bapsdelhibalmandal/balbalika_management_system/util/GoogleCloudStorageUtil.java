package com.bapsdelhibalmandal.balbalika_management_system.util;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GoogleCloudStorageUtil {

    private final Storage storage;

    public GoogleCloudStorageUtil(ResourceLoader resourceLoader,
                                  @Value("${gcp.credentials.path}") String credentialsPath) throws IOException {
        Resource resource = resourceLoader.getResource(credentialsPath);
        this.storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build()
                .getService();
    }

    public String uploadFile(String bucketName, String folderName, String fileName, Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);

        BlobId blobId = BlobId.of(bucketName, folderName + "/" + fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();

        storage.create(blobInfo, bytes);
        return String.format("https://storage.googleapis.com/%s/%s/%s", bucketName, folderName, fileName);
    }
}
