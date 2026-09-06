# UltiNote — my school notebook that actually keeps up

Honestly? I built UltiNote because I was tired of fighting my notes app during class.

I wanted something that opens fast, lets me scribble with a stylus without my palm ruining everything, annotate the PDFs my teachers send, and still look nice enough that I *want* to open it. No login. No cloud weirdness. No subscription popup. Just my notes, on my device, ready for class.

That's UltiNote. Local-first, handwriting-first, made for real school life — phones in portrait on the bus, tablets in landscape at a desk.

> AI study buddy + cloud sync are coming soon. Right now everything is 100% offline on purpose so it just works in class, on the bus, anywhere.

---

## What it actually does (v1.2.1)

**Write like paper**
- Ballpoint / fountain / brush pens with real pressure response (S-Pen, USI, generic active stylus)
- **Tidy handwriting** (Subtle by default): de-wobbles invisibly in *any* language — Urdu, Arabic, German, math. Strong mode also straightens lines. Off keeps raw ink.
- **Convert to text** (opt-in): lasso your writing → Convert → typed text appears, ink stays. 27 on-device languages, English built in, rest download once then work offline.
- Highlighter that behaves like a highlighter, not a marker
- Palm rejection toggle — fingers pan + zoom, stylus writes
- S-Pen barrel-button eraser just works
- **Two-finger tap = undo, three-finger tap = redo**, anywhere
- Pressure feel setting: Soft / Medium / Firm (Settings → Tablet & Stylus)
- Auto shape snap for circles, lines, rectangles when your handwriting is shaky at 8am

**Fix mistakes fast**
- Stroke eraser (big, forgiving) + precision eraser (tiny, for details) — both erase ink, shapes, text, stickers
- Lasso select: drag a box, then Duplicate / Delete from the little pill at the top
- Full undo / redo (capped so long sessions don't eat your RAM)
- Layers with opacity + blend modes if you're doing diagrams

**PDFs from teachers**
- **Read mode vs Draw mode**: read scrolls + zooms like a proper book (double-tap zooms), draw inks. One switch, no more accidental marks.
- Import any PDF → it becomes a notebook, one page per PDF page
- Draw right on top, export the whole thing back to a clean HQ PDF and share it — or **share any single page as an image** for homework portals
- Thumbnails to jump pages, plus Duplicate / ◀ ▶ reorder, delete, add blank pages

**Photos + stickers**
- Drop in whiteboard shots, textbook pages, lab setups (they're stored in `UltiNoteImages/` on your device)
- 20 real emoji stickers, not the old "star" text bug — ⭐ ✅ 📐 📚 etc.

**Stay organized**
- Folders + subfolders, 7 pastel colors, 10 icons that actually change with note count
- Pin to top, favorites, tags (`math, chapter-4, exam`) — search hits titles *and* tags
- Calendar planner: tap a date, make a day planner, it links itself
- 6 paper themes (Matcha, Sakura, Lavender, Obsidian dark for OLED, Vintage, Nordic) + 4 fonts — all saved on-device

**Looks like glass**
- Dark-desk editor with a framed, rounded canvas card — real margins, nothing glued to the edges
- **Retractable side rails**: one edge tab hides everything for full-bleed reading, one tap brings tools back (mirrored for lefties)
- Apple Liquid Glass-inspired bars, docks, side rails — specular top light, soft shadows, works in light + dark
- Phone portrait: top bar + bottom dock you can scroll with your thumb
- Tablet landscape: slim side rail + vertical tool rail, canvas gets the whole screen
- Tablet portrait: top bar + dock, no cramped side rail

---

## Install it

**Easiest (APK from GitHub Releases):**
1. Go to Releases → download the latest `UltiNote-v*.apk`
2. Open it on your phone/tablet → allow install → done
3. Works on Android 7.0 (API 24) and up. Stylus optional but lovely.
4. Seeing "App not installed"? Uninstall any older UltiNote copy first (the signing key changed in v1.1.2), then install fresh. Your notes live in app storage — back up any export PDFs first if you have them.

**Build it yourself:**
```bash
git clone https://github.com/HBAABA119/UltiNote.git
cd UltiNote
# needs Android SDK platform 36 + JDK 17
./gradlew :app:assembleDebug
# APK lands in app/build/outputs/apk/debug/
```

No `google-services.json` needed. No `.env` needed unless you're hacking on the future AI stuff.

---

## How I use it for school

- **Math:** Engineering grid + fountain pen on Firm, lasso to duplicate a diagram, export PDF before homework is due
- **Lecture PDFs:** Import slides, highlight in class with stylus-only mode on, add a photo of the board when the prof moves too fast
- **Revision:** Tags like `bio, midterm` — search actually finds them. Pin the current chapter to the top.
- **Planner:** Calendar → tap today → New Day Planner. Done.

---

## Permissions (why each one)

UltiNote uses scoped storage + the system file picker, so on Android 10+ it needs basically nothing:
- `READ_MEDIA_IMAGES` (Android 13+) — only so the photo picker is nicer. The picker itself (SAF) doesn't need permission.
- `READ/WRITE_EXTERNAL_STORAGE` (only on Android ≤ 9, maxSdkVersion capped) — old devices importing PDFs
- `VIBRATE` — tiny haptics when you switch tools / snap a shape
- That's it. No `MANAGE_EXTERNAL_STORAGE`, no location, no contacts. Your notes live in app-specific `UltiNote/`, `UltiNoteImages/`, `UltiNoteExports/`.

---

## Project layout (for nerds)

```
app/src/main/java/com/ultinote/app/
  canvas/       ink engine (NoteCanvasView, splines, shape snap, ruler, paper)
  data/model/   Room entities + PhotoAnnotation, StickerCatalog, PressureSensitivity
  data/local/   Room DB (ultinote_notes.db v2) + DataStore prefs
  pdf/          PdfRenderer import/render/export/share
  ui/screens/   Library, NoteEditor, CalendarPlanner, Settings
  ui/components/toolbar, liquid glass, sheets, layers, thumbnails
  ui/theme/     6 palettes + glass theme + fonts
```

Stack: Kotlin 2.2, Compose (BOM 2026.04.01), Room 2.8.1 + KSP, DataStore, Navigation-Compose, PdfRenderer, ML Kit Digital Ink 19.0.0. Min SDK 24, target/compile 36.

---

## Roadmap (honest)

**Done in v1.1.0:**
pressure tuning, precision eraser, lasso duplicate/delete, photo insert, page duplicate/reorder, pin + tags + tag search, full-template PDF export (all 10 papers, real STAR export), sticker emoji fix, DataStore persistence, scoped-storage permissions, tablet portrait/landscape layouts, Apple-style glass polish, CI that builds debug APKs + signed releases.

**Coming soon (tracked, not faked):**
- AI Study Companion (Gemini) — summarize, practice questions, diagrams. UI exists, backend stubbed on purpose.
- Cloud sync + backup across phone/tablet. Right now it's local-first by design.

If you want something else for school, open an issue and tell me how you'd actually use it in class. That's how features get in here.

---

## Contributing / dev

- CI runs on every push: unit tests + debug APK artifact. Tag `v*` → signed release APK + auto changelog.
- Release signing uses `UPLOAD_KEYSTORE_B64` / `STORE_PASSWORD` / `KEY_PASSWORD` secrets. No secret = unsigned release build, still uploads.
- Code style: official Kotlin. Keep composables small, keep canvas code off the main thread.

Made for class, not for demos. If it survives a full school day on a cheap tablet, it's good enough.
