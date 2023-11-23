<svelte:options tag="app-configuration-tab-bar" />

<script lang="ts">
  const tabs = ['Localization', 'Theme'];
  export let selectedTabIndex = 0;
  let rootElement: HTMLDivElement;

  const handleChange = (nextIndex: number) => {
    selectedTabIndex = nextIndex;

    const event = new CustomEvent('selectedTabChange', {
      detail: { selectedTabIndex },
      bubbles: true,
      cancelable: true,
      composed: true,
    });

    rootElement.dispatchEvent(event);
  };
</script>

<div bind:this={rootElement} class="tabs">
  {#each tabs as tabLabel, index (tabLabel)}
    <div
      class="tab"
      class:active={index === selectedTabIndex}
      on:click={() => handleChange(index)}
    >
      {tabLabel}
    </div>
  {/each}
</div>

<style type="text/scss">
  .tabs {
    display: flex;
    padding: var(--outer-indentation) var(--outer-indentation) 0;
    overflow: auto hidden;
  }

  .tab {
    padding: 1em 3ch 4em;
    margin-bottom: -3em;
    border-radius: 4px 4px 0 0;
    background-color: var(--paper);
    filter: drop-shadow(0 0 2px var(--shadow-color));
    transition: transform 0.15s cubic-bezier(0.175, 0.885, 0.320, 1.275);
    &:not(.active) {
      background-color: var(--paper-pale);
      filter: drop-shadow(0 0 1px var(--shadow-color));
      transform: translateY(0.5em);
    }
    &:not(:last-of-type) {
      margin-right: 1ch;
    }
    cursor: pointer;
  }
</style>
