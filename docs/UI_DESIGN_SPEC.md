# ruuun! UI design spec (companion to developer handoff v2)

**Date:** 1 November 2026
**Applies to:** `ruuun_developer_handoff_v2.md`. That file owns behaviour, rules and data. This file owns layout, styling and copy. If they conflict, the handoff wins.
**Reference mockups:** `reference/mockup-html/*.dc.html` (one file per screen, 390 px wide, HTML/CSS). Open in a browser or read as markup. They are visual references only: do not ship HTML or WebViews.

## 0. How to use this

1. Copy `app/src/main/java/app/ruuun/ui/theme/*` into the project (adjust the package) and wrap the app in `RuuunTheme { }`.
2. Copy `app/src/main/res/drawable-nodpi/ruuun_logo.png` (cropped, 640x446, transparent-free white background). Use it on Home header (48 dp tall) and Setup (210 dp wide). `contentDescription = "ruuun!"`.
3. Add fonts to `res/font` (see `Type.kt` header). Fallback to `FontFamily.Default` is acceptable.
4. Icons: use `androidx.compose.material:material-icons-extended` (bundled, offline). Mapping in section 3.
5. All numbers in the mockups are **sample data for one example child**. In the app every value is derived from Room as the handoff defines. Copy strings are final unless marked `[sample]`.

## 1. Global layout

- Phone portrait, designed at 360 to 412 dp wide. Content is a single column, `Dimens.ScreenPadding` (20 dp) on all sides, `SectionGap` (16 dp) between cards. Every screen scrolls vertically (`LazyColumn` or `verticalScroll`).
- Background `Ground`. Cards are `Surface` with 1 dp `Border` and `Shapes.Card` (20 dp). No elevation shadows, except the selected segment in segmented controls (1 dp soft shadow).
- Top bar: white `Surface` with 1 dp bottom `Border`. Not a Material TopAppBar colour change on scroll.
- Bottom navigation: on Home, Journey, Badges, History only. Not on Setup, Log a run, Run saved, Settings.
- Must work with font scale up to 200%: let cards grow, never fix card heights, allow text to wrap to two lines. Stat tiles may wrap to a 1-column stack above 150% font scale.
- Respect reduce motion: the only animations are the Run saved check pop and confetti; both skippable.

## 2. Reusable components

