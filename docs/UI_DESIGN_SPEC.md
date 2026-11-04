# ruuun! UI design spec (companion to developer handoff v2)

**Date:** 1 November 2026
**Applies to:** `ruuun_developer_handoff_v2.md`. That file owns behaviour, rules and data. This file owns layout, styling and copy. If they conflict, the handoff wins.
**Reference mockups:** `reference/mockup-html/*.dc.html` (one file per screen, 390 px wide, HTML/CSS). Open in a browser or read as markup. They are visual references only: do not ship HTML or WebViews.

## 0. How to use this

1. Copy `app/src/main/java/app/ruuun/ui/theme/*` into the project (adjust the package) and wrap the app in `RuuunTheme { }`.
2. The logo is Ruu's head over the ruuun! name: `app/src/main/res/drawable-nodpi/ruuun_logo.webp`, on Setup (190 dp wide). `contentDescription = "ruuun!"`. The splash shows the launcher icon (Ruu's head on Navy) with `ruuun_wordmark.png` underneath on Android 12 and up.
3. Add fonts to `res/font` (see `Type.kt` header). Fallback to `FontFamily.Default` is acceptable.
4. Icons: use `androidx.compose.material:material-icons-extended` (bundled, offline). Mapping in section 3.
5. All numbers in the mockups are **sample data for one example child**. In the app every value is derived from Room as the handoff defines. Copy strings are final unless marked `[sample]`.

## 0.5 Design system since 1.5 ("chunky")

1.5 restyled every screen to a playful, game-like look. **Where this section and sections 1, 2 or 4 disagree, this section wins.** Content, data and behaviour did not change.

**Tokens** (`ui/theme/Color.kt`): Navy `#111A2E` ink · Sky `#3DBDF5` / shade `#1597D6` / deep text `#0B3A55` / tint `#E4F6FF` · Orange `#FF8A1F` / shade `#D66A00` / tint `#FFE2C7` · Lime `#C6F03C` / shade `#9CC417` / text `#5C7A00` / tint `#F1FBD4` · Ground `#F6F8FB` · Border `#E3E8EF` · Divider and locked fill `#EEF1F5` · dashed `#C9D1DC` · secondary text `#4A5468`. Type: Baloo 2 800 for titles (30), section titles (20 to 22), big numbers (30 to 40) and the button text (21, capitals, 0.04 em); Nunito 700 to 800 for the rest; overlines 12 sp, 0.12 em, capitals. Icons: Material Rounded (filled).

**Contrast rules (a change from the design handoff, on purpose).** The handoff put white text on Sky and on Orange. Those measure 2.1:1 and 2.4:1, below the 3:1 minimum even for large text, so the app uses Navy (or SkyDeep on Sky) instead. Orange text on white uses OrangeShade, large sizes only. Medal icons are Navy, except Secret (lime on black). Inactive nav icons are `#7D8799` (3.6:1).

**`Chunky`** (`ui/components/Chunky.kt`) is the base of every card, chip, tile and button: a solid face over a darker shade that shows as a thick bottom edge (no blur shadows anywhere). Pressing moves the face down to a 2 dp edge in 80 ms; the outer size never changes. White faces get a 2 dp `Border` outline and a `Border` edge; coloured faces get their shade (`shadeOf`). Depths: buttons 6, cards 5, chips 4, medals and small tiles 3 to 4. The edge is the face's own shape dropped by the depth, so a circle shows a circle under it, not a taller oval. Sideways-scrolling chip rows fade at the edge that has more.

**Screen structure.** Tabs and sub-screens start with a **blue header** (`BlueHeader`, Sky, reaching up behind the status bar) holding the title and chips, then a **rounded sheet** (`SheetTop`, Ground, 28 dp corners) for the content. A Sky strip stays behind the status bar while content scrolls. Things sitting on blue (chips, the speech card, the fox circle, the closest-badge card) are white with a `SkyShade` edge. Sub-screens (Log a run, Settings, a run, Setup) use `SubScreenHeader`: a white back or close chip and the title on the same blue.

**Bottom nav:** white, 2 dp top border; 64 × 48 slots; active slot `SkyTint` with a 2 dp Sky outline and a SkyShade icon; 30 dp Rounded icons, inactive `IconMuted`. Icons only on screen; each tab still has its name for TalkBack.

**Splash ("night navy"):** Android's own splash shows Ruu's head on Navy with the icon circle in the inner ring colour, then the app's splash carries on while the data loads (at least 0.9 s): two halo rings (300 dp `#1A2540`, 240 dp `#223052`), Ruu's head in a 220 dp circle bobbing 8 dp and tilting 2 degrees each way over 2.4 s, the ruuun! wordmark 30 dp below, and three 14 dp dots (orange, lime, sky) 70 dp above the bottom, hopping 10 dp in turn. Both stop when animations are off; the splash fades out in 200 ms. Status bar icons are light while it shows.

