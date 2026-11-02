# ruuun! - Simplified Offline Android App: Build Handoff

**Version:** 2.0 (simplified)  
**Date:** 1 November 2026  
**Audience:** Android development agent building a working APK  
**App name:** `ruuun!` (lowercase, three `u`s, exclamation mark)  
**Product:** A fun, private, fully offline, manually logged running-adventure app for one 11-year-old beginner.

> **Build directive:** Implement a small, complete Android app. The child should enjoy logging runs, seeing achievable next goals, earning badges and making progress for at least 52 weeks. Do not introduce accounts, servers, live tracking, shopping, virtual currency or systems that only exist to support those features. Make sensible implementation decisions without asking for new product direction. Prioritize an installable, tested APK and durable on-device progress.

## 1. Non-negotiable product scope

- **Single child, single device.** No account, authentication, registration, social features or multi-user support.
- **100% offline.** No backend, Firebase, API calls, sync, internet permission, analytics, ads or in-app purchases.
  *Revised in 1.1:* one exception, the update check. The app reads `version.json` from this project's newest GitHub release and can download and install a newer signed APK. It sends nothing about the runner, and everything else stays offline.
- **Manual logs only.** No GPS, activity recognition, pedometer, smart watches, stopwatch, route maps or location/weather access.
- **No virtual currency.** No coins, points to spend, wallet, shop, purchase ledger, virtual goods, collectible cosmetics, outfits, pets, equipment, inventory, customisable avatar or loot rewards.
- **Keep the actual motivations.** Runs, adaptive next target, friendly levels/XP, milestones, achievement badges, weekly quests, 12 story chapters and 52 weekly themes.
- **All rewards are intrinsic.** Completing a run shows a cheerful message, XP progress (computed from runs), any newly reached badges, current chapter progress and the next mission. A badge is a visual trophy only; it grants nothing else.
- **Simple, parent-adjustable and kind.** Walking counts; distance suggestions adapt; allow repeats/easier outings. No weight, calories, public performance or shaming.
- **Private and recoverable.** Store locally in Room/SQLite and support deliberate manual JSON export/import through Android's file picker.
- **Year of interest, no year-long escalation.** Twelve narrative chapters, 52 authored weekly themes and **141 seeded achievements**, without requiring running farther every session.

### 1.1 Deliberately NOT building

No runner-avatar builder, item art packs, coin economy, inventory, shop screen, reward transactions, avatar equipment state, daily login rewards, push notifications, audio coach, animation engine, location tags captured automatically, reminders, parental PIN, parent account, custom trophy marketplace, remote content download, AI features or separate service layer. Do not add these later unless explicitly requested.

One or two very small built-in confetti/checkmark animations are fine, but the app must also work without animation and sounds. Do not spend development time on a sophisticated game engine.
*Revised after 1.1:* more motion was added for excitement (celebration after a run, rolling mission number, bouncing weekly dots, sliding worlds, badge wiggle, haptic buzzes), all built from plain Compose animation with no engine, and all switched off by the phone's "Remove animations" setting.

### 1.2 Acceptance definition of the experience

At any point, the child can answer: "How many times have I been out?", "What's my next achievable mission?", "What badge am I close to?", and "What part of the adventure is next?" A single manual session should be loggable in under 30 seconds. The app never insists that the next run must be longer.

## 2. Brand and visual direction

The approved **ruuun!** logo in this chat is a blue running shoe with orange accents and energetic wordmark. Use it as the visual reference; if its image asset is available in the project, bundle it as a local bitmap/drawable. If not, use a clean text `ruuun!` title until the asset is supplied. No remote assets.

- **Palette:** navy `#101C2E`, sky blue `#49C6FF`, orange `#FF8C24`, lime `#C9F54A`, white `#F7F9FC`. Choose accessible contrast.
- **Style:** energetic but not babyish, friendly large cards, simple icons, excellent readability on a phone.
- **Tone:** celebrate trying, returning, walking and consistency. Avoid the words "failed", "behind", "lazy", "bad run", "burn calories" and "lose weight".
- **UI illustrations:** one background/header illustration per world is optional, not required. A world can be a plain coloured banner with a title and emoji/vector icon.
- **Bottom navigation:** `Home` / `Journey` / `Badges` / `History`. Settings accessible via a gear in the Home header. `Log Run` is the main CTA on Home, not an extra tab.

## 3. Screens, in enough detail to build

### 3.1 First-run setup (one screen, ~1 minute)

Collect only: optional nickname (default `Runner`), starting distance plan (`New to it` 300 m; `Getting moving` 500 m; `Already jogging` 800 m), and preferred weekly outings (1, 2 default, or 3, parent-assisted). Brief note: walk breaks are encouraged and rest is good. No age, DOB, email, surname, photograph, precise location or login. Save locally. Freeze the initial story pacing at setup: 1 outing/week uses half-distance **chapter run counts** (not distance targets); 2 or 3/week uses the normal chapter run counts. Weekly goal can be changed later without reversing earned story progress.

### 3.2 Home

1. Header: `ruuun!` logo and settings gear.
2. `Hello, [nickname]` and a simple `Level N` bar showing XP toward next level.
3. **Next Mission** hero: suggest a distance in metres, "Walk or jog. Breaks welcome", plus `Log a run` CTA and `Make this easier` button.
4. Three stat tiles: `Runs`, `Distance (km)`, `Badges`.
5. Weekly goal progress: `1 / 2 outings this week` with no red overdue states.
6. Upcoming badge: one reachable numeric milestone and `X/Y` progress.
7. Story chapter: name, one-sentence teaser and `current qualifying days / chapter target`.
8. Empty state must feel complete: zero counters, first 300 m suggestion and first `Liftoff!` badge hint.

### 3.3 Add a run (single screen or bottom sheet)

**Required fields:** session date (defaults today; may backdate, never future); *actual* distance (positive number; metres or kilometres); how it felt (`Easy`, `Okay`, `Hard`).  
**Optional:** duration (minutes, not used for pacing, rankings or progression), movement mode (`Walk`, `Walk + jog`, `Mostly jog`), brief note (140 chars), and simple self-reported tags (`Park`, `Family`, `New route`, `Nature`). Multi-select tags. No location/time/weather permission is required. Nothing about tags is checked remotely.

*Added in 1.3:* an optional **stop counter** ("How many times did you stop?"), a minus/plus stepper that starts at "Not counted". 0 means non-stop. When there is a stop goal, the hint under it shows the goal and "Breaks are fine."

Distance input displays suggested mission distance as a **hint only**; never silently saves the suggested distance as the actual distance. Validate input; keep save enabled after edits and protect against double taps. Confirmation shows actual distance, participation XP, any newly earned badges, chapter progress, and next mission. Provide `Done` and `View badges`; keep animations short and optional.

**Definition of an outing day:** Multiple valid sessions on the same local calendar date are allowed. All distances contribute to lifetime total, but that date counts as **one** run day for XP, weekly outing goal, run-count badges and chapter milestones. No same-day grinding. A 1-metre real effort may be logged without punishment; warn (do not prevent) implausible extreme manual entries.

### 3.4 Journey

*Revised after 1.0 beta (was "Missions"): the tab name and the distance target shared a word, so the tab is now Journey and the next mission lives on Home only.*

**Naming, used the same way on every screen:** *mission* means only the suggested distance for the next run; *weekly goal* means the run-days-per-week target (the "main quest" in section 6); *side quest* means the optional weekly task. "Quest" on its own is not used for the weekly goal in the UI.

Ordered by time span:
- **This week:** the weekly goal (`Run on {goal} days this week`) and one optional side quest derived from the 52-week calendar below. Checkmarks are computed from logs; no claim, reward button or currency.
- **Chapter card:** current world/chapter name, short narrative, progress toward next chapter, saying that any distance counts.
- **Whole journey:** four worlds, twelve chapters, what is done, current and ahead.
- **52-week map/list:** current week highlighted, older weeks browsable and future titles visible. This is a simple vertical list, not a graphical game map.
- No next-mission card (Home has it) and no badge hints (Home and Badges have them); no endlessly scrolling mission board or branching campaign state.

