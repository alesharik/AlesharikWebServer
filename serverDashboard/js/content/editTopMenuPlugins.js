'use strict';
var menuPluginsEditorHandler = new MenuPluginsEditorHandler(dashboard.menuPluginHandler);
events.addEventListener("loadingContentEnded", () => {
    MenuUtils.sortable(true);
    menuPluginsEditorHandler.redraw();
});
events.addEventListener("finalizeContent", () => {
    MenuUtils.sortable(false);
    menuPluginsEditorHandler.destroy();
    events.removeEventListener("finalizeContentScript", this);
});