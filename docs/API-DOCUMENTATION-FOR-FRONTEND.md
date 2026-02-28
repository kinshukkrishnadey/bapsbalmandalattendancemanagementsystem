# BAPS Bal Mandal Attendance Management System - API Documentation for Frontend

This document describes all APIs for building a frontend UI. Provide this to your Cursor agent or frontend developer.

**Important for agents:** Kid register (`POST /api/kid/register`) and kid update (`PUT /api/kid/update/{id}`) require **multipart/form-data** with parts `kid`/`updateDTO` (must be Blob with `type: 'application/json'`) and optional `photo` (file). Do **not** set `Content-Type` header manually—let the client set it with the boundary. The `kid`/`updateDTO` parts must be created as Blobs with explicit `application/json` Content-Type. See **Critical: Multipart Requests** for details.

---

## Base Configuration

| Property | Value |
|----------|-------|
| **Base URL** | `http://localhost:3900` |
| **Authentication** | JWT Bearer token (except auth endpoints) |
| **Content-Type** | `application/json` for JSON bodies; `multipart/form-data` for kid register/update (see below) |
| **Date Format** | `yyyy-MM-dd` (e.g., `2025-02-26`) |

---

## Authentication Flow

### 1. Request OTP
User enters phone number → Frontend calls this API → Backend generates OTP (in dev, OTP is returned in response; in production, send via SMS).

**Endpoint:** `POST /auth/request-otp`

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| phone | query | Yes | User's phone number (must exist in system) |

**Response (200):**
```json
{
  "message": "OTP sent successfully",
  "otp": "342787"
}
```

**Error (400):** `{"error": "Phone number is required"}` or `{"error": "User not found for this phone number"}`

---

### 2. Login
User enters OTP → Frontend calls this API → Backend returns JWT token.

**Endpoint:** `POST /auth/login`

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| phone | query | Yes | User's phone number |
| otp | query | Yes | OTP received (from step 1 or SMS) |