### 3.5 Badges

A searchable/filterable list is *optional*; simple category tabs or grouped sections suffice. Each badge shows a static icon/emoji, name, rule, status (`Earned` or `Locked`) and count progress where meaningful. Hidden/secret badges can show `???` until reached. Show a tiny celebratory banner when a new criterion becomes true on save. Badge artwork can use 8-12 category icons shared across badges: no need for 141 separate illustrated assets.

**Important simplicity rule:** Badges are recalculated from current logs and the fixed bundled rules. If a parent corrects or removes a mistaken run, totals, level and currently eligible badges may decrease. This is acceptable, because **data accuracy and one source of truth** outweigh a complicated permanent-award ledger. There is no `AchievementAward`, reward ledger or claims table. Explain the effect in the edit/delete confirmation.

### 3.6 History

Newest-first dated list of logged runs; each item shows actual distance, effort and optional note. Tap to view, edit or delete with confirmation. Header shows all-time distance, unique outing days, longest single outing, days active this month. A simple grouped-by-month list is sufficient; no calendar grid is required. Editing/deleting recomputes all derived state. Keep back navigation obvious.

### 3.7 Settings (parent-assisted, no PIN)

Nickname, weekly target (1/2/3 outings), maximum suggested session distance (default 5,000 m since 1.3, was 3,000 m), initial story pacing (view only), `Reset next mission to [distance]`, export progress to JSON, import JSON replacing all existing progress (with preview and warning), `Delete all progress` (double confirmation), about/privacy. No account settings, remote parent dashboard or separate parental authentication system. Any app user with device access can change settings; this is a local family app, not security software.

*Added in 1.3:* "Distance goes up after", the stop limit for the distance rule (0 to 5 stops, default 2).

## 4. Beginner distance progression (one small deterministic function)

This is a gentle suggestion algorithm, **not medical advice or a rigid coaching schedule**. Walking is always welcome. A parent chooses an achievable starting distance. No badge requires running continuously. *Revised in 1.3, at the parent's request after the first real run (1.1 km with 8 stops):* the optional Non-stop badges and the stop goal do reward stopping less over time. Counting stays optional, nothing is lost by not counting, and stopping is never described as failing. A user can log any actual distance regardless of the suggestion.

**Defaults:** New 300 m; Moderate 500 m; Jogging 800 m; 2 outing days/week; cap 5,000 m (3,000 m before 1.3); minimum 100 m. Current target is an integer number of metres.

Rules applied when a *new, latest-date qualifying outing day* is logged:

1. **Hard:** next target becomes `max(100, floor_to_25(current_target * 0.90))`. Show a supportive rest/easier message.
2. **Actual distance below suggested target:** repeat the same target, regardless of rating (unless `Hard`, above). Still grant ordinary participation XP.
3. **Okay:** repeat the same target.
4. **Easy + met target:** after **two qualifying outings on distinct dates** at the same suggested target, with both rated Easy, increase one small step; otherwise repeat. Do not increase in response to multiple logs on one day.
5. **Step size:** +25 m below 500 m, +50 m from 500 to 1,499 m, +100 m from 1,500 m upward. Never increase by more than 10% in one decision or above the configured cap. When the step would exceed 10%, hold target steady. Thus 300 -> 325 m is valid; 300 -> 350 m is not.
6. **14+ days without an outing:** on the next mission display offer a gentle `Welcome back: repeat or reduce` option; do not erase earlier progress or drop the target automatically.
7. `Make this easier` on Home explicitly lowers the suggested mission to the nearest lower 25 m increment (not below 100 m); the parent can also reset the target in Settings.
8. **Stop goal (added in 1.3).** Once any run has its stops counted, the mission has a second half shown under the distance, built on the latest counted day (its longest counted run): one fewer stop, never below 0 ("non-stop"). It holds at the same number after a `Hard` run, and while the stops are already within the parent's limit (the distance does the growing then), except at the longest mission setting, where it asks for one fewer again. A worse day relaxes the goal instead of piling on. Runs shorter than half their mission and runs entered for an earlier date (worked out from the order the logs were created) never move the goal. The goal is derived from the logs, not stored; each new log stores the goal shown at the time as `stopsGoalAtLog` (none for a backdated log). **The distance only grows after a run with few stops:** at most `lowStopsLimit` (Settings "Distance goes up after", 0 to 5, default 2). Any rise from beating the mission needs a counted run within the limit, and rule 4 becomes: two days that matched the mission with low stops, `Easy` or `Okay`, step it up one band. A run with more stops, or none counted, repeats the mission instead. Rule 1 (Hard eases off) is unaffected. Run saved explains a held mission and how far off it is: "With 3 stops the distance stays the same. 1 fewer stop and it gets longer."

Store `targetMAtLog` on each session as the suggestion displayed when the entry was created. It is used for `met target` badges. Store only `currentTargetM` in settings, not a full target decision/history table. On editing/deleting runs, recompute basic counters and badges and set next suggested target to a safe repeat of the most recent remaining log's `targetMAtLog` (or onboarding starting distance if no logs); this deliberately simple recovery behavior is fine. *Since 1.3* this reset only happens when the edit changed the run's date, distance or effort. Correcting the latest run's stops across the limit replays the rules for that run instead; adding a stop count, a note or tags otherwise leaves the mission alone. Target progression only considers **new logs on or after the latest existing outing date**; backdated entries affect history/badges/weekly quests but must not automatically raise the current target.

Safety copy: `Start gently. Walk whenever you need. Stop and tell a trusted adult if you feel pain, dizziness or unwell.` Do not set fixed pace or one-year distance targets; one-week or more rest breaks are okay.

## 5. XP and levels: no economy

The app needs one understandable progression meter but **no coins, purchases, unlockable items, reward accounting or event ledger**.

- **XP = 50 x number of distinct calendar dates containing at least one valid logged outing.** This is derived from Room data every time.
- **Level = 1 + floor(XP / 250)**, or equivalently 1 + floor(outingDays / 5).
- Each level covers five outing days. Next level bar = `(XP % 250)/250`.
- Names can be fixed: L1 `First Steps`, L2 `Rookie`, L3 `Trail Finder`, L4 `Steady Strider`, L5 `Path Maker`, L6 `Explorer`, L7 `Adventure Ace`, L8+ `Trail Hero`. Titles are descriptive only.
- Completing a weekly quest, unlocking a badge, finishing a chapter or opening the app never earns additional XP: these show a celebration/checkmark only. This intentionally avoids duplicate-award rules.
- If a historical log is corrected or deleted, totals and XP update from facts. No negative balance, wallet or transaction model is necessary.
- The summary animation uses `+50 XP` **only when a new qualifying outing day is added**. A second entry on the same day shows `Distance added` rather than `+50 XP`.

## 6. Weekly quests: two checkboxes, one content calendar

Define an ISO week as **Monday to Sunday**, using the recorded local dates. `weeklyGoal` is 1, 2 or 3 distinct outing dates. `Main quest = count unique outing days in this ISO week >= weeklyGoal`.

Every ISO week has an authored title from Appendix B and exactly one *optional* side quest from this finite set of simple rules. Side quests are never required for levels or story progression; only satisfying them creates a second checkmark.

| Code | Side quest description | Evaluation within the active ISO week |
|---|---|---|
| `ANY_RUN` | Enjoy any outing | Any qualifying outing day |
| `EASY` | Enjoy an easy-feeling outing | At least one log with effort `EASY` |
| `WALK` | Try a walking adventure | At least one log with mode `WALK_ONLY` |
| `NOTE` | Save a tiny memory | At least one log with a non-empty note |
| `PARK` | Visit a park | At least one log tagged `PARK` |
| `FAMILY` | Go with family | At least one log tagged `FAMILY` |
| `NEW_ROUTE` | Try a new safe path | At least one log tagged `NEW_ROUTE` |
| `NATURE` | Notice nature | At least one log tagged `NATURE` |

