# BAPS Bal Mandal - Product Functionality & Domain Design

## Overview

This document captures the complete functionality and domain model for the BAPS Bal Mandal Attendance Management System.

---

## Hierarchy & Entities

### 1. Zone (Top Level – like a classroom/building)
- **Current name:** Was conceptually "SabhaKshetra" at top level
- **New name:** **Zone**
- **Description:** A Zone is a top-level container (e.g., a hall, building, or classroom area)
- **Admin action:** Admin can create multiple Zones

### 2. SabhaKshetra (Under Zone – like a section/class)
- **Description:** A SabhaKshetra is a section/class **within** a Zone. Multiple SabhaKshetra exist under each Zone.
- **Admin action:** Admin creates SabhaKshetra under a Zone
- **Constraint:** Each SabhaKshetra must have **at least one Sanchalak** to take attendance

### 3. Kid (Participant)
- **Description:** A kid (child) registered in the system. Can have **multiple roles**.
- **Admin action:** Admin can create/register kids

---

## Status

| Status | Description |
|--------|-------------|
| **Active** | Participant is active |

---

## Gender

| Gender | Description |
|--------|-------------|
| **Bal** | Male |
| **Balika** | Female |

---

## Roles & Responsibilities

| Role | Description | Responsibility |
|------|-------------|----------------|
| **Sanchalak** | Class teacher | Takes attendance of students in a **particular** SabhaKshetra. One SabhaKshetra needs at least one Sanchalak. |
| **Sah-Sanchalak** | Assistant teacher | **Same permissions as Sanchalak** – can take attendance of students in their assigned SabhaKshetra. |
| **Nirdeshak** | Supervisor | Oversees **multiple** SabhaKshetra. Monitors data and stats of Sanchalak and Sah-Sanchalak. |

**Student:** A kid enrolled in a SabhaKshetra (via `kid.sabhaKshetra`) – no separate role. Gender (Bal/Balika) indicates male/female.

**Note:** A kid can have **multiple roles** – e.g. student in one SabhaKshetra AND Sanchalak in another.

---

## Relationships Summary

```
Zone (1)
  └── SabhaKshetra (many)
        ├── Students (many) – kids enrolled here (Gender: Bal/Balika, Status: Active)
        └── Sanchalak / Sah-Sanchalak (≥1) – takes attendance (same permissions)

Kid (Sanchalak/Sah-Sanchalak) → assigned to 1 SabhaKshetra
Kid (Nirdeshak) → oversees multiple SabhaKshetra
Kid (enrolled) → in 1 SabhaKshetra
Kid can have BOTH Student + Sanchalak roles (in different SabhaKshetra)
```

---

## Business Rules

1. **Zone** → has many **SabhaKshetra**
2. **SabhaKshetra** → belongs to one **Zone**
3. **SabhaKshetra** → must have **at least one Sanchalak** (or Sah-Sanchalak) to take attendance
4. **Kid (enrolled as student)** → belongs to **one** SabhaKshetra. Gender = Bal (male) or Balika (female). Status = Active.
5. **Kid (Sanchalak/Sah-Sanchalak)** → assigned to **one** SabhaKshetra (takes attendance there). Same permissions for both.
6. **Kid (Nirdeshak)** → oversees **multiple** SabhaKshetra (monitors stats)
7. **Kid** can have **multiple roles** – e.g. student in SabhaKshetra A + Sanchalak in SabhaKshetra B
8. **Attendance** → marked by Sanchalak/Sah-Sanchalak for students in their SabhaKshetra

---

## Proposed Data Model

### Zone (NEW)
| Field | Type | Description |
|-------|------|-------------|
| zoneId | Long | PK |
| zoneName | String | Name of the zone |
| description | String | Optional |

### SabhaKshetra (MODIFIED)
| Field | Type | Description |
|-------|------|-------------|
| kshetraId | Long | PK |
| kshetraName | String | Name (e.g., "Section A") |
| zone | Zone | ManyToOne – parent Zone |

### Kid (MODIFIED)
| Field | Type | Description |
|-------|------|-------------|
| kidId | Long | PK |
| ... (existing fields) | | |
| sabhaKshetra | SabhaKshetra | For Bal/Balika – the SabhaKshetra they attend |
| roles | Set&lt;Role&gt; | Bal, Balika, Sanchalak, Sah-Sanchalak, Nirdeshak |
| assignedSabhaKshetra | SabhaKshetra | For Sanchalak/Sah-Sanchalak – where they take attendance |
| supervisedSabhaKshetra | Set&lt;SabhaKshetra&gt; | For Nirdeshak – SabhaKshetra they oversee |

**Note:** A kid can have multiple roles. The `assignedSabhaKshetra` and `supervisedSabhaKshetra` apply when the kid has Sanchalak/Sah-Sanchalak or Nirdeshak role respectively. For Bal/Balika, `sabhaKshetra` is their class.

### Role (UPDATED – seed data)
Roles to seed: **Sanchalak**, **Sah-Sanchalak**, **Nirdeshak**

**Status** (existing): **Active** – participant status  
**Gender:** **Bal** (male), **Balika** (female)

---

## Alternative: Simpler Kid–SabhaKshetra Model

If a kid has only one "primary" role at a time, we could use:

| Kid Type | Relationship |
|----------|--------------|
| Bal/Balika | `kid.sabhaKshetra` = class they attend |
| Sanchalak/Sah-Sanchalak | `kid.assignedSabhaKshetra` = class they teach |
| Nirdeshak | `kid.supervisedSabhaKshetra` (ManyToMany) = classes they oversee |

---

## Migration from Current Model

| Current | New |
|---------|-----|
| SabhaKshetra (top level) | → **Zone** (top level) |
| (none) | → **SabhaKshetra** (under Zone) |
| Kid.sabhaKshetra | Kid.sabhaKshetra (for Bal – their class) |
| (none) | Kid.assignedSabhaKshetra (for Sanchalak) |
| (none) | Kid.supervisedSabhaKshetra (for Nirdeshak) |
| Status (Bal, Balika) | May merge into Role or keep separate |

---

## API Impact

- **Zone API:** CRUD for Zone
- **SabhaKshetra API:** CRUD, filter by Zone
- **Kid API:** Register with Zone/SabhaKshetra, assign roles, assign Sanchalak to SabhaKshetra
- **Attendance API:** Sanchalak marks attendance for Bal in their SabhaKshetra
- **Stats/Dashboard:** Nirdeshak views stats for their supervised SabhaKshetra

---

## Clarifications (Confirmed)

1. **Status** = Active (participant status). **Gender** = Bal (male), Balika (female).
2. **Multiple roles:** A kid can have both Student and Sanchalak roles (in different SabhaKshetra).
3. **Sanchalak & Sah-Sanchalak:** Same permissions – both can take attendance.

---

*This document serves as the specification for implementation.*
