# ruuun!

A fun, private, manually logged running-adventure app for one beginner runner.
Android, Kotlin, Jetpack Compose, Room. No account, no server, no GPS, no economy. Everything works
offline; the only network use is checking GitHub for a newer version of the app.

Download page: **[ruuun.t-o.ie](https://ruuun.t-o.ie)** (source in [`site/`](site/)).

Built to [`docs/ruuun_developer_handoff_v2.md`](docs/ruuun_developer_handoff_v2.md) (behaviour,
data, rules) and [`docs/UI_DESIGN_SPEC.md`](docs/UI_DESIGN_SPEC.md) (layout, copy, styling). Where
the two disagree, the handoff wins.

## Build and install

| Tool | Version used |
|---|---|
| JDK | 17 (Temurin 17.0.21) |
| Gradle | 8.9 (via `./gradlew`) |
| Android Gradle Plugin | 8.6.1 |
| Kotlin | 2.0.21 (K2, Compose compiler plugin) |
| compileSdk / targetSdk | 34 |
| minSdk | 26 (Android 8.0) |
| Build tools | 34.0.0 |

```bash
export JAVA_HOME=/path/to/jdk-17
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew :app:testDebugUnitTest    # 103 unit tests, pure JVM, no device needed
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Opening the project in Android Studio works as-is; `local.properties` is the only machine-specific
file and it is git-ignored.

## The offline data model

Two Room tables. Everything else is derived on read, never stored.

| Table | Rows | Holds |
|---|---|---|
| `runner_settings` | exactly one (`id = 1`) | nickname, onboarded date, starting/current/max target metres, weekly goal, frozen story pace, onboarding flag, `lowStopsLimit` (1.3) |
| `run_log` | one per logged outing | id, local date, actual metres, effort, `targetMAtLog`, optional minutes/mode/tags/note, optional `stops` and the `stopsGoalAtLog` shown at the time, timestamps |

Derived from those rows every time the UI reads them, by pure functions in
[`logic/`](app/src/main/java/app/ruuun/logic/):

- **Outing days**: distinct local calendar dates with at least one log. Several logs on one date
  add distance but count as **one** day for XP, the weekly goal, run-count badges and chapters.
- **XP and level**: `XP = 50 × outing days`, `level = 1 + outingDays / 5`. Nothing else grants XP.
- **Next mission**: [`TargetRecommender`](app/src/main/java/app/ruuun/logic/TargetRecommender.kt):
  **The distance only grows after a run with few stops** (since 1.3): at most `lowStopsLimit`, 2
  by default, set in Settings ("Distance goes up after", 0 to 5). A run that beats the mission
  with more stops, or with stops not counted, **catches the mission up to the distance run and no
  further** (1.3.1): the same distance next time, with fewer stops. A `Hard` short run still eases
  it off. With low stops, **beating the mission sets the next one beyond the distance just run**, by a bump that depends on
  how it felt: +25 m for `Hard`, one band step for `Okay`, two for `Easy` (bands are 25 m under
  500 m, 50 m under 1,500 m, 100 m above). Effort never drags the mission back below what was
  actually covered. Falling short repeats the mission, or eases off 10% if it felt `Hard`. Matching
  it exactly steps up one band after two days at that mission with low stops, `Easy` or `Okay`.
  Backdated entries never raise it, and `Easier` / `Harder` on Home move it by 25 m by hand. **The
  Settings cap is the main brake**: it defaults to 5,000 m (3,000 m before 1.3; the database
  migration and a restored 1.2 backup move anyone still on the old default, and leave a cap a
  parent chose alone). Editing a run's date, distance or effort resets the mission to a safe
  repeat; correcting the latest run's stops across the limit replays the rules for that run; adding
  a stop count, a note or tags otherwise leaves the mission where it is.
- **Stop goal**: [`StopGoal`](app/src/main/java/app/ruuun/logic/StopGoal.kt). Counting stops is
  optional (0 means non-stop). Once a run has been counted, the mission gains a second half, built
  on the latest counted day: one fewer stop, never below 0. It holds instead after a `Hard` run, and
  while the stops are already within the limit (the distance does the growing then), except at the
  longest mission setting, where it asks for one fewer again. A worse day relaxes the goal rather
  than piling on. The longest counted run stands for a day; runs shorter than half their mission
  and runs entered for an earlier date (worked out from `createdAt` order) never move the goal. It
  is derived from the logs, never stored; each log keeps the goal it was run against.
- **Weeks and quests**: ISO Monday-to-Sunday, each week identified by its Monday date, which keeps
  the December/January and leap-year boundaries honest. One weekly goal (the child's own run days per
  week; `mainQuest` in the code) and one authored optional side quest per week.
- **Chapters**: 24 chapters in two seasons of four worlds (season 2, added in 1.3, opens after the
  festival at 100 run days and ends at 320), gated purely on distinct outing days; a journey started on the
  1-outing/week plan uses `ceil(days / 2)` throughout, frozen at setup.
- **Badges**: all 193 evaluate as typed `metric >= threshold` rules against a `StatsSnapshot`.
  There is no award table: a badge is earned while its rule holds, and locks again if corrected data
  stops satisfying it. The edit and delete dialogs say so.

**Naming in the UI.** Each word means one thing on every screen: *mission* is only the suggested
distance for the next run, plus its stop goal once stops are counted (Home), *weekly goal* is run days this week, *side quest* is the optional
weekly task, and the *Journey* tab holds this week, the story and the 52 weeks. XP and chapters both
count run days, never distance, and their captions say so.

Bundled content lives in [`content/`](app/src/main/java/app/ruuun/content/) as Kotlin constants, not
database rows: 24 chapters, 52 weekly themes, 193 badge definitions (the handoff's 141, six for the extra tags, and in 1.3 nineteen Non-stop badges, twelve season 2 chapter badges and fifteen long-tail badges). Never raise an existing badge's threshold: badges are recomputed, so it would lock again for a child who had earned it.

### Backup

Settings → Export writes a UTF-8 JSON file through `ACTION_CREATE_DOCUMENT`; Import reads one
through `ACTION_OPEN_DOCUMENT`, validates format, schema version, unique ids, enums and numeric
ranges, shows a preview, then replaces settings and logs **atomically** in one Room transaction. A
rejected file leaves existing progress untouched. XP, badges, quests and chapters are not exported:
they are recomputed. The file is `schemaVersion` 2 since 1.3 (it carries stops); version 1 files
from older builds still restore, with no stops.

## Source layout

```
app/src/main/java/app/ruuun/
  MainActivity.kt
  data/      Room entities, converters, DAO, database, repository
  logic/     StatsCalculator, TargetRecommender, StopGoal, BadgeEvaluator, WeekCalculator,
             JourneyView, RunInput, Formatters   (pure Kotlin, no Android imports, fully unit tested)
  content/   Chapters, Weeks, Achievements
  backup/    BackupFile (JSON read/write and validation)
  ui/        theme/, components/, screens/, RuuunApp (navigation), RuuunViewModel,
             Mascot (which picture of Ruu, each world and each chapter shows where)
app/src/main/res/drawable-nodpi/
             ruu_*.webp (Ruu, one shared canvas), scene_*.webp (worlds), chapter_*.webp
```

## Privacy and the network

- The only permissions are `INTERNET` and `REQUEST_INSTALL_PACKAGES`, both for the in-app updater
  below. No location, activity-recognition or storage permission is declared.
- No Firebase, analytics, ads, or networking library on the classpath. The updater uses the
  platform's `HttpURLConnection` for two plain GETs to github.com and sends nothing about the runner.
- Fonts are bundled TTFs; the logo is a bundled drawable. Run data never leaves the phone except as
  a backup file the user exports by hand.
- Automatic cloud backup and device transfer are switched off in
  [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml); exports are a
  deliberate manual action.

## Updates and releases

On launch the app fetches
`https://github.com/to-ie/ruuun/releases/latest/download/version.json`, which GitHub redirects to
the newest full (not pre-) release. If its `versionCode` is higher than the installed one, a banner
appears at the top of the four tabs: **Update** downloads `ruuun.apk` into the app's cache,
checking its SHA-256 against `version.json`; **Install** hands it to Android's installer (the first
time, Android asks to allow installs from ruuun). Settings has **Check for updates** too. The rules
for what the app accepts live in [`UpdateInfo`](app/src/main/java/app/ruuun/update/UpdateInfo.kt):
the APK link must point at this repository's releases. Android itself refuses any APK signed with
a different key, so the release key is what really protects users.

