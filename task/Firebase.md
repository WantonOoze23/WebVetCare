# WebVetCare Android: Firebase Remote Config & Dynamic Settings

---

### Project Overview
Mobile applications must remain secure, flexible, and resilient to poor network conditions. Directly connecting a mobile app to a private configuration server is a security risk. Instead, you will integrate **Firebase Remote Config** (refer to the [Firebase Remote Config Android Guide](https://firebase.google.com/docs/remote-config/android/get-started) for details) to manage runtime application settings (like target backend URLs and diagnostic flags).

As part of this task, you will create a dedicated **Settings Screen** accessible to all user roles, displaying active environment configurations.

---


### Architectural Requirements

To keep the application modular, maintainable, and testable, you must follow the **Clean Architecture / Repository Pattern** guidelines:

1.  **Multi-Module Separation:**
    *   **Domain Layer / Core Module:** Expose the configuration contracts (interfaces) and data classes. Presentation components (ViewModels) must depend only on these abstractions, not on Firebase libraries.
    *   **Data Layer / Config Module:** Implement the concrete Firebase SDK integrations, local fallback assets, and local cache handlers.

---

### Step-by-Step Implementation Guide

#### Step 1: Declare Catalog Dependencies
In your client build configuration, import the Firebase BoM, Remote Config, and Google Services plugin configurations. Ensure you reference catalog properties from `libs.versions.toml`:
*   `libs.firebase.bom`
*   `libs.firebase.config`
*   `libs.firebase.analytics`
*   `libs.plugins.google.services`

#### Step 2: Establish Offline In-App Defaults
1.  Configure a default XML config file (`remote_config_defaults.xml`) under your resources directory.
2.  Define key-value pairs (any three parameters)
3.  **Required Verification Test Data:** Make sure the offline default values for test parameters (like `test_data`) are **different** from the values you configure in the Firebase console. This ensures you can verify that the app successfully switches between local fallbacks and active remote values.

#### Step 3: Implement settings retrieval logic
1.  Inside your data module implementation, initialize the Firebase Remote Config instance.
2.  Bind your local defaults XML to the SDK.
3.  Implement the `SettingsRepository` getters.
4.  Implement asynchronous configuration fetching and activation (`fetchAndActivate`).
5.  Attach a real-time configuration update listener (`addOnConfigUpdateListener`) to immediately hot-reload values if changes are published to the Firebase console.

#### Step 4: Inject via Dependency Injection
1.  Register your `SettingsRepository` singleton interface binding pointing to your `FirebaseSettingsRepository` in Koin.
2.  Ensure Koin injects this repository into ViewModels.

#### Step 5: Implement the Settings Screen & Navigation
1.  **Navigation Entry Point:**
    *   Locate the app's sidebar/main navigation drawer.
    *   Place a **Settings** menu item at the **bottom-left** of the navigation layout.
    *   Ensure this settings menu item is **visible and accessible to all user roles** (Guests, Users, Doctors, and Administrators).
2.  **Settings Screen UI:**
    *   Create a dedicated Compose screen for Settings.
    *   On navigation, display:
        *   **Application Language:** The active language profile loaded from config.
        *   **Diagnostic Test Data:** A text element displaying your custom configuration string.
        *   **Config Source Status:** A clear badge indicating if the configuration is active **Online (Firebase)** or **Offline (In-App Defaults)**.

---

### How to Evaluate Success

Confirm the correct implementation of the task by running the following tests:

#### 1. Verify Offline Defaults (In-App Fallback)
1.  Disconnect the emulator/device from the internet.
2.  Launch the application and navigate to the **Settings** screen.
3.  Verify that the screen shows:
    *   **Config Source Status:** Displays `Offline (In-App Defaults)`.
    *   **Diagnostic Test Data:** Shows your pre-defined local fallback value.

#### 2. Verify Online Firebase Config overrides
1.  Connect the device back to the internet.
2.  In the **Firebase Console**, add overrides for the settings parameters (e.g., change `test_data` to a custom remote string).
3.  Publish the changes on Firebase console.
4.  Navigate back to the app's **Settings** screen and verify it updates dynamically:
    *   **Config Source Status:** Transitions to `Online (Firebase)`.
    *   **Diagnostic Test Data:** Displays the updated value published on the console.