**Response (200):**
```json
{
  "message": "Login successful",
  "type": "Bearer",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Usage:** Store the `token` and send it in the `Authorization` header for all subsequent API calls:
```
Authorization: Bearer <token>
```

---

## Role-Based Access

| Right | Description | Typical Roles |
|-------|-------------|---------------|
| CREATE_ZONE | Create/update/delete zones | ADMIN |
| CREATE_ROLE | Create new roles | ADMIN |
| CREATE_SABHAKSHETRA | Create/update/delete Sabha Kshetra | ADMIN |
| ADD_KID | Register new kids | ADMIN |
| UPDATE_KID | Update kid details | ADMIN |
| MAP_STATUS | Assign status to kids | ADMIN |
| MARK_ATTENDANCE | Mark attendance | ADMIN, VOLUNTEER, SANCHALAK, SAH_SANCHALAK |
| VIEW_KIDS | View kid list/details | All roles |
| VIEW_ATTENDANCE | View attendance | All roles |

**Roles:** `ADMIN`, `VOLUNTEER`, `SANCHALAK`, `SAH_SANCHALAK`, `NIRDESHAK`
- **Sanchalak / Sah-Sanchalak:** Take attendance in one SabhaKshetra
- **Nirdeshak:** Oversees multiple SabhaKshetra, monitors stats

---

## API Endpoints

---

### ZONES (Top-level, like classroom)

#### Get All Zones
**Endpoint:** `GET /api/zones`  
**Auth:** Required

**Response (200):**
```json
[
  {
    "zoneId": 1,
    "zoneName": "North Delhi",
    "description": "North Delhi Zone"
  }
]
```

---

#### Get Zone by ID
**Endpoint:** `GET /api/zones/{id}`  
**Auth:** Required

**Response (200):** Single Zone object  
**Response (404):** Not found

---

#### Create Zone
**Endpoint:** `POST /api/zones`  
**Auth:** Required  
**Permission:** CREATE_ZONE

**Request Body:**
```json
{
  "zoneName": "North Delhi",
  "description": "North Delhi Zone"
}
```

**Response (200):** Created Zone with `zoneId`

---

#### Update Zone
**Endpoint:** `PUT /api/zones/{id}`  
**Auth:** Required  
**Permission:** CREATE_ZONE

**Request Body:** Same as Create

**Response (200):** Updated Zone

---

#### Delete Zone
**Endpoint:** `DELETE /api/zones/{id}`  
**Auth:** Required  
**Permission:** CREATE_ZONE

**Response (204):** No content

---

### ROLES

#### Get All Roles
**Endpoint:** `GET /api/roles`  
**Auth:** Required  
**Permission:** Any authenticated user

**Response (200):**
```json
[
  {
    "roleId": 1,
    "roleName": "ADMIN",
    "rights": ["CREATE_ZONE", "CREATE_ROLE", "CREATE_SABHAKSHETRA", "ADD_KID", "UPDATE_KID", "MAP_STATUS", "MARK_ATTENDANCE", "VIEW_KIDS", "VIEW_ATTENDANCE"]
  },
  {
    "roleId": 2,
    "roleName": "VOLUNTEER",
    "rights": ["VIEW_KIDS", "MARK_ATTENDANCE", "VIEW_ATTENDANCE"]
  },
  {
    "roleId": 3,
    "roleName": "SANCHALAK",
    "rights": ["VIEW_KIDS", "MARK_ATTENDANCE", "VIEW_ATTENDANCE"]
  },
  {
    "roleId": 5,
    "roleName": "NIRDESHAK",
    "rights": ["VIEW_KIDS", "VIEW_ATTENDANCE"]
  }
]
```

---

#### Get Role by ID
**Endpoint:** `GET /api/roles/{id}`  
**Auth:** Required

**Response (200):** Same structure as single role object above  
**Response (404):** Not found

---

#### Create Role
**Endpoint:** `POST /api/roles`  
**Auth:** Required  
**Permission:** CREATE_ROLE

**Request Body:**
```json
{
  "roleName": "ADMIN",
  "rights": ["ADD_KID", "UPDATE_KID", "MARK_ATTENDANCE", "VIEW_KIDS", "VIEW_ATTENDANCE"]
}
```

**Valid rights:** `CREATE_ZONE`, `CREATE_ROLE`, `CREATE_SABHAKSHETRA`, `ADD_KID`, `UPDATE_KID`, `MAP_STATUS`, `MARK_ATTENDANCE`, `VIEW_KIDS`, `VIEW_ATTENDANCE`

**Response (200):** Created role object with `roleId`

---

#### Delete Role
**Endpoint:** `DELETE /api/roles/{id}`  
**Auth:** Required  
**Permission:** CREATE_ROLE (typically)

**Response (204):** No content

---

### SABHA KSHETRA (Sections under a Zone)

SabhaKshetra belongs to a Zone. Each zone can have multiple SabhaKshetra. `kshetraName` must be unique within a zone.

#### Get All Sabha Kshetra
**Endpoint:** `GET /api/sabhakshetras`  
**Auth:** Required

**Response (200):**
```json
[
  {
    "kshetraId": 1,
    "kshetraName": "North Delhi",
    "zone": { "zoneId": 1, "zoneName": "North Delhi", "description": "North Delhi Zone" }
  }
]
```

---

#### Get Sabha Kshetra by ID
**Endpoint:** `GET /api/sabhakshetras/{id}`  
**Auth:** Required

**Response (200):** Single SabhaKshetra object  
**Response (404):** Not found

---

#### Get Sabha Kshetra by Zone
**Endpoint:** `GET /api/sabhakshetras/zone/{zoneId}`  
**Auth:** Required

**Response (200):** Array of SabhaKshetra for that zone

---

#### Create Sabha Kshetra
**Endpoint:** `POST /api/sabhakshetras`  
**Auth:** Required  
**Permission:** CREATE_SABHAKSHETRA

**Request Body:**
```json
{
  "kshetraName": "East Delhi",
  "zone": { "zoneId": 1 }
}
```

**Response (200):** Created object with `kshetraId`  
**Error (400):** `"Kshetra name already exists in this zone: ..."`

---

#### Update Sabha Kshetra
**Endpoint:** `PUT /api/sabhakshetras/{id}`  
**Auth:** Required  
**Permission:** CREATE_SABHAKSHETRA

**Request Body:**
```json
{
  "kshetraName": "North Delhi Updated",
  "zone": { "zoneId": 1 }
}
```

**Response (200):** Updated object

---

#### Delete Sabha Kshetra
**Endpoint:** `DELETE /api/sabhakshetras/{id}`  
**Auth:** Required  
**Permission:** CREATE_SABHAKSHETRA

**Response (204):** No content

---

### KIDS

#### Get All Kids (Full Details)
**Endpoint:** `GET /api/kid/getallkiddetails`  
**Auth:** Required

**Response (200):** Array of Kid objects (full entity with nested sabhaKshetra, status, roles)

---

#### Get All Kids (Partial - for lists)
**Endpoint:** `GET /api/kid/getallkidpartialdetails`  
**Auth:** Required

**Response (200):** Array of KidDto. For full kid structure, use `getallkiddetails` or `getkiddetails`:
```json
[
  {
    "gender": "BAL",
    "dateOfBirth": "2015-05-10",
    "registrationDate": "2026-02-28",
    "age": 9,
    "motherName": "Jane",
    "photoUrl": "https://...",
    "phoneNumber": "9876543210",
    "area": "Sector 5",
    "sabhaKshetraId": 1,
    "assignedSabhaKshetraId": 1,
    "supervisedSabhaKshetraIds": [1, 2],
    "statusId": 1,
    "roleIds": [1, 2]
  }
]
```

**KidDto fields:** `gender` (BAL | BALIKA), `dateOfBirth`, `registrationDate` (LocalDate, auto-set on registration), `age`, `motherName`, `photoUrl`, `phoneNumber`, `area`, `sabhaKshetraId` (Long, enrolled SabhaKshetra), `assignedSabhaKshetraId` (Long, where Sanchalak takes attendance), `supervisedSabhaKshetraIds` (Long[], SabhaKshetra Nirdeshak oversees), `statusId`, `roleIds`

---

#### Get Kid by ID (Full)
**Endpoint:** `GET /api/kid/getkiddetails/{id}`  
**Auth:** Required

**Response (200):** Full Kid object with nested sabhaKshetra, status, roles

---

#### Get Kid by ID (Partial)
**Endpoint:** `GET /api/kid/getkidpartialdetails/{id}`  
**Auth:** Required

**Response (200):** KidDto object

---

#### Get Kids by Sabha Kshetra
**Endpoint:** `GET /api/kid/by-sabhakshetra/{sabhaKshetraId}`  
**Auth:** Required  
**Path param:** `sabhaKshetraId` (Long) – returns kids enrolled in this SabhaKshetra

**Response (200):** Array of KidDto filtered by kshetra

---

#### Get Kids by Role
**Endpoint:** `GET /api/kid/by-role/{roleId}`  
**Auth:** Required

**Response (200):** Array of KidDto filtered by role

---

#### Register Kid
**Endpoint:** `POST /api/kid/register`  
**Auth:** Required  
**Permission:** ADD_KID  
**Content-Type:** `multipart/form-data` (see **Critical: Multipart Requests** below)

| Part | Type | Required | Description |
|------|------|----------|-------------|
| kid | JSON string | Yes | Kid object serialized as JSON (see below) |
| photo | File | No | Profile photo (image) |

**Kid object (JSON string in 'kid' part):**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "gender": "BAL",
  "dateOfBirth": "2016-03-15",
  "age": 8,
  "motherName": "Priya",
  "fatherName": "Amit",
  "phoneNumber": "9876543211",
  "emailAddress": "rahul@example.com",
  "houseNumber": "45",
  "area": "Sector 12",
  "state": "Delhi",
  "pincode": 110045,
  "sabhaKshetra": { "kshetraId": 1 },
  "status": { "statusId": 1 },
  "roles": [{ "roleId": 1 }],
  "registrationDate": "2026-02-28"
}
```

