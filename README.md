# Library Rental

An Android app for a small library, built with Kotlin, Jetpack Compose and Room.
You can browse books, reserve one, and manage your reservations (pick up, renew, return, cancel).
Everything is stored on the phone. Ten sample books are added the first time the app runs.

## Features

- Book catalog with a green "Available" / orange "Borrowed" badge
- Search by title or author, and filter by All / Available / Borrowed
- Reserve a book: enter your name and pick 7, 14, 21 or 30 days
- My Reservations screen with a Current tab and a History tab
  - Cancel a reservation, or confirm pickup
  - Renew a loan for 7, 14 or 21 days (maximum 60 days in total)
  - Return a book

## Project structure

- `data/` Room entities, DAOs, the database and the repository
- `logic/` rental rules and input validation (plain Kotlin, easy to unit test)
- `viewmodel/` CatalogViewModel, DashboardViewModel and the factory that creates them
- `ui/screens/` the two screens (Catalog and Dashboard)
- `ui/components/` smaller composables (book card, reserve sheet, booking card, renew dialog)
- `ui/navigation/` the NavHost with the two routes
- `ui/theme/` colors, typography and theme

## Running

Open the folder in Android Studio (JDK 17), let Gradle sync, then press Run.
Minimum Android version is 8.0 (API 26).

- Unit tests: `./gradlew test`
- Database tests (emulator running): `./gradlew connectedAndroidTest`
