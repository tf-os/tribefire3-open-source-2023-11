import { get, readable, writable } from "svelte/store";

import { ThemeRecord } from "./types";
import { DEFAULT_REPLACE_THEME_ON_ADD, themeMap } from "./ThemeMap";

export const addTheme = (
  themeName: string,
  theme: ThemeRecord,
  owerwriteExisting: boolean = DEFAULT_REPLACE_THEME_ON_ADD,
): void => {
  let themeStore= themeMap.get(themeName);

  if (!themeStore) {
    themeStore = writable({...theme});
    themeMap.set(themeName, themeStore);
  } else if (owerwriteExisting) {
      themeStore.set({...theme});
  } else {
    themeStore.update(prevTheme => ({
      ...prevTheme,
      ...theme,
    }));
  }
}

export const addThemes = (
  themes: Record<string, Record<string, any>>,
  owerwriteExisting: boolean = DEFAULT_REPLACE_THEME_ON_ADD,
): void => {
  Object.entries(themes).forEach(([name, theme]) => {
    addTheme(name, theme, owerwriteExisting);
  })
}
