# Wyng — Session Change Summary (Sep 8, 2026)

## Client (this repo) — all compiled OK (`:TMessagesProj:compileReleaseJavaWithJavac` passed)

### Bugfixes ported from InuGram
1. **Stuck-cancel fix** — `FileLoadOperation.cancelRequests` never ran
   `fullyCancelled` when no request needed a cancel callback → downloads
   waited forever (blank sticker/media until force-stop).
   `TMessagesProj/src/main/java/org/telegram/messenger/FileLoadOperation.java`
2. **Dead animated drawable** — cancelled decodes no longer post dead
   drawables; cancelled streams are reset before decoder creation.
   - `ImageLoader.java` (recycle cancelled webm sticker)
   - `AnimatedFileDrawable.java` (stream reset + no-frame fallback)
3. **Media cancel race** — `reqId` was never stored on sending messages,
   so cancel-sending could not cancel the request (stuck sending media).
   - `SendMessagesHelper.java` (uploadMultiMedia all 5 branches + group send)
   - `ChatActivity.java` (cancel group send once instead of N times)

### Appearance / UI (new `org.telegram.inugram.InuConfig`)
4. **Show seconds** in message timestamps — `LocaleController.getFormatterDay`
   → `getFormatterDayWithSeconds` (on by default; `show_seconds` in
   `inugram_config` prefs).
5. **Animation multiplier** — `ActionBarLayout` nav animation,
   `AnimatedFloat` transitions, `ProfileActivity` expand scroll all honor
   `animation_multiplier` (default 1.0).
6. **Instant tap** — `RecyclerListView.instantClick = true` (no click delay).
7. **Background parallax off-switch** — `SizeNotifierFrameLayout.checkMotion`
   honors `disable_bg_parallax` (default off = parallax stays on).

### Build config
8. **arm64-only** — `abiFilters "arm64-v8a"` in all 4 app modules
   (App, AppTests, AppHuawei, AppHockeyApp).

## Build
```
cd /home/taha/Projects/Telegram
./gradlew assembleAfatRelease --no-daemon
# output: TMessagesProj_App/build/outputs/apk/afat/release/app.apk
```
Uninstall the old build before installing this one.

## Server (gramsrv) — separate deploy, diffs saved at:
- `/home/taha/gramsrv_getfile_thumb_fallback.diff`
  (thumb→full-doc fallback; fixes ~3700 declared-thumbs-without-blob
  → `upload.getFile LOCATION_INVALID` blank-media storms)
- quote_entities fix in `internal/rpc/convert_messages.go`
  (`tgMessageEntitiesOrEmpty`; fixes `sendMessage`/`getHistory` failing to
  encode when a reply has quote text but nil quote_entities — the
  "two separate worlds" send/receive bug).
  Regenerate with: `cd /tmp/opencode/gramsrv && git diff internal/rpc/convert_messages.go`

Both server fixes compile clean (`go build ./internal/rpc/ ./internal/app/files/`).
