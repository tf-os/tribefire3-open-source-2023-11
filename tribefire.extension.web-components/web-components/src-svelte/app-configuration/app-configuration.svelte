<svelte:options tag="app-configuration" />

<script lang="ts">
  import {
    createAppConfigurationState,
    createLocalizationState,
    createThemeState,
    languageToEmoji,
    themeNameToEmoji,
  } from '@app-configuration';

  export const isContentEditable = true;
  export let content = null;

  export let can_add_localization = true;
  export let can_edit_localization_name = true;
  export let can_add_localization_key = true;
  export let can_edit_localization_key = true;
  export let can_edit_localization_entry_description = true;

  export let can_add_theme = true;
  export let can_edit_theme_name = true;
  export let can_add_theme_key = true;
  export let can_edit_theme_key = true;
  export let can_edit_theme_entry_description = true;

  // there may be more than one instance of app-configuration in runtime
  // thus no global state is allowed
  const {
    appConfigurationTab,
    appConfigurationStream,
    setAppConfiguration,
  } = createAppConfigurationState();

  const localizationState = createLocalizationState(appConfigurationStream);
  const themeState = createThemeState(appConfigurationStream);

  // $: console.log({ localizationState });

  $: setAppConfiguration(content);

  const instructionsLink =
    'https://formatjs.io/docs/core-concepts/basic-internationalization-principles/';
</script>

<div class="root">
  <app-configuration-tab-bar
    selectedTabIndex={$appConfigurationTab}
    on:selectedTabChange={(event) =>
      appConfigurationTab.set(event.detail.selectedTabIndex)}
  />

  {#if $appConfigurationStream}
    <app-configuration-tab-content class:hidden={$appConfigurationTab !== 0}>
      <app-configuration-collection-editor
        can_add_collection={can_add_localization}
        can_edit_collection_name={can_edit_localization_name}
        can_add_key={can_add_localization_key}
        can_edit_key={can_edit_localization_key}
        collection_name_to_emoji={languageToEmoji}
        {...localizationState}
      >
        <div slot="list-menu" class="instructions-link">
          <a href={instructionsLink} target="_blank"> Instructions </a>
        </div>
      </app-configuration-collection-editor>
    </app-configuration-tab-content>

    <app-configuration-tab-content class:hidden={$appConfigurationTab !== 1}>
      <app-configuration-collection-editor
        can_add_collection={can_add_theme}
        can_edit_collection_name={can_edit_theme_name}
        can_add_key={can_add_theme_key}
        can_edit_key={can_edit_theme_key}
        collection_name_to_emoji={themeNameToEmoji}
        {...themeState}
      />
    </app-configuration-tab-content>
  {/if}
</div>

<style type="text/scss">
  .root {
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    height: 100%;
    background: var(--background);
    color: var(--color);
    --outer-indentation: 16px;
    --shadow-color: rgba(0, 0, 0, 0.2);
  }

  .hidden {
    display: none;
  }

  app-configuration-tab-content,
  app-configuration-localizations-editor {
    display: grid;
    grid-template-rows: 1fr;
    min-height: 0;
  }

  app-configuration-tab-content {
    --padding: 1em 3ch 1em 0;
  }

  app-configuration-collection-editor {
    display: grid;
    grid-template-rows: 1fr;
    min-height: 0;
  }

  .instructions-link {
    text-align: center;
  }
</style>
