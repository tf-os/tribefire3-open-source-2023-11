<svelte:options tag="wc-message-button" />

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
      d="M2 6C2 4.89543 2.89543 4 4 4H20C21.1046 4 22 4.89543 22 6V17C22 18.1046 21.1046 19 20 19H15.4142L12.7071 21.7071C12.3166 22.0976 11.6834 22.0976 11.2929 21.7071L8.58579 19H4C2.89543 19 2 18.1046 2 17V6ZM20 6H4V17H9C9.26522 17 9.51957 17.1054 9.70711 17.2929L12 19.5858L14.2929 17.2929C14.4804 17.1054 14.7348 17 15 17H20V6ZM6 9.5C6 8.94772 6.44772 8.5 7 8.5H17C17.5523 8.5 18 8.94772 18 9.5C18 10.0523 17.5523 10.5 17 10.5H7C6.44772 10.5 6 10.0523 6 9.5ZM6 13.5C6 12.9477 6.44772 12.5 7 12.5H13C13.5523 12.5 14 12.9477 14 13.5C14 14.0523 13.5523 14.5 13 14.5H7C6.44772 14.5 6 14.0523 6 13.5Z"
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
