import { derived, Readable } from "svelte/store";
import { getThemeStore } from "./getThemeStore";

/**
 * @param themeName: string
 * @returns store that emits style string generated from theme, where theme.name === @themeName
 * 
 * Usage:
 * 
 * <script>
 *   activeThemeName.set('light');
 *   $: styleStore = themeAsStyleStore($activeThemeName);
 * </script>
 * 
 * <main style={$styleStore}/>
 */
export const themeAsStyleStore = (themeName: string): Readable<string> => {
  return derived(
    getThemeStore(themeName),
    (theme) => {
      const style = Object.entries(theme)
        .filter(([_key, value]) => typeof value === 'string' ? value.trim().length > 0 : true)
        .map(([key, value]) => key.startsWith('--')
          ? [key, value]
          : key.startsWith('-')
            ? ['-' + key, value]
            : ['--' + key, value]
        )
        .map(([key, value]) => `${key}: ${value}`)
        .join(';');

      return style
        ? `;${style};`
        : '';
    }
  );
}
