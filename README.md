# RoundNews Android Client

## Project Overview

This repository contains the source code for RoundNews, a news client for Android. The application is built entirely with Kotlin and leverages modern Android development practices. It is designed to fetch and display articles from the NewsAPI, with a focus on a clean architecture, a reactive UI, and efficient network handling.

The core functionality includes searching for articles, filtering by category, and displaying results in a paginated list that supports infinite scrolling.

---

## Technical Implementation

This project is built with a clear separation of concerns, following an **MVVM (Model-View-ViewModel)** architecture.

### 1. Core Technologies

-   **UI Toolkit**: **Jetpack Compose** is used exclusively for the UI layer. The UI is declarative and reacts to state changes from the ViewModel via `StateFlow` and `collectAsState()`. **Material 3** components provide the design system.
-   **Networking**: **Ktor** is the HTTP client used for all network operations. It's configured with plugins for logging (`Logging`), content negotiation (`ContentNegotiation`), and timeouts (`HttpTimeout`) for robust API communication.
-   **JSON Parsing**: **Kotlinx Serialization** is used for its efficiency and compile-time safety when parsing JSON responses from the News API into Kotlin data classes. The implementation uses a `sealed class` (`ApiResponse`) to elegantly handle both successful (`ok`) and error (`error`) states from the API.
-   **Asynchronous Programming**: **Kotlin Coroutines and Flow** are central to managing background tasks and data streams.
    -   The `NewsRepository` exposes `Flow` objects to stream data from the API.
    -   The `NewsViewModel` collects these flows within the `viewModelScope`, ensuring all operations are lifecycle-aware.
-   **Image Loading**: **Coil** is used for asynchronously loading and caching article images within Jetpack Compose UI.
-   **Dependency Management**: Dependencies are managed manually via a simple factory pattern (`NewsApiFactory`). This approach was chosen for its simplicity and sufficiency for the project's current scale, avoiding the overhead of a larger DI framework.

### 2. Architectural Breakdown

-   **View (`MainActivity` / `MainScreen.kt`)**: The UI layer is stateless and dumb. It observes the `NewsUiState` from the `NewsViewModel` and renders the UI accordingly. User interactions (like searching or scrolling) trigger functions in the ViewModel.
-   **ViewModel (`NewsViewModel.kt`)**: This layer contains the presentation logic.
    -   It holds the application state in a `MutableStateFlow<NewsUiState>`.
    -   It exposes an immutable `StateFlow` for the View to observe.
    -   It handles the logic for new searches and pagination (loading more articles), updating the UI state as data is fetched.
-   **Model / Data Layer (`NewsRepository.kt` & `NewsApiService.kt`)**:
    -   The `NewsRepository` is the single source of truth for data. It abstracts the data source from the ViewModel.
    -   The `NewsApiService` contains the Ktor client implementation and defines the API endpoints (`/everything`, `/top-headlines/sources`). It is responsible for constructing and executing HTTP requests.

---

## Build and Setup

### Prerequisites

-   Android Studio (latest stable version recommended)
-   JDK 11
-   Android SDK Platform 36
-   Minimum Target: Android 5.0 (API Level 21)

### Configuration

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/War004/CANTILEVER-News-app.git
    cd CANTILEVER-News-app
    ```
2.  **Obtain an API Key**: This project requires a key from [NewsAPI.org](https://newsapi.org/). Register an account to get a free key.
3.  **Set the API Key**: Create a `gradle.properties` file in the root directory of the project and add the following line, replacing `YOUR_API_KEY` with your actual key:
    ```properties
    NEWS_API_KEY="YOUR_API_KEY"
    ```
    The build script (`build.gradle.kts`) is configured to read this key and make it accessible via `BuildConfig`.

### Running the Application

1.  Open the project in Android Studio.
2.  Allow Gradle to sync and download all dependencies.
3.  Select a target device or emulator.
4.  Run the application. It will launch with a default search for "Android" articles.
