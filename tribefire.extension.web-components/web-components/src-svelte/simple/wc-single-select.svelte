<svelte:options tag="wc-single-select" />

<script>
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";
  export let id = "";
  export let value = [];
  export let readonly = false;
  export let placeholder = "";
  export let options = [];

  let input,
    inputValue,
    activeOption,
    showOptions = false,
    selected = {},
    first = true;

  const iconClearPath =
    "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";

  onMount(() => {
    value &&
      (selected = options.reduce(
        (obj, op) =>
          value.includes(op.value) ? { ...obj, [op.value]: op } : obj,
        {}
      ));
    first = false;
  });

  $: if (!first) value = Object?.values(selected).map((o) => o?.value);

  $: filtered = options.filter((o) =>
    inputValue ? o.name.toLowerCase().includes(inputValue.toLowerCase()) : o
  );

  $: if (
    (activeOption && !filtered.includes(activeOption)) ||
    (!activeOption && inputValue)
  )
    activeOption = filtered[0];

  function add(token) {
    !readonly && selected.length < 1
      ? (selected[token?.value] = token)
      : (selected = {})
      ? (selected[token?.value] = token)
      : {};
    inputValue = selected[token.value].name;
  }

  function optionsVisibility(show) {
    if (typeof show === "boolean") {
      showOptions = show;
      show && input.focus();
    } else {
      showOptions = !showOptions;
    }
  }

  $: if (
    inputValue &&
    options.every((a) => a.name.toLowerCase() !== inputValue?.toLowerCase())
  )
    optionsVisibility(true);

  function handleAdd() {
    if (activeOption !== undefined) {
      Object.keys(selected).includes(activeOption?.value)
        ? (inputValue = activeOption?.name)
        : add(activeOption);
      optionsVisibility(false);
    } else {
      selected = {};
      inputValue = "";
    }
  }

  function handleKeyup(e) {
    if (e.keyCode === 13) handleAdd();

    if ([38, 40].includes(e.keyCode)) {
      const increment = e.keyCode === 38 ? -1 : 1;
      const calcIndex = filtered.indexOf(activeOption) + increment;
      activeOption =
        calcIndex < 0
          ? filtered[filtered.length - 1]
          : calcIndex === filtered.length
          ? filtered[0]
          : filtered[calcIndex];
    }
  }

  function handleBlur(e) {
    handleAdd();
    optionsVisibility(false);
    selected ? dispatch("selected-onblur", selected) : {};
  }

  function handleTokenClick(e) {
    if (e.target.closest(".remove-all")) {
      selected = {};
      inputValue = "";
      activeOption = "";
    } else {
      optionsVisibility(true);
    }
  }

  function handleOptionMousedown(e) {
    const value = e.target.dataset.value;
    activeOption = options.filter((o) => o.value === value)[0];
    handleAdd();
    input.focus();
  }

  const dispatch = (eventName, detail) => {
    const event = new CustomEvent(eventName, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    input.dispatchEvent(event);
    return event;
  };
</script>

<div class="input-wrap" class:readonly>
  <div class="tokens" class:showOptions on:click={handleTokenClick}>
    <div class="actions">
      {#if !readonly}
        <input
          {id}
          autocomplete="off"
          bind:value={inputValue}
          bind:this={input}
          on:keyup={handleKeyup}
          on:blur={handleBlur}
          {placeholder}
          spellcheck="false"
        />

        <div
          class="remove-all"
          title="Remove All"
          class:hidden={!Object.keys(selected).length}
        >
          <svg
            class="icon-clear"
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
          >
            <path d={iconClearPath} />
          </svg>
        </div>

        <svg
          class="dropdown-arrow"
          xmlns="http://www.w3.org/2000/svg"
          width="18"
          height="18"
          viewBox="0 0 18 18"><path d="M5 8l4 4 4-4z" /></svg
        >
      {/if}
    </div>
  </div>

  <!-- <select bind:this={slot} type="multiple" class="hidden"><slot /></select> -->

  {#if showOptions}
    <ul
      class="options"
      transition:fly={{ duration: 200, y: 5 }}
      on:mousedown|preventDefault={handleOptionMousedown}
    >
      {#each filtered as option}
        <li
          class:selected={selected[option.value]}
          class:active={activeOption === option}
          data-value={option.value}
        >
          {option.name}
        </li>
      {/each}
      {#if filtered.length <= 0 && inputValue && inputValue.length > 0}
        <li>Select from the list !</li>
      {/if}
    </ul>
  {/if}
</div>

<style>
  .input-wrap {
    /* background-color: white;
    border-bottom: 1px solid hsl(0, 0%, 70%); */
    position: relative;
    color: #808080;
    padding: 3px;
    border: 0.5px solid silver;
    border-radius: 4px;
    background: white;
    display: inline-block;
  }
  .input-wrap:not(.readonly):hover {
    border-bottom-color: hsl(0, 0%, 50%);
  }

  .tokens {
    align-items: center;
    display: flex;
    flex-wrap: wrap;
    position: relative;
  }
  .tokens::after {
    background: none repeat scroll 0 0 transparent;
    bottom: -1px;
    content: "";
    display: block;
    height: 2px;
    left: 50%;
    position: absolute;
    background: hsl(45, 100%, 51%);
    transition: width 0.3s ease 0s, left 0.3s ease 0s;
    width: 0;
  }
  .tokens.showOptions::after {
    width: 100%;
    left: 0;
  }

  .remove-all {
    align-items: center;
    background-color: hsl(214, 15%, 55%);
    border-radius: 50%;
    color: hsl(214, 17%, 92%);
    display: flex;
    justify-content: center;
    height: 0.75rem;
    margin-left: 0.25rem;
    width: 0.75rem;
  }

  .remove-all:hover {
    background-color: hsl(215, 21%, 43%);
    cursor: pointer;
  }

  .actions {
    align-items: center;
    display: flex;
    flex: 1;
    min-width: 15rem;
  }

  input {
    border: none;
    /* font-size: 1.5rem;
    line-height: 1.5rem; */
    margin: 0;
    outline: none;
    padding: 0;
    width: 100%;
  }

  .dropdown-arrow path {
    fill: hsl(0, 0%, 70%);
  }
  .input-wrap:hover .dropdown-arrow path {
    fill: hsl(0, 0%, 50%);
  }

  .icon-clear path {
    fill: white;
  }

  .options {
    z-index: 1000;
    box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1), 0px -2px 4px rgba(0, 0, 0, 0.1);
    left: 0;
    list-style: none;
    margin-block-end: 0;
    margin-block-start: 0;
    max-height: 70vh;
    overflow: auto;
    padding-inline-start: 0;
    position: absolute;
    top: calc(100% + 1px);
    width: 100%;
  }
  li {
    background-color: white;
    cursor: pointer;
    padding: 0.5rem;
  }
  li:last-child {
    border-bottom-left-radius: 0.2rem;
    border-bottom-right-radius: 0.2rem;
  }
  li:not(.selected):hover {
    background-color: hsl(214, 17%, 92%);
  }
  li.selected {
    background-color: rgb(240, 240, 240);
    /* color: white; */
  }
  li.selected:nth-child(even) {
    background-color: rgb(240, 240, 240);
    /* color: white; */
  }
  li.active {
    background-color: hsl(214, 17%, 88%);
  }
  li.selected.active,
  li.selected:hover {
    background-color: rgb(240, 240, 240);
  }

  .hidden {
    display: none;
  }
</style>
