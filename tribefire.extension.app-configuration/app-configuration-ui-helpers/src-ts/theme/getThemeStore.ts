import { writable } from "svelte/store";
import type { Readable } from "svelte/store";

import { ThemeRecord } from "./types";
import { themeMap } from "./ThemeMap";

export const getThemeStore = (themeName: string): Readable<ThemeRecord> => {
  let themeStore = themeMap.get(themeName);

  if (!themeStore) {
    themeStore = writable({});
    themeMap.set(themeName, themeStore);
  }

  return themeStore;
}
