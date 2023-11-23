<svelte:options tag="app-configuration-collection-editor" />

<script lang="ts">
  import { combineLatest, of } from 'rxjs';
  import { map, shareReplay, throttleTime } from 'rxjs/operators';

  import '../../key-value-editor/key-value-editor.svelte';
  import {
    compareEntries,
    entityAsObservable,
    entriesWithDescriptorsToJson,
    propertyAsReadableStream,
    validateInterpolationString,
  } from '@app-configuration';
import { afterUpdate } from 'svelte';

  // access rights
  export let can_add_collection = true;
  export let can_edit_collection_name = true;
  export let can_add_key = true;
  export let can_edit_key = true;
  // rest properties
  export let add_button_label: string = 'Add';
  export let code: string;
  export let collection_name_to_emoji = null;
  export let descriptors: Record<string, string> = null;
  export let entries;

  // collection
  export let add_new_collection: any = null;
  export let create_collection_entry: any = null;
  export let edit_collection_entry_value: any = null;
  export let remove_collection_entry: any = null;
  export let remove_collection: any = null;
  export let rename_collection: any = null;
  export let rename_collection_entry_key: any = null;
  export let selected_collection_name_stream: any = null;
  export let selected_collection_stream: any = null;
  export let set_selected_collection_name: any = null;
  export let sorted_collection_names_stream: any = null;
  export let update_collection_from_object: any = null;
  export let update_descriptors_from_object: any = null;
  // collection descriptors
  export let descriptor_list_stream: any = null;
  export let edit_or_create_descriptor_entry: any = null;
  export let rename_descriptor_entry_key: any = null;
  export let remove_descriptor_entry: any = null;

  $: selectedDictionaryStream = propertyAsReadableStream(
    $selected_collection_stream,
    'values'
  );

  $: selectedDictionaryEntriesArrayStream = $selectedDictionaryStream?.toArray();

  $: selectedDictionaryEntriesStream =
    selectedDictionaryEntriesArrayStream?.length > 0
      ? combineLatest(
          $selectedDictionaryStream.toArray().map(entityAsObservable) as []
        ).pipe(
          throttleTime(1),
          map((items) => items.sort(compareEntries)),
          shareReplay(1)
        )
      : of(null);

  $: descriptionsArray = $descriptor_list_stream?.toArray();
  $: descriptionsArrayStream =
    descriptionsArray?.length > 0
      ? combineLatest(descriptionsArray.map(entityAsObservable) as []).pipe(
          throttleTime(1),
          shareReplay(1)
        )
      : of([]);

  $: descriptionsHashMap = Object.fromEntries(
    ($descriptionsArrayStream || []).map((descriptor) => [
      descriptor.key,
      descriptor.value,
    ])
  );

  // $: console.log({ $descriptionsArrayStream });
  // $: console.log({ descriptionsHashMap });

  $: selectedDictionaryJSON = $selectedDictionaryEntriesStream
    ? entriesWithDescriptorsToJson(
        $selectedDictionaryEntriesStream,
        descriptionsHashMap
      )
    : '[ ]';

  function handleAddCollection() {
    const newCollectionName = add_new_collection();
    if (newCollectionName) {
      // TODO: find a cleaner way to do this (maybe using the streams directly)
      setTimeout(() => set_selected_collection_name(newCollectionName), 100);
    }
  }

  function handleRemoveCollection({ detail: key }) {
    remove_collection(key);
  }

  function handleRenameCollection(event) {
    const {
      detail: { oldName, newName },
    } = event;
    const isRenamed = rename_collection(oldName, newName);
    if (!isRenamed) {
      event.preventDefault();
    } else {
      // TODO: find a cleaner way to do this (maybe using the streams directly)
      setTimeout(() => set_selected_collection_name(newName), 100);
    }
  }

  // TODO: make sure description is sent by create_entry event
  function handleCreateEntry({ detail: { key, value, description } }) {
    create_collection_entry($selected_collection_name_stream, key, value);
    if (description) {
      $edit_or_create_descriptor_entry(key, description);
    }
  }

  function handleRemoveEntry({ detail: { key } }) {
    remove_collection_entry(key);
    $remove_descriptor_entry(key);
  }

  function handleRenameKey(event) {
    const {
      detail: { oldName, newName },
    } = event;
    if (!rename_collection_entry_key(oldName, newName)) event.preventDefault();
    $rename_descriptor_entry_key(oldName, newName);
  }

  function handleEditEntryValue({ detail: { key, value } }) {
    validateInterpolationString(value);
    edit_collection_entry_value($selected_collection_name_stream, key, value);
  }

  function handleEditDescription({ detail: { key, description } }) {
    console.log('handleEditDescription', { key, description });
    $edit_or_create_descriptor_entry(key, description);
  }

  function handleCodeChange({ detail: { parsed } }) {
    let keyValueHashMap = null;
    let keyDescriptionHashMap = null;

    if (Array.isArray(parsed)) {
      keyValueHashMap = Object.fromEntries(
        parsed.map(({ key, value }) => [key, value])
      );
      keyDescriptionHashMap = Object.fromEntries(
        parsed.map(({ key, description }) => [key, description])
      );
    } else {
      keyValueHashMap = parsed;
    }

    const updatedLocalizationsSuccess = update_collection_from_object(
      $selected_collection_name_stream,
      keyValueHashMap,
      {
        canAddKey: can_add_key,
        canEditKey: can_edit_key,
      }
    );

    if (updatedLocalizationsSuccess && keyDescriptionHashMap) {
      console.log({ update_descriptors_from_object, keyDescriptionHashMap });
      $update_descriptors_from_object(keyDescriptionHashMap);
    }
  }
</script>

<div class="wrapper">
  <app-configuration-list-menu
    {add_button_label}
    can_add={can_add_collection}
    can_edit={can_edit_collection_name}
    items={$sorted_collection_names_stream}
    prefix_getter={collection_name_to_emoji}
    selected={$selected_collection_name_stream}
    on:add={handleAddCollection}
    on:remove={handleRemoveCollection}
    on:rename={handleRenameCollection}
    on:select={(event) => set_selected_collection_name(event.detail)}
  >
    <slot name="list-menu" />
  </app-configuration-list-menu>

  {#if $selected_collection_stream}
    <key-value-editor
      {can_add_key}
      {can_edit_key}
      code={selectedDictionaryJSON}
      descriptors={descriptionsHashMap}
      entries={$selectedDictionaryEntriesStream}
      on:create_entry={handleCreateEntry}
      on:remove_entry={handleRemoveEntry}
      on:edit_entry_key={handleRenameKey}
      on:edit_entry_value={handleEditEntryValue}
      on:edit_description={handleEditDescription}
      on:code_change={handleCodeChange}
    />
  {/if}
</div>

<style type="text/scss">
  .wrapper {
    display: grid;
    grid-template-columns: auto 1fr;
    grid-template-rows: 1fr;
    min-height: 0;
    & > * {
      min-height: 0;
    }
  }

  app-configuration-list-menu {
    --left-inset: 32px;
  }

  key-value-editor {
    --left-inset: 32px;
  }
</style>
