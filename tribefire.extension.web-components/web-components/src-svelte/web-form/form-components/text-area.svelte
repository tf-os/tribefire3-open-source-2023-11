<script>
    import ErrorMessage from "./error-message.svelte";
  import { generateNextUniqueId } from "../lib/web-form.helpers.ts";

  export let label = "";
  export let value = "";
  export let rows = null;
  export let columns = null;
  export let error = null;

  const id = generateNextUniqueId();
</script>

<style>

</style>

<svelte:options tag="dynamic-form-textarea" />
<label part="label" for={id}>{label}</label>
<textarea part="textarea" {value} {rows} {columns} {id} on:input on:blur />
<ErrorMessage message={error} />
