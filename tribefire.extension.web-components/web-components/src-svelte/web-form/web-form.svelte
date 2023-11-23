<script lang="ts">
  import { createEventDispatcher } from "svelte";
  import FormElement from "./form-components/form-element.svelte";

  const svelteDispatch = createEventDispatcher();

  const dispatch = (eventType: string, eventDetail?: any) => {
    // if web-component
    if (formRef.parentNode && formRef.parentNode.host) {
      const eventToDispatch = new Event(eventType);
      (eventToDispatch as any).detail = eventDetail;
      formRef.parentNode.host.dispatchEvent(eventToDispatch);
    }
    // else svelte component
    else {
      svelteDispatch(eventType, eventDetail);
    }
  };

  export let css = null;
  export let formfields = null;

  $: formFields = Array.isArray(formfields) ? formfields : [];

  // export let initialvalues = null;
  export let initialvalues = null;

  $: initialValues =
    initialvalues && typeof initialvalues === "object"
      ? { ...initialvalues }
      : {};

  let valuesFromUserInput = {};

  $: values = {
    ...initialValues,
    ...valuesFromUserInput
  };

  export let validationerrors = null;
  let triedSubmitting = formfields ? false : false;

  $: validationErrors =
    validationerrors && typeof validationerrors === "object"
      ? triedSubmitting
        ? validationerrors
        : Object.entries(validationerrors)
            .filter(([key]) => touched.hasOwnProperty(key))
            .reduce((acc, [key, value]) => {
              acc[key] = value;
              return acc;
            }, {})
      : {};

  $: touched = formfields ? {} : {};

  function validate(params) {
    dispatch("change", params);
  }

  function handleInput(event) {
    const changes = { [event.detail.fieldName]: event.detail.value };
    valuesFromUserInput = {
      ...valuesFromUserInput,
      ...changes
    };
    validate({
      changes,
      previousValues: values,
      values: {
        ...values,
        ...changes
      }
    });
  }

  function handleBlur(event) {
    if (!touched[event.detail.fieldName]) {
      touched = {
        ...touched,
        [event.detail.fieldName]: true
      };
    }
  }

  function handleAction(event) {
    switch (event.detail.action) {
      case "reset":
        valuesFromUserInput = {};
        touched = {};
        triedSubmitting = false;
        break;
      case "submit":
        triedSubmitting = true;
        validate({ values, chnages: {}, previousValues: {} });
        if (!hasErrors()) {
          dispatch("submit", values);
        }
        break;
      case "discard": {
        dispatch("discard");
        break;
      }
      default: {
        console.error(`Unknown form action "${event.detail.action}"`);
      }
    }
  }

  function hasErrors() {
    return Object.values(validationErrors).some(Boolean);
  }

  let formRef;
</script>

<svelte:options tag="dynamic-form" />
{@html `${'<'}style${'>'}\n${css}\n</style>`}
<form
  on:submit|preventDefault
  bind:this={formRef}
  part="form">
  <slot name="header" />
  {#each formFields as formField (formField && formField.props && formField.props.name)}
    <FormElement
      {...formField}
      on:input={handleInput}
      on:blur={handleBlur}
      on:action={handleAction}
      on:click={(e) => console.log({ e })}
      {values}
      {touched}
      validationerrors={validationErrors} />
  {/each}
  <slot name="footer" />
</form>

<style type="text/scss">

</style>