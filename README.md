# NoDoomScroll

Blocks **YouTube Shorts** (YouTube's version of reels). Comes in two parts:

- **Android app**: for the YouTube app on your phone. See [Android app](#android-app) below.
- **Browser extension**: for YouTube in Chrome, Edge, Brave or Firefox on a computer.

## Android app

### What it does

Whenever the Shorts player opens in the YouTube app (from the Shorts tab, a Short in your feed, or a shared link), NoDoomScroll presses Back for you and shows a "Shorts blocked" message. You can't scroll through Shorts anymore.

Android doesn't let one app change what another app shows, so Shorts **thumbnails can still appear** in your home feed. Tapping one just closes again.

The app uses Android's accessibility feature, limited to the YouTube app only. It has no internet permission, so it can't send anything anywhere.

### Install

1. On your phone, open **[the latest release](../../releases/latest)** and download **NoDoomScroll.apk**.
2. Open the downloaded file. If Android asks, allow your browser to install apps.
3. Open **NoDoomScroll**, tap **Turn on in Accessibility settings**, choose NoDoomScroll and switch it on.

**"Restricted setting" message?** Android 13 and later blocks this for apps not from the Play Store. Go to **Settings → Apps → NoDoomScroll**, tap **⋮** (top right) → **Allow restricted settings**, then turn it on again.

**Stops working after a while?** Some phones (Xiaomi, Samsung, Oppo, etc.) kill background apps. Set NoDoomScroll's battery use to **Unrestricted**.

The app has a **Pause blocking** switch and counts how many Shorts it has blocked.

### Building it yourself

GitHub Actions builds the APK on every change under `android/` (see `.github/workflows/android.yml`) and publishes it as a release. Locally: `gradle -p android assembleRelease` with the Android SDK installed.

Each GitHub build is signed with a new temporary key, so to install a newer build you may need to uninstall the old one first. To keep one key, add a keystore and set `NDS_KEYSTORE`, `NDS_KEYSTORE_PASSWORD`, `NDS_KEY_ALIAS` and `NDS_KEY_PASSWORD` in the workflow.

## Browser extension

### What it does

- **Hides Shorts everywhere they're suggested:** the Shorts shelf on the home page, Shorts shelves in search results and next to videos, single Shorts mixed into feeds, the "Shorts" button in the sidebar, the "Shorts" filter chip, and the Shorts tab on channel pages.
- **Opens Shorts links as normal videos:** if you open a `youtube.com/shorts/...` link (from a friend, a chat or a search), you're sent to the regular player at `youtube.com/watch?v=...`, so you can't swipe on to the next Short.
- **Popup with two switches:** click the extension icon to turn either feature on or off. Changes apply to open YouTube tabs right away.

Works on `www.youtube.com` and `m.youtube.com`. It collects no data and needs only the `storage` permission (for your two settings).

### Install (browser)

#### Chrome, Edge, Brave, Opera (any Chromium browser)

1. Download this repository (green **Code** button → **Download ZIP**) and unzip it.
2. Open `chrome://extensions` (or `edge://extensions`).
3. Turn on **Developer mode** (top right).
4. Click **Load unpacked** and choose the `extension` folder.
5. Pin the extension and reload any open YouTube tabs.

#### Firefox

1. Open `about:debugging#/runtime/this-firefox`.
2. Click **Load Temporary Add-on…** and choose `extension/manifest.json`.

Firefox removes temporary add-ons when it restarts. To keep it installed for good, package the `extension` folder as a zip and sign it through [addons.mozilla.org](https://addons.mozilla.org/developers/) (you can sign it for your own use only, without listing it).

### How it works

| File | Job |
| --- | --- |
| `extension/manifest.json` | Extension setup (Manifest V3, Chrome and Firefox). |
| `extension/content.css` | CSS rules that hide Shorts elements. Loads before the page draws, so Shorts never flash on screen. |
| `extension/content.js` | Hides Shorts that can only be found by their label (such as the "Shorts" chip), watches for new content as you scroll, and redirects `/shorts/<id>` to `/watch?v=<id>`. |
| `extension/popup/` | The settings popup. |

### If Shorts come back

YouTube changes its page layout from time to time, and a change can stop a rule from matching. To fix one: right-click the Short that reappeared → **Inspect**, find the element that wraps it, and add a selector for it to `extension/content.css`.
