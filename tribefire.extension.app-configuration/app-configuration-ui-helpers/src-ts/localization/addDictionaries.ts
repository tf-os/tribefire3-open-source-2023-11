import { addMessages } from "svelte-i18n";

export const addDictionary = addMessages;

export const addDictionaries = (
  dictionaries: Record<string, Record<string, any>>
): void => {
  Object.entries(dictionaries).forEach(([locale, dictionary]) => {
    addDictionary(locale, dictionary);
  })
}
