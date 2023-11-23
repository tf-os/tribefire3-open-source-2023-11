<svelte:options tag="wc-burger" />

<script lang="ts">
  export let bardistance = 8;
  export let barthicknes = 4;
  export let barwidth = 30;
  export let size = 50;
  export let background = "var(--primary-color-accent)";
  export let color = "var(--color)";
  export let xstyle: string = "";

  let rootElement;

  let menuOpen = false;

  function toggle() {
    menuOpen = !menuOpen;
    dispatch("burger", { value: menuOpen });
  }

  const dispatch = (eventName, detail) => {
    const event = new CustomEvent(eventName, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    rootElement.dispatchEvent(event);
    return event;
  };
</script>

<div
  bind:this={rootElement}
  class="menu-btn {menuOpen ? 'open' : ''}"
  on:click={toggle}
  style="width:{size}px; height:{size}px; background: {background}; {xstyle}"
>
  <div
    class="top-bar"
    style="transform: translateY(-{bardistance}px);{menuOpen
      ? 'transform: rotate(45deg)'
      : ''}; height: {barthicknes}px; width: {barwidth}px; background: {color};"
  />
  <div
    class="middle-bar"
    style="height: {barthicknes}px; width: {barwidth}px; ; {menuOpen
      ? 'background: transparent'
      : `background: ${color}`}"
  />
  <div
    class="bottom-bar"
    style="transform: translateY({bardistance}px);{menuOpen
      ? 'transform: rotate(-45deg)'
      : ''}; height: {barthicknes}px;width: {barwidth}px;background: {color}; "
  />
</div>

<style>
  .menu-btn {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s ease-in-out;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgb(0 0 0 / 60%);
  }
  .middle-bar {
    border-radius: 4px;
    transition: all 0.3s ease-in-out;
  }
  .top-bar,
  .bottom-bar {
    position: absolute;
    border-radius: 4px;
    transition: all 0.3s ease-in-out;
  }
</style>
