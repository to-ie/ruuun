# ruuun! UI handoff package

For the Android build agent. Read with `ruuun_developer_handoff_v2.md` (behaviour, data, rules). This package adds the visual design.

| Path | What it is |
|---|---|
| `UI_DESIGN_SPEC.md` | Screen-by-screen layout, components, copy, states, accessibility. Start here. |
| `app/src/main/java/app/ruuun/ui/theme/` | Compose Material 3 theme: `Color.kt`, `Type.kt`, `Dimens.kt`, `Theme.kt`. Drop in, fix package name. |
| `app/src/main/res/drawable-nodpi/ruuun_logo.png` | Approved logo, cropped to content. Bundle locally. |
| `app/src/main/res/font/` | Empty. Add Baloo 2 ExtraBold and Nunito (Regular, SemiBold, Bold, ExtraBold) TTFs, SIL OFL licence. See `Type.kt`. |
| `reference/mockup-html/` | The 8 mockup screens as HTML. Visual reference only, not for shipping. |

Priority stays as in the handoff: a working, tested offline APK beats pixel-matching.
