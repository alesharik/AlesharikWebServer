'use strict';
console.log(dashboard.menuPluginHandler);
var menuPluginsEditorHandler = new MenuPluginsEditorHandler(dashboard.menuPluginHandler);
document.addEventListener("loadingContentEnded", () => {
    MenuUtils.sortable(true);
    menuPluginsEditorHandler.redraw();
});
document.addEventListener("finalizeContentScript", () => {
    MenuUtils.sortable(false);
    menuPluginsEditorHandler.destroy();
    document.removeEventListener("finalizeContentScript", this);
});