| Component | Spec |
|---|---|
| **PrimaryCta** | Height 56, fill `Orange`, text `Navy` `RuuunText.Cta`, `Shapes.Button`, optional leading icon 22 dp. Full width. Disabled: fill `Track`, text `TextMuted`. Debounce taps (handoff 3.3). |
| **SecondaryButton** | Height 48 to 56, `Surface` fill, 2 dp `Navy` border (or `InputBorder` for low-emphasis), text `Navy` 15 to 17 ExtraBold. On navy backgrounds: transparent fill, 2 dp `HeroOutline`, text `Ground`. |
| **Card** | `Surface`, 1 dp `Border`, 20 dp corners, 16 dp padding. |
| **WorldCard** | Card with a coloured banner row on top (12 to 14 dp vertical, 16 dp horizontal): icon 22 to 24 dp + world name in `Overline` uppercase. Banner colour per world below. Body: chapter title (`CardTitle`), teaser (`Body`, `TextMuted`), progress row. |
| **ProgressBar** | Height 10 (XP bar 12), `Track` background, fully rounded. Fill: XP `Sky`; badge progress `Orange`; chapter `Navy`. Expose `progressBarRangeInfo` semantics. |
| **StatTile** | `Shapes.Tile` card, 14 dp padding, value `Greeting`/`StatValue`, label `Label` `TextMuted` (14 sp Bold). |
| **CheckDot** | 30 to 34 dp circle, `Lime` fill, 2 dp `Navy` border, Navy check (stroke heavy). Empty state: white fill, 2 dp dashed `PipDashed`. Never red. |
| **SegmentedPicker** | `SegmentBg` container, 5 to 6 dp padding, 14 to 16 dp corners. Options equal width, 44 to 48 dp tall. Selected: `Surface` + soft shadow. Text `Navy` 17 to 18 ExtraBold. Semantics: `Role.RadioButton` / selected. |
| **Chip (toggle)** | Pill, min height 44, 2 dp border. Mode chips: selected `Navy` fill + `Ground` text; idle white + Navy border. Tag chips (multi-select): selected `Lime` fill + Navy border + check icon; idle white + `InputBorder` + plus icon. |
| **EffortButton** | Grid of 3, min height 92, 16 dp corners. Face icon 34 dp + label. Idle: white, 2 dp `Border`. Selected: 3 dp `Navy` border and tint fill: Easy `LimeTint`, Okay `SkyTint`, Hard `OrangeTint`. |
| **BadgeCell** | 3-column grid, 10 dp gap. 18 dp corners, 14/8/12 dp padding, centred. Icon circle 52 dp. Earned: white, 2 dp `Navy` border, icon circle in category colour, pill `Earned` on `Lime`. Locked: `LockedCard`, 1 dp `Border`, icon circle `Border` with lock icon `TextMuted`, pill shows progress (`12/15`) on `OrangeTint`. Secret locked: name `???`, pill `Locked` on `Track`. |
| **RunRow** | Full-width clickable row, min 64 dp, white card, 16 dp corners. Left: 46 dp date column (weekday `Pill` uppercase muted, day number Baloo 22). Middle: distance `BodyStrong` 17 + effort pill (tint by effort), note one line ellipsised `TextMuted`. Right: chevron. |
| **BottomNav** | White, 1 dp top `Border`, 4 equal items, padding 8/8/14. Item: 56x32 indicator + 22 dp icon + label 13 sp. Selected: indicator `Sky` pill, label Navy ExtraBold. Idle: no indicator, icon and label `TextMuted` Bold. |

### World banner colours and icons

| World | Banner | Icon |
|---|---|---|
| Rookie Woods (ch 1 to 3) | `Lime` | `Icons.Outlined.Park` |
| Desert Dash (ch 4 to 6) | `SunTint` | `Icons.Outlined.WbSunny` |
| Sky Kingdom (ch 7 to 9) | `SkyTint` | `Icons.Outlined.Cloud` |
| Planet Zoom (ch 10 to 12) | `OrangeTint` | `Icons.Outlined.Public` |

### Badge category colours and icons (one icon per category, handoff section 9)

| Category (ID prefix) | Earned circle | Icon |
|---|---|---|
| First steps (FS) | `Sky` | `Icons.Outlined.DirectionsWalk` |
| Run days (RC) | `Sky` | `Icons.Outlined.CalendarMonth` |
| Best single outing (SD) | `Orange` | `Icons.Outlined.Bolt` |
| Total distance (TD) | `Orange` | `Icons.Outlined.Route` |
| Weekly (WK, CO) | `Lime` | `Icons.Outlined.EventRepeat` |
| Adventure tags (AD, AT) | `Lime` | `Icons.Outlined.Explore` |
| Reflection (RF) | `SkyTint` | `Icons.Outlined.EditNote` |
| Story (CH) | `Lime` | `Icons.Outlined.MenuBook` |
| Quests (QS) | `Orange` | `Icons.Outlined.Flag` |
| Seasons (MO) | `Orange` | `Icons.Outlined.Eco` |
| Secret (SE) | `Lime` | `Icons.Outlined.Star` |

## 3. Icon mapping

Settings `Settings`; Home `Home`; Journey `Map`; Badges `MilitaryTech`; History `Schedule`; add `Add`; check `Check`; close `Close`; back `ArrowBack`; date `CalendarMonth`; locked `Lock`; row chevron `ChevronRight`; info `Info`; export `Upload`; import `Download`; delete `DeleteOutline`. Effort faces: `SentimentSatisfied` (Easy), `SentimentNeutral` (Okay), `SentimentDissatisfied` (Hard). No emoji in UI.

## 4. Screens

