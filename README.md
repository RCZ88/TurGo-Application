# TurGo

TurGo is a production Android education app focused on courses, scheduling, and student�teacher interactions. The app uses Firebase Realtime Database as its backend, follows a repository pattern with domain models and Firebase mappers, and leans heavily on async-safe flows built with Google Play Services `Task<T>`.

## Highlights
- **Course lifecycle**: explore courses, join/register, view agendas and tasks.
- **Scheduling system**: schedules, time slots, and day/time arrangements.
- **Roles**: student, teacher, parent (role-aware data access and UI).
- **Media**: Cloudinary-backed image loading with Glide.
- **Navigation**: Activities host Fragments (including nested fragments) with explicit bundle passing.

## Tech Stack
- **Platform**: Android
- **Language**: Java
- **Backend**: Firebase Realtime Database
- **Async model**: Google Play Services `Task<T>`
- **Image loading**: Glide
- **Architecture**: Repository pattern + domain models + Firebase mappers

## Project Structure (high level)
- `app/src/main/java/com/example/turgo/`
  - **Domain models**: `Student`, `Teacher`, `Course`, `Schedule`, `TimeSlot`, `DayTimeArrangement`, `Agenda`, `Task`, etc.
  - **Repositories**: `StudentRepository`, `CourseRepository`, `ScheduleRepository`, etc.
  - **Firebase mappers**: `StudentFirebase`, `CourseFirebase`, `ScheduleFirebase`, etc.
  - **Screens (Activities/Fragments)**: `StudentScreen`, `CourseJoinedFullPage`, `CourseExploreFullPage`, etc.
- `app/src/main/res/layout/`
  - Layouts for course pages, schedules, and dashboards.

## Core Domain Entities
- `User`, `Student`, `Teacher`, `Parent`
- `Course`, `CourseType`
- `Schedule`, `TimeSlot`, `DayTimeArrangement`
- `StudentCourse`
- `Task`, `Agenda`

## Data Model & Repository Pattern
Each major model has a Repository responsible for RTDB operations. Repositories are thin and focused on database CRUD operations; model classes hold in-memory state and know how to map to Firebase via corresponding `*Firebase` classes.

### Async Safety (important)
Firebase writes are asynchronous. Any multi-step write (especially those that feed UI navigation) must return `Task<?>` and be awaited by callers. The codebase uses `Tasks.whenAll(...)` to aggregate multi-write flows.

## Key Flows
### Join Course
Joining a course can trigger multiple writes (course membership, student course records, schedule creation, agenda/task setup, etc.). This flow is Task-based to avoid race conditions and to ensure UI navigation only happens after all writes succeed.

### Schedule Listing
Schedules are displayed using a RecyclerView and a dedicated schedule display layout (`schedule_display_layout.xml`). A full schedule list for a course is shown in a dedicated fragment (`CourseScheduleListFragment`).

## Build & Run
1. Open the project in Android Studio.
2. Ensure you have a valid `google-services.json` for Firebase in `app/`.
3. Configure `local.properties` / `local.yaml` as needed for your environment.
4. Sync Gradle and run on device/emulator.

## Firebase Setup Notes
- This app uses Firebase Realtime Database.
- Ensure your RTDB rules and indexes align with your data access patterns.
- Required Firebase nodes include (not exhaustive):
  - `students`, `teachers`, `courses`, `schedules`, `dayTimeArrangements`, `agendas`, `tasks`, `studentCourses`.

## UI/Design System
The UI follows a modern �Emerald academic� aesthetic:
- Emerald greens for primary actions and surfaces.
- Rounded corners (16�20dp) with subtle strokes on cards.
- Bold, quirky headers (Bricolage Grotesque) + technical body text (Space Mono / Bricolage Body).
- Parallax banners on course pages using `CollapsingToolbarLayout`.

## Contributing
- Favor Task-based async flows for any multi-step Firebase write.
- Avoid blocking the main thread.
- Treat Firebase as the source of truth; in-memory objects can be stale.

## License
Add your license here.
