# FirstDown - Football Learning App

## Overview

FirstDown is an educational Android application designed to teach American Football fundamentals to beginners. The app provides interactive lessons, quizzes, and community features to help users learn and engage with the sport.

## Features

### Educational Content
- **Structured Courses**: Progressive learning paths from basics to advanced concepts
- **Interactive Lessons**: Clear explanations with supporting visuals
- **Knowledge Assessment**: Quizzes to test understanding after completing chapters
- **Progress Tracking**: Visual indicators of course completion and learning achievements

### User Experience
- **Personalized Dashboard**: Shows learning progress and daily goals
- **Streak Tracking**: Motivates consistent learning with day streaks
- **Quick Tips**: Random football tips to enhance knowledge
- **Offline Learning**: Core content available without internet connection

### Community Features
- **Discussion Forum**: Users can post questions and share tips
- **Social Interactions**: Like and comment on community posts
- **Notifications**: Real-time alerts for social interactions
- **User Profiles**: Track personal achievements and statistics

## Technical Implementation

### Architecture
- **MVVM Pattern**: Clean separation of UI, business logic, and data
- **LiveData & ViewModel**: Lifecycle-aware data handling
- **Repository Pattern**: Centralized data management through DataManager
- **Navigation Component**: Fragment-based navigation with safe args

### Firebase Integration
- **Authentication**: Email and Google sign-in options
- **Firestore**: Cloud database for user progress and content
- **Realtime Database**: Live updates for community features
- **Cloud Storage**: Image storage for profiles and content

### UI Components
- **Material Design**: Modern interface with material components
- **ConstraintLayout**: Responsive layouts for various screen sizes
- **RecyclerView**: Efficient list displays with custom adapters
- **ViewBinding**: Type-safe view access

## Installation

1. Clone the repository:
```
git clone https://github.com/yourusername/FirstDown.git
```

2. Open the project in Android Studio.

3. Create a Firebase project and add the google-services.json file to the app directory.

4. Build and run the application on an emulator or physical device.

## Requirements

- Android 8.0 (API level 26) or higher
- Internet connection for community features and content updates
- Google Play Services for Firebase functionality

## Libraries Used

- **Material Components**: UI design system
- **Navigation Component**: Fragment navigation
- **ViewModel & LiveData**: Lifecycle management
- **RecyclerView**: List rendering
- **Firebase Auth**: User authentication
- **Firebase Firestore**: Cloud database
- **Firebase UI**: Auth UI components

## Acknowledgments

- Made as a Final Project in an Android Development Course