**Gender values:** `BAL` (male), `BALIKA` (female)

**Note:** 
- For sabhaKshetra, status, roles - send only the ID reference. Backend accepts `{ "kshetraId": 1 }` or full object with id.
- `registrationDate` is optional. If not provided, backend automatically sets it to the current date. Format: `yyyy-MM-dd` (e.g., `"2026-02-28"`).

**Response (201):** Created Kid object with `registrationDate` included (auto-set if not provided)

**Frontend implementation (fetch):**
```javascript
const formData = new FormData();
formData.append('kid', JSON.stringify(kidPayload));
if (photoFile) formData.append('photo', photoFile);

const response = await fetch(`${baseUrl}/api/kid/register`, {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});
// Do NOT set Content-Type header - browser sets it with boundary automatically
```

**Frontend implementation (axios):**
```javascript
const formData = new FormData();
formData.append('kid', JSON.stringify(kidPayload));
if (photoFile) formData.append('photo', photoFile);

await axios.post(`${baseUrl}/api/kid/register`, formData, {
  headers: { 'Authorization': `Bearer ${token}` }
});
// Axios auto-sets Content-Type for FormData - do not override with application/json
```

---

#### Update Kid
**Endpoint:** `PUT /api/kid/update/{id}`  
**Auth:** Required  
**Permission:** UPDATE_KID  
**Content-Type:** `multipart/form-data` (see **Critical: Multipart Requests** below)

