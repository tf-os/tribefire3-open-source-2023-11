import { combineLatest, from, Observable, of } from "rxjs";
import { map, shareReplay, switchMap, tap } from "rxjs/operators";

import {
  AppDescriptor,
  AppDescriptorEntry,
  internalEditOrCreateOneEntry,
  internalRemoveOneEntry,
  internalRenameKey,
  lazyLoadProperty,
  propertyAsReadableStream,
} from "@app-configuration";

interface AddNewTopLevelDescriptorFactoryArguments {
  appConfiguration: any;  // function that returns AppConfiguration instance
  descriptorName: string; // i.e. 'AppLocalization' or 'AppTheme'
}

const getExistingOrCreateDescriptor = (
  {
    appConfiguration, // AppConfiguration instance
    descriptorName,   // i.e. 'AppLocalization' or 'AppTheme'
  }: AddNewTopLevelDescriptorFactoryArguments
): typeof AppDescriptor => {
  if (descriptorName) {
    let descriptor = appConfiguration.descriptors
      .toArray()
      .find(descriptor => descriptor.descriptor?.toLocaleLowerCase() === descriptorName?.toLocaleLowerCase());

    if (!descriptor) {
      console.log('create descriptor:', descriptorName)
      const session = appConfiguration?.Session();
      descriptor = session.create(AppDescriptor);
      descriptor.descriptor = descriptorName;
      appConfiguration.descriptors.add(descriptor);
    } else {
      console.log('descriptor already exists', descriptorName, descriptor);
    }

    return descriptor;
  }
  return null;
}

export const descriptorStreamFactory = (
  appConfigurationStream: Observable<any>,
  descriptorName: string, // i.e. 'AppLocalization': appConfiguration.descriptors.find(d => d.name === 'AppLocalization')
): Observable<any> => {
  const descriptorNameLowerCase = descriptorName?.toLocaleLowerCase();
  return appConfigurationStream
    .pipe(
      // create AppDescriptor if not exist
      tap(appConfiguration => getExistingOrCreateDescriptor({appConfiguration, descriptorName})),
      switchMap(appConfiguration =>
        propertyAsReadableStream(
          appConfiguration,
          'descriptors',
        )
      ),
      map(descriptorCollections =>
        descriptorCollections
          ? descriptorCollections
              .toArray()
              .find(descriptorCollection =>
                descriptorCollection?.descriptor?.toLocaleLowerCase() === descriptorNameLowerCase
              )
          : null
      ),
      switchMap(descriptorCollection => {
        console.log({ descriptorCollection });
        return descriptorCollection
          ? from(lazyLoadProperty(descriptorCollection, 'values'))
            .pipe(
              map(() => descriptorCollection),
              shareReplay(1),
            )
          : of(null)
      }),
      shareReplay(1),
    );
}


/**
 * @returns stream of appConfiguration.descriptors.toArray().find(d => d.descriptor === descriptorName)
 * auto fetches all nested lists
 */
export const descriptorValuesStreamFactory = (
  descriptorStream: Observable<any>,
): Observable<any> => {
  return descriptorStream
    .pipe(
      switchMap(descriptor =>
        propertyAsReadableStream(
          descriptor,
          'values',
        )
      ),
      shareReplay(1),
    );
}

export const editOrCreateDescriptorEntryFactory = (
  descriptorStream: Observable<any>
) => descriptorStream.pipe(
  map(descriptor => {
  const session = descriptor?.Session();
  const entries = descriptor?.values;
    return function(key: string, value: string) {
      if (entries) {
        const isEntryEdited = 
          internalEditOrCreateOneEntry(
            session,
            entries,
            entries?.toArray(),
            key,
            value,
            AppDescriptorEntry,
          );
        return isEntryEdited;
      }
      return false;
    }
  }),
  shareReplay(1),
)

export const renameDescriptorEntryKeyFactory = (
  descriptorStream: Observable<any>
) => descriptorStream.pipe(
  map(descriptor => {
    const entries = descriptor?.values;
    return function(oldKey: string, newKey: string) {
      if (entries) {
        const isKeyRenamed =
          internalRenameKey(
            descriptor.Session(),
            entries,
            entries?.toArray(),
            oldKey,
            newKey,
            AppDescriptorEntry,
          );

        return isKeyRenamed;
      }
      return false;
    }
  }),
  shareReplay(1),
)

export const removeDescriptorEntryFactory = (
  descriptorStream: Observable<any>
) => descriptorStream.pipe(
  map(descriptor => {
    const entries = descriptor?.values;
    return function(key: string) {
      if (entries) {
        const isEntryRemoved = internalRemoveOneEntry(entries, entries?.toArray(), key);
        return isEntryRemoved;
      }
      return false;
    }
  }),
  shareReplay(1),
)

export const updateDescriptorsFromObjectFactory = ({
  descriptorStream,
  editOrCreate,
  remove,
}) =>
  combineLatest([
    descriptorStream,
    editOrCreate,
    remove,
  ])
    .pipe(
      map(
        ([descriptor, editOrCreate, remove]) => {
          const session = (descriptor as any)?.Session();
          const entries = (descriptor as any)?.values;

          return (nextDictionary: Record<string, string>): void => {
            const entriesArray = entries?.toArray();
            const currentKeysSet = new Set(entriesArray.map(entry => entry.key));
          
            const allEntries = Object.entries(nextDictionary);

            const keysToRemove =
              allEntries
                .filter(([key, value]) => value === null && currentKeysSet.has(key))
                .map(([key, _value]) => key);

            const entriesToEditOrCreate = allEntries.filter(([_key, value]) => value !== null);

            console.log({ keysToRemove, entriesToEditOrCreate });

            const nestedTransaction = session.getTransaction().beginNestedTransaction();

            // removing keys
            keysToRemove.forEach(key => {
              (remove as Function)(entries, entriesArray, key);
            });

            // adding keys / modifying values
            entriesToEditOrCreate.forEach(([key, value]) => {
              (editOrCreate as Function)(key, value);
            });

            nestedTransaction.commit();
          }
        }
      ),
      shareReplay(1),
    );
