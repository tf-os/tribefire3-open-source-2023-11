<script>
  import { createEventDispatcher } from "svelte";
  import ErrorMessage from "./error-message.svelte";
  import { generateNextUniqueId } from "../lib/web-form.helpers.ts";

  const dispatch = createEventDispatcher();

  export let label = "";
  export let value = "";
  export let type = null;
  export let error = null;

  const id = generateNextUniqueId();
</script>

<svelte:options tag="dynamic-form-input" />
{#if type !== 'checkbox'}
  <label part="label" for={id}>{label}</label>
  <input part="input" {type} {value} {id} on:input on:blur />
{:else}
  <div>
    <input part="input" {type} checked={value} {id} on:input on:blur />
    {#if type !== 'hidden'}
      <label part="label" for={id}>{label}</label>
    {/if}
  </div>
{/if}
{#if type !== 'hidden'}
  <ErrorMessage message={error} />
{/if}
