<svelte:options tag="wc-checkbox" />

<script lang="ts">
  export let id: string = null;
  export let checked: boolean = false;

  let rootElement: HTMLInputElement;

  const dispatch = (eventName: string, detail?: any) => {
    const event = new CustomEvent(eventName, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    rootElement.dispatchEvent(event);
    return event;
  };

  function selected() {
    checked = !checked;
    dispatch("select", {
      checked,
    });
  }
</script>

<input
  {id}
  bind:this={rootElement}
  type="checkbox"
  {checked}
  on:change={selected}
/>

<style type="text/scss">
  input {
    -webkit-appearance: none;
    background-color: #fafafa;
    border: 1px solid #cacece;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05),
      inset 0px -15px 10px -12px rgba(0, 0, 0, 0.05);
    padding: 9px;
    border-radius: 3px;
    display: inline-block;
    position: relative;
  }

  input:active,
  input:checked:active {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05),
      inset 0px 1px 3px rgba(0, 0, 0, 0.1);
  }

  input:checked {
    background-color: #e9ecee;
    border: 1px solid #adb8c0;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05),
      inset 0px -15px 10px -12px rgba(0, 0, 0, 0.05),
      inset 15px 10px -12px rgba(255, 255, 255, 0.1);
    color: #99a1a7;
  }
  input:checked:after {
    content: "\2714";
    font-size: 18px;
    position: absolute;
    top: -2px;
    left: 2px;
    color: var(--primary-color);
  }
</style>
