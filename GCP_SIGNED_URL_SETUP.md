# GCP Signed URL Setup Guide

## Issue
Photos are uploaded to GCS successfully, but accessing them directly returns `403 Forbidden` because the bucket is private.

## Solution: Signed URLs
The application now generates **signed URLs** that provide temporary, secure access to private GCS objects without making the bucket public.

## How It Works
- Photos are uploaded to private GCS bucket
- When returning kid data via API, the application generates a signed URL
- Signed URLs are valid for 24 hours (configurable)
- Frontend can use these signed URLs to display photos

## Required GCP Permissions

For signed URL generation to work, the service account needs:

1. **Storage Admin** on the bucket (already granted ✅)
2. **Service Account Token Creator** role on itself (may need to be added)

### Grant Service Account Token Creator Role

The service account needs permission to create tokens for itself:

```bash
# Grant the service account the ability to sign URLs
gcloud iam service-accounts add-iam-policy-binding \
    kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com \
    --member=serviceAccount:kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com \
    --role=roles/iam.serviceAccountTokenCreator \
    --project=myspringbootproject-438315
```

Or via Google Cloud Console:
1. Go to **IAM & Admin** > **Service Accounts**
2. Find `kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com`
3. Click on it, go to **Permissions** tab
4. Click **Grant Access**
5. Add principal: `kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com`
6. Role: **Service Account Token Creator**
7. Save

## Alternative: Make Bucket Public (Less Secure)

If you prefer to make photos publicly accessible (not recommended for production):

```bash
# Make bucket public for reading
gsutil iam ch allUsers:objectViewer gs://baps-child-photos
```

**Note:** This makes all photos publicly accessible without authentication. Signed URLs are more secure.

## Configuration

The signed URL expiration time can be configured in `application.properties`:

```properties
# Signed URL expiration time in hours (default: 24 hours)
gcp.signed.url.expiration.hours=24
```

Or via environment variable:
```bash
export GCP_SIGNED_URL_EXPIRATION_HOURS=24
```

## Testing

After granting permissions, test by:

1. Restart the application
2. Retrieve a kid with a photo:
   ```bash
   curl -X GET "http://localhost:3900/api/kid/getkiddetails/6" \
     -H "Authorization: Bearer <TOKEN>"
   ```
3. Check the `photoUrl` in the response - it should be a signed URL (long URL with query parameters)
4. Try accessing the signed URL directly in a browser - it should work

## Troubleshooting

### Signed URLs Not Generated
- Check application logs for errors
- Verify service account has "Service Account Token Creator" role on itself
- Verify service account has "Storage Admin" on the bucket

### Signed URLs Generated But Still 403
- Check if the signed URL has expired (default: 24 hours)
- Verify the blob exists in GCS
- Check service account permissions on the specific blob

### Logs Show Warnings
- Check `application.properties` for `gcp.signed.url.expiration.hours`
- Verify GCS credentials are correctly configured
- Check if the photoUrl format is correct (should start with `https://storage.googleapis.com/`)
