import { DEFAULT_ENTRY_VALUE } from "@app-configuration";

interface AddNewTopLevelEntityFactoryArguments {
  appConfigurationGetter: Function;                 // function that returns AppConfiguration instance
  appConfiguration_CollectionsPropertyName: string; // i.e. 'localizations': appConfiguration['localizations']
  collectionKeyPropertyName: string;                // i.e. 'location':      localization['location']
  collectionValuePropertyName: string;              // i.e. 'values':        localization['values']
  collectionItemModel: any;                         // i.e. AppLocalization
  entryModel: any;                                  // i.e. AppLocalizationEntry
  collectionCloneSourceGetter?: Function;           // i.e. Existing localization getter, to clone it's entries to the new localization
  promptMessage: string;                            // i.e. 'Enter new key'
  keyAlreadyExistMessage: string;                   // i.e. 'Key {newKey} already exists'
}

/**
 * @returns a function that creates a collection in the appConfiguration
 */
export const addNewTopLevelEntityFactory = (
  {
    appConfigurationGetter,                   // function that returns AppConfiguration instance
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    collectionKeyPropertyName,                // i.e. 'location':      localization['location']
    collectionValuePropertyName,              // i.e. 'values':        localization['values']
    collectionItemModel,                      // i.e. AppLocalization
    entryModel,                               // i.e. AppLocalizationEntry
    collectionCloneSourceGetter,              // i.e. Existing localization getter, to clone it's entries to the new localization
    promptMessage = 'Enter new key',
    keyAlreadyExistMessage = 'Key {newKey} already exists',
  }: AddNewTopLevelEntityFactoryArguments
) => (): string => {
  const newKey = prompt(promptMessage)?.trim();

  if (newKey) {
    const appConfiguration = appConfigurationGetter();
    const existingTopLevelKeys = appConfiguration[appConfiguration_CollectionsPropertyName]
      .toArray()
      .map(topLevelEntity => topLevelEntity[collectionKeyPropertyName]);

    if (existingTopLevelKeys?.includes(newKey) ?? false) {
      alert(keyAlreadyExistMessage.replace('{newKey}', newKey));
    } else {
      const session = appConfiguration?.Session();
      const nestedTransaction = session.getTransaction().beginNestedTransaction();
      const newTopLevelEntity = session.create(collectionItemModel);
      if (newTopLevelEntity) {
        newTopLevelEntity[collectionKeyPropertyName] = newKey;
        const topLevelEntityToClone = collectionCloneSourceGetter?.call(null);
        if (topLevelEntityToClone) {
          const entriesToClone = topLevelEntityToClone[collectionValuePropertyName]?.toArray();
          entriesToClone.forEach(({key}) => {
            const clonedEntry = session.create(entryModel);
            clonedEntry.key = key;
            clonedEntry.value = DEFAULT_ENTRY_VALUE;
            newTopLevelEntity[collectionValuePropertyName].add(clonedEntry);
          });
        }
        appConfiguration[appConfiguration_CollectionsPropertyName].add(newTopLevelEntity);
      }
      nestedTransaction.commit();
      return newKey;
    }
  }
  return null;
}
