# Task: Registration & Authentication System

## 1. Overview
This task focuses on implementing the entry point of the WebVetCare application. It involves creating a landing page, authentication flows, and a user dashboard that handles multiple user roles and profile customization.

**Goal:** Secure the application, allow users to define their roles (Patient, Doctor), and provide a personalized experience based on these roles.

---

## 2. User Flow & UI Requirements

### Screen 1: Landing Page (Public)
- **Content:**
    - Project Title: "WebVetCare".
    - Brief description of the project.
    - Animation: A continuous stream of floating particles drifting upward, vanishing smoothly, creating a dynamic and lightweight visual atmosphere.
    - "Login" / "Register" buttons.
- **Functionality:**
    - Buttons navigate to the Login or Registration screens.

### Screen 2: Login
- **UI:**
    - **Input Fields:** Email, Password.
    - Link to navigate to Registration screen.
    - **Feedback:** SnackBar to display success or error messages (e.g., "Incorrect input", "Wrong password").
- **Logic:**
    - **Login:** Authenticates the user.
    - **Success:** Redirects to the User Dashboard.

### Screen 3: Registration
- **UI:**
    - **Input Fields:** Email, Password, UserName.
    - Link to navigate to Login screen.
    - **Feedback:** SnackBar to display success or error messages (e.g., "User already exists", "Invalid email format").
- **Logic:**
    - **Registration:** Creates a new user with an `USER` role.
    - **Success:** Redirects to the User Dashboard.

### Screen 4: User Dashboard (Private)
- **Header:**
    - Display User Name (from profile).
- **Side Navigation Menu:**
    - **Structure:**
        - Global expandable side menu showing user routes.
        - **Toggle:** Button with arrows to expand (show text + icons) and shrink (show icons only) the menu.
        - **Grouping:** Routes are grouped by Role. Each group is separated by a **Divider** displaying the Role Name (e.g., "User", "Patient", "Doctor").
    - **Access Control:**
        - **New User:** Sees only "User" routes.
        - **Patient:** Sees "User" routes + "Patient" routes.
        - **Doctor:** Sees "User" routes + "Doctor" routes.

### Core Routes & Screens
**General UI Requirement:** All screens must use **Snackbars** to provide immediate feedback (success/error) on requests.

1.  **User Screen (Route: `/user`):**
    - **Display:** Email, User Name, ID, and current Roles.
    - **Actions:** "Me" button to force update/sync user information from the server.

2.  **Become Doctor (Route: `/become-doctor`):**
    - **Input Fields:** `specialization`, `licenseNumber`, `clinicAddress`, `availability`.
    - **Logic:**
        - Submit form -> Backend assigns `DOCTOR` role.
        - **On Success:** Show success SnackBar, redirect to **User Screen**, and update side menu to reveal Doctor routes.

3.  **Doctor Profile (Route: `/doctor-profile`):**
    - **Display:** Exposes the doctor's submitted data (`specialization`, `license`, etc.).
    - **Access:** Only available if user has `DOCTOR` role.

4.  **Become Patient (Route: `/become-patient`):**
    - **Input Fields:** `contactPhoneNumber`, `contactEmail`.
    - **Logic:**
        - Submit form -> Backend assigns `PATIENT` role.
        - **On Success:** Show success SnackBar, redirect to **User Screen**, and update side menu to reveal Patient routes.

5.  **Patient Profile (Route: `/patient-profile`):**
    - **Display:** Exposes the patient's submitted data.
    - **Access:** Only available if user has `PATIENT` role.

---

## 3. Technical Implementation Guide

#### 1. Architecture Overview

- **UI Framework:** **Jetpack Compose** (Android).
- **Architecture:** **MVI (Model-View-Intent)** with Contracts (State, Event, Effect).
- **Navigation:** **Navigation 3** (Must include **Animated Transitions**).
- **Dependency Injection:** Koin.
- **Build Structure:** Create three distinct Gradle modules:
    1.  `:app` (The Client / UI)
    2.  `:auth` (The Authority / Logic)
    3.  `:user` (The Resource / Database)

#### 2. Module Responsibilities

**Module 1: `:auth` (The Security Core)**
- **Responsibility:** `auth-service`.
- **Functionality:**
    - **Token Issuance:** Generates real **JWTs** (Access & Refresh) upon login/registration.
    - **Key Management:** Uses **RSA PEM Keys** (generated locally or stored in assets) to sign and validate tokens.
    - **Validation:** Provides functions to validate token signatures and expiration.
    - **Storage:** Securely stores user credentials (email and hashed password) in its own database/storage.
- **Testing:** High test coverage for all public functions (Registration, Login, Token Generation, Validation).

**Module 2: `:user` (The Data Core)**
- **Responsibility:** `user-service`.
- **Database:** Internal **Room Database** to store Users, Roles, Doctor Profiles, and Patient Profiles.
- **Functionality:**
    - Manages user entities and relationships.
    - Handles "Become Doctor" and "Become Patient" data persistence.
- **Testing:** High test coverage for all public repository functions.

**Module 3: `:app` (The Application)**
- **Responsibility:** The UI layer (Screens 1-4).
- **Implementation:**
    - Connects to `:auth` and `:user` modules.
    - Implements the UI flows described in "User Flow & UI Requirements".
    - **Logic:** Handles the "Token Expiry" and "Force Logout" flows by interacting with the `:auth` module.