| Part | Type | Required | Description |
|------|------|----------|-------------|
| updateDTO | JSON string | Yes | KidUpdateDto serialized as JSON (see below) |
| photo | File | No | New profile photo (replaces existing) |

**KidUpdateDto:**
```json
{
  "emailAddress": "rahul.new@example.com",
  "phoneNumber": "9876543211",
  "houseNumber": "46",
  "area": "Sector 12",
  "state": "Delhi",
  "pincode": 110045,
  "sabhaKshetraId": 2,
  "assignedSabhaKshetraId": 1,
  "supervisedSabhaKshetraIds": [1, 2],
  "statusId": 2,
  "roleIds": [1, 2]
}
```

**Fields:** `sabhaKshetraId` (Long, enrolled), `assignedSabhaKshetraId` (Long, for Sanchalak/Sah-Sanchalak), `supervisedSabhaKshetraIds` (Long[], for Nirdeshak)

**Response (200):** Updated KidDto

**Frontend implementation (fetch):**
```javascript
const formData = new FormData();
// CRITICAL: Create Blob with explicit Content-Type for updateDTO part
const updateBlob = new Blob([JSON.stringify(updatePayload)], { 
    type: 'application/json' 
});
formData.append('updateDTO', updateBlob);
if (photoFile) formData.append('photo', photoFile);

await fetch(`${baseUrl}/api/kid/update/${kidId}`, {
  method: 'PUT',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});
```

---

### ATTENDANCE

#### Get All Attendance
**Endpoint:** `GET /api/attendance`  
**Auth:** Required

**Response (200):**
```json
[
  {
    "attendanceId": 1,
    "attendanceDate": "2025-02-26",
    "status": "PRESENT",
    "kid": {
      "kidId": 1,
      "firstName": "Rahul",
      "lastName": "Sharma",
      ...
    }
  }
]
```

---

#### Get Attendance by ID
**Endpoint:** `GET /api/attendance/{id}`  
**Auth:** Required

**Response (200):** Single Attendance object  
**Response (404):** Not found

---

#### Get Attendance by Kid ID
**Endpoint:** `GET /api/attendance/kid/{kidId}`  
**Auth:** Required

**Response (200):** Array of Attendance for that kid