Order of sections top to bottom. Copy in quotes is literal. `{x}` is derived data.

### 4.1 Setup (`Setup.dc.html`)
No bottom nav.
1. White header block, centred: logo 210 dp wide; "Let's set up your adventure" (`SubTitle` 26); "Takes about a minute. A grown-up can help." (`Body`, muted).
2. Text field: label "Nickname (optional)", placeholder "Runner", max 20 chars.
3. Radio cards, legend "Where are you starting?": "New to it" / "First mission 300 m", "Getting moving" / "First mission 500 m", "Already jogging" / "First mission 800 m". Default New to it. Selected card: 2 dp Navy border, `OrangeTintLight` fill. Idle: 2 dp `Border`, white.
4. SegmentedPicker, legend "Weekly goal": 1 / 2 / 3, default 2. Helper: "How many days to run each week. You can change this later in Settings."
5. Info panel (`InfoBg`, 16 dp corners, info icon): "Walk breaks are encouraged and rest days are good. Stop and tell a trusted adult if you feel pain, dizziness or unwell."
6. PrimaryCta "Let's go!" pinned to bottom of content.

### 4.2 Home (`Home.dc.html`)
1. Header: logo left (48 dp tall); gear icon button right, 48x48, `IconButtonBg`, 14 dp corners, `contentDescription = "Settings"`.
2. "Hello, {nickname}" (`Greeting`). Row: "Level {n} · {levelName}" (`BodyStrong`) and "{xp} XP" (muted, right). XP bar (`Sky`). Caption "50 XP for each day you run. {n} more to Level {level+1}", saying what earns XP.
3. **Next Mission hero**: `Navy` fill, `Shapes.Hero`, 20 dp padding. Decorative circles top-right (Orange 18% and Sky 16% alpha), decorative only. Overline "NEXT MISSION" in `Sky`. Distance `HeroNumber` in `Ground`. "Walk or jog. Breaks welcome." 17 SemiBold. PrimaryCta "Log a run" with Add icon. SecondaryButton (on navy) "Make this easier". If 14+ days since last outing, swap subtitle for "Welcome back! Repeat or make it easier." and keep both buttons.
4. Three StatTiles: `{runDays}` "Runs", `{km one decimal}` "Distance (km)", `{badges}` "Badges".
5. Weekly card: overline "THIS WEEK · {weekTheme}"; title "Weekly goal: {done} of {goal} runs"; right side one CheckDot per goal slot (filled for done). Never red, never "overdue".
6. Badge card: 52 dp `OrangeTint` icon square; overline "BADGE ALMOST THERE"; badge title; rule hint; "{cur}/{target}" right; Orange progress bar. Pick the locked numeric badge with the highest progress ratio.
7. WorldCard: world banner, "Chapter {n} · {title}", teaser, Navy bar + "{days} of {target} runs · any distance counts". After chapter 12: title "Legend of the Trail".
8. BottomNav, Home selected.

**Empty state:** greeting, Level 1 · First Steps, 0 XP, bar empty, hero shows starting distance, tiles all 0, weekly "0 / {goal}", badge card shows "Liftoff!" "Record your first adventure." 0/1, chapter 1 "The First Footprint" 0 / 4 days, teaser "Your adventure starts with one outing."

### 4.3 Log a run (`LogRun.dc.html`)
Full screen (or full-height bottom sheet). Top bar: Close icon (48 dp) + "Log a run" (`SubTitle`).
1. "When": outlined button with calendar icon, "Today · {EEE d MMM}" and "Change" link text. Opens DatePicker; dates after today disabled.
2. "How far did you go?": large numeric field (64 dp tall, 2 dp Navy border, `DistanceInput` style) + SegmentedPicker m / km (120 dp wide). Field starts **empty**; show the mission as placeholder only. Helper: "Your mission was {target}. Enter what you actually did." Implausible value (>10,000 m) shows a non-blocking warning line in `InfoText` on `InfoBg`: "That's a long way! Double-check the number." Never block save for it.
3. "How did it feel?": three EffortButtons Easy / Okay / Hard. Required, no default.
4. Divider, then "Extras (optional)".
5. "Walk or jog?": mode chips "Walk", "Walk + jog", "Mostly jog" (single select, tap again to clear).
6. "Tags": multi-select tag chips "Park", "Family", "New route", "Nature".
7. "Minutes" field (130 dp wide, numeric) with side text "Just for your notes. Never used for speed."
8. "A tiny memory" textarea, 2 lines, counter "{n}/140", placeholder "What did you notice?".
9. Sticky bottom bar (white, top border, 14/20/22 padding): PrimaryCta "Save run" with check icon. Enabled when distance valid and effort chosen.

