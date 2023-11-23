import { findEntry } from "@app-configuration";

interface EditEntryValueFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                // i.e. 'location':      localization['location']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values']
}

/**
 * @returns a function that edits entry value by a key
 *          in each appConfiguration[appConfiguration_CollectionsPropertyName]
 */
export const editEntryValueFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
  }: EditEntryValueFactoryArguments
) => function(targetDictionaryIdentifier: string, key: string, value: string) {
  const appConfiguration = appConfigurationGetter();
  const topLevelEntitiesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();

  const topLevelEntity = topLevelEntitiesArray.find(entry => entry[collectionKeyPropertyName] === targetDictionaryIdentifier);
  if (topLevelEntity) {
    const entries = topLevelEntity[collectionValuePropertyName];
    const isEntryEdited = internalEditOneEntryValue(entries, entries?.toArray(), key, value);
    if (isEntryEdited) {
      topLevelEntity[collectionValuePropertyName] = topLevelEntity[collectionValuePropertyName];
    }
  }
}

/**
 * @returns true if an entry was edited
 */
export function internalEditOneEntryValue(entries: any, entriesArray: any, key: string, value: string): boolean {
  console.log('%c:internalEditOneEntryValue', 'color: orange', {key, value})
  let entry = findEntry(entriesArray, key);
  if (entry.value !== value) {
    entry.value = value;
    return true;
  }
  return false;
}