Releases are signed with a key kept **outside** the repo: `~/.android/ruuun-release.jks`, with its
passwords in `~/.gradle/gradle.properties` (`RUUUN_RELEASE_STORE_FILE`, `..._STORE_PASSWORD`,
`..._KEY_ALIAS`, `..._KEY_PASSWORD`). Without those properties `assembleRelease` builds unsigned.
**Back both files up together.** If they are lost, no future APK can update an installed copy.

To cut a release:

```bash
# 1. bump versionCode and versionName in app/build.gradle.kts, update the history below
# 2. commit and push main, write the notes to a file, then:
scripts/release.sh notes.md
```

[`scripts/release.sh`](scripts/release.sh) refuses a dirty tree, an unpushed `main`, a reused tag or
a `versionCode` that is not above the published one, runs the unit tests, builds the release APK,
checks it carries the release key's certificate, then publishes `ruuun.apk` and `version.json` as the
newest GitHub release. Phones see the banner on their next launch, and the download button on
ruuun.t-o.ie serves the new APK straight away.

The landing page is static HTML in [`site/`](site/), deployed to GitHub Pages by
[`.github/workflows/pages.yml`](.github/workflows/pages.yml) on any push to `main` that touches it.

## Tests

`./gradlew :app:connectedDebugAndroidTest` runs 31 instrumented tests against a real Room database
on a connected device or emulator: onboarding, saving, same-date runs, editing, deleting (only the
latest run moves the mission), tag and note persistence, atomic import, delete-everything, the
mission controls against the cap, the database migrations, and 150 runs.

