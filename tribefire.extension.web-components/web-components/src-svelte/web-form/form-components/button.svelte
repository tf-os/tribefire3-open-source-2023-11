<script>
  import { generateNextUniqueId } from '../lib/web-form.helpers.ts'

  export let label = "";
  export let name = null;
</script>

<svelte:options tag="dynamic-form-button" />
<button part="button" {name} on:click>{label}</button>
