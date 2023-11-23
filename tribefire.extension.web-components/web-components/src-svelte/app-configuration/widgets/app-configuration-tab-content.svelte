<svelte:options tag="app-configuration-tab-content" />

<div class="content">
  <div class="wrapper">
    <slot />
  </div>
</div>

<style type="text/scss">
  .content {
    display: grid;
    grid-template-rows: 1fr;
    min-height: 0;
    overflow: hidden;
    padding: 0 var(--outer-indentation) var(--outer-indentation);
  }

  .wrapper {
    display: grid;
    grid-template-rows: 1fr;
    min-height: 0;
    padding: var(--padding);
    background: var(--paper);
    filter: drop-shadow(0 0 2px var(--shadow-color));
  }
</style>
