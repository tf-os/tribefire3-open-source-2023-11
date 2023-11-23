<svelte:options tag="wc-modal" />

<script lang="ts">
  let topDiv;
  export let visible = false;

  export let id = "";

  const dispatch = (eventName: string, detail?: any) => {
    const event = new CustomEvent(eventName, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    topDiv.dispatchEvent(event);
    return event;
  };

  $: if (visible) {
    document.body.style.overflow = "hidden";
  }

  function close() {
    visible = false;
    dispatch("close", {
      visible: true,
    });
  }
</script>

<div id="topModal" class:visible bind:this={topDiv} on:click={() => close()}>
  <div id="modal" on:click|stopPropagation={() => {}}>
    <div id="modal-content">
      <slot />
    </div>
    <div class="actions" on:click={() => close()}>
      <button>
        <svg viewBox="0 0 40 40">
          <path class="close-x" d="M 10,10 L 30,30 M 30,10 L 10,30" />
        </svg>
      </button>
    </div>
  </div>
</div>

<style>
  #topModal {
    visibility: hidden;
    z-index: 9999;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: #4448;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  #modal {
    position: relative;
    /* border-radius: 6px; */
    background: white;
    /* border: 2px solid #000; */
    filter: drop-shadow(5px 5px 5px #555);
    padding: 1em;
  }

  .visible {
    visibility: visible !important;
  }

  #modal-content {
    max-width: calc(100vw - 20px);
    max-height: calc(100vh - 20px);
    overflow: auto;
  }

  .close-x {
    stroke: black;
    fill: transparent;
    stroke-linecap: round;
    stroke-width: 1;
  }

  button {
    cursor: pointer;
    width: 50px;
    height: 50px;
    padding: 0;
    background: transparent;
    border: none;
  }

  .actions {
    margin-top: 10px;
    border-top: 0.5px solid black;
    min-width: 300px;
    text-align: end;
  }
</style>
