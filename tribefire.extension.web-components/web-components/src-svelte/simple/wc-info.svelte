<svelte:options tag="wc-info" />

<script lang="ts">
  let tooltip: boolean = false;
  let height;

  export let tooltipPosition = "center";
  export let tooltipHeight = height;
  export let tooltipWidth = 120;
  export let size = 22;

  let bindTooltipWidth;

  $: topDistance = -6 - height + "px";
  $: centerTooltip = -bindTooltipWidth / 2 + size / 2 + "px";

  function openTooltip(e) {
    e.stopPropagation();

    tooltip = !tooltip;
  }

  function clickOutside(node) {
    const handleClick = (event) => {
      if (node && !node.contains(event.target) && !event.defaultPrevented) {
        node.dispatchEvent(new CustomEvent("click_outside"));
        tooltip = false;
      }
    };
    tooltip
      ? document.addEventListener("click", handleClick, true)
      : document.addEventListener("click", handleClick, false);

    return {
      destroy() {
        document.removeEventListener("click", handleClick, true);
      },
    };
  }

  function handleMouseScroll() {
    tooltip = false;
  }
</script>

<svelte:window on:scroll={handleMouseScroll} />

<div class="info">
  <div
    class="icon"
    style={`width:${size}px; height:${size}px;`}
    use:clickOutside
    on:click={openTooltip}
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="14px"
      height="14px"
      viewBox="0 0 543.059 543.059"
    >
      <path
        d="M346.438,468.137h-18.727V171.06c0-5.062-1.855-9.456-5.557-13.17c-3.709-3.703-8.098-5.557-13.164-5.557H196.62
			c-5.073,0-9.461,1.854-13.17,5.557c-3.708,3.715-5.563,8.103-5.563,13.17v37.454c0,5.074,1.854,9.462,5.563,13.17
			c3.703,3.715,8.091,5.569,13.17,5.569h18.727v240.883H196.62c-5.073,0-9.461,1.855-13.17,5.557
			c-3.708,3.715-5.563,8.104-5.563,13.17v37.455c0,5.074,1.854,9.461,5.563,13.17c3.703,3.715,8.091,5.57,13.17,5.57h149.817
			c5.074,0,9.461-1.855,13.17-5.57c3.703-3.701,5.564-8.09,5.564-13.17v-37.455c0-5.061-1.855-9.455-5.564-13.17
			C355.898,469.984,351.506,468.137,346.438,468.137z"
      />
      <circle cx="267.673" cy="62.92" r="62.92" />
    </svg>

    <!-- 
<svg viewBox="0 0 40 40">
  <path class="close-x" d="M 10,10 L 30,30 M 30,10 L 10,30" />
</svg> -->
  </div>

  {#if tooltip && tooltipPosition == "left"}
    <span
      bind:clientHeight={height}
      bind:clientWidth={bindTooltipWidth}
      class={tooltipPosition}
      style="height:{tooltipHeight +
        'px'}; top:{topDistance}; width:{tooltipWidth + 'px'}; 
      "
    >
      <slot />
    </span>
  {/if}

  {#if tooltip && tooltipPosition == "center"}
    <span
      bind:clientHeight={height}
      bind:clientWidth={bindTooltipWidth}
      class={tooltipPosition}
      style="height:{tooltipHeight + 'px'}; 
            top:{topDistance}; left:{centerTooltip}; 
            width:{tooltipWidth + 'px'};"
    >
      <slot />
    </span>
  {/if}

  {#if tooltip && tooltipPosition == "right"}
    <span
      bind:clientHeight={height}
      bind:clientWidth={bindTooltipWidth}
      class={tooltipPosition}
      style="height:{tooltipHeight + 'px'}; 
            top:{topDistance}; 
            width:{tooltipWidth + 'px'};"
    >
      <slot />
    </span>
  {/if}
</div>

<style type="scss">
  .info {
    display: flex;
    user-select: none; /* supported by Chrome and Opera */
    -webkit-user-select: none; /* Safari */
    -khtml-user-select: none; /* Konqueror HTML */
    -moz-user-select: none; /* Firefox */
    -ms-user-select: none; /* Internet Explorer/Edge */
    position: relative;
    width: fit-content;
    height: fit-content;

    .icon {
      display: flex;
      cursor: pointer;
      border-radius: 50%;
      box-shadow: 0 1px 4px rgb(0 0 0 / 60%);
      background-color: var(--primary-color-accent);
      color: var(--color);
      align-items: center;
      justify-content: center;
    }

    span {
      align-items: center;
      background-color: #fff;
      border-radius: 4px;
      padding: 6px;
      box-shadow: 0 1px 4px rgb(0 0 0 / 60%);
      text-align: center;
      line-height: 1;
      position: absolute;
      z-index: 9999;
      border: 1px solid #8e8e8e;
    }
  }

  .left {
    width: 260px;
    right: 0px;
  }

  .right {
    left: 0px;
  }
</style>
