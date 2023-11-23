<svelte:options tag="wc-editable-button" />

<script>
  export let svgsize = 18;
  export let size = 22;
  export let url = "";
  export let shape = "round";
  export let color = "var(--color)";
  export let background = "var(--primary-color-accent)";
  export let xstyle = "";
</script>

<div
  on:click
  class="btn {shape}"
  style="width: {size}px; height: {size}px;background-color:{background}; color:{color}; {xstyle}"
>
  <svg width={svgsize} height={svgsize}>
    <image
      xlink:href={url}
      src="yourfallback.png"
      width={svgsize}
      height={svgsize}
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
    opacity: 0.85;
  }
</style>