**Ticks** on the week row and quest checks are drawn (`ThickCheck`), heavier than the icon font's, with round ends.

**Screens**
- **Home:** the blue header fills whatever the sheet leaves (never less than its own content; short phones scroll). Level chip, XP chip and settings chip; a white XP bar on a SkyShade track; Ruu's speech card "Let's do 1.3 km" (distance in OrangeShade, rolls on Easier and Harder); Ruu in a 180 dp round frame on the current world's scene, dressed for it, breathing gently; a navy pill in lime ("2 stops or fewer · from 3 · breaks are fine", or what the rule is before stops are counted, or the welcome-back line). Sheet: Monday to Sunday circles (lime with a check on days run, dashed orange ring on today, grey otherwise, with the weekly goal on the right); Easier (south arrow) · LOG A RUN · Harder (north arrow); a card with the closest badge and the current chapter as progress lines. The run, distance and badge counters moved off Home (they are on History and Badges).
- **Journey:** "Journey" and an "N of 24" chip; the current chapter card on blue (world scene, Ruu, lime world pill, 14 dp orange bar). Sheet: the week (goal and side quest as two tiles), stops as a navy card with one block per stop for the first counted run and the latest (12 shown, then "+N"; zero reads "Non-stop!"), the whole journey, week by week. The current world is a coloured banner then its three chapters as a zig-zag path (offsets 24 / 120 / 52 dp): finished chapters show their picture in a circle with a lime check and open it when tapped, the current one is an orange 76 dp circle with the run count, "You are here" and a slow pulse (none when animations are off), locked ones are grey with a lock and the runs needed. Finished worlds fold to a card that opens to the same path; worlds ahead show their chapters and run range; a season not yet open is drawn dashed.
- **Badges:** "Badges" and an "N of 193" chip, a white progress bar, Ruu holding a medal with a speech card naming the closest badge, and that badge as a card with its bar and hint. Filter chips, then each category with a colour square, six badges and "Show N more". Earned: a medal in the category colour with its darker edge and a lime check. Locked: grey tile, dashed medal with a lock (a question mark for secrets), a 7 dp orange bar and "a/b".
- **History:** "History" and four stat tiles on blue (all-time distance on orange, the rest white). Each run: a date block, the distance, a feel pill with its face, a dot for each stop (12, then "+N") and the note.
- **Not in the handoff, extrapolated in the same style:** Log a run (blue header with a close chip, Ruu taking notes, chunky effort buttons and stops stepper, LOG A RUN style save button), Run saved (the celebration on blue with Ruu cheering, then the sheet), Settings and a run's detail (blue header with a back chip, chunky cards), Setup (logo on blue).

**Badge category colours** (face / edge): First steps `#3DBDF5` / `#1597D6` · Run days `#B9A4FF` / `#8A6FE8` · Best single run `#FF8A1F` / `#D66A00` · Total distance `#FFC83D` / `#D9A200` · Weekly `#C6F03C` / `#9CC417` · Adventure `#4FD08A` / `#2AA566` · Reflection `#FFA8CF` / `#E0709F` · Story `#F2B880` / `#C98A4B` · Quests `#FF9C8A` / `#E0604F` · Seasons `#7FE0D3` / `#3BB5A5` · Non-stop `#7FB2FF` / `#3D7BE0` · Secret `#111A2E` / black. The handoff gave seven; the other five were chosen to stay apart from those.

**World colours and icons:** Rookie Woods `#C6F03C` forest · Desert Dash `#FFD98A` wb_sunny · Sky Kingdom `#A8E2FF` cloud · Planet Zoom `#D6C8FF` public · Coral Coast `#FFC2B5` beach_access · Jungle Run `#9FE6B6` park · Frosty Peaks `#CDEFFA` ac_unit · Comet Run `#FFE98A` auto_awesome.

