import { AppLocalizationEntry, findEntry } from "@app-configuration";

interface RenameEntryKeyFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string,                // i.e. 'location':      localization['location']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values']
  entryModel: any;                                  // i.e. AppLocalizationEntry
}

/**
 * @returns a function that changes entry key
 *          in each appConfiguration[appConfiguration_CollectionsPropertyName]
 *          nested function returns true when successful or false when not (i.e. newKey already exists)
 */
export const renameEntryKeyFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
    entryModel,                               // i.e. AppLocalizationEntry
  }: RenameEntryKeyFactoryArguments
) => function(oldKey: string, newKey: string): boolean {
  const appConfiguration = appConfigurationGetter();
  const topLevelEntriesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();

  if (topLevelEntriesArray.length > 0) {
    const entitiesArrayOfArrays = topLevelEntriesArray.map((topLevelEntity) => {
      const name = topLevelEntity[collectionKeyPropertyName];
      const entries = topLevelEntity[collectionValuePropertyName];
      return [name, entries, entries?.toArray(), topLevelEntity];
    })

    // check if newKey exists if any of the collections
    const offendingCollections = entitiesArrayOfArrays
      .filter(([_name, _entries, entriesArray]) => findEntry(entriesArray, newKey));

    if (offendingCollections.length > 0) {
      const message = offendingCollections.length === topLevelEntriesArray.length
        ? `Key "${newKey}" already exists in all collections!`
        : `Key "${newKey}" already exists in collections: ${
          offendingCollections.map(([name]) => `"${name}"`).join(', ')
        }!`;
      alert(message);
    } else {
      const session = topLevelEntriesArray[0].Session();
      const nestedTransaction = session.getTransaction().beginNestedTransaction();
      entitiesArrayOfArrays.forEach(([name, entries, entriesArray, topLevelEntity]) => {
        const isEntryRenamed =
          internalRenameKey(
            session,
            entries,
            entriesArray,
            oldKey,
            newKey,
            entryModel
          );

        if (isEntryRenamed) {
          topLevelEntity[collectionValuePropertyName] = topLevelEntity[collectionValuePropertyName];
        }
      });
      nestedTransaction.commit();
      return true;
    }
  }

  return false;
}

export function internalRenameKey(
  session: any,
  entries: any,
  entriesArray: any,
  oldKey: string,
  newKey: string,
  entryModel: any,
): boolean {
  console.log('%c:internalRenameKey', 'color: orange', { oldKey, newKey });
  let entry = findEntry(entriesArray, oldKey);
  if (!entry) {
    entry = session.create(entryModel);
    entry.key = newKey;
    entries.add(entry);
  } else {
    entry.key = newKey;
  }
  return true;
}
