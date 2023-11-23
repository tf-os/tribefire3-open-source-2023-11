import { deleteEntity } from "@app-configuration";

interface RemoveTopLevelEntityFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                // i.e. 'location':      localization['location']
}

/**
 * @returns a function that creates removes a collection in the appConfiguration
 */
export const removeTopLevelEntityFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
  }: RemoveTopLevelEntityFactoryArguments
) => (keyToRemove: string): boolean => {
  const appConfiguration = appConfigurationGetter();
  const existingEntries = appConfiguration[appConfiguration_CollectionsPropertyName]
  const existingEntriesArray = existingEntries?.toArray();
  const index = existingEntriesArray?.findIndex(entry => entry[collectionKeyPropertyName] === keyToRemove) ?? -1;
  if (index >= 0) {
    const entry = existingEntriesArray[index];
    // remove all children entities
    const children = entry?.values?.toArray();
    if (children?.length > 0) {
      // entry.values.removeAll();
      children.forEach(deleteEntity);
    }
    // remove parent
    existingEntries.removeAtIndex(index);
    deleteEntity(entry);
    return true;
  }
  return false;
}