The 52-row table below includes the fixed side quest; no random quest generation, quest instances, quest state table, expiry penalties, separate objective engine, quest claim button or repeat-roll logic. For all 52 weeks, there is always the achievable main quest and the optional side quest may be skipped. Prior weeks are evaluated from their recorded dates. Week title index = 1 + number of ISO week starts elapsed since the ISO week of onboarding, with week 53+ rotating the **theme** using modulo 52; use the absolute ISO week identity to evaluate quest completion and maintain history. The first week may be partial, which is okay.

`Total quests completed` for achievement purposes = sum, over the *journey weeks so far*, of 1 for each completed main quest and 1 for each completed side quest. Never count the same week/task twice, even when opened repeatedly. Changing the weekly goal uses the current goal for all weeks as a simple deterministic policy; in settings explain that historical weekly-goal badge counts may change. An alternative snapshot per week is outside the v1 scope.

## 7. Eight worlds / twenty-four chapters, two seasons (text-based)

Chapters are a linear story and require **only the number of distinct outing days**, never coins, purchases, gear, collectible characters, location tracking or specific run distances. Finishing a chapter reveals the next title, one short congratulatory sentence and the related badge. No choice trees or saved chapter-state rows.

Story milestones below are default for a 2+ outing/week initial plan. If the initial plan was 1/week, use `ceil(milestone / 2)` for each chapter throughout that journey; preserve this initial pacing even if the current weekly goal later changes. A child who does fewer outings still sees fresh weekly themes without being penalized. *Revised in 1.3:* after chapter 12 a second season of four worlds opens (below). After chapter 24, display `Legend of the Trail` and keep logging as usual.

| No. | World | Chapter | Distinct run days needed | Simple reveal text |
|---:|---|---|---:|---|
| 1 | Rookie Woods | The First Footprint | 4 | The adventure has begun. |
| 2 | Rookie Woods | The Hidden Path | 8 | A new trail appears. |
| 3 | Rookie Woods | The Forest Gate | 12 | The forest gate opens. |
| 4 | Desert Dash | The Sandy Start | 18 | The desert is yours to explore. |
| 5 | Desert Dash | Canyon of Echoes | 24 | Your footsteps echo through the canyon. |
| 6 | Desert Dash | The Sunset Beacon | 32 | The beacon lights up. |
| 7 | Sky Kingdom | Clouds Below | 40 | You made it into the clouds. |
| 8 | Sky Kingdom | The Floating Bridge | 50 | The bridge is ready to cross. |
| 9 | Sky Kingdom | The Star Tower | 60 | The tower shines brightly. |
| 10 | Planet Zoom | Landing Day | 72 | Welcome to a brand-new planet. |
| 11 | Planet Zoom | The Rainbow Rings | 84 | The rings come alive with colour. |
| 12 | Planet Zoom | The Great ruuun! Festival | 100 | A big celebration of every step. |
| 13 | Coral Coast | The Tide Pools | 112 | The sea has found you. |
| 14 | Coral Coast | Shell Beach | 124 | Every shell hums a little song of the sea. |
| 15 | Coral Coast | The Lighthouse | 136 | The lighthouse lamp is shining. |
| 16 | Jungle Run | Vines and Rivers | 150 | You are deep in the jungle now. |
| 17 | Jungle Run | The Waterfall Steps | 165 | You climbed the waterfall steps. |
| 18 | Jungle Run | The Hidden Temple | 180 | The temple doors swing open. |
| 19 | Frosty Peaks | Snowy Switchbacks | 200 | Your footprints zigzag up the mountain. |
| 20 | Frosty Peaks | The Ice Cave | 220 | The ice cave glows blue. |
| 21 | Frosty Peaks | Summit Lights | 240 | The sky lights up over the summit. |
| 22 | Comet Run | The Launch Pad | 265 | Lift off! |
| 23 | Comet Run | Riding the Comet | 290 | You are riding a comet's tail. |
| 24 | Comet Run | The Grand Finish | 320 | Every world, every run: you did it. |

**Year-one finish:** On/after the 52nd weekly theme, show a summary card with personal total outing days, distance, chapters, and badges. This is a **computed view**, not a saved annual event, downloadable poster or extra app subsystem. Never reset progress at one year. Chapters can also complete earlier or later than the calendar.

## 8. Implementation architecture (small and native)

Recommended one-module stack: **Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room and coroutines/Flow**. For maximum simplicity, use **one Room database with only two entities:** `RunnerSettings` (exactly one row) and `RunLog` (unbounded rows). The chapter table, weekly calendar and achievements are bundled as Kotlin constant lists or JSON resources, not database tables. No ViewModel framework beyond normal lifecycle ViewModels, no DI framework requirement, no remote libraries, no background scheduler.

```text
app/src/main/
  java/.../ruuun/
    MainActivity.kt
    ui/        Home, LogRun, Missions, Badges, History, Settings, Theme, Nav
    data/      RuuunDatabase, RunnerSettingsEntity, RunLogEntity, Dao
    logic/     StatsCalculator, TargetRecommender, BadgeEvaluator, WeekCalculator
    content/   Chapters, Achievements, Weeks
    backup/    ExportImport
  res/drawable/  approved local logo, a few reusable icons
```

### 8.1 Only two persisted entities

| Record | Required fields | Notes |
|---|---|---|
| `RunnerSettings` | `id=1`, `nickname`, `onboardedDate`, `startingTargetM`, `currentTargetM`, `maxTargetM`, `weeklyGoal`, `initialStoryPaceOnePerWeek`, `lowStopsLimit` (1.3, default 2) | Just one row; values edited via settings; may include `onboardingComplete` boolean |
| `RunLog` | `id` UUID, `date` local `YYYY-MM-DD`, `distanceM` positive integer, `effort` enum, `targetMAtLog`, `durationMinutes?`, `mode?`, `tags` list, `note?`, `stops?` (1.3), `stopsGoalAtLog?` (1.3), `createdAt`, `updatedAt` | All run facts; tags stored as a small JSON string or converter |

**All else is derived**: number of unique run days, distance totals, XP/level, week goal checkmarks, completed side quests, completed chapters, badge eligibility, personal records. No inventory tables, currency tables, purchase/equip tables, award history, reward source IDs, target-history table, separate quest instances or chapter completion table. For badges, if the criterion is true, it is earned; otherwise it is not.

Use integer metres, not floating-point storage. Display `300 m`, `950 m`, `1.2 km`. Accept decimal km and convert accurately. Optional duration is whole minutes; no timing/speed metrics. `RunLog` dates are **local calendar dates**, not UTC day calculations; handle ISO week-year rollovers. Persist at least one valid log for the same date; do not reject legitimate same-day extras. Use safe edit/delete and recompute UI from Room `Flow`.

### 8.2 Form validation and failure cases

- Distance must be >0; suggest 1 to 50,000 m as data-entry range, warn for implausible values >10,000 m, but permit corrections. Invalid numeric text cannot save.
- No future session date; past session dates okay, including dates before onboarding (may affect monthly totals, not weekly themes before journey start).
- Duration blank or positive whole number; notes <=140 chars; tags restricted to four known values.
- A `Hard` entry must **never** cause an upward next-target change.
- Treat empty/no-run state, app restart, rotation, large Android fonts, airplane mode, and records spanning years correctly.
- Use simple Material snackbars/confirm dialogs instead of a complicated event-bus or toast queue.

### 8.3 Manual JSON backup: required for a year's data

