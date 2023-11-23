
import { get } from "svelte/store";
import { combineLatest, from, Observable, of } from "rxjs";
import { throttleTime, map, shareReplay, switchMap, tap }from "rxjs/operators";
import {
  AppConfigurationCollectionKeys,
  collectionMethodsFactory,
  propertyAsReadableStream,
  lazyLoadProperty,
  SvelteSubject,
  CollectionInfosMap,
} from "@app-configuration";

interface CollectionStreamsFactoryArguments {
  appConfigurationStream: Observable<any>;
  appConfiguration_CollectionsPropertyName: AppConfigurationCollectionKeys; // i.e. 'localizations': appConfiguration['localizations']
  promptMessage: string;                                                    // i.e. 'Enter new localization name (i.e.: at, en-GB, etc.):';
  keyAlreadyExistMessage: string;                                           // i.e. 'Localization with name "{newKey}" already exists';
}

export function collectionStreamsFactory(
  {
    appConfigurationStream,
    appConfiguration_CollectionsPropertyName, // i.e. 'localizations': appConfiguration['localizations']
    promptMessage,                            // i.e. 'Enter new localization name (i.e.: at, en-GB, etc.):';
    keyAlreadyExistMessage,                   // i.e. 'Localization with name "{newKey}" already exists';
  }: CollectionStreamsFactoryArguments
) {

  const {
    collectionKeyPropertyName,
    collectionValuePropertyName,
  } = CollectionInfosMap[appConfiguration_CollectionsPropertyName];

  const collectionsStream: Observable<any> =
    appConfigurationStream
      .pipe(
        switchMap(appConfiguration =>
          propertyAsReadableStream(
            appConfiguration,
            appConfiguration_CollectionsPropertyName,
          )
        ),
        shareReplay(1),
      );

  const collectionsArrayStream =
    collectionsStream
      .pipe(
        map(collections =>
          collections ? collections.toArray() : []
        ),
        switchMap(collections =>
          collections.length > 0
            ? combineLatest(
                collections.map(
                  collection => {
                    return from(lazyLoadProperty(collection, collectionValuePropertyName))
                      .pipe(
                        map(() => collection),
                        shareReplay(1),
                      )
                    }
                ) as Observable<any>[]
              )
            : of([])
        ),
        shareReplay(1),
      );

  const collectionNamesStream =
    collectionsArrayStream
      .pipe(
        switchMap(collections =>
          collections?.length > 0
            ? combineLatest(
                (collections.map((collection) =>
                  propertyAsReadableStream(collection, collectionKeyPropertyName)
                )) as Observable<string>[]
              )
            : of([])
        ),
        // select first collectionName if selectedCollectionName
        // does not exist in the collectionNames array
        tap(collectionNames => {
          if (collectionNames?.length > 0 && !collectionNames.includes(internalSelectedCollectionNameStream.value)) {
            internalSelectedCollectionNameStream.next(collectionNames[0]);
          }
        }),
        shareReplay(1),
      );

  const sortedCollectionNamesStream =
    collectionNamesStream
      .pipe(
        map(collectionNames => collectionNames.slice(0).sort()),
        shareReplay(1),
      )

  const internalSelectedCollectionNameStream = new SvelteSubject<string>(null);
  const selectedCollectionNameStream = internalSelectedCollectionNameStream.asObservable();

  const selectedCollectionIndexStream =
    combineLatest([
      collectionNamesStream,
      selectedCollectionNameStream,
    ])
      .pipe(
        map(([collectionNames, selectedCollection]) => collectionNames ? collectionNames.indexOf(selectedCollection) : -1),
        shareReplay(1),
      )

  // Setters
  const setSelectedCollection = (collectionName: string): boolean => {
    const collectionNames = get(collectionNamesStream as any);
    if (Array.isArray(collectionNames) && (collectionNames.length === 0 || collectionNames.includes(collectionName))) {
      internalSelectedCollectionNameStream.next(collectionName);
      return true;
    }
    return false;
  }

  const selectedCollectionStream = combineLatest([
    collectionsArrayStream,
    selectedCollectionIndexStream,
  ]).pipe(
    map(([collectionsArray, selectedLanguageIndex]) =>
      collectionsArray ? collectionsArray[selectedLanguageIndex] : null
    ),
    throttleTime(50),
    shareReplay(1),
  );

  return {
    collections_stream: collectionsStream,
    collections_array_stream: collectionsArrayStream,
    collection_names_stream: collectionNamesStream,
    sorted_collection_names_stream: sortedCollectionNamesStream,
    selected_collection_name_stream: selectedCollectionNameStream,
    selected_collection_name_index_stream: selectedCollectionIndexStream,
    set_selected_collection_name: setSelectedCollection,
    selected_collection_stream: selectedCollectionStream,

    ...collectionMethodsFactory({
      appConfigurationStream,
      appConfiguration_CollectionsPropertyName,
      selectedCollectionStream,
      promptMessage,
      keyAlreadyExistMessage,
    })
  }
}