`./gradlew :app:testDebugUnitTest` runs 169 JVM tests with no emulator required:

| File | Covers |
|---|---|
| `StatsCalculatorTest` | XP/level maths, same-day grinding, comeback gaps, active months, recomputation purity |
| `TargetRecommenderTest` | every progression rule, the 10% ceiling, `Make this easier`, corrections, welcome-back |
| `WeekCalculatorTest` | ISO week identity, Dec/Jan rollover, leap day, journey weeks, week 53 rotation, side quests |
| `BadgeEvaluatorTest` | badge rules, progress, secret handling, a 150-outing journey |
| `ContentTest` | 193 badges / 52 weeks / 24 chapters, every rule backed by a computed metric, early badges keep their thresholds, banned-words check |
| `BackupFileTest` | export/import round trip, duplicate ids, out-of-range, future-dated and malformed files |
| `StopGoalTest` | the stop goal rules, the low-stops gate on distance, the Run saved stop note, Non-stop badge metrics, stops in backups, version 1 backups |
| `StopEdgeCaseTest` | same-day badge farming, back-filled and short runs, the stop limit setting, extra-run notes, personal bests, one year on, old backups and the cap |
| `RunInputTest` | distance and duration parsing, note/nickname cleaning, distance formatting |
| `WorkedScenariosTest` | all ten Appendix D scenarios plus the UI spec's sample child |
| `JourneyMapTest` | the four-world map: ordering, exactly one chapter current, world completion, gentle pacing |
| `InvariantSweepTest` | 1,000 randomised journeys, every rule re-checked after each run |
| `MascotTest` | which Ruu shows after a save (a cheer, Hard, ordinary, warm-ups and extra runs), one picture per chapter |
| `EdgeCaseTest` | backups missing optional keys or carrying unknown ones, runs logged before onboarding, weekly-goal changes, a future onboarding date, a 1 m outing, a decade of logs |

## Verified on a device

Built and driven on an Android 14 (API 34) emulator: first-run setup, Home empty state showing the
300 m first mission and the `Liftoff!` hint, logging a 320 m Easy outing (+50 XP, 10 badges,
chapter 1 at 1/4 days), a second log on the same date showing `Distance added` rather than a second
+50 XP, Missions, the 141-badge cabinet, History, Settings, and a JSON export written through the
system file picker with no storage permission.

## Audit notes

A security and robustness pass in 0.4 found and fixed: an imported backup with a corrupted start
date could ask the Missions screen for ~100,000 week rows; imports had no size limit; file reads,
JSON parsing and serialisation all ran on the main thread; a file-size guard used an API 33 method
under a minSdk of 26 and would have crashed on Android 8 to 12; and a single unrecognised tag
rejected an entire backup. Start dates, run counts and file size are now bounded, all of that work
runs on `Dispatchers.IO`, and unknown tags and modes are skipped rather than fatal. An unknown
*effort* is still refused, because it drives the rules.

The app logs nothing and pulls in no third-party libraries. Since 1.1 it declares `INTERNET` and
`REQUEST_INSTALL_PACKAGES` for the updater only.
`androidx.compose.ui.tooling.PreviewActivity` is exported in **debug** builds only (it arrives with
`ui-tooling`), which is one reason releases ship the signed release build. Android Lint
reports zero errors.

## Known gaps

- **No UI tests.** The Room layer has 31 instrumented tests (including the 1 to 2 and 2 to 3 migrations), but the Compose screens are exercised
  by hand on an emulator.
- **Release build is unminified.** R8 is off; the signed release APK is about 12 MB.
- **The updater has no automated test against a live release.** Its parsing and URL rules are unit
  tested; the download and install path was checked by hand on an emulator.
