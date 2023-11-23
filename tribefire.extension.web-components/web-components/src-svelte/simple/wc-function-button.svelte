<svelte:options tag="wc-function-button" />

<script>
  export let size = 24;
  export let svgsize = 20;
  export let shape = "round";
</script>

<!-- use https://icons.mono.company/# for ICONS -->
<div on:click class="btn {shape}" style="width: {size}px; height: {size}px; ;">
  <svg
    version="1.1"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    height={svgsize}
    width={svgsize}
    viewBox="0 0 16 16"
  >
    <path
      fill="#444444"
      d="M10 0c0 0-2.1 0-2.7 3l-0.4 2h-1.9l-0.5 1h2.2l-1.4 7c-0.4 2-1.9 2-1.9 2h-1l-0.4 1h3c0 0 2.1 0 2.7-3l1.4-7h2.4l0.5-1h-2.7l0.4-2c0.4-2 1.8-2 1.8-2h1l0.5-1h-3z"
    />
  </svg>
</div>

<style>
  .btn {
    display: flex;
    justify-content: center;
    align-items: center;
    align-content: center;
    text-decoration: none;
    box-shadow: 0 1px 4px rgb(0 0 0 / 60%);
    background-color: var(--primary-color-accent);
    color: var(--color);

    text-align: center;
    font-weight: bold;
    vertical-align: middle;
    overflow: hidden;
    transition: 0.4s;
  }
  .round {
    border-radius: 50%;
  }

  .square {
    border-radius: 4px;
  }

  .btn:hover,
  .btn:focus {
    background-color: var(--primary-color-tint);
  }
</style>