### 4.4 Run saved (`RunSaved.dc.html`)
No bottom nav. Shown after save.
1. Navy top panel, centred, 36/24/28 padding: small static confetti shapes (decorative), 84 dp `Lime` circle with Navy check, "Nice one, {nickname}!" (`Greeting` 32), "{distance} saved for today", pill "+50 XP" (`Orange` fill, Navy text) **only if a new outing day was added**, otherwise pill "Distance added" on `SkyTint`. Below: "Level {n} · {name}" and "{x} of 5 days to Level {n+1}", Sky bar on `HeroTrack`.
2. "{k} new badge(s)" grid of up to 3 earned BadgeCells (no pill), then text button "+{n} more" if needed. Omit section when none.
3. If a chapter completed: WorldCard variant with `SunTint`-style banner "CHAPTER COMPLETE", reveal text from handoff table, then "Next: {world}" and next chapter progress. Otherwise show the current chapter progress card.
4. Quest rows as on Journey: "Weekly goal" / "Run on {goal} days this week" / "{done}/{goal}", and "Side quest (optional)" / quest text / "0/1" or "1/1".
5. Next mission panel (`InfoBg`): overline "NEXT MISSION" (`InfoLabel`), distance Baloo 28, supporting line on the right saying what happened to the mission and why, following the target rules, e.g. "You beat 300 m and it felt easy, so it goes up.", "One more easy day at 425 m and it goes up.", "That felt hard, so next time is a bit shorter.", "Runs on earlier days don't change the mission."
6. Two buttons side by side: SecondaryButton "View badges", PrimaryCta "Done".

### 4.5 Journey (`Missions.dc.html`, renamed after 1.0 beta)
Top bar: "Journey" (`ScreenTitle`). No next-mission card and no badge hints here; see handoff §3.4.
1. Week card: overline "WEEK {n} · {flavour}", title = week theme, helper "Your weekly goal, plus an optional side quest. They reset every Monday, and missing a week costs you nothing." Two rows on `QuestRowBg` (14 dp corners): "Weekly goal" / "Run on {goal} days this week" / "{done}/{goal}"; "Side quest (optional)" / quest text / "0/1" or "1/1". CheckDot left (filled when complete, dashed otherwise).
2. WorldCard for the current chapter with a one-sentence narrative and "{days} of {target} runs · any distance counts".
3. "The whole journey": four world cards, helper "Every day you run moves the story on, whatever the distance."
4. "Week by week": rows with 36 dp number square, theme title, "Side quest: {text}", status right. Past: number square `Lime`, status "{n}/2 done". Current: row `InfoBg` with 2 dp Navy border, square Navy with `Ground` number, status "Now · {n}/2". Future: square `SegmentBg`, status "Coming up". Collapsed list shows weeks 1 to current+2, then SecondaryButton "See all 52 weeks" expands. After week 52 the numbering continues (53, 54) with rotated themes.

### 4.6 Badges (`Badges.dc.html`)
1. Header: "Badges" + "{earned} of 141" right, Orange progress bar, horizontally scrolling filter chips: All, Run days, Distance, Weekly, Story, Adventure, Seasons, Secret (selected Navy fill). Extend the list to all categories if wanted; keep it one row.
2. One section per category: title + "{earned} of {total}" muted, BadgeCell grid. Order inside a section: earned first by threshold, then locked by threshold.
3. Tapping a cell opens a bottom sheet: icon, name, rule hint, status, progress. Secret locked: "Keep exploring to find this one." with no rule text.
4. BottomNav, Badges selected.