- **History renders every month** (lazily) rather than paging a month at a time. Fine at the tested
  scale; revisit if a journey ever runs to thousands of runs.
- **Accessibility is specified, not audited.** Semantics, content descriptions and 48 dp targets are
  in place and large font scales were considered, but no TalkBack pass has been run.
- **Stat tiles stay in a row at very large font scales.** The spec allows them to stack into one
  column above 150%; text shrinks to fit instead.
- **Motion is checked by eye only.** The animations were recorded and reviewed frame by frame on an
  emulator, with and without "Remove animations"; there are no automated UI tests for them.

## Not built, deliberately

No avatar builder, coins, shop, inventory, cosmetics, daily rewards, notifications, audio coach,
reminders, parental PIN, remote content, accounts or sync. See handoff §1.1. The update check is
the one deliberate exception to "no network".

## Docs

Everything that is not build input lives in [`docs/`](docs/):

| Path | What it is |
|---|---|
| [`docs/ruuun_developer_handoff_v2.md`](docs/ruuun_developer_handoff_v2.md) | The brief: product rules, 141-badge catalogue, 52 weekly themes |
| [`docs/UI_DESIGN_SPEC.md`](docs/UI_DESIGN_SPEC.md) | Screen-by-screen layout, components and copy |
| [`docs/UI_HANDOFF_README.md`](docs/UI_HANDOFF_README.md) | The note that came with the UI handoff package |
| [`docs/reference/mockup-html/`](docs/reference/mockup-html/) | The eight mockup screens. Visual reference only, never shipped |
| [`docs/licences/`](docs/licences/) | SIL OFL licences for Baloo 2 and Nunito |
| [`docs/original/`](docs/original/) | The handoff zip and the uncropped logo, as supplied. Every file in them is already extracted into the project; kept only as the untouched originals |

## Version history

The Android `versionCode` counts up independently of the name (currently 13) because it must never
decrease.

- **1.4.1**: more Ruu. On Home, Ruu is much bigger (176 dp, from 108) and stands on the mission card
  with its head above the top edge, breathing gently (still when animations are off). Run saved
  shows Ruu at 220 dp, the world pictures are taller with a bigger Ruu, and Ruu now also appears on
  Log a run (taking notes, with a speech bubble) and on Badges (holding up a medal and naming the
  closest badge). On a narrow phone or with large text Ruu still shrinks before the distance wraps.

- **1.4**: Ruu the fox joins. Ruu is the new logo (with the ruuun! name) and the app icon, stands
  beside the next mission on Home (arms wide after a long break), cheers on Run saved for anything
  new, wipes its brow after a Hard run, and is proud the rest of the time. Every world card shows the
  world's picture with Ruu dressed for it. Finishing a chapter shows that chapter's own picture, and
  finished chapters on the Journey map open their picture when tapped. Also: editing or deleting an
  older run no longer resets the mission (only the latest run does), Home's mission number never
  wraps next to Ruu on a small phone or with large text, and the stop goal only says "from N" when it
  is lower than N. Log a run says what is missing ("Type how far you went to save.") instead of
  leaving a grey Save button unexplained. A backup with a run dated after tomorrow is refused.
  169 unit tests and 31 device tests.

