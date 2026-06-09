<p align="center">
  <a href="https://github.com/w2sv/FileNavigator">
    <img width="200" height="200" src="https://github.com/w2sv/FileNavigator/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" alt="File Navigator app icon">
  </a>
</p>

<h1 align="center">File Navigator</h1>

<p align="center">
  <b>The missing link between Android and a sorted file system.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.filenavigator%26l%3DPlay%2520Store%26m%3D%24version" alt="Google Play version">
  <img src="https://img.shields.io/f-droid/v/com.w2sv.filenavigator?label=F-Droid&logo=fdroid&color=blue" alt="F-Droid version">
  <img src="https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.w2sv.filenavigator&logo=fdroid" alt="IzzyOnDroid version">
  <img src="https://img.shields.io/github/v/release/w2sv/FileNavigator?label=GitHub&logo=github&color=purple" alt="GitHub release">
  <br>
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.filenavigator%26l%3DDownloads%26m%3D%24totalinstalls" alt="Google Play downloads">
  <img src="https://img.shields.io/github/downloads/w2sv/FileNavigator/total?label=Downloads&logo=github&color=purple" alt="GitHub downloads">
  <br>
  <img src="https://img.shields.io/github/license/w2sv/FileNavigator" alt="License">
  <img src="https://img.shields.io/github/languages/code-size/w2sv/FileNavigator" alt="Code size">
  <a href="https://github.com/w2sv/FileNavigator/actions/workflows/workflow.yaml">
    <img src="https://github.com/w2sv/FileNavigator/actions/workflows/workflow.yaml/badge.svg" alt="Build status">
  </a>
</p>

File Navigator watches Android's shared storage for new files and helps move them
to the folders where they belong. Rules can distinguish not only between file
types, but also between sources such as the camera, screenshots, downloads,
recordings, and other apps.

The app supports Android 11 and newer.

## Features

- **File discovery notifications** - get notified when a new matching file
  appears and move, open, or delete it directly from the notification.
- **Quick Move** - assign frequently used destination folders to a file source
  and move files with a single action.
- **Auto Move** - automatically route new files to configured folders without
  further interaction.
- **Source-specific rules** - configure different behavior for camera photos,
  screenshots, recordings, downloads, and files created by other apps.
- **Built-in file types** - organize pictures, videos, audio, PDFs, text files,
  archives, APKs, and eBooks.
- **Custom file types** - create your own categories with a name, color, and set
  of file extensions.
- **Configurable extensions** - include or exclude extensions from supported
  non-media file types.
- **Batch moving** - collect multiple active navigation notifications and move
  their files to one destination.
- **Move history** - review previously moved files and their destinations.
- **Quick Settings tile** - start or stop the navigator from Android's Quick
  Settings panel.
- **Background controls** - optionally start on boot and stop navigation when
  the battery is low.
- **Material 3 appearance** - follow the system theme, use light or dark mode,
  enable dynamic colors, and opt into an AMOLED black theme.

## How It Works

1. Select the file types and sources that File Navigator should observe.
2. Start the navigator.
3. When a matching file appears, either choose a new destination or a configured Quick Move
   destination from the notification, or let an Auto Move rule handle it.
4. Review completed operations in the move history.

Android restricts access to app-private storage. File Navigator therefore works
with files exposed through shared storage and Android's media and document APIs.

## Download

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.w2sv.filenavigator"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"></a>
  <a href="https://f-droid.org/packages/com.w2sv.filenavigator/"><img alt="Download from F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"></a>
  <a href="https://apt.izzysoft.de/fdroid/index/apk/com.w2sv.filenavigator"><img alt="Get it on IzzyOnDroid" src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80"></a>
  <a href="https://github.com/w2sv/FileNavigator/releases/latest"><img alt="Get it on GitHub" src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" height="80"></a>
</p>

## Screenshots

<table>
  <tr>
    <td><img src="https://github.com/w2sv/FileNavigator/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/1.png" alt="File Navigator home screen"></td>
    <td><img src="https://github.com/w2sv/FileNavigator/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/2.png" alt="File Navigator notification"></td>
    <td><img src="https://github.com/w2sv/FileNavigator/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/3.png" alt="File Navigator file type settings"></td>
    <td><img src="https://github.com/w2sv/FileNavigator/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/4.png" alt="File Navigator app settings"></td>
  </tr>
</table>

## Tech Stack

- [Kotlin](https://kotlinlang.org/) and
  [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Jetpack Compose](https://developer.android.com/compose) with Material 3
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
  for dependency injection
- [Room](https://developer.android.com/training/data-storage/room) for move
  history
- Protocol Buffers and DataStore-backed preferences for persistent
  configuration
- Android MediaStore, Storage Access Framework, foreground services,
  notifications, and Quick Settings APIs
- Gradle convention plugins and a version catalog for shared build
  configuration
- JUnit, Robolectric, MockK, Turbine, and Compose UI tests
- Baseline Profiles for startup and runtime performance

## Architecture

![File Navigator module dependency graph](docs/module-graph.svg)

| Module | Responsibility |
| --- | --- |
| `:app` | Compose UI, app navigation, and top-level dependency wiring |
| `:modules:domain` | Core models, repository contracts, and use-case contracts |
| `:modules:usecase` | Domain use-case implementations |
| `:modules:navigator` | File observation, moving, and foreground service behavior |
| `:modules:navigator-domain` | Navigator-specific models and contracts |
| `:modules:navigator-notifications` | Navigation and move notifications |
| `:modules:navigator-quicktile` | Quick Settings tile integration |
| `:modules:database` | Room database and move-history persistence |
| `:modules:datastore` | Persisted navigator and app configuration |
| `:modules:datastore-proto` | Generated Protocol Buffer configuration models |
| `:modules:designsystem` | Shared Compose theme and UI components |
| `:modules:common` | Shared Android utilities and resources |
| `:modules:test` | Shared test dependencies and utilities |
| `:benchmarking` | Baseline Profile generation and macrobenchmarks |

## Permissions

File Navigator requests only the platform capabilities required for its core
workflow:

- **Manage all files** to observe and move files in shared storage.
- **Notifications** to present newly discovered files and move actions.
- **Foreground service** access to keep the navigator active in the background.
- **Selected-folder access** through Android's system picker for Quick Move and
  Auto Move destinations.

Folder access granted through the system picker is persisted so the same
destination does not need to be approved for every move.

## Contributing

Bug reports and feature requests are welcome in
[GitHub Issues](https://github.com/w2sv/FileNavigator/issues). For code
changes, keep commits focused and ensure `./gradlew check assembleDebug` passes
before opening a pull request.

## Donations

<p align="center">
  <a href="https://www.buymeacoffee.com/w2sv"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" height="41" width="174"></a>
</p>

## License

File Navigator is distributed under the
[GNU General Public License v3.0](LICENSE).

Copyright © [w2sv](https://github.com/w2sv), 2023-present.
