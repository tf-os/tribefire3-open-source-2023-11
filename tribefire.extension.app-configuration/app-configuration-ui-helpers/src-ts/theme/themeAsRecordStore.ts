import { derived, Readable } from "svelte/store";
import { getThemeStore } from "./getThemeStore";
import { ThemeRecord } from "./types";

/**
 * @param themeName: string
 * @returns store that emits ThemeRecord, where theme.name === @themeName
 */
export const themeAsRecordStore = (themeName: string): Readable<ThemeRecord> => {
  return derived(
    getThemeStore(themeName),
    (theme) => theme,
  );
}