---

#### Mark Attendance
**Endpoint:** `POST /api/attendance`  
**Auth:** Required  
**Permission:** MARK_ATTENDANCE

**Request Body:**
```json
{
  "attendanceDate": "2025-02-26",
  "status": "PRESENT",
  "kid": {
    "kidId": 1
  }
}
```

**Status values:** `PRESENT`, `ABSENT`

**Response (200):** Created Attendance object  
**Response (400):** Duplicate attendance (already marked for this kid on this date)

---

#### Delete Attendance
**Endpoint:** `DELETE /api/attendance/{id}`  
**Auth:** Required

**Response (200):** `"Attendance deleted successfully."`

---

### STATUS (Reference Data)

**Note:** There is no Status API controller. Status is seeded via SQL. For frontend dropdowns, use the values from Kid responses or add a Status API if needed. Current statuses: `Active` (id 1), `Inactive` (id 2). Gender (Bal/Balika) is a separate field on Kid.

---

## Data Models Summary

### Zone
| Field | Type | Description |
|-------|------|-------------|
| zoneId | Long | Auto-generated |
| zoneName | String | |
| description | String | Optional |

### Kid (Full)
| Field | Type | Description |
|-------|------|-------------|
| kidId | Long | Auto-generated |
| firstName | String | |
| lastName | String | |
| gender | "BAL" \| "BALIKA" | Male / Female |
| dateOfBirth | LocalDate | yyyy-MM-dd |
| registrationDate | LocalDate | yyyy-MM-dd, auto-set on registration |
| age | Integer | |
| motherName | String | |
| fatherName | String | |
| photoUrl | String | GCS URL |
| phoneNumber | String | |
| emailAddress | String | |
| houseNumber | String | |
| area | String | |
| state | String | |
| pincode | Long | |
| sabhaKshetra | SabhaKshetra | Enrolled SabhaKshetra |
| assignedSabhaKshetra | SabhaKshetra | Where Sanchalak takes attendance |
| supervisedSabhaKshetra | SabhaKshetra[] | SabhaKshetra Nirdeshak oversees |
| status | Status | { statusId, status } |
| roles | Role[] | Array of { roleId, roleName } |

### Role
| Field | Type |
|-------|------|
| roleId | Integer |
| roleName | String |
| rights | String[] |

### SabhaKshetra
| Field | Type |
|-------|------|
| kshetraId | Long | |
| kshetraName | String | |
| zone | Zone | { zoneId, zoneName, description } |

### Attendance
| Field | Type |
|-------|------|
| attendanceId | Long |
| attendanceDate | LocalDate |
| status | "PRESENT" \| "ABSENT" |
| kid | Kid (nested) |

---

## Error Responses

| Status | When |
|--------|------|
| 400 | Bad request (validation, duplicate attendance) |
| 401 | Unauthorized (missing or invalid token) |
| 403 | Forbidden (insufficient permission) |
| 404 | Resource not found |
| 415 | Unsupported Media Type – kid register/update sent with wrong Content-Type; see **Critical: Multipart Requests** |
| 500 | Server error |

---

## CORS

CORS is configured for: `http://localhost:3000`, `http://localhost:3900` (configurable via `app.cors.allowed-origins`). Add your frontend origin if different.

---

## Critical: Multipart Requests (Kid Register / Update)

Kid register (`POST /api/kid/register`) and kid update (`PUT /api/kid/update/{id}`) use **multipart/form-data**, not JSON. Follow these rules to avoid **415 Unsupported Media Type** errors:

### Rules

1. **Use `FormData`** – Build the request body with `new FormData()`.
2. **Do NOT set `Content-Type` header manually** – The browser/axios must set it automatically with the correct `boundary`. If you set `Content-Type: application/json` or any other value in the request headers, the request will fail.
3. **CRITICAL: Set Content-Type for the `kid` part** – The `kid` part must be sent as a Blob with `type: 'application/json'`. This ensures Spring Boot correctly parses it as JSON.
4. **Part names must match** – Register: `kid`, `photo`. Update: `updateDTO`, `photo`.

