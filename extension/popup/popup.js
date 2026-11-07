const api = globalThis.browser ?? globalThis.chrome;
const DEFAULTS = { enabled: true, redirect: true };
const enabled = document.getElementById("enabled");
const redirect = document.getElementById("redirect");

function render(s) {
  enabled.checked = s.enabled;
  redirect.checked = s.redirect;
  redirect.disabled = !s.enabled;
}

api.storage.sync.get(DEFAULTS, render);

enabled.addEventListener("change", () => {
  api.storage.sync.set({ enabled: enabled.checked });
  redirect.disabled = !enabled.checked;
});
redirect.addEventListener("change", () => api.storage.sync.set({ redirect: redirect.checked }));