- **1.3.2**: **Check for updates** in Settings works. It found new versions, but the update banner
  was only drawn on the four tabs, so on Settings nothing visible happened. The banner now shows on
  Settings too, and a manual check always says what it found ("ruuun 1.3.2 is ready. Tap Update at
  the top.").

- **1.3.1**: a run that beats the mission with more than 2 stops (or none counted) now sets the
  next mission to the distance run, instead of holding it: a 1.1 km run with 8 stops on a 300 m
  mission makes the mission 1.1 km with a stop goal of 7. Phones that 1.3 left stuck are caught
  up once by a data-only database migration (2 to 3) that only touches a mission nobody moved
  since. 161 unit tests and 28 device tests.

- **1.3**: after the first real run (1.1 km with 8 stops), stops became part of the mission.
  - **Stops:** an optional stop counter on the log screen, above "How did it feel?". Once stops
    are counted, Home shows a stop goal under the distance ("Stop goal: 7 stops or fewer · 8 last
    time"). Run saved says how the stops went, History and the run details show them, and a Stops
    card on Journey charts the last ten counted days.
  - **Distance follows stops:** the mission only gets longer after a run with 2 stops or fewer
    (adjustable in Settings). Two runs that match the mission with low stops also step it up, Easy
    or Okay. Run saved says how many fewer stops are needed ("1 fewer stop and it gets longer").
  - **Longest mission 5 km:** the default is now 5 km; a phone or an old backup still on the 3 km
    default moves up.
  - **Badges, 193 in all:** nineteen **Non-stop** badges (counting, fewer than before, stop goals
    met, non-stop runs from 100 m to 5 km), counted per day and ignoring short laps so they cannot
    be farmed, and a long tail for year two (run days 175 to 365, total distance to 500 km, 65 to
    104 weekly goals, 150 quests, one and two years on). Personal bests on Run saved: longest run
    and longest non-stop run. No existing badge changed, so nothing earned is lost.
  - **Story season 2:** four new worlds (Coral Coast, Jungle Run, Frosty Peaks, Comet Run) and
    twelve chapters after the festival, from 112 to 320 run days, with a chapter badge each.
  - **Fixes:** editing an old run no longer knocks the next mission back; a run entered for an
    earlier date, or a short extra run, no longer moves the stop goal; screen reader labels on the
    stop counter.
  - Backups are version 2 (they carry stops and the stop limit); older ones still restore. 160 unit
    tests and 23 device tests, including the database upgrade.

- **1.2**: the installer's "App installed" screen shows ruuun! instead of app.ruuun (the name is
  now plain text in the manifest, which Android 14's installer can read). On the Journey map, a finished world folds down to its banner so the current
  one stays near the top. Tap the banner to look back at its chapters. Every badge now has its own
  icon, revealed when it is earned (147 Material icons, no two the same, in
  [`BadgeIcons.kt`](app/src/main/java/app/ruuun/ui/BadgeIcons.kt)); locked badges still show a
  padlock. Movement, all of it switched off by the phone's "Remove animations" setting
  ([`Motion.kt`](app/src/main/java/app/ruuun/ui/Motion.kt)): after a run, confetti falls, the tick
  and "+50 XP" pop in, the level bar fills from where it was (with a "Level up!" moment), new
  badges pop in one by one and a finished chapter unfolds, with a buzz on save, on new badges and
  on levelling up. On Home the mission number rolls when Easier or Harder is tapped and newly
  filled weekly goal dots bounce in; on Journey the chapter bars fill as the tab opens and
  finished worlds slide open and shut; an earned badge wiggles before its sheet opens. The
  celebration plays once and does not replay on rotation. 121 unit tests.

- **1.1**: the app can update itself. When a newer release is on GitHub, a banner at the top of
  the tabs offers to download and install it, and Settings has **Check for updates**. Releases are
  now signed with a dedicated release key, so moving from 1.0 beta means one export, reinstall and
  import. Download page at ruuun.t-o.ie. Also: one word per idea: the Missions tab is now **Journey** (map icon) and no longer
  repeats the next-mission card or the closest badges; "Main quest" and "Runs each week" are now
  **Weekly goal** everywhere; quest badge hints say "weekly goal or side quest". The level caption,
  chapter progress and Run saved mission line now say what moves them, including why the mission
  went up, down or stayed.

- **1.0 beta**: the whole journey is now visible on Missions: four worlds, twelve chapters, showing
  what is finished, what is in progress and what each one still asks for. Form state survives a
  rotation. First instrumented tests against a real database, plus a randomised invariant sweep.

- **0.5**: a splash screen showing the logo on brand navy while the database is read; Home drops
  its logo bar for the same header the other tabs use, with the settings gear beside the greeting.
- **0.4**: beating your mission now sets the next one *beyond* the distance you ran, by an amount
  that depends on how it felt; hardening from a security and robustness audit (see above).
- **0.3**: eight run tags instead of four; every badge explains itself in a tap-through sheet from
  the cabinet, the `Closest badges` list and the save celebration; `Show all N badges` replaces the
  dead `+N more`; chapter progress reads "1 of 4 runs" rather than "1 / 4 days"; beating your
  mission now raises it even when the run felt hard.
- **0.2**: `Harder` button beside `Easier`; the mission now follows a distance you beat rather than
  creeping up; launcher icon rebuilt from the real logo artwork; navy app header; "outings" renamed
  to "runs" throughout; badge and mission copy rewritten to say plainly what to do.
- **0.1**: first complete build.

## Installing on a phone

Normal use: open **[ruuun.t-o.ie](https://ruuun.t-o.ie)** on the phone and tap Download. Later
versions arrive through the in-app banner.

For development, the debug APK is signed with this machine's debug key, so it sideloads without
Play but cannot update a release install (or the other way round):

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk   # phone on USB, USB debugging on
```

Without a cable, copy the APK to the phone and open it from the Files app; Android will ask to
allow installs from that app the first time. Requires Android 8.0 or newer.