### 4.7 History (`History.dc.html`)
1. Top bar "History".
2. 2x2 stat grid: first tile Navy fill ("{km} km" / "All-time distance"); then "Outing days", "Longest outing", "Days out in {Month}".
3. Month groups newest first: overline month name ("NOVEMBER 2026"), RunRows newest first. Effort pill tints: Easy `LimeTint`, Okay `SkyTint`, Hard `OrangeTint`.
4. Paging: older months load on scroll or via "Show {previous month}" SecondaryButton.
5. Row tap opens run detail with Edit and Delete. Delete and Edit confirmation copy: "Totals, level and badges are worked out from your runs, so they may change." Buttons "Delete run" / "Keep it".
6. Empty state: "No runs yet. Your first one will show up here." + PrimaryCta "Log a run".

### 4.8 Settings (`Settings.dc.html`)
No bottom nav. Top bar: back arrow ("Back to Home") + "Settings" with subtitle "Best changed with a grown-up".
1. Section "YOUR PLAN" card: Nickname field; "Weekly goal" SegmentedPicker with helper "How many days to run each week. Changing this can change past weekly-goal badges. Story progress stays."; row "Longest suggested mission" / "Missions never go above this" with value button "3,000 m" (opens number dialog, 100 to 50,000 m); row "Story pace" / "Set at the start, can't be changed" / "Normal" or "Gentle" (1 per week); SecondaryButton "Reset next mission to {startingTarget}".
2. Section "YOUR DATA" card with three 64 dp rows: "Export progress" / "Save a backup file. Keep it somewhere safe."; "Import progress" / "Replaces everything on this phone. You'll see a preview first."; "Delete all progress" (text and icon `Destructive`) / "Asks twice before anything is removed".
3. "About and privacy" panel (`InfoBg`): "ruuun! has no account, no location and no ads. Everything you log stays on this phone unless you export it. The app only goes online to check GitHub for a new version of itself." Below it, "Version {versionName}" with a low-emphasis "Check for updates" button (revised in 1.1).

**Update banner (1.1):** on the four tabbed screens only, a full-width `Navy` strip above the screen header when a newer release exists: title "ruuun {version} is ready" (`BodyStrong`, `Ground`), "Your runs and badges stay just as they are.", PrimaryCta "Update" and on-navy SecondaryButton "Later". While downloading, a `Lime` progress bar replaces the buttons; when done, "Install"; on failure, "Try again".

## 5. Navigation map

Setup → Home (first launch only). Home "Log a run" → Log a run → Run saved → Done: Home; View badges: Badges. Home gear → Settings → back: Home. Bottom nav switches Home / Journey / Badges / History, single back stack per tab is not needed. History row → Run detail → Edit (reuses Log a run screen, title "Edit run", button "Save changes").

## 6. Accessibility checklist

- All interactive elements at least 48 dp (chips 44 dp tall are acceptable only with 8 dp spacing).
- Icon-only buttons have `contentDescription`. Decorative illustrations and confetti have none.
- Progress bars expose value semantics, e.g. "11 of 12 days".
- Selected chips, segments and effort buttons expose selected state.
- Text contrast meets 4.5:1; follow the rules in `Color.kt`.
- Copy avoids: failed, behind, lazy, bad run, burn calories, lose weight.

## 7. Sample data used in the mockups (not app data)

Example child "Runner", 2 outings/week, onboarded week of 21 Sep 2026, 11 outing days and 4,050 m before logging 450 m (Easy, Walk + jog, Park + Family) on Sun 1 Nov 2026. After: 12 days, 4.5 km, Level 3 Trail Finder, 600 XP, chapter 3 complete, 40 badges. Run notes in History are `[sample]`. Use this to build a debug seed if useful; do not ship it.
