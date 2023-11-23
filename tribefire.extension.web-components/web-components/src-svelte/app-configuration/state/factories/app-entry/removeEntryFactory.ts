import { deleteEntity, findEntry } from "@app-configuration";

interface RemoveEntryFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values']
}

/**
 * @returns a function that removes entries by a key
 *          in each appConfiguration[appConfiguration_CollectionsPropertyName]
 */
export const removeEntryFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
  }: RemoveEntryFactoryArguments
) => function(key: string) {
  const appConfiguration = appConfigurationGetter();
  const topLevelEntriesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();

  if (topLevelEntriesArray.length > 0) {
    const session = topLevelEntriesArray[0].Session();
    const nestedTransaction = session.getTransaction().beginNestedTransaction();
    topLevelEntriesArray.forEach((topLevelEntity) => {
      const entries = topLevelEntity[collectionValuePropertyName];
      const isEntryRemoved = internalRemoveOneEntry(entries, entries?.toArray(), key);
      if (isEntryRemoved) {
        topLevelEntity[collectionValuePropertyName] = topLevelEntity[collectionValuePropertyName];
      }
    });
    nestedTransaction.commit();
  }
}

/**
 * @returns true if an entry was removed
 */
export function internalRemoveOneEntry(entries: any, entriesArray: any, key: string): boolean {
  console.log('%c:internalRemoveOneEntry', 'color: orange', key)
  let entry = findEntry(entriesArray, key);
  if (entry) {
    entries.remove(entry);
    deleteEntity(entry);
    return true;
  }
  return false;
}
