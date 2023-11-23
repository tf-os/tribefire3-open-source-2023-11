<svelte:options tag="wc-chevronleft-button" />

<script>
  export let size = 24;
  export let svgsize = 20;
  export let shape = "round";
</script>

<!-- use https://icons.mono.company/# for ICONS -->
<div on:click class="btn {shape}" style="width: {size}px; height: {size}px; ;">
  <svg
    fill="none"
    viewBox="0 0 24 24"
    height={svgsize}
    width={svgsize}
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      xmlns="http://www.w3.org/2000/svg"
      d="M14.7071 5.29289C15.0976 5.68342 15.0976 6.31658 14.7071 6.70711L9.41421 12L14.7071 17.2929C15.0976 17.6834 15.0976 18.3166 14.7071 18.7071C14.3166 19.0976 13.6834 19.0976 13.2929 18.7071L7.29289 12.7071C6.90237 12.3166 6.90237 11.6834 7.29289 11.2929L13.2929 5.29289C13.6834 4.90237 14.3166 4.90237 14.7071 5.29289Z"
      fill="#0D0D0D"
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
