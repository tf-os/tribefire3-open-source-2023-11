import { DEFAULT_ENTRY_VALUE, findEntry } from "@app-configuration";

interface CreateEntryFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                // i.e. 'location':      localization['location']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values']
  entryModel: any;                                  // i.e. AppLocalizationEntry
}

/**
 * @returns a function that creates an entry
 *          in the appConfiguration[appConfiguration_CollectionsPropertyName]
 */
export const createEntryFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
    entryModel,                               // i.e. AppLocalizationEntry
  }: CreateEntryFactoryArguments
) => function(targetDictionaryIdentifier: string, key: string, value?: string) {
  const appConfiguration = appConfigurationGetter();
  const topLevelEntriesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();
  if (topLevelEntriesArray?.length > 0) {
    const session = topLevelEntriesArray[0].Session();
    const nestedTransaction = session.getTransaction().beginNestedTransaction();
    topLevelEntriesArray.forEach((topLevelEntity) => {
      const entries = topLevelEntity[collectionValuePropertyName];
      const nextValue = topLevelEntity[collectionKeyPropertyName] === targetDictionaryIdentifier
        ? value || DEFAULT_ENTRY_VALUE
        : DEFAULT_ENTRY_VALUE;

      const isEntityCreated = internalEditOrCreateOneEntry(
        topLevelEntity.Session(),
        entries,
        entries?.toArray(),
        key,
        nextValue,
        entryModel,
      );

      if (isEntityCreated) {
        // make sure tf listeners are updated
        topLevelEntity[collectionValuePropertyName] = topLevelEntity[collectionValuePropertyName];
      }
    });
    nestedTransaction.commit();
  }
}

/**
 * @returns true if an entry was created or existing one was modified
 */
export function internalEditOrCreateOneEntry(
  session: any,
  entries: any,
  entriesArray: any,
  key: string,
  value: string,
  entryModel: any,
): boolean {
  let entry = findEntry(entriesArray, key);
  // add "value" to selected AppLocalizationEntry and " " to every other AppLocalizationEntry
  // console.log('%c:internalEditOrCreateOneEntry', 'color: orange', {key, value})

  if (!entry) {
    entry = session.create(entryModel);
    entry.key = key;
    entry.value = value;
    entries.add(entry);
    return true;
  } else if (entry.value !== value) {
    entry.value = value;
    return true;
  } else {
    return false;
  }
}