### Proxy / application/octet-stream fallback

If the dev proxy (e.g. Vite) changes `multipart/form-data` to `application/octet-stream` when forwarding, the backend now handles this: it detects multipart body and rewrites the Content-Type. No frontend change needed. If 415 persists, bypass the proxy by calling the backend directly (`http://localhost:3900`).

### Common Pitfalls (causes 415)

| Mistake | Fix |
|---------|-----|
| Setting `Content-Type: application/json` | Omit Content-Type; let fetch/axios set it for FormData |
| Global axios default `headers['Content-Type'] = 'application/json'` | Override per-request: omit Content-Type for kid register/update |
| Sending `{ kid: kidObject }` as JSON body | Use FormData with `formData.append('kid', JSON.stringify(kidObject))` |
| Using `JSON.stringify()` directly for kid part | **REQUIRED:** Use `new Blob([JSON.stringify(obj)], { type: 'application/json' })` to set Content-Type for the part |

### Fetch Example (correct)

```javascript
const formData = new FormData();

// CRITICAL: Create Blob with explicit Content-Type for kid part
const kidBlob = new Blob([JSON.stringify(kidPayload)], { 
    type: 'application/json' 
});
formData.append('kid', kidBlob);

if (photoFile) formData.append('photo', photoFile);

fetch(`${baseUrl}/api/kid/register`, {
  method: 'POST',
  headers: { 
    'Authorization': `Bearer ${token}`
    // DO NOT set Content-Type header - let browser set it with boundary
  },
  body: formData
});
```

### Axios Example (correct)

```javascript
const formData = new FormData();

// CRITICAL: Create Blob with explicit Content-Type for kid part
const kidBlob = new Blob([JSON.stringify(kidPayload)], { 
    type: 'application/json' 
});
formData.append('kid', kidBlob);

if (photoFile) formData.append('photo', photoFile);

axios.post(`${baseUrl}/api/kid/register`, formData, {
  headers: { 
    'Authorization': `Bearer ${token}`
    // DO NOT set Content-Type header - let axios set it with boundary
  }
});
```

If using axios interceptors that set `Content-Type: application/json` globally, exclude it for multipart requests:

```javascript
axios.post(url, formData, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': undefined  // Let axios set multipart boundary
  }
});
```

---

## Frontend Implementation Notes

1. **Auth storage:** Store JWT in localStorage or secure cookie after login.
2. **Token expiry:** JWT expires in 1 hour. Implement refresh or re-login flow.
3. **Multipart requests:** See **Critical: Multipart Requests** section above for kid register/update.
4. **Date format:** Send dates as `yyyy-MM-dd` strings (e.g., `"2016-03-15"`).
5. **Registration date:** The `registrationDate` field is automatically set by the backend when a kid is registered. You can:
   - **Omit it** during registration (recommended) - backend will set it to current date
   - **Optionally send it** if you need to set a specific registration date
   - **Display it** in the UI - it's included in all KidDto responses (GET endpoints)
   - **Cannot be updated** - it's not included in KidUpdateDto, so it remains fixed after registration
6. **Permission checks:** Hide/disable UI elements based on user's role rights.
7. **Dropdowns:** Fetch `/api/zones`, `/api/sabhakshetras` (or `/api/sabhakshetras/zone/{zoneId}`), `/api/roles`, and status values on app load for kid forms.
7. **Proxy (Vite):** If using a dev proxy (frontend on 3000 → backend on 3900), the proxy may change `Content-Type` to `application/octet-stream` when forwarding multipart requests, causing 415 errors. Fix by preserving headers in `vite.config.js`:

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3900',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            const contentType = req.headers['content-type'];
            if (contentType?.startsWith('multipart/form-data')) {
              proxyReq.setHeader('Content-Type', contentType);
            }
          });
        },
      },
    },
  },
});
```
