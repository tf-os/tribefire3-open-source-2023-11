import { get } from "svelte/store";

interface RenameTopLevelKeyFactoryArguments {
  appConfigurationGetter: Function;                       // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string;       // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                      // i.e. 'location':      localization['location']
  removeTopLevelEntityFunction: (key: string) => boolean; // i.e. removeLocalizationFunction
}

/**
 * @returns a function that creates renames collection identifier in the appConfiguration
 *   if newKeyName.trim() is empty, collection will be removed
 */
export const renameTopLevelKey = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    removeTopLevelEntityFunction,             // i.e. removeLocalizationFunction
  }: RenameTopLevelKeyFactoryArguments
) => (oldKeyName: string, newKeyName: string): boolean => {
  if (newKeyName && newKeyName.trim()) {
    if (newKeyName.trim() !== oldKeyName?.trim() ?? '') {
      const appConfiguration = appConfigurationGetter();
      const existingEntriesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();
      const existingEntry = existingEntriesArray?.find(entry => entry[collectionKeyPropertyName] === newKeyName);

      if (existingEntry) {
        alert(`Key "${newKeyName}" already exists!`);
      } else {
        const entryToChange = existingEntriesArray?.find(entry => entry[collectionKeyPropertyName] === oldKeyName);

        if (entryToChange) {
          entryToChange[collectionKeyPropertyName] = newKeyName;
          return true;
        }
      }
    }
    return false;
  } else {
    return removeTopLevelEntityFunction(oldKeyName);
  }
}
