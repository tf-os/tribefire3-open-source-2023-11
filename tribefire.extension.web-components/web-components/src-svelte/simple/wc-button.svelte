<svelte:options tag="wc-button" />

<script lang="ts">
</script>

<button class="btn" on:click><slot /></button>

<style type="text/scss">
  .btn {
    padding: 5px 10px;
    border-width: 0;
    outline: none;
    border-radius: 5px;
    box-shadow: 0 1px 4px rgb(0 0 0 / 60%);
    background-color: var(--primary-color-accent);
    color: var(--color);
  }

  .btn:hover,
  .btn:focus {
    background-color: var(--primary-color-tint);
  }

  .btn > * {
    position: relative;
  }

  .btn span {
    display: block;
    padding: 12px 24px;
  }

  .btn:before {
    content: "";

    position: absolute;
    top: 50%;
    left: 50%;

    display: block;
    width: 0;
    padding-top: 0;

    border-radius: 100%;

    background-color: rgba(236, 240, 241, 0.3);

    -webkit-transform: translate(-50%, -50%);
    -moz-transform: translate(-50%, -50%);
    -ms-transform: translate(-50%, -50%);
    -o-transform: translate(-50%, -50%);
    transform: translate(-50%, -50%);
  }

  .btn:active:before {
    width: 120%;
    padding-top: 120%;

    transition: width 0.2s ease-out, padding-top 0.2s ease-out;
  }
</style>