**Accessibility kept:** every button keeps a normal-case TalkBack label even though it shows capitals; the week row, quest lines and chapter nodes each read as one sentence; the pulse and the fox's breathing stop when animations are off; on a narrow button the icon gives way to the words.

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
| **WorldCard** | Card with a banner on top, then the chapter title (`CardTitle`), teaser (`Body`, `TextMuted`) and progress row. With a picture (Home, Journey, Run saved): the world scene cropped to 2:1 with Ruu in the world's outfit standing bottom right (97% of the banner height), and the world name on a chip in the banner colour, bottom left, kept to the left 56% so it never covers Ruu. A finished chapter uses its own 3:2 picture with no Ruu. Without a picture: a coloured banner row (12 to 14 dp vertical, 16 dp horizontal) with icon and world name in `Overline`. |
| **Ruu** | Decorative only (`contentDescription = null`): the words next to Ruu say the same thing. All Ruu pictures share one canvas (425:480) with the feet on the same line, so poses swap without a jump. Home hero: `ready` (or `welcome_back` after 14 days away), 176 dp tall, standing on the right of the card with the head 44 dp above its top edge and a slow 2.5% breathing scale from the feet (none when animations are off); smaller, or left out, when the distance needs the room. Run saved: 220 dp, `cheer` for anything new, `tired_proud` after Hard, `proud` otherwise. Log a run (new runs only): `notebook`, 100 dp, with a speech bubble. Badges: `badge_proud`, 104 dp, with a speech bubble naming the closest badge. History empty: `notebook`. Speech bubbles: white card with a 1 dp `Border`, `BodyStrong` Navy text, tail on the left pointing at Ruu. |
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
3. **Next Mission hero**: `Navy` fill, `Shapes.Hero`, 20 dp padding. Decorative circles top-right (Orange 18% and Sky 16% alpha), decorative only. Overline "NEXT MISSION" in `Sky`. Distance `HeroNumber` in `Ground`. *Since 1.3*, once stops have been counted, a line under the distance in `Lime` `BodyStrong`: "Stop goal: {n} stops or fewer · {m} last time" or "Stop goal: non-stop". Subtitle (1.3): "Breaks are fine. It gets longer after a run with {limit} stops or fewer.", or at the longest mission setting "Breaks are fine. This is your longest mission setting." 17 SemiBold. PrimaryCta "Log a run" with Add icon. SecondaryButton (on navy) "Make this easier". If 14+ days since last outing, swap subtitle for "Welcome back! Repeat or make it easier." and keep both buttons.
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
2a. *Added in 1.3, placed before effort because stops move the mission.* "How many times did you stop?": a row of minus button (52 dp square, 2 dp Navy border), the count centred ("Not counted" muted, "{n} stops" or "0 · non-stop" in `CardTitle`), plus button. Starts at "Not counted"; minus from there gives 0, plus gives 1. Text button "Clear, nobody counted" appears once a number is set. Helper below: the stop goal and "Breaks are fine.", or "Non-stop! The whole way." for 0, otherwise "The mission gets longer after a run with {limit} stops or fewer." No goal is shown when an earlier date is picked (that run is saved without one). Screen reader: from "Not counted" the minus button reads "No stops, non-stop" and plus "One stop"; the count is a polite live region. Optional.
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
1a. *Added in 1.3:* from the second run on, a "PERSONAL BEST" card (`LimeTint`) under the top panel listing "New longest run: {d}" and "New longest non-stop run: {d}" when they happen; pops in with the XP.
4a. *Added in 1.3*, only when stops were counted: Stops panel (`InfoBg`), overline "STOPS", the number in `CardTitle`, and a line on the right: "First count: 8 stops. Next time, try 7 stops or fewer.", "6 stops. You beat your stop goal!", "7 stops. Stop goal met!", "9 stops this time. Breaks are fine.", "Non-stop! You went the whole way.", each followed by the next goal; after a Hard run "It felt hard, so the goal stays at {n} stops or fewer."
5. Next mission panel (`InfoBg`): under the distance, the next stop goal in `Secondary` ("5 stops or fewer") when there is one. overline "NEXT MISSION" (`InfoLabel`), distance Baloo 28, supporting line on the right saying what happened to the mission and why, following the target rules, e.g. "You beat 300 m and it felt easy, so it goes up.", "You beat 300 m, so the new mission is 1.1 km. Now try it with fewer stops: 6 fewer stops and it gets longer." (1.3.1), "Same distance again. Count your stops: it gets longer after a run with 2 stops or fewer.", "One more run at 1.2 km with 2 stops or fewer and it goes up.", plus stop notes for an extra run ("Your longest run today sets the stop goal."), a short run ("Short runs don't change the stop goal.") and a run within the limit ("Keep it at 2 stops or fewer while the distance grows."), "One more easy day at 425 m and it goes up.", "That felt hard, so next time is a bit shorter.", "Runs on earlier days don't change the mission."
6. Two buttons side by side: SecondaryButton "View badges", PrimaryCta "Done".

