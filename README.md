# ruuun!

A fun, private, fully offline, manually logged running-adventure app for one beginner runner.
Android, Kotlin, Jetpack Compose, Room. No account, no server, no GPS, no economy.

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

./gradlew :app:testDebugUnitTest    # 89 unit tests, pure JVM, no device needed
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Opening the project in Android Studio works as-is; `local.properties` is the only machine-specific
file and it is git-ignored.

## The offline data model

Two Room tables. Everything else is derived on read, never stored.

| Table | Rows | Holds |
|---|---|---|
| `runner_settings` | exactly one (`id = 1`) | nickname, onboarded date, starting/current/max target metres, weekly goal, frozen story pace, onboarding flag |
| `run_log` | one per logged outing | id, local date, actual metres, effort, `targetMAtLog`, optional minutes/mode/tags/note, timestamps |

Derived from those rows every time the UI reads them, by pure functions in
[`logic/`](app/src/main/java/app/ruuun/logic/):

- **Outing days** — distinct local calendar dates with at least one log. Several logs on one date
  add distance but count as **one** day for XP, the weekly goal, run-count badges and chapters.
- **XP and level** — `XP = 50 × outing days`, `level = 1 + outingDays / 5`. Nothing else grants XP.
- **Next mission** — [`TargetRecommender`](app/src/main/java/app/ruuun/logic/TargetRecommender.kt):
  **Running further than the mission moves it to that distance** (rounded down to 25 m, capped by
  the longest suggested mission in Settings) whatever the effort felt like — distance wins over
  effort. Falling short repeats the mission, or eases off 10% if it felt `Hard`. Matching it exactly
  steps up one band (+25/+50/+100) after two `Easy` days at that distance. Backdated entries never
  raise it, and `Easier` / `Harder` on Home move it by 25 m by hand.
- **Weeks and quests** — ISO Monday-to-Sunday, each week identified by its Monday date, which keeps
  the December/January and leap-year boundaries honest. One main quest (the child's own weekly goal)
  and one authored optional side quest per week.
- **Chapters** — 12 chapters gated purely on distinct outing days; a journey started on the
  1-outing/week plan uses `ceil(days / 2)` throughout, frozen at setup.
- **Badges** — all 141 evaluate as typed `metric >= threshold` rules against a `StatsSnapshot`.
  There is no award table: a badge is earned while its rule holds, and locks again if corrected data
  stops satisfying it. The edit and delete dialogs say so.

Bundled content lives in [`content/`](app/src/main/java/app/ruuun/content/) as Kotlin constants, not
database rows: 12 chapters, 52 weekly themes, 141 badge definitions.

### Backup

Settings → Export writes a UTF-8 JSON file through `ACTION_CREATE_DOCUMENT`; Import reads one
through `ACTION_OPEN_DOCUMENT`, validates format, schema version, unique ids, enums and numeric
ranges, shows a preview, then replaces settings and logs **atomically** in one Room transaction. A
rejected file leaves existing progress untouched. XP, badges, quests and chapters are not exported —
they are recomputed.

## Source layout

```
app/src/main/java/app/ruuun/
  MainActivity.kt
  data/      Room entities, converters, DAO, database, repository
  logic/     StatsCalculator, TargetRecommender, BadgeEvaluator, WeekCalculator, JourneyView,
             RunInput, Formatters   (pure Kotlin, no Android imports, fully unit tested)
  content/   Chapters, Weeks, Achievements
  backup/    BackupFile (JSON read/write and validation)
  ui/        theme/, components/, screens/, RuuunApp (navigation), RuuunViewModel
```

## Offline guarantees

- No `INTERNET`, location, activity-recognition or storage permission is declared. The merged
  manifest's only entry is androidx's local `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` signature
  permission, which grants no access to anything.
- No Firebase, analytics, ads, or networking library on the classpath.
- Fonts are bundled TTFs; the logo is a bundled drawable. Nothing is fetched at runtime.
- Automatic cloud backup and device transfer are switched off in
  [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml); exports are a
  deliberate manual action.

## Tests

`./gradlew :app:testDebugUnitTest` — 89 JVM tests, no emulator required:

| File | Covers |
|---|---|
| `StatsCalculatorTest` | XP/level maths, same-day grinding, comeback gaps, active months, recomputation purity |
| `TargetRecommenderTest` | every progression rule, the 10% ceiling, `Make this easier`, corrections, welcome-back |
| `WeekCalculatorTest` | ISO week identity, Dec/Jan rollover, leap day, journey weeks, week 53 rotation, side quests |
| `BadgeEvaluatorTest` | badge rules, progress, secret handling, a 150-outing journey |
| `ContentTest` | 141 badges / 52 weeks / 12 chapters, every rule backed by a computed metric, banned-words check |
| `BackupFileTest` | export/import round trip, duplicate ids, out-of-range and malformed files |
| `RunInputTest` | distance and duration parsing, note/nickname cleaning, distance formatting |
| `WorkedScenariosTest` | all ten Appendix D scenarios plus the UI spec's sample child |
| `EdgeCaseTest` | backups missing optional keys or carrying unknown ones, runs logged before onboarding, weekly-goal changes, a future onboarding date, a 1 m outing, a decade of logs |

## Verified on a device

Built and driven on an Android 14 (API 34) emulator: first-run setup, Home empty state showing the
300 m first mission and the `Liftoff!` hint, logging a 320 m Easy outing (+50 XP, 10 badges,
chapter 1 at 1/4 days), a second log on the same date showing `Distance added` rather than a second
+50 XP, Missions, the 141-badge cabinet, History, Settings, and a JSON export written through the
system file picker with no storage permission.

## Known gaps

- **No instrumented tests.** The Room DAO transactions and the Compose screens were exercised by
  hand on an emulator; only the pure logic is covered automatically.
- **Release build is unsigned and unminified.** The debug APK is the deliverable; `assembleRelease`
  works but has no signing config and R8 is off.
- **History renders every month** (lazily) rather than paging a month at a time. Fine at the tested
  scale; revisit if a journey ever runs to thousands of runs.
- **Accessibility is specified, not audited.** Semantics, content descriptions and 48 dp targets are
  in place and large font scales were considered, but no TalkBack pass has been run.
- **Stat tiles stay in a row at very large font scales.** The spec allows them to stack into one
  column above 150%; text shrinks to fit instead.
- **No animations at all.** The check and confetti are static, which satisfies "must work without
  animation"; the one or two small motion touches the spec permits were not added.

## Not built, deliberately

No avatar builder, coins, shop, inventory, cosmetics, daily rewards, notifications, audio coach,
reminders, parental PIN, remote content, accounts or sync. See handoff §1.1.

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

- **1.2** — eight run tags instead of four; every badge explains itself in a tap-through sheet from
  the cabinet, the `Closest badges` list and the save celebration; `Show all N badges` replaces the
  dead `+N more`; chapter progress reads "1 of 4 runs" rather than "1 / 4 days"; beating your
  mission now raises it even when the run felt hard.
- **1.1** — `Harder` button beside `Easier`; the mission now follows a distance you beat rather than
  creeping up; launcher icon rebuilt from the real logo artwork; navy app header; "outings" renamed
  to "runs" throughout; badge and mission copy rewritten to say plainly what to do.
- **1.0** — first complete build.

## Installing on a phone

The debug APK is signed with the standard debug key, so it sideloads without Play:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk   # phone on USB, USB debugging on
```

Without a cable, copy the APK to the phone and open it from the Files app; Android will ask to
allow installs from that app the first time. Requires Android 8.0 or newer.