Use Android `ACTION_CREATE_DOCUMENT` to export and `ACTION_OPEN_DOCUMENT` to import a UTF-8 JSON file such as `ruuun-backup-2026-11-01.json`. The envelope should contain `format: "ruuun-offline-backup"`, `schemaVersion: 1`, `exportedAt`, settings and all run logs. No badge, weekly quest, XP or chapter data need exporting because they can be recomputed.

On import: parse file; validate format/schema, uniqueness of run IDs, enums, numeric ranges and required settings; display a concise preview (nickname and log count); request explicit confirmation; **replace settings and all logs atomically in a Room transaction**. On failure, preserve old data. No cloud sync, storage permission, encryptor, checksum ledger, automatic scheduling or backup account needed. Note that exports are readable JSON files and the device owner must store them safely.

## 9. Achievement engine design

The Appendix A seed list is the canonical **141-badge** catalog. Give each definition `id`, `title`, `category`, `ruleType`, `threshold`, `hint`, and optionally `tag`/`month`. Reuse one icon per category; there is no per-badge asset production requirement. Calculate an in-memory `StatsSnapshot` from current logs; test pure functions (no Android context). Then evaluate definitions against the snapshot and show an `Earned` or `Locked` state. On a new log, compare before/after eligibility sets to show newly reached badges; a second same-day run may earn a distance badge but not run-day XP.

**Allowed rule families** (these are simple counters computed from local data): unique outing days, max single outing metres, total metres, weeks with weekly goal met, most days in any week, distinct active months, one of four tag counts, count by optional mode, count by effort, count of noted logs, outing days with met suggestion, short outings, comeback gaps, month-of-year activity, chapter threshold, completed weekly quest count, and a few `ALL_OF` combinations for secret badges. No rule depends on spending coins, tapping UI controls, historical wardrobe choices, internet or external sensors.

**Data correction policy:** No permanent award table. All badges reflect the latest corrected data. On save, `newBadges = afterBadgeIds - beforeBadgeIds`. If changing a log removes eligibility, the badge becomes locked; warn when editing/deleting that progress may change. No additional XP comes from badges.

**Badge progress:** For numeric badges show `current / threshold`; for tag/mode badges show `0/1` or `N/M`; chapters use `run days / target`; seasonal badges show earned/locked. Secret badge details remain hidden until earned, with no misleading countdown. At most 3 new badges in the result sheet, plus `+N more`.

## 10. QA and test contract

**Must pass before considering the build finished:**

1. Brand-new install works in airplane mode, with zero logs and a 300 m next mission.
2. An outing can be manually logged in <30 seconds; its *actual* metres are stored, not the suggested metres.
3. First 250 m/OKAY outing yields one qualifying day, 250 m total, 50 XP, no `target met` badge.
4. Two Easy outings meeting a 300 m target on separate days can increase the next mission to 325 m, never to 350 m.
5. `Hard` effort leads to an equal/easier next mission and no punishments.
6. Three logs on the same date sum distance, but count as one outing day, one +50 XP event and at most one chapter/run-count increment.
7. One run on two different ISO weeks is not incorrectly counted as two runs in the same week.
8. 52 authored titles exist; week 53 returns to week-1 *theme* without resetting logs, chapters or badges.
9. All 12 chapters can be reached by their run-day thresholds. Initial one/week story pace changes required run-day counts, but later weekly goal edits do not.
10. Every Appendix A badge evaluates from data (test thresholds and categories); none requires removed features or inaccessible sensors.
11. No badge or XP award can be farmed by opening screens or force closing/reopening.
12. Editing/deleting a log updates metres, run count, weekly quests, XP, level, chapters and badges immediately and consistently.
13. Backdated date, leap day and December/January ISO week changes are handled without time-zone off-by-one errors.
14. Empty notes/tags, future dates, negative and zero distance, malformed numeric input cannot crash the app.
15. JSON export/import roundtrip restores settings and logs exactly with no network; malformed file fails without deleting current progress.
16. 100-run and 150-run simulations work without slow scrolling, missing badge resources or broken content.
17. TalkBack labels, large fonts, touch targets at least 48 dp and reduce-motion system preference are respected.
18. No location/activity permissions (internet only for the 1.1 update check), Firebase config, coin/cosmetic/shop models or runtime network fetches in the final APK.

## 11. Practical build order and deliverables

**Step 1 - core:** Android project, Material 3 theme, first-run setup, Room entities/DAO, Home, Add Run, History with edit/delete. Test fully offline.

**Step 2 - motivation:** target suggestion, computed XP/level, weekly goal, simple save confirmation. Write unit tests for the progression rules.

**Step 3 - year of content:** bundle the 52-week calendar, 12 chapters and 141 achievement definitions; render Journey and Badges. Do not draw 141 original illustrations.

**Step 4 - durability:** settings and JSON export/import; add automated tests for data corrections, leap/ISO boundaries and import validation.

**Step 5 - polish and shipment:** final approved logo where supplied, accessible layouts, a few lightweight animations, verify no network dependency and provide installable debug APK.

**Required output from coding agent:** complete Android Studio source project; `README.md` with compatible JDK/Gradle/SDK versions, build/install steps and description of the offline data model; debug APK; tests for stats/target/achievement/week logic; short list of any unfinished items. **A working simple app is more important than an elaborate UI prototype.**

---

# Appendix A - Achievement seed catalog (141 badges)

Every row is a working requirement, not a decorative placeholder. IDs are stable; preserving prior IDs is deliberate. The `Rule` column describes values to encode as typed rules in Kotlin; **do not parse natural-language descriptions at runtime**. Some thresholds are long-term stretch milestones and need not be earned during year one. Do not use pace, speed, weight or forced consecutive-day running as badge criteria.

