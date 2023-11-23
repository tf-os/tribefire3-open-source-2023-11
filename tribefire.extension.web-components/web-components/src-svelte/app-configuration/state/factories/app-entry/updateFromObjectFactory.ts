import {
  arrayMinusSet,
  internalEditOrCreateOneEntry,
  internalEditOneEntryValue,
  internalRemoveOneEntry,
  DEFAULT_ENTRY_VALUE,
} from "@app-configuration";

interface UpdateFromObjectFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                // i.e. 'location':      localization['location']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values'
  entryModel: any;                                  // i.e. AppLocalizationEntry
}

const DefaultUpdateFromObjectOptions = {
  canAddKey: true,
  canEditKey: true,
}

/**
 * @returns a function that
 *   - collections is equal to appConfigurationGetter()[appConfiguration_CollectionsPropertyName];
 *   - adds non existing keys to every collection
 *   - remove keys from every collection that do not exist in nextDictionary
 *   - sets values in selectedCollection from nextDictionary
 */
export const updateFromObjectFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
    entryModel,                               // i.e. AppLocalizationEntry
  }: UpdateFromObjectFactoryArguments
) => function(
  targetDictionaryIdentifier: string,
  nextDictionary: Record<string, string>,
  options = DefaultUpdateFromObjectOptions,
): boolean {
  const {
    canAddKey = DefaultUpdateFromObjectOptions.canAddKey,
    canEditKey = DefaultUpdateFromObjectOptions.canEditKey,
  } = options;

  const appConfiguration = appConfigurationGetter();
  const topLevelEntitiesArray = appConfiguration[appConfiguration_CollectionsPropertyName]?.toArray();
  const topLevelEntity = topLevelEntitiesArray.find(entity => entity[collectionKeyPropertyName] === targetDictionaryIdentifier);
  const entries = topLevelEntity[collectionValuePropertyName];
  const entriesArray = entries?.toArray();
  const currentKeys = entriesArray.map(entry => entry.key);
  const currentKeysSet = new Set(currentKeys);

  const nextEntries = Object.entries(nextDictionary);
  const nextKeysSet = new Set(Object.keys(nextDictionary));

  const keysToRemove = arrayMinusSet(currentKeys, nextKeysSet);

  let confirmed: boolean = true;
  let confirmationMessage: string = '';
  let skippedChanges = false;

  if (!canAddKey) {
    const keysToAdd = nextEntries
      .map(([key]) => key)
      .filter(key => !currentKeysSet.has(key));
    if (keysToAdd.length > 0) {
      skippedChanges = true;
      confirmationMessage = `You are trying to add ${
        keysToAdd.length
      } key(s) to all collections:\n${
        createBulletList(keysToAdd)
      }\nbut have no privileges.\nKeys will not be added.\n\n`;
    }
  }

  if (keysToRemove.length > 0) {
    if (!canEditKey) {
      skippedChanges = true;
      confirmationMessage += `You are trying to remove ${
        keysToRemove.length
      } key(s) from all collections:\n${
        createBulletList(keysToRemove)
      }\nbut have no privileges.\nKeys will not be removed.\n\n`;
    } else {
      confirmationMessage += `${
        keysToRemove.length
      } key(s) will be removed from all collections:\n${
        createBulletList(keysToRemove)
      }\n\n`;
    }
  }

  if (confirmationMessage) {
    confirmed = confirm(`${confirmationMessage}Continue?`);
  }

  if (confirmed) {
    const session = topLevelEntitiesArray[0].Session();
    const nestedTransaction = session.getTransaction().beginNestedTransaction();

    topLevelEntitiesArray.forEach((topLevelEntity) => {
      const entries = topLevelEntity[collectionValuePropertyName];
      const entriesArray = entries?.toArray();
      const currentKeys = entriesArray.map(entry => entry.key);
      const currentKeysSet = new Set(currentKeys);

      let changesCount = 0;

      // removing keys
      if (canEditKey) {
        const keysToRemove = arrayMinusSet(currentKeys, nextKeysSet);
        keysToRemove.forEach(key => {
          const isRemoved = internalRemoveOneEntry(entries, entriesArray, key)
          if (isRemoved) changesCount++;
        });
      }

      const isLocalizationBeingEditedByUser = topLevelEntity[collectionKeyPropertyName] === targetDictionaryIdentifier

      nextEntries.forEach(([key, value]) => {
        if (!currentKeysSet.has(key)) {
          if (canAddKey) {
            const nextValue = isLocalizationBeingEditedByUser ? value : DEFAULT_ENTRY_VALUE;
            const isCreated =
              internalEditOrCreateOneEntry(
                topLevelEntity.Session(),
                entries,
                entriesArray,
                key,
                nextValue,
                entryModel,
              )
            if (isCreated) changesCount++;
          }
        } else if (isLocalizationBeingEditedByUser) {
          const isEdited = internalEditOneEntryValue(entries, entriesArray, key, value);
          if (isEdited) changesCount++;
        }
      });

      if (changesCount > 0 || (skippedChanges && isLocalizationBeingEditedByUser)) {
        topLevelEntity[collectionValuePropertyName] = topLevelEntity[collectionValuePropertyName];
      }
    });

    nestedTransaction.commit();
    return true;
  }
  return false;
}

function createBulletList(items: string[], maxItems: number = 3): string {
  const finalItems = items.length > maxItems + 1
    ? [...items.slice(0, maxItems), '...']
    : items;
  return finalItems.map(addBullet).join('\n');
}

function addBullet(s: string): string {
  return `  • ${s}`;
}
