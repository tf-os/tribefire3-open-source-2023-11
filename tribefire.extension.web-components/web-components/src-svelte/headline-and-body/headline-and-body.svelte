<svelte:options tag="headline-and-body" />

<script lang="ts">
  export let headline: string = 'Hello world';
  export let body: string = 'Blah blah blah ...';
</script>

<h1>
  {headline}
</h1>
<p>
  {body}
</p>

<style type="text/scss">

</style>