| ID | Achievement | Rule | Friendly hint |
|---|---|---|---|
| `FS-001` | Liftoff! | `RUN_DAYS >= 1` | Record your first adventure. |
| `FS-002` | Try Again | `RUN_DAYS >= 2` | Come back for another outing. |
| `FS-003` | Your First Footprint | `MAX_SESSION_M >= 100` | Every journey begins small. |
| `FS-004` | Walk On | `MODE_WALK_ONLY >= 1` | A walking outing is a win. |
| `FS-005` | Mix It Up | `MODE_WALK_JOG >= 1` | Try walking and jogging together. |
| `FS-006` | All Smiles | `EFFORT_EASY >= 1` | An outing that felt comfortable. |
| `FS-007` | Checked In | `EFFORT_OKAY >= 1` | Notice how you feel. |
| `FS-008` | Brave Reporter | `EFFORT_HARD >= 1` | It is good to tell the truth about difficulty. |
| `FS-009` | My Story | `NOTED_LOGS >= 1` | Write down a memory. |
| `FS-010` | First Quest | `QUEST_COMPLETIONS >= 1` | You did a side mission. |
| `RC-001` | Third Time Lucky | ``RUN_DAYS >= 3`` | Complete 3 separate qualifying outing days. |
| `RC-002` | Fourward! | ``RUN_DAYS >= 4`` | Complete 4 separate qualifying outing days. |
| `RC-003` | High Five | ``RUN_DAYS >= 5`` | Complete 5 separate qualifying outing days. |
| `RC-004` | Six Steps Ahead | ``RUN_DAYS >= 6`` | Complete 6 separate qualifying outing days. |
| `RC-005` | Lucky Seven | ``RUN_DAYS >= 7`` | Complete 7 separate qualifying outing days. |
| `RC-006` | Great Eight | ``RUN_DAYS >= 8`` | Complete 8 separate qualifying outing days. |
| `RC-007` | Double Digits | ``RUN_DAYS >= 10`` | Complete 10 separate qualifying outing days. |
| `RC-008` | The Dozen | ``RUN_DAYS >= 12`` | Complete 12 separate qualifying outing days. |
| `RC-009` | Fifteen Adventures | ``RUN_DAYS >= 15`` | Complete 15 separate qualifying outing days. |
| `RC-010` | Running Rookie | ``RUN_DAYS >= 20`` | Complete 20 separate qualifying outing days. |
| `RC-011` | Quarter Century | ``RUN_DAYS >= 25`` | Complete 25 separate qualifying outing days. |
| `RC-012` | Thirty & Thriving | ``RUN_DAYS >= 30`` | Complete 30 separate qualifying outing days. |
| `RC-013` | Forty Footprints | ``RUN_DAYS >= 40`` | Complete 40 separate qualifying outing days. |
| `RC-014` | Half-Century Hero | ``RUN_DAYS >= 50`` | Complete 50 separate qualifying outing days. |
| `RC-015` | Super Sixty | ``RUN_DAYS >= 60`` | Complete 60 separate qualifying outing days. |
| `RC-016` | Seventy-Five Strong | ``RUN_DAYS >= 75`` | Complete 75 separate qualifying outing days. |
| `RC-017` | Ninety Ninjas | ``RUN_DAYS >= 90`` | Complete 90 separate qualifying outing days. |
| `RC-018` | Century Club | ``RUN_DAYS >= 100`` | Complete 100 separate qualifying outing days. |
| `RC-019` | The Long Game | ``RUN_DAYS >= 125`` | Complete 125 separate qualifying outing days. |
| `RC-020` | Legend of the Trail | ``RUN_DAYS >= 150`` | Complete 150 separate qualifying outing days. |
| `SD-001` | Pocket Rocket | ``MAX_SESSION_M >= 200`` | Record at least 200 m in one outing. |
| `SD-002` | Tiny but Mighty | ``MAX_SESSION_M >= 250`` | Record at least 250 m in one outing. |
| `SD-003` | Three Hundred Hero | ``MAX_SESSION_M >= 300`` | Record at least 300 m in one outing. |
| `SD-004` | Four Hundred Feet-First | ``MAX_SESSION_M >= 400`` | Record at least 400 m in one outing. |
| `SD-005` | Half-K Hero | ``MAX_SESSION_M >= 500`` | Record at least 500 m in one outing. |
| `SD-006` | Six Hundred Superstar | ``MAX_SESSION_M >= 600`` | Record at least 600 m in one outing. |
| `SD-007` | Three Quarter Quest | ``MAX_SESSION_M >= 750`` | Record at least 750 m in one outing. |
| `SD-008` | Eight Hundred Explorer | ``MAX_SESSION_M >= 800`` | Record at least 800 m in one outing. |
| `SD-009` | Kilometre Kid | ``MAX_SESSION_M >= 1000`` | Record at least 1 km in one outing. |
| `SD-010` | A Little Beyond | ``MAX_SESSION_M >= 1200`` | Record at least 1.2 km in one outing. |
| `SD-011` | Rocket Runner | ``MAX_SESSION_M >= 1500`` | Record at least 1.5 km in one outing. |
| `SD-012` | Longer Trail | ``MAX_SESSION_M >= 1750`` | Record at least 1.75 km in one outing. |
| `SD-013` | Two-K Triumph | ``MAX_SESSION_M >= 2000`` | Record at least 2 km in one outing. |
| `SD-014` | Two and a Half | ``MAX_SESSION_M >= 2500`` | Record at least 2.5 km in one outing. |
| `SD-015` | Three-K Trail | ``MAX_SESSION_M >= 3000`` | Record at least 3 km in one outing. |
| `SD-016` | Fantastic Four-K | ``MAX_SESSION_M >= 4000`` | Record at least 4 km in one outing. |
| `SD-017` | Fantastic Five | ``MAX_SESSION_M >= 5000`` | Record at least 5 km in one outing. |
| `TD-001` | First Half Kilometre | ``TOTAL_M >= 500`` | Accumulate 0.5 km across all logged outings. |
| `TD-002` | One Kilometre Together | ``TOTAL_M >= 1000`` | Accumulate 1 km across all logged outings. |
| `TD-003` | Two Kilometres Travelled | ``TOTAL_M >= 2000`` | Accumulate 2 km across all logged outings. |
| `TD-004` | Three Kilometres Travelled | ``TOTAL_M >= 3000`` | Accumulate 3 km across all logged outings. |
| `TD-005` | Little Explorer | ``TOTAL_M >= 5000`` | Accumulate 5 km across all logged outings. |
| `TD-006` | Trail Collector | ``TOTAL_M >= 7500`` | Accumulate 7.5 km across all logged outings. |
| `TD-007` | World Wanderer | ``TOTAL_M >= 10000`` | Accumulate 10 km across all logged outings. |
| `TD-008` | Fifteen-K Club | ``TOTAL_M >= 15000`` | Accumulate 15 km across all logged outings. |
| `TD-009` | Long Way Round | ``TOTAL_M >= 20000`` | Accumulate 20 km across all logged outings. |
| `TD-010` | Quarter to a Hundred | ``TOTAL_M >= 25000`` | Accumulate 25 km across all logged outings. |
| `TD-011` | Thirty-K Trek | ``TOTAL_M >= 30000`` | Accumulate 30 km across all logged outings. |
| `TD-012` | Forty-K Trek | ``TOTAL_M >= 40000`` | Accumulate 40 km across all logged outings. |
| `TD-013` | Halfway to a Hundred | ``TOTAL_M >= 50000`` | Accumulate 50 km across all logged outings. |
| `TD-014` | Seventy-Five K | ``TOTAL_M >= 75000`` | Accumulate 75 km across all logged outings. |
| `TD-015` | 100-K Adventurer | ``TOTAL_M >= 100000`` | Accumulate 100 km across all logged outings. |
| `TD-016` | Super Long Journey | ``TOTAL_M >= 150000`` | Accumulate 150 km across all logged outings. |
| `WK-001` | Weekly Warrior | `WEEKS_MET >= 1` | Meet your own weekly target in your first week. |
| `WK-002` | Double Weekly | `WEEKS_MET >= 2` | Meet your own weekly target in 2 different weeks. |
| `WK-003` | Three Good Weeks | `WEEKS_MET >= 3` | Meet your own weekly target in 3 different weeks. |
| `WK-004` | Four Week Wonder | `WEEKS_MET >= 4` | Meet your own weekly target in 4 different weeks. |
| `WK-005` | Steady Five | `WEEKS_MET >= 5` | Meet your own weekly target in 5 different weeks. |
| `WK-006` | Six Weeks of Stories | `WEEKS_MET >= 6` | Meet your own weekly target in 6 different weeks. |
| `WK-007` | Eight Week Explorer | `WEEKS_MET >= 8` | Meet your own weekly target in 8 different weeks. |
| `WK-008` | Ten Week Hero | `WEEKS_MET >= 10` | Meet your own weekly target in 10 different weeks. |
| `WK-009` | Twelve Week Quest | `WEEKS_MET >= 12` | Meet your own weekly target in 12 different weeks. |
| `WK-010` | Sixteen Chapters of Effort | `WEEKS_MET >= 16` | Meet your own weekly target in 16 different weeks. |
| `WK-011` | Twenty Week Wanderer | `WEEKS_MET >= 20` | Meet your own weekly target in 20 different weeks. |
| `WK-012` | Half-Year Hero | `WEEKS_MET >= 26` | Meet your own weekly target in 26 different weeks. |
| `WK-013` | Three-Quarter Year | `WEEKS_MET >= 39` | Meet your own weekly target in 39 different weeks. |
| `WK-014` | Full-Year Footprints | `WEEKS_MET >= 52` | Meet your own weekly target in 52 different weeks. |
| `CO-001` | One More This Week | `MAX_DAYS_IN_ANY_WEEK >= 2` | Two outings in one calendar week. |
| `CO-002` | Hat Trick Week | `MAX_DAYS_IN_ANY_WEEK >= 3` | Three outings in one calendar week; optional. |
| `CO-003` | New Month, New Start | `ACTIVE_MONTHS >= 2` | Make memories in two months. |
| `CO-004` | Across the Seasons | `ACTIVE_MONTHS >= 4` | Explore across four months. |
| `CO-005` | A Whole Year of ruuun! | `ACTIVE_MONTHS >= 12` | Your story spans twelve months. |
| `AD-001` | Dynamic Duo | `TAG_FAMILY >= 1` | Share a walk/jog with family. |
| `AD-002` | Park Ranger | `TAG_PARK >= 1` | Head to a park. |
| `AD-003` | The Explorer | `TAG_NEW_ROUTE >= 1` | Try a new path. |
| `AD-004` | Nature Ninja | `TAG_NATURE >= 1` | Spot the outdoors. |
| `AT-001` | Route Discoverer | `TAG_NEW_ROUTE >= 3` | Try 3 new routes. |
| `AT-002` | Explorer Supreme | `TAG_NEW_ROUTE >= 5` | Try 5 new routes. |
| `AT-003` | Route Master | `TAG_NEW_ROUTE >= 8` | Try 8 new routes. |
| `RF-001` | Mood Keeper | `RUN_LOGS_WITH_EFFORT >= 3` | Check in with yourself three times. |
| `RF-002` | Feel-Good Five | `EFFORT_EASY >= 5` | Collect five comfortable outings. |
| `RF-003` | Well Done, Me | `NOTED_LOGS >= 5` | Save five little memories. |
| `RF-004` | Notebook Explorer | `NOTED_LOGS >= 15` | Your story is growing. |
| `RF-005` | Small But Real | `BELOW_TARGET_LOGS >= 1` | Showing up counts, even below target. |
| `RF-007` | Comeback Kid | `COMEBACK_GAPS_14D >= 1` | Welcome back after time away. |
| `RF-008` | Back Again Again | `COMEBACK_GAPS_14D >= 2` | Returning is something to celebrate. |
| `RF-009` | First Goal Met | `MET_TARGET_LOGS >= 1` | You reached your own mission target. |
| `RF-010` | Five Goals Met | `MET_TARGET_LOGS >= 5` | Meet five personal targets. |
| `RF-011` | Ten Goals Met | `MET_TARGET_LOGS >= 10` | Your goals are adding up. |
| `RF-012` | Slow and Steady | `MODE_WALK_ONLY >= 5` | Walks are part of the adventure. |
| `RF-013` | Mixed Adventure | `MODE_WALK_JOG >= 5` | Use a walk-and-jog mix. |
| `RF-014` | Happy Hundred | `LOGS_AT_LEAST_100M >= 10` | Ten outings with little victories. |
| `CH-001` | Chapter 1: The First Footprint | `CHAPTER_COMPLETED >= 1` | Finish story chapter 1. |
| `CH-002` | Chapter 2: The Hidden Path | `CHAPTER_COMPLETED >= 2` | Finish story chapter 2. |
| `CH-003` | Chapter 3: The Forest Gate | `CHAPTER_COMPLETED >= 3` | Finish story chapter 3. |
| `CH-004` | Chapter 4: The Sandy Start | `CHAPTER_COMPLETED >= 4` | Finish story chapter 4. |
| `CH-005` | Chapter 5: Canyon of Echoes | `CHAPTER_COMPLETED >= 5` | Finish story chapter 5. |
| `CH-006` | Chapter 6: The Sunset Beacon | `CHAPTER_COMPLETED >= 6` | Finish story chapter 6. |
| `CH-007` | Chapter 7: Clouds Below | `CHAPTER_COMPLETED >= 7` | Finish story chapter 7. |
| `CH-008` | Chapter 8: The Floating Bridge | `CHAPTER_COMPLETED >= 8` | Finish story chapter 8. |
| `CH-009` | Chapter 9: The Star Tower | `CHAPTER_COMPLETED >= 9` | Finish story chapter 9. |
| `CH-010` | Chapter 10: Landing Day | `CHAPTER_COMPLETED >= 10` | Finish story chapter 10. |
| `CH-011` | Chapter 11: The Rainbow Rings | `CHAPTER_COMPLETED >= 11` | Finish story chapter 11. |
| `CH-012` | Chapter 12: The Great ruuun! Festival | `CHAPTER_COMPLETED >= 12` | Finish story chapter 12. |
| `QS-001` | Quest Starter | `QUEST_COMPLETIONS >= 1` | Complete your first weekly quest. |
| `QS-002` | Quest Apprentice | `QUEST_COMPLETIONS >= 5` | Complete five weekly quests. |
| `QS-003` | Side Quest Hero | `QUEST_COMPLETIONS >= 10` | Complete 10 weekly quests. |
| `QS-004` | Quest Collector | `QUEST_COMPLETIONS >= 20` | Complete 20 weekly quests. |
| `QS-005` | Quest Veteran | `QUEST_COMPLETIONS >= 35` | Complete 35 weekly quests. |
| `QS-006` | Quest Master | `QUEST_COMPLETIONS >= 52` | Complete 52 weekly quests. |
| `QS-007` | Trail Task Expert | `QUEST_COMPLETIONS >= 75` | Complete 75 weekly quests. |
| `QS-008` | Quest Legend | `QUEST_COMPLETIONS >= 100` | Complete 100 weekly quests. |
| `MO-001` | Fresh Start | `RUNS_IN_MONTH_01 >= 1` | Collect the January seasonal badge. |
| `MO-002` | Heart of Adventure | `RUNS_IN_MONTH_02 >= 1` | Collect the February seasonal badge. |
| `MO-003` | Spring Steps | `RUNS_IN_MONTH_03 >= 1` | Collect the March seasonal badge. |
| `MO-004` | April Explorer | `RUNS_IN_MONTH_04 >= 1` | Collect the April seasonal badge. |
| `MO-005` | May Momentum | `RUNS_IN_MONTH_05 >= 1` | Collect the May seasonal badge. |
| `MO-006` | Sunny Trails | `RUNS_IN_MONTH_06 >= 1` | Collect the June seasonal badge. |
| `MO-007` | Summer Scout | `RUNS_IN_MONTH_07 >= 1` | Collect the July seasonal badge. |
| `MO-008` | August Adventure | `RUNS_IN_MONTH_08 >= 1` | Collect the August seasonal badge. |
| `MO-009` | Back to the Path | `RUNS_IN_MONTH_09 >= 1` | Collect the September seasonal badge. |
| `MO-010` | Spooky Steps | `RUNS_IN_MONTH_10 >= 1` | Collect the October seasonal badge. |
| `MO-011` | Autumn Tracks | `RUNS_IN_MONTH_11 >= 1` | Collect the November seasonal badge. |
| `MO-012` | Jingle Jogger | `RUNS_IN_MONTH_12 >= 1` | Collect the December seasonal badge. |
| `SE-001` | Hidden High Five | `MAX_NOTED_LOGS_IN_WEEK >= 2` | Two memories in one week. |
| `SE-002` | Family Trail | `TAG_FAMILY >= 3` | Three family adventures. |
| `SE-003` | Little Steps | `LOGS_AT_MOST_500M >= 5` | Little outings count. |
| `SE-004` | The Return | `COMEBACK_GAPS_30D >= 1` | No time limit on adventures. |
| `SE-005` | Park Explorer | `TAG_PARK >= 3` | Return to the park. |
| `SE-006` | Three Routes | `TAG_NEW_ROUTE >= 3` | Find three different paths. |
| `SE-007` | Every Effort Counts | `ALL_THREE_EFFORTS_RECORDED` | All feelings are okay. |

**Non-stop badges (added in 1.3).**

| ID | Name | Rule | Hint |
|---|---|---|---|
| `NS-001` | Stop Counter | `STOPS_COUNTED_DAYS >= 1` | Count your stops on a run. |
| `NS-002` | Keeping Count | `STOPS_COUNTED_DAYS >= 10` | Count your stops on 10 run days. |
| `NS-003` | One Fewer | `FEWER_STOPS_DAYS >= 1` | Stop fewer times than the run before. |
| `NS-004` | Fewer and Fewer | `FEWER_STOPS_DAYS >= 5` | Stop fewer times than the run before, 5 times. |
| `NS-005` | Smooth Strider | `FEWER_STOPS_DAYS >= 12` | Stop fewer times than the run before, 12 times. |
| `NS-006` | Stop Goal Star | `STOP_GOALS_MET >= 1` | Meet your stop goal. |
| `NS-007` | Goal Keeper | `STOP_GOALS_MET >= 5` | Meet your stop goal on 5 different days. |
| `NS-008` | Stop Goal Pro | `STOP_GOALS_MET >= 15` | Meet your stop goal on 15 different days. |
| `NS-009` | Non-stop! | `NONSTOP_RUNS >= 1` | Go at least 100 m without stopping. |
| `NS-010` | Non-stop Habit | `NONSTOP_RUNS >= 10` | Go the whole way without stopping on 10 different days. |
| `NS-011` | Non-stop 500 | `NONSTOP_MAX_M >= 500` | Go 500 m without stopping. |
| `NS-012` | Non-stop Kilometre | `NONSTOP_MAX_M >= 1000` | Go 1 km without stopping. |
| `NS-013` | Non-stop Two-K | `NONSTOP_MAX_M >= 2000` | Go 2 km without stopping. |
| `NS-014` | Non-stop Three-K | `NONSTOP_MAX_M >= 3000` | Go 3 km without stopping. |
| `NS-015` | Non-stop Five-K | `NONSTOP_MAX_M >= 5000` | Go 5 km without stopping. |
| `NS-016` | Non-stop One and a Half | `NONSTOP_MAX_M >= 1500` | Go 1.5 km without stopping. |
| `NS-017` | Non-stop Four-K | `NONSTOP_MAX_M >= 4000` | Go 4 km without stopping. |
| `NS-018` | Stop Goal Legend | `STOP_GOALS_MET >= 30` | Meet your stop goal on 30 different days. |
| `NS-019` | Non-stop Hero | `NONSTOP_RUNS >= 25` | Go the whole way without stopping on 25 different days. |

