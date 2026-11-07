# NoDoomScroll

A browser extension that blocks **YouTube Shorts** (YouTube's version of reels) so they stop getting suggested to you.

## What it does

- **Hides Shorts everywhere they're suggested:** the Shorts shelf on the home page, Shorts shelves in search results and next to videos, single Shorts mixed into feeds, the "Shorts" button in the sidebar, the "Shorts" filter chip, and the Shorts tab on channel pages.
- **Opens Shorts links as normal videos:** if you open a `youtube.com/shorts/...` link (from a friend, a chat or a search), you're sent to the regular player at `youtube.com/watch?v=...`, so you can't swipe on to the next Short.
- **Popup with two switches:** click the extension icon to turn either feature on or off. Changes apply to open YouTube tabs right away.

Works on `www.youtube.com` and `m.youtube.com`. It collects no data and needs only the `storage` permission (for your two settings).

## Install

### Chrome, Edge, Brave, Opera (any Chromium browser)

1. Download this repository (green **Code** button → **Download ZIP**) and unzip it.
2. Open `chrome://extensions` (or `edge://extensions`).
3. Turn on **Developer mode** (top right).
4. Click **Load unpacked** and choose the `extension` folder.
5. Pin the extension and reload any open YouTube tabs.

### Firefox

1. Open `about:debugging#/runtime/this-firefox`.
2. Click **Load Temporary Add-on…** and choose `extension/manifest.json`.

Firefox removes temporary add-ons when it restarts. To keep it installed for good, package the `extension` folder as a zip and sign it through [addons.mozilla.org](https://addons.mozilla.org/developers/) (you can sign it for your own use only, without listing it).

## How it works

| File | Job |
| --- | --- |
| `extension/manifest.json` | Extension setup (Manifest V3, Chrome and Firefox). |
| `extension/content.css` | CSS rules that hide Shorts elements. Loads before the page draws, so Shorts never flash on screen. |
| `extension/content.js` | Hides Shorts that can only be found by their label (such as the "Shorts" chip), watches for new content as you scroll, and redirects `/shorts/<id>` to `/watch?v=<id>`. |
| `extension/popup/` | The settings popup. |

## If Shorts come back

YouTube changes its page layout from time to time, and a change can stop a rule from matching. To fix one: right-click the Short that reappeared → **Inspect**, find the element that wraps it, and add a selector for it to `extension/content.css`.
