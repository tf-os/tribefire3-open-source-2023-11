<script lang="typescript" context="module">
  import get from "lodash/get";
  import Slot from "./slot.svelte";
  import Input from "./input.svelte";
  import TextArea from "./text-area.svelte";
  import Button from "./button.svelte";

  const ComponentMap = {
    slot: Slot,
    input: Input,
    textarea: TextArea,
    button: Button,
    group: true
  };

  const FieldComponents = new Set([Input, TextArea]);
</script>

<script lang="typescript">
  import { createEventDispatcher } from "svelte";

  const dispatch = createEventDispatcher();

  export let level = 1;
  export let type = null;
  export let props = null;
  export let values = {};
  export let touched = {};
  export let validationerrors = {};

  $: componentType = type ? ComponentMap[type] : null;
  $: isFieldComponent = FieldComponents.has(componentType);
  $: children = Array.isArray(props && props.children) ? props.children : null;
  $: hasOnInput = componentType === Input || componentType === TextArea;

  function handleInput(fieldName, event) {
    dispatch("input", {
      fieldName,
      value:
        event.target.type === "checkbox"
          ? event.target.checked
          : event.target.type === "number"
          ? Number(event.target.value)
          : event.target.value
    });
  }

  function handleBlur(fieldName) {
    dispatch("blur", {
      fieldName
    });
  }

  function handleAction(actionName) {
    dispatch("action", {
      action: actionName
    });
  }
</script>

<svelte:options tag="dynamic-form-element" />
{#if type === 'group'}
  <div
    part="fieldset"
    style={props.style || null}
    name={props.name || null}
    {level}>
    {#each props.children as child (get(child, ['props', 'name']))}
      <svelte:self
        {...child}
        {values}
        {touched}
        {validationerrors}
        level={level + 1}
        on:input
        on:blur
        on:action />
    {/each}
  </div>
{:else if componentType}
  <!-- filed component like input, textare, select, etc. -->
  {#if isFieldComponent}
    <div part="field" style={props.style || ''} name={props.name || null}>
      <svelte:component
        this={componentType}
        {...props}
        value={get(values, get(props, 'name'), null)}
        error={validationerrors[get(props, 'name')]}
        on:input={handleInput.bind(null, get(props, 'name'))}
        on:blur={handleBlur.bind(null, get(props, 'name'))} />
    </div>
  {:else}
    <svelte:component
      this={componentType}
      name={props.name || null}
      {...props}
      on:click={handleAction.bind(null, get(props, 'action'))}
      on:blur={handleBlur.bind(null, get(props, 'name'))} />
  {/if}
{/if}