`STOPS_COUNTED_DAYS` counts days with at least one counted run; `FEWER_STOPS_DAYS` compares each counted day (its longest counted run) with the counted day before; `STOP_GOALS_MET` counts **days** with a log at least half its mission whose `stops <= stopsGoalAtLog`; `NONSTOP_RUNS` counts **days** with a log of at least 100 m and `stops = 0`, and `NONSTOP_MAX_M` is the longest such log. Counting days, not logs, keeps extra laps from farming them.

**Long tail and season 2 (added in 1.3).** Badges are recomputed from the logs, so an existing badge's threshold must never be raised: a child who earned it would see it lock again. New tiers always get new ids.

| ID | Name | Rule | Hint |
|---|---|---|---|
| `CH-013` | Chapter 13: The Tide Pools | `CHAPTER_COMPLETED >= 13` | Finish chapter 13 of the story. |
| `CH-014` | Chapter 14: Shell Beach | `CHAPTER_COMPLETED >= 14` | Finish chapter 14 of the story. |
| `CH-015` | Chapter 15: The Lighthouse | `CHAPTER_COMPLETED >= 15` | Finish chapter 15 of the story. |
| `CH-016` | Chapter 16: Vines and Rivers | `CHAPTER_COMPLETED >= 16` | Finish chapter 16 of the story. |
| `CH-017` | Chapter 17: The Waterfall Steps | `CHAPTER_COMPLETED >= 17` | Finish chapter 17 of the story. |
| `CH-018` | Chapter 18: The Hidden Temple | `CHAPTER_COMPLETED >= 18` | Finish chapter 18 of the story. |
| `CH-019` | Chapter 19: Snowy Switchbacks | `CHAPTER_COMPLETED >= 19` | Finish chapter 19 of the story. |
| `CH-020` | Chapter 20: The Ice Cave | `CHAPTER_COMPLETED >= 20` | Finish chapter 20 of the story. |
| `CH-021` | Chapter 21: Summit Lights | `CHAPTER_COMPLETED >= 21` | Finish chapter 21 of the story. |
| `CH-022` | Chapter 22: The Launch Pad | `CHAPTER_COMPLETED >= 22` | Finish chapter 22 of the story. |
| `CH-023` | Chapter 23: Riding the Comet | `CHAPTER_COMPLETED >= 23` | Finish chapter 23 of the story. |
| `CH-024` | Chapter 24: The Grand Finish | `CHAPTER_COMPLETED >= 24` | Finish chapter 24 of the story. |
| `RC-023` | Still Going Strong | `RUN_DAYS >= 175` | Run on 175 different days. |
| `RC-024` | Double Century | `RUN_DAYS >= 200` | Run on 200 different days. |
| `RC-025` | Two-Fifty Trailblazer | `RUN_DAYS >= 250` | Run on 250 different days. |
| `TD-019` | Two Hundred K | `TOTAL_M >= 200000` | Clock up 200 km in total. |
| `TD-020` | Quarter of a Thousand K | `TOTAL_M >= 250000` | Clock up 250 km in total. |
| `WK-016` | Sixty-Five Good Weeks | `WEEKS_MET >= 65` | Hit your weekly goal in 65 different weeks. |
| `WK-017` | Year and a Half of Weeks | `WEEKS_MET >= 78` | Hit your weekly goal in 78 different weeks. |
| `QS-009` | Quest Champion | `QUEST_COMPLETIONS >= 150` | Tick off 150 weekly goals or side quests. |
| `RC-021` | Three Hundred Club | `RUN_DAYS >= 300` | Run on 300 different days. |
| `RC-022` | A Run for Every Day of the Year | `RUN_DAYS >= 365` | Run on 365 different days. |
| `TD-017` | Three Hundred K Explorer | `TOTAL_M >= 300000` | Clock up 300 km in total. |
| `TD-018` | Half a Thousand K | `TOTAL_M >= 500000` | Clock up 500 km in total. |
| `WK-015` | Two Years of Weekly Goals | `WEEKS_MET >= 104` | Hit your weekly goal in 104 different weeks. |
| `CO-006` | One Year On | `JOURNEY_DAYS >= 365` | Still running a whole year after your first run. |
| `CO-007` | Two Years On | `JOURNEY_DAYS >= 730` | Still running two years after your first run. |

`JOURNEY_DAYS` is the number of days from the first run date to the latest. The catalogue is 193 badges. Run saved also shows personal bests (longest run, longest non-stop run) from the second run on; they are not badges.


**Rule semantics and implementation details:** `RUN_DAYS` is distinct local calendar days containing valid logs. `MAX_SESSION_M` and `TOTAL_M` use actual recorded metres, including extra same-day sessions. `WEEKS_MET` uses ISO weeks on/after journey start and current weekly goal. `ACTIVE_MONTHS` is distinct year-months, not months-of-year. `MAX_DAYS_IN_ANY_WEEK` counts unique days in an ISO week. `MET_TARGET_LOGS` compares each log with its saved `targetMAtLog`; `BELOW_TARGET_LOGS` uses the inverse; `LOGS_AT_LEAST_100M` and `LOGS_AT_MOST_500M` count individual sessions; `RUN_LOGS_WITH_EFFORT` counts logs with an effort value (required, so this equals total valid logs). `COMEBACK_GAPS_14D` and `_30D` count gaps between **consecutive distinct outing days** at least that many calendar days apart, counting the return outing once per gap. `RUNS_IN_MONTH_MM` is at least one run on that calendar month in any year. `CHAPTER_COMPLETED` is the count of completed chapter thresholds adjusted for initial story pace. `QUEST_COMPLETIONS` counts complete weekly main + side quests, with an absolute week identity.

**Special secret rules:** `MAX_NOTED_LOGS_IN_WEEK` counts non-empty notes within any ISO week; `ALL_THREE_EFFORTS_RECORDED` means at least one log each of Easy, Okay and Hard. Secret badges should not pressure the child to record a Hard outing; treat it as acknowledging honest self-reporting, not as a task to pursue. All non-secret badges show their real requirement.

