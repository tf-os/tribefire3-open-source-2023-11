
import { get } from "svelte/store";
import { Observable } from "rxjs";

import {
  addNewTopLevelEntityFactory,
  AppLocalization,
  AppLocalizationEntry,
  AppTheme,
  AppThemeEntry,
  createEntryFactory,
  descriptorStreamFactory,
  descriptorValuesStreamFactory,
  editEntryValueFactory,
  editOrCreateDescriptorEntryFactory,
  removeDescriptorEntryFactory,
  removeEntryFactory,
  removeTopLevelEntityFactory,
  renameDescriptorEntryKeyFactory,
  renameEntryKeyFactory,
  renameTopLevelKey,
  updateDescriptorsFromObjectFactory,
  updateFromObjectFactory,
} from "@app-configuration";

export type AppConfigurationCollectionKeys = 'localizations' | 'themes';

export const CollectionInfosMap: Record<AppConfigurationCollectionKeys,Record<string, any>> = {
  localizations: {
    collectionKeyPropertyName: 'location',
    collectionValuePropertyName: 'values',
    descriptorCollectionName: 'AppLocalization',
    collectionItemModel: AppLocalization,
    entryModel: AppLocalizationEntry,
  },
  themes: {
    collectionKeyPropertyName: 'theme',
    collectionValuePropertyName: 'values',
    descriptorCollectionName: 'AppTheme',
    collectionItemModel: AppTheme,
    entryModel: AppThemeEntry,
  },
}

interface CollectionMethodsFactoryArguments {
  appConfigurationStream: Observable<any>;
  selectedCollectionStream: Observable<any>;                                // stream that emits selected collection of this context
  appConfiguration_CollectionsPropertyName: AppConfigurationCollectionKeys; // i.e. 'localizations': appConfiguration['localizations']
  promptMessage: string;                                                    // i.e. 'Enter new localization name (i.e.: at, en-GB, etc.):';
  keyAlreadyExistMessage: string;                                           // i.e. 'Localization with name "{newKey}" already exists';
}

export function collectionMethodsFactory(
  {
    appConfigurationStream,
    selectedCollectionStream,                 // stream that emits selected collection of this context
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations':   appConfiguration['localizations']
    promptMessage,                            // i.e. 'Enter new localization name (i.e.: at, en-GB, etc.):';
    keyAlreadyExistMessage,                   // i.e. 'Localization with name "{newKey}" already exists';
  }: CollectionMethodsFactoryArguments
) {
  if (!CollectionInfosMap[appConfiguration_CollectionsPropertyName]) {
    throw new Error(`Unknown AppConfiguration property "${appConfiguration_CollectionsPropertyName}"`)
  }

  const {
    collectionKeyPropertyName,
    collectionValuePropertyName,
    descriptorCollectionName,
    collectionItemModel,
    entryModel,
  } = CollectionInfosMap[appConfiguration_CollectionsPropertyName];

  const appConfigurationGetter = () => get(appConfigurationStream as any);

  // collection methods
  const add_new_collection = addNewTopLevelEntityFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    collectionValuePropertyName,
    collectionItemModel,
    entryModel,
    collectionCloneSourceGetter: () => get(selectedCollectionStream as any) as any,
    promptMessage,
    keyAlreadyExistMessage,
  });

  const remove_collection = removeTopLevelEntityFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
  });

  const rename_collection = renameTopLevelKey({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    removeTopLevelEntityFunction: remove_collection,
  });

  // entry methods
  const create_collection_entry = createEntryFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    collectionValuePropertyName,
    entryModel,
  });

  const remove_collection_entry = removeEntryFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionValuePropertyName,
  });

  const rename_collection_entry_key = renameEntryKeyFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    collectionValuePropertyName,
    entryModel,
  });

  const edit_collection_entry_value = editEntryValueFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    collectionValuePropertyName,
  });

  const update_collection_from_object = updateFromObjectFactory({
    appConfigurationGetter,
    appConfiguration_CollectionsPropertyName,
    collectionKeyPropertyName,
    collectionValuePropertyName,
    entryModel,
  });

  // descriptor state / methods
  const descriptorStream = descriptorStreamFactory(appConfigurationStream, descriptorCollectionName);
  const descriptor_list_stream = descriptorValuesStreamFactory(descriptorStream);
  const edit_or_create_descriptor_entry = editOrCreateDescriptorEntryFactory(descriptorStream);
  const rename_descriptor_entry_key = renameDescriptorEntryKeyFactory(descriptorStream);
  const remove_descriptor_entry = removeDescriptorEntryFactory(descriptorStream);
  const update_descriptors_from_object = updateDescriptorsFromObjectFactory({
    descriptorStream,
    editOrCreate: edit_or_create_descriptor_entry,
    remove: remove_descriptor_entry,
  });

  return {
    add_new_collection,
    remove_collection,
    rename_collection,
    create_collection_entry,
    remove_collection_entry,
    rename_collection_entry_key,
    edit_collection_entry_value,
    update_collection_from_object,
    descriptorStream,
    descriptor_list_stream,
    edit_or_create_descriptor_entry,
    rename_descriptor_entry_key,
    remove_descriptor_entry,
    update_descriptors_from_object,
  }

}
