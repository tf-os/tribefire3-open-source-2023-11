<script>
  export let message = null;

  const NBSP = String.fromCharCode(160);
</script>

<svelte:options tag="dynamic-form-error-message" />

<div part="error-message">{message || NBSP}</div>