---

# Appendix B - All 52 weekly themes and optional side quests

These are **content**, not independent state machines or 52 unique reward definitions. Weekly main quest is always the child-selected weekly outing count; the optional side quest uses one of the eight simple rules from section 6. Same weekly content rotates from week 53 onward without resetting historic logs.

| Week | Theme | One-sentence flavour | Side quest |
|---:|---|---|---|
| 1 | Launch Week | First steps | `ANY_RUN` |
| 2 | Back for More | Comfort over distance | `NOTE` |
| 3 | Tiny Victories | Small steps are real | `EASY` |
| 4 | Finding Your Feet | Repeat with confidence | `PARK` |
| 5 | Explore the Park | A new view | `ANY_RUN` |
| 6 | Forest Friends | Buddy week | `FAMILY` |
| 7 | The Hidden Path | Try something different | `NEW_ROUTE` |
| 8 | The Forest Gate | World finale preview | `NATURE` |
| 9 | Footprints and Stories | Build a journal | `WALK` |
| 10 | Canyon Calling | Fresh scenery | `NOTE` |
| 11 | The Sunny Shortcut | Find your rhythm | `EASY` |
| 12 | Calm Counts | Rest and return | `ANY_RUN` |
| 13 | Pace Is Yours | No race necessary | `PARK` |
| 14 | Gecko Week | Meet the new friend | `ANY_RUN` |
| 15 | Little Longer, If Ready | Flexible target | `NOTE` |
| 16 | Repeat Champion | Repeating helps | `EASY` |
| 17 | Find the Beacon | Look ahead | `PARK` |
| 18 | Canyon Stories | Notice the world | `ANY_RUN` |
| 19 | Halfway to Sky | New chapter energy | `FAMILY` |
| 20 | Something Different | Switch the scene | `NEW_ROUTE` |
| 21 | Slow-Down Superpower | Choose kindness | `NATURE` |
| 22 | Sidekick Week | Family matters | `WALK` |
| 23 | Floating Thoughts | Keep memories | `NOTE` |
| 24 | Cloud Explorer | Gentle exploration | `EASY` |
| 25 | Six Months of Adventure | Half-year celebration | `ANY_RUN` |
| 26 | Bridge Builder | Small pieces make progress | `PARK` |
| 27 | Easy Wins | Confidence week | `ANY_RUN` |
| 28 | The Long View | Look back | `NOTE` |
| 29 | Trophy Room | Celebrate yourself | `EASY` |
| 30 | Sky Lanterns | Enjoy the journey | `PARK` |
| 31 | A Change of Scene | New path week | `ANY_RUN` |
| 32 | Tower Time | Keep showing up | `FAMILY` |
| 33 | Just Because | No pressure week | `NEW_ROUTE` |
| 34 | First Landing | Start the last world | `NATURE` |
| 35 | Rocket Shoes | Playful momentum | `WALK` |
| 36 | Space Friend | Take a sidekick | `NOTE` |
| 37 | Rainbow Rings | Colorful journey | `EASY` |
| 38 | Story Collector | Write it down | `ANY_RUN` |
| 39 | The Gentle Quest | A kind challenge | `PARK` |
| 40 | Cosmic Paths | Explore again | `ANY_RUN` |
| 41 | Comet Week | Bright energy | `NOTE` |
| 42 | Trail of Memories | Remember a moment | `EASY` |
| 43 | The Home Stretch | Progress is personal | `PARK` |
| 44 | Planet Party | Fun first | `ANY_RUN` |
| 45 | Mystery Week | A surprise | `FAMILY` |
| 46 | The Grand Trail | Your own path | `NEW_ROUTE` |
| 47 | Welcome-Back Window | No pressure | `NATURE` |
| 48 | Festival Preparations | Get ready | `WALK` |
| 49 | Look How Far | Reflect on total | `NOTE` |
| 50 | One More Memory | Make it special | `EASY` |
| 51 | The Last Chapter | Finish your story | `ANY_RUN` |
| 52 | ruuun! Anniversary | Celebrate the year | `PARK` |


---

# Appendix C - Minimal code contracts and examples

Use Kotlin types similar to these, with Room-compatible conversions. The examples are **illustrative** and should be made compiling and tested by the developer.

```kotlin
data class RunLog(
  val id: String,
  val date: LocalDate,
  val distanceM: Int,
  val effort: Effort,             // EASY, OKAY, HARD
  val targetMAtLog: Int,
  val durationMinutes: Int?,
  val mode: RunMode?,             // WALK_ONLY, WALK_JOG, MOSTLY_JOG
  val tags: Set<RunTag>,          // PARK, FAMILY, NEW_ROUTE, NATURE
  val note: String?
)

data class StatsSnapshot(
  val outingDays: Int,
  val totalDistanceM: Long,
  val maxSingleDistanceM: Int,
  val weeksMet: Int,
  val questsCompleted: Int,
  val completedChapters: Int,
  val earnedBadgeIds: Set<String>
)

fun xpForDays(outingDays: Int): Int = 50 * outingDays
fun levelForDays(outingDays: Int): Int = 1 + outingDays / 5

interface BadgeEvaluator {
  fun earnedBadges(logs: List<RunLog>, settings: RunnerSettings): Set<String>
}
```

For DB/backup content, one log may serialize as:

```json
{
  "id": "locally-created-uuid",
  "date": "2026-11-01",
  "distanceM": 300,
  "effort": "OKAY",
  "targetMAtLog": 300,
  "durationMinutes": 8,
  "mode": "WALK_JOG",
  "tags": ["PARK", "FAMILY"],
  "note": "Nice walk with Dad"
}
```

**Computing the celebration:** On save, calculate `before = snapshot(existing logs)` and `after = snapshot(existing + new log)`. The banner shows `+50 XP` only if `after.outingDays > before.outingDays`; badge reveals use `after.badges - before.badges`; chapter reveal uses `after.completedChapters - before.completedChapters`. Updating older logs can also trigger recomputation but should not produce dramatic celebration screens.

---

# Appendix D - Worked scenarios

| Scenario | Action | Expected |
|---|---|---|
| First outing | New runner logs 220 m, Okay, target 300 m | 1 run day, 220 m total, 50 XP, Liftoff badge, next target stays 300 m |
| Gradual progress | Two Easy sessions of 300 m on different days | One small increase to 325 m; never jump to 350 m |
| Hard day | 270 m, Hard, target 300 m | Count the outing, show encouragement, suggest 250 m next time |
| Two logs today | 200 m + 300 m same date | 500 m added; 1 run day; 50 XP total; no second chapter increment |
| Comeback | New outing after 16 days away | Optional welcome-back prompt, old XP/badges intact, repeat/easier possible |
| Backdated entry | Add run last month | Correct total and month badges; cannot automatically raise today's target |
| Correction | Change incorrect 1,500 m to 150 m | Correct all totals, badges and levels from underlying data; no negative coin accounting |
| One per week | Initial plan 1/week, 48 unique outing days | Chapter requirements scaled 50%; achievable year-long story progress |
| Year two | Enter 53rd weekly theme | Reuse week-1 text; ongoing stats, chapters and achievements preserved |
| Restore | Export, reset, import | Same profile and logs; same computed XP, chapters, quests and achievements |

## Final instruction to the implementation agent

Build the **minimum durable product**: manual logs, a motivating dashboard, adjustable easy targets, simple XP/levels, a meaningful badge cabinet, 52 weekly themes, 12 text-led chapters, edit/history and JSON backup. Everything is offline and computed from two small Room entities. No money-like game economy, no cosmetic/customisation features, no collectible objects, no server and no feature inflation. If capacity is limited, ship the full basic journey with working achievements rather than elaborate animations or decorative extras.
