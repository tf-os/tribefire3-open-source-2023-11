<svelte:options tag="wc-avatar" />

<script lang="ts">
  export let srcimg = null;
  export let size = 90;
  export let name = "";
  export let lastname = "";
  export let color = "var(--color)";
  export let backgroundcolor = "var(--primary-color-accent)";
  export let fontsize = "26";
  export let xstyle: string = "";

  let rootElement;

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

  $: matches = (name + " " + lastname).match(/\b(\w)/g);
  $: acronym = matches?.join("").toUpperCase();

  $: acronym === undefined || null ? (acronym = "") : acronym;

  function editable() {
    dispatch("edit", true);
  }

  // https://picsum.photos/600/1000/?random?image=3
</script>

<div
  class="avatar"
  style=" width: {size}px; height: {size}px; background-color: {backgroundcolor}; {xstyle}"
  bind:this={rootElement}
>
  <div class="edit" on:click={editable}>Edit</div>
  <div class="avatar-img">
    {#if !srcimg}
      <span style="color: {color}; font-size: {fontsize}px ">{acronym} </span>
    {:else}
      <img src={srcimg} alt="" />
    {/if}
  </div>
</div>

<style lang="scss">
  * {
    padding: 0;
    margin: 0;
    box-sizing: border-box;
  }

  .avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    border-radius: 50%;

    &:hover {
      .edit {
        display: flex;
        cursor: pointer;
        height: 30%;
      }
    }

    span {
      font-size: 24px;
      font-weight: 900;
      color: #fff;
      letter-spacing: 2px;
      margin-right: -2px;
    }

    .avatar-img,
    .edit {
      position: absolute;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .edit {
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 0px;
      z-index: 1;
      bottom: 0;
      color: #fff;
      font-weight: 600;
      text-align: center;
      transition: all 0.3s;
      background: rgba(0, 0, 0, 0.3);
    }
  }
</style>
