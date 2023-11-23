import { addDictionary } from "./addDictionaries";
import { validateI18nString } from "./validateI18nString";

export const addAppLocalization = (
  localization: $T.tribefire.extension.appconfiguration.model.AppLocalization,
): void => {
  let valuesRecord = {};

  const valuesArray = Array.isArray(localization.values)
    ? localization.values
    : localization.values?.toArray();

  valuesArray.forEach(({key, value}) => {
    const i18nValue = value?.replaceAll('`', '\'');
    const validationError = validateI18nString(i18nValue);
    if (validationError) {
      console.error(`Localization.${localization.location}["${key}"] parsing error: `, validationError);
      valuesRecord[key] = key;
    } else {
      valuesRecord[key] = i18nValue;
    }
  })
  if (Array.from(Object.keys(valuesRecord)).length > 0) {
    // console.log('addAppLocalization', localization.location, valuesRecord)
    addDictionary(localization.location, valuesRecord as any);
  } else {
    console.warn(`Localization "${localization.location}" seems to be empty`);
  }
}

export const addAppLocalizations = (
  localizations: $T.tribefire.extension.appconfiguration.model.AppLocalization[],
): void => {
  if (localizations) {
    localizations.forEach(addAppLocalization);
  }
}
