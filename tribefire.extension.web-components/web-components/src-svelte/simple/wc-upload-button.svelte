<svelte:options tag="wc-upload-button" />

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
      d="M11.2929 2.29289C11.6834 1.90237 12.3166 1.90237 12.7071 2.29289L16.7071 6.29289C17.0976 6.68342 17.0976 7.31658 16.7071 7.70711C16.3166 8.09763 15.6834 8.09763 15.2929 7.70711L13 5.41421V16C13 16.5523 12.5523 17 12 17C11.4477 17 11 16.5523 11 16V5.41421L8.70711 7.70711C8.31658 8.09763 7.68342 8.09763 7.29289 7.70711C6.90237 7.31658 6.90237 6.68342 7.29289 6.29289L11.2929 2.29289ZM5 17C5.55228 17 6 17.4477 6 18V20H18V18C18 17.4477 18.4477 17 19 17C19.5523 17 20 17.4477 20 18V20C20 21.1046 19.1046 22 18 22H6C4.89543 22 4 21.1046 4 20V18C4 17.4477 4.44772 17 5 17Z"
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
