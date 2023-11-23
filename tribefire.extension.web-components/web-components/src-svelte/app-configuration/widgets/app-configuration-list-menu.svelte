<svelte:options tag="app-configuration-list-menu" />

<script lang="ts">
  import {
    instantTouch,
    longpress,
    removeButton,
    unfocusOnEnterKey,
  } from '@app-configuration';

  export let can_add = true;
  export let can_edit = true;
  export let items: string[] = null;
  export let selected: string = null;
  export let prefix_getter: (value: string) => string = null;
  export let add_button_label: string = '+';

  let rootElement: HTMLUListElement;

  let valueBeforeEdit: string;
  const handleEditStart = (event: CustomEvent, value: string) => {
    valueBeforeEdit = value;

    const labelElement = (event.target as HTMLElement).querySelector(
      '.label'
    ) as HTMLSpanElement;

    if (labelElement) {
      labelElement.contentEditable = (true as unknown) as string;
      labelElement.focus();
    }
  };

  const handleBlur = (event: FocusEvent) => {
    if (can_edit) {
      (event.target as HTMLElement).contentEditable = (false as unknown) as string;
      const newValue = (event.target as HTMLElement).innerText;
      // console.log('handleBlur', valueBeforeEdit, newValue, event);
      const oldName = valueBeforeEdit;
      const dispatchedEvent = dispatch('rename', {
        oldName,
        newName: newValue,
      });
      if (dispatchedEvent.defaultPrevented) {
        (event.target as HTMLElement).innerHTML = oldName;
      }
    }
  };

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

  const removeButtonOptions = {
    style: {
      fontSize: '2em',
    },
  };
</script>

<div class="list-menu-wrapper">
  <button disabled={!can_add} on:click={() => dispatch('add')}>
    {add_button_label}
  </button>
  <ul bind:this={rootElement} class="items-wrapper">
    {#if items}
      {#each items as item (item)}
        <li
          class:active={item === selected}
          use:longpress={can_edit}
          use:instantTouch
          on:instanttouch={() => setTimeout(() => dispatch('select', item))}
          on:longpress={(event) => handleEditStart(event, item)}
        >
          <div class="item-wrapper">
            {#if prefix_getter}
              <span class="emoji">{@html prefix_getter(item)}</span>
            {/if}
            <span
              class="label"
              use:removeButton={removeButtonOptions}
              use:unfocusOnEnterKey
              on:blur={handleBlur}
              on:remove={() => dispatch('remove', item)}
            >
              {item}
            </span>
          </div>
        </li>
      {/each}
    {/if}
  </ul>
  <div class="slot-wrapper">
    <slot />
  </div>
</div>

<style type="text/scss">
  .list-menu-wrapper {
    display: flex;
    flex-direction: column;
    flex-wrap: nowrap;
    min-height: 100%;
    max-height: 100%;
    max-width: 100%;
    & > * {
      flex: 0 0 auto;
    }
  }

  .items-wrapper {
    flex: 0.01 1 auto;
    min-height: 0;
    overflow-y: auto;
    list-style-type: none;
    margin: 1em 0;
    padding: 0;
    padding-left: var(--left-inset, 0);
  }

  li {
    position: relative;
    padding: 1ch 1em;
    &:not(:focus-within):not(.active) {
      cursor: pointer;
    }
    &.active {
      background-color: var(--primary-color);
    }
    &:hover:not(.active) {
      background-color: var(--primary-color-tint);
    }
  }

  .item-wrapper {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
  }

  .emoji {
    display: inline-block;
    margin: 0 2ch 0 1ch;
    transform-origin: 50% 50%;
    transform: scale(2);
  }

  .label {
    margin: -0.8em;
    padding: 0.8em;
    white-space: nowrap;
  }

  .slot-wrapper {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    margin-bottom: 1em;
  }

  .slot-wrapper,
  button {
    margin-left: var(--left-inset, 0);
  }
</style>
