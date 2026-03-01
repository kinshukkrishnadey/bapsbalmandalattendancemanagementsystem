package com.bapsdelhibalmandal.balbalika_management_system.util;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Component
public class GoogleCloudStorageUtil {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageUtil.class);
    
    private final Storage storage;
    
    @Value("${gcp.signed.url.expiration.hours:24}")
    private int signedUrlExpirationHours;

    /**
     * Initializes GCS Storage with credentials from multiple sources (in priority order):
     * 1. GCP_CREDENTIALS_JSON environment variable (JSON as string) - for Docker/Kubernetes
     * 2. GCP_CREDENTIALS_PATH environment variable (file path) - for mounted secrets
     * 3. gcp.credentials.path property (classpath or file path) - for local dev
     */
    public GoogleCloudStorageUtil(ResourceLoader resourceLoader,
                                  @Value("${gcp.credentials.path:classpath:gcs-service-account.json}") String credentialsPath) throws IOException {
        GoogleCredentials credentials = loadCredentials(credentialsPath, resourceLoader);
        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    private GoogleCredentials loadCredentials(String credentialsPath, ResourceLoader resourceLoader) throws IOException {
        // Priority 1: Check for GCP_CREDENTIALS_JSON environment variable (JSON as string)
        String credentialsJson = System.getenv("GCP_CREDENTIALS_JSON");
        if (credentialsJson != null && !credentialsJson.isEmpty()) {
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );
        }

        // Priority 2: Check for GCP_CREDENTIALS_PATH environment variable (file path)
        String credentialsEnvPath = System.getenv("GCP_CREDENTIALS_PATH");
        if (credentialsEnvPath != null && !credentialsEnvPath.isEmpty()) {
            Path path = Paths.get(credentialsEnvPath);
            if (Files.exists(path)) {
                return GoogleCredentials.fromStream(new FileInputStream(path.toFile()));
            }
        }

        // Priority 3: Use gcp.credentials.path property (classpath or file path)
        Resource resource = resourceLoader.getResource(credentialsPath);
        if (resource.exists()) {
            return GoogleCredentials.fromStream(resource.getInputStream());
        }

        // Priority 4: Try GOOGLE_APPLICATION_CREDENTIALS (standard GCP env var)
        String googleAppCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleAppCreds != null && !googleAppCreds.isEmpty()) {
            Path path = Paths.get(googleAppCreds);
            if (Files.exists(path)) {
                return GoogleCredentials.fromStream(new FileInputStream(path.toFile()));
            }
        }

        // Fallback: Use Application Default Credentials (for GCP environments like Cloud Run)
        return GoogleCredentials.getApplicationDefault();
    }

    public String uploadFile(String bucketName, String folderName, String fileName, Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);

        BlobId blobId = BlobId.of(bucketName, folderName + "/" + fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();

        storage.create(blobInfo, bytes);
        return String.format("https://storage.googleapis.com/%s/%s/%s", bucketName, folderName, fileName);
    }

    /**
     * Generates a signed URL for accessing a GCS object.
     * The signed URL provides temporary access without making the bucket public.
     * 
     * @param gcsUrl The full GCS URL (e.g., https://storage.googleapis.com/bucket/folder/file.jpg)
     * @return Signed URL that can be used to access the object for a limited time
     * @throws IOException If URL parsing or signing fails
     */
    public String generateSignedUrl(String gcsUrl) throws IOException {
        if (gcsUrl == null || gcsUrl.isEmpty()) {
            return null;
        }

        try {
            // Parse the GCS URL to extract bucket and blob name
            // Format: https://storage.googleapis.com/bucket/folder/file.jpg
            String prefix = "https://storage.googleapis.com/";
            if (!gcsUrl.startsWith(prefix)) {
                // If it's not a GCS URL, return as-is (might be a different storage)
                return gcsUrl;
            }

            String path = gcsUrl.substring(prefix.length());
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) {
                return gcsUrl; // Invalid format, return original
            }

            String bucketName = path.substring(0, firstSlash);
            String blobName = path.substring(firstSlash + 1);

            BlobId blobId = BlobId.of(bucketName, blobName);

            // Generate signed URL valid for specified hours
            URL signedUrl = storage.signUrl(
                    BlobInfo.newBuilder(blobId).build(),
                    signedUrlExpirationHours,
                    TimeUnit.HOURS,
                    Storage.SignUrlOption.withV4Signature()
            );

            logger.debug("Generated signed URL for: {}", gcsUrl);
            return signedUrl.toString();
        } catch (StorageException e) {
            // If signing fails, log the error and return original URL
            logger.warn("Failed to generate signed URL for {}: {}. Returning original URL.", gcsUrl, e.getMessage());
            return gcsUrl;
        } catch (Exception e) {
            // Catch any other exceptions
            logger.error("Unexpected error generating signed URL for {}: {}", gcsUrl, e.getMessage(), e);
            return gcsUrl;
        }
    }
}
