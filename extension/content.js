// NoDoomScroll – content script for YouTube.
// 1. Turns on the CSS rules in content.css (class "nds-on" on <html>).
// 2. Hides Shorts things that can only be found by their text (filter chips, channel tab).
// 3. Sends /shorts/<id> pages to the normal /watch?v=<id> player (optional).
(() => {
  "use strict";

  const api = globalThis.browser ?? globalThis.chrome;
  const DEFAULTS = { enabled: true, redirect: true };
  const settings = { ...DEFAULTS };
  const root = document.documentElement;

  // Hide straight away so Shorts never flash on screen while settings load.
  root.classList.add("nds-on");

  // ---------- Redirect /shorts/<id> -> /watch?v=<id> ----------
  const SHORTS_PATH = /^\/shorts\/([A-Za-z0-9_-]{6,})/;

  function watchUrlFor(url) {
    const m = SHORTS_PATH.exec(url.pathname);
    if (!m) return null;
    const out = new URL("/watch", url.origin);
    out.searchParams.set("v", m[1]);
    return out.href;
  }

  function redirectIfShorts() {
    if (!settings.enabled || !settings.redirect) return;
    const target = watchUrlFor(new URL(location.href));
    if (target) location.replace(target);
  }

  // Catch clicks on Shorts links before YouTube's own router handles them.
  document.addEventListener(
    "click",
    (event) => {
      if (!settings.enabled || !settings.redirect) return;
      if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      const link = event.target instanceof Element && event.target.closest("a[href]");
      if (!link) return;
      const target = watchUrlFor(new URL(link.href, location.href));
      if (!target) return;
      event.preventDefault();
      event.stopImmediatePropagation();
      location.assign(target);
    },
    true
  );

  // YouTube is a single-page app, so also check after its own navigations.
  for (const evt of ["yt-navigate-start", "yt-navigate-finish", "popstate"]) {
    window.addEventListener(evt, redirectIfShorts, true);
  }
  let lastPath = location.pathname;
  setInterval(() => {
    if (location.pathname !== lastPath) {
      lastPath = location.pathname;
      redirectIfShorts();
    }
  }, 300);

  // ---------- Text-based hiding ----------
  const isShortsLabel = (el) => (el.textContent || "").trim().toLowerCase() === "shorts";

  function tagTextMatches(scope) {
    if (!settings.enabled) return;
    // "Shorts" filter chips on home / search / subscriptions.
    scope
      .querySelectorAll?.("yt-chip-cloud-chip-renderer:not(.nds-hidden), ytm-chip-cloud-chip-renderer:not(.nds-hidden), chip-shape:not(.nds-hidden)")
      .forEach((chip) => {
        if (isShortsLabel(chip)) chip.classList.add("nds-hidden");
      });
    // Channel page "Shorts" tab (older layout without a tab-title attribute).
    scope.querySelectorAll?.("tp-yt-paper-tab:not(.nds-shorts-tab), yt-tab-shape:not(.nds-hidden)").forEach((tab) => {
      if (isShortsLabel(tab)) tab.classList.add(tab.localName === "tp-yt-paper-tab" ? "nds-shorts-tab" : "nds-hidden");
    });
    // Section titles that say "Shorts" (e.g. new search / watch-page shelves).
    scope.querySelectorAll?.("ytd-rich-shelf-renderer #title, ytd-shelf-renderer #title, h2 #title").forEach((title) => {
      if (!isShortsLabel(title)) return;
      const shelf = title.closest("ytd-rich-section-renderer, ytd-shelf-renderer, ytd-rich-shelf-renderer, ytd-item-section-renderer");
      if (shelf) shelf.classList.add("nds-hidden");
    });
  }

  let scheduled = false;
  const observer = new MutationObserver(() => {
    if (scheduled) return;
    scheduled = true;
    requestAnimationFrame(() => {
      scheduled = false;
      tagTextMatches(document);
    });
  });

  function startObserving() {
    observer.observe(root, { childList: true, subtree: true });
    tagTextMatches(document);
  }

  // ---------- Settings ----------
  function apply() {
    root.classList.toggle("nds-on", settings.enabled);
    redirectIfShorts();
    if (settings.enabled) tagTextMatches(document);
  }

  try {
    api.storage.sync.get(DEFAULTS, (stored) => {
      Object.assign(settings, stored);
      apply();
    });
    api.storage.onChanged.addListener((changes, area) => {
      if (area !== "sync") return;
      for (const [key, { newValue }] of Object.entries(changes)) {
        if (key in settings) settings[key] = newValue;
      }
      apply();
    });
  } catch {
    // Storage unavailable (e.g. test harness): keep defaults.
    apply();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", startObserving, { once: true });
  } else {
    startObserving();
  }
})();
