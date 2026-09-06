# UltiNote — version ledger

Single source of truth for what's in each release and what's queued next.
`versionCode` / `versionName` live in `app/build.gradle.kts`.

## Current: v1.2.1 (versionCode 7)

Zer0-inspired chrome + landscape reader fixes.

- **Retractable side rails** — edge tab collapses sidebar + tool rail to the side (mirrored for left-handed mode) with slide/fade; full-bleed canvas on demand.
- **Framed canvas** — 12dp breathing room, 24dp rounded card + soft shadow on a darker desk mat; UI no longer touches screen edges.
- **Status-pill chrome** — Read/Draw switch restyled as a dark pill with micro-caps labels + accent dot.
- **Landscape PDF fix** — page renders as a floating paper card (shadow + sheet) that letterboxes intentionally; bitmap re-renders on rotation via a split effect that never touches unsaved ink.
- **Vector-only UI sweep** — onboarding, lasso pill, thumbnail pills/move buttons all use Material vector icons now; zero emoji in chrome (stickers are user content and stay).
- Same upload-key signature as v1.1.2+ — installs over v1.1.2/v1.1.3 cleanly. Coming from ≤ v1.1.1, uninstall first.

Reader-first release.

- **Read / Draw mode switch** — Read scrolls + zooms PDFs like a book (nothing inks, double-tap zooms 1x↔2x); Draw is the full canvas. Phone gets a mode pill above the dock + slim reader bar; tablets get a floating pill.
- **Two-finger tap = undo, three-finger tap = redo** — quick still taps only; real pinches are untouched.
- **Tidy handwriting (default Subtle)** — geometric de-wobble in every language, no recognition involved. Strong adds axis straightening. Off keeps raw ink. Settings → Tablet & Stylus.
- **Convert handwriting to text (opt-in)** — lasso ink → Convert pill → typed text inserted below, ink always kept. 27 on-device language packs (English default, ~20MB each downloaded once, offline after). Settings → Convert language. Urdu/Arabic/Hebrew/Persian flagged RTL.
- **Share page as image** — reader bar button + pages-sheet "Share page" → PNG via share sheet (homework portals want images).
- **AI companion fully hidden** behind `FeatureFlags.AI_COMPANION_ENABLED = false` — zero UI traces until the Gemini backend lands.
- **Left-handed tablet layout** — rails mirror right (Settings toggle).
- **First-launch onboarding** — 30-second card (snap, gesture undo, Read/Draw), once ever.
- ML Kit `digital-ink-recognition:19.0.0`; new `INTERNET`/`ACCESS_NETWORK_STATE` permissions (pack downloads only).

## Past

- **v1.1.3** — sources moved to `com.ultinote.app` so the launcher activity resolves (fixed open-crash).
- **v1.1.2** — real upload key + signed releases (fixed "App not installed"); debug-sign fallback so releases are never unsigned.
- **v1.1.1** — CI green (`gradlew` exec bit, actions refresh).
- **v1.1.0** — school-ready core: pressure stylus, palm rejection, lasso, photos, pin/tags, glass UI, planner.

## Queued (not started)

- **v1.3.0 — continuous reader:** true vertical multi-page scroll with per-page annotation layers, resume position + % progress rings, OLED night invert. (Today: single-page + filmstrip.)
- **Self-quiz mode:** hide text boxes, tap to reveal. Offline, no AI.
- **Checklist blocks:** tappable checkboxes in planner/text pages.
- **Pen presets:** saved tool/color/width combos, one-tap switch.
- **Manual backup/restore:** one `.ultinote` zip (Room DB + images + exports), no cloud needed.
- **Starred pages + jump list** in page overview.
- **Tablet split view:** PDF left, blank notes right.
- **AI Study Companion (Gemini)** + **cloud sync** — Coming Soon, backend work, UI already built and flagged off.
