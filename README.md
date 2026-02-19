# MovieMate

A social movie review Android application built with Kotlin.

## Features
- **User Authentication**: Register/Login with Firebase Authentication, auto-login support
- **Social Reviews**: Browse movie reviews from the community, create/edit/delete your own
- **TMDB Integration**: Search for movies using the TMDB REST API with autocomplete
- **Profile Management**: View/edit profile with photo and username
- **Filtering**: Filter reviews by genre and rating, search by movie title
- **Offline Support**: Local caching with Room database (SQLite)
- **MVVM Architecture**: ViewModel, LiveData, Room following Google's design guidelines
- **Navigation**: Single Activity with Fragments, Navigation Component with SafeArgs

## Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (ViewModel + LiveData + Room)
- **Backend**: Firebase (Auth, Firestore, Storage)
- **API**: TMDB REST API (Retrofit)
- **Image Loading**: Picasso
- **Navigation**: Navigation Component with SafeArgs
- **Local DB**: Room (SQLite)
- **UI**: Material Design Components

## Setup
1. Clone the repository
2. Add your `google-services.json` file to the `app/` directory (from Firebase Console)
3. Open in Android Studio
4. Build and run

## Project Structure
```
app/src/main/java/com/moviemate/
├── MovieMateApplication.kt
├── data/
│   ├── local/          # Room database, DAOs
│   ├── model/          # Data classes (User, Review, Movie)
│   ├── remote/         # Retrofit API service
│   └── repository/     # Repositories (Auth, User, Review, Movie)
└── ui/
    ├── MainActivity.kt
    ├── adapter/        # RecyclerView adapters
    ├── auth/           # Welcome, Login, Register fragments
    ├── feed/           # Home feed fragment
    ├── profile/        # Profile, Edit profile fragments
    ├── review/         # Create, Edit review fragments
    └── viewmodel/      # ViewModels
```
