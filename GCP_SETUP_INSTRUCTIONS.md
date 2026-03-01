# GCP Cloud Storage Setup Instructions

## Issue Found
The service account `kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com` does not have permission to create objects in the GCS bucket.

## Steps to Fix

### Option 1: Using Google Cloud Console (Web UI)

1. **Go to Cloud Storage**:
   - Navigate to https://console.cloud.google.com/storage
   - Select your project: `myspringbootproject-438315`

2. **Create or Select Bucket**:
   - If bucket `baps-child-photos` doesn't exist, create it:
     - Click "Create Bucket"
     - Name: `baps-child-photos`
     - Location: Choose your preferred region
     - Click "Create"

3. **Grant Permissions**:
   - Click on the bucket name
   - Go to "Permissions" tab
   - Click "Grant Access"
   - In "New principals", enter: `kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com`
   - Select role: **Storage Object Creator** (or **Storage Admin** for full access)
   - Click "Save"

### Option 2: Using gcloud CLI

```bash
# Set your project
gcloud config set project myspringbootproject-438315

# Create bucket (if it doesn't exist)
gsutil mb -p myspringbootproject-438315 -l us-central1 gs://baps-child-photos

# Grant permissions to service account
gsutil iam ch serviceAccount:kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com:roles/storage.objectCreator gs://baps-child-photos
```

### Option 3: Using IAM Policy Binding

```bash
# Grant Storage Object Creator role
gcloud storage buckets add-iam-policy-binding gs://baps-child-photos \
    --member=serviceAccount:kinshukdey@myspringbootproject-438315.iam.gserviceaccount.com \
    --role=roles/storage.objectCreator
```

## Verify Permissions

After granting permissions, you can verify:

```bash
# Check IAM policy
gsutil iam get gs://baps-child-photos
```

You should see the service account listed with `roles/storage.objectCreator` or `roles/storage.admin`.

## Required Roles

- **Minimum**: `roles/storage.objectCreator` - Allows creating/uploading objects
- **Recommended for development**: `roles/storage.admin` - Full control over bucket and objects

## After Fixing Permissions

1. Restart your Spring Boot application
2. The bucket name will now use `baps-child-photos` (from `application.properties`)
3. Try uploading a photo again using the curl command

## Testing

After fixing permissions and restarting, test with:

```bash
# Request OTP
curl -X POST "http://localhost:3900/auth/request-otp?phone=9876543210"

# Login (use OTP from response)
curl -X POST "http://localhost:3900/auth/login?phone=9876543210&otp=<OTP>"

# Register kid with photo (use token from login)
curl -X POST 'http://localhost:3900/api/kid/register' \
  -H 'Authorization: Bearer <TOKEN>' \
  -F "kid=@kid.json;type=application/json" \
  -F "photo=@photo.jpg"
```

The response should include a `photoUrl` field with the GCS URL if successful.
