export { default as IntlMessageFormat } from "intl-messageformat";
export { init as i18nInit, getLocaleFromNavigator, locale as i18nLocale, number as i18nNumber } from "svelte-i18n";
export type { LocaleDictionary, LocalesDictionary } from "svelte-i18n/types/runtime/types";

export * from "./localization/validateI18nString";
export * from "./localization/addDictionaries";
export * from "./localization/addAppLocalizations";
export * from "./localization/InternationalFormatter";
export * from "./localization/translate";
export * from "./theme/activeThemeName";
export * from "./theme/addAppThemes";
export * from "./theme/addThemes";
export * from "./theme/injectTheme";
export * from "./theme/themeAsRecordStore";
export * from "./theme/themeAsStyleStore";