### 4.5 Journey (`Missions.dc.html`, renamed after 1.0 beta)
Top bar: "Journey" (`ScreenTitle`). No next-mission card and no badge hints here; see handoff §3.4.
1. Week card: overline "WEEK {n} · {flavour}", title = week theme, helper "Your weekly goal, plus an optional side quest. They reset every Monday, and missing a week costs you nothing." Two rows on `QuestRowBg` (14 dp corners): "Weekly goal" / "Run on {goal} days this week" / "{done}/{goal}"; "Side quest (optional)" / quest text / "0/1" or "1/1". CheckDot left (filled when complete, dashed otherwise).
1a. *Added in 1.3*, once any run has its stops counted: Stops card. Overline "STOPS"; title "From {first} to {latest}" (or "{n} stops on your first count"); helper with the change and the current stop goal; a bar chart of the last ten counted days, oldest left, Navy bars (Sky fails contrast on white) with 4 dp rounded tops on a 1 dp `Border` baseline, ten fixed slots so bars keep their width, a muted 4 dp stub for 0, and numbers only over the first and latest bars. The whole chart has one content description listing every value.
2. WorldCard for the current chapter with a one-sentence narrative and "{days} of {target} runs · any distance counts".
3. "The whole journey": eight world cards under overlines "SEASON 1" and "SEASON 2 · OPENS AFTER THE FESTIVAL" (1.3), helper "Two seasons, eight worlds, 24 chapters. Every day you run moves the story on, whatever the distance. Finished worlds fold up: tap one to look back." A world with all three chapters complete shows only its banner, with "Done · 3 of 3" and a chevron; tapping the banner opens or folds it.
4. "Week by week": rows with 36 dp number square, theme title, "Side quest: {text}", status right. Past: number square `Lime`, status "{n}/2 done". Current: row `InfoBg` with 2 dp Navy border, square Navy with `Ground` number, status "Now · {n}/2". Future: square `SegmentBg`, status "Coming up". Collapsed list shows weeks 1 to current+2, then SecondaryButton "See all 52 weeks" expands. After week 52 the numbering continues (53, 54) with rotated themes.

### 4.6 Badges (`Badges.dc.html`)
1. Header: "Badges" + "{earned} of 141" right, Orange progress bar, horizontally scrolling filter chips: All, Run days, Distance, Weekly, Story, Adventure, Seasons, Secret (selected Navy fill). Extend the list to all categories if wanted; keep it one row.
2. One section per category: title + "{earned} of {total}" muted, BadgeCell grid. Order inside a section: earned first by threshold, then locked by threshold.
3. Tapping a cell opens a bottom sheet: icon, name, rule hint, status, progress.
   *Revised after 1.1:* an earned badge shows its own icon (one per badge, all different, chosen from Material's outlined set to suit the name); a locked badge shows a padlock. The category colour stays. Secret locked: "Keep exploring to find this one." with no rule text.
4. BottomNav, Badges selected.

### 4.7 History (`History.dc.html`)
1. Top bar "History".
2. 2x2 stat grid: first tile Navy fill ("{km} km" / "All-time distance"); then "Outing days", "Longest outing", "Days out in {Month}".
3. Month groups newest first: overline month name ("NOVEMBER 2026"), RunRows newest first; a counted run shows "{n} stops" or "non-stop" muted after its effort pill (1.3). Effort pill tints: Easy `LimeTint`, Okay `SkyTint`, Hard `OrangeTint`.
4. Paging: older months load on scroll or via "Show {previous month}" SecondaryButton.
5. Row tap opens run detail with Edit and Delete. Delete and Edit confirmation copy: "Totals, level and badges are worked out from your runs, so they may change." Buttons "Delete run" / "Keep it".
6. Empty state: "No runs yet. Your first one will show up here." + PrimaryCta "Log a run".

### 4.8 Settings (`Settings.dc.html`)
No bottom nav. Top bar: back arrow ("Back to Home") + "Settings" with subtitle "Best changed with a grown-up".
1. Section "YOUR PLAN" card: Nickname field; "Weekly goal" SegmentedPicker with helper "How many days to run each week. Changing this can change past weekly-goal badges. Story progress stays."; row "Longest suggested mission" / "Missions never go above this" with value button "5,000 m" (default since 1.3) (opens number dialog, 100 to 50,000 m); row "Distance goes up after" / "A run with this many stops or fewer" with value button "2 stops or fewer" opening a dialog with a 0 to 5 SegmentedPicker and "Lower is stricter. 2 is a good start." (1.3); row "Story pace" / "Set at the start, can't be changed" / "Normal" or "Gentle" (1 per week); SecondaryButton "Reset next mission to {startingTarget}".
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
