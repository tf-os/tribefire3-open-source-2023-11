<script>
  export let name = null;
</script>

<svelte:options tag="dynamic-form-slot" />

{@html `<slot name=${name}></slot>`}
