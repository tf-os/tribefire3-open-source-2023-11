import { DEFAULT_REPLACE_THEME_ON_ADD } from "./ThemeMap";
import { addTheme } from "./addThemes";

/**
 * Adds AppTheme instance to themeMap
 */
export const addAppTheme = (
  theme: $T.tribefire.extension.appconfiguration.model.AppTheme,
  replaceExisting: boolean = DEFAULT_REPLACE_THEME_ON_ADD,
): void => {
  let valuesRecord = {};

  const valuesArray = Array.isArray(theme.values)
  ? theme.values
  : theme.values?.toArray();

  valuesArray.forEach(({key, value}) => {
    valuesRecord[key] = value;
  })

  if (Array.from(Object.keys(valuesRecord)).length > 0) {
    // console.log('addAppTheme', theme.theme, valuesRecord)
    addTheme(theme.theme, valuesRecord, replaceExisting);
  } else {
    console.warn(`Theme "${theme.theme}" seems to be empty`);
  }
}

/**
 * Adds multiple instances of AppTheme to themeMap
 */
export const addAppThemes = (
  themes: $T.tribefire.extension.appconfiguration.model.AppTheme[],
  replaceExisting: boolean = DEFAULT_REPLACE_THEME_ON_ADD,
): void => {
  if (themes) {
    themes.forEach(theme => addAppTheme(theme, replaceExisting));
  }
}
