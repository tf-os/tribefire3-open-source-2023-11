<svelte:options tag="wc-input" />

<script lang="ts">
  export let type: string = "";
  export let max: string = null;
  export let id: any = null;
  export let name: string = "";
  export let placeholder: string = "";
  export let maxlength: number = null;
  export let label: string = "";
  export let xstyle: string = "";

  let rootElement: HTMLInputElement;
  let value;

  const dispatch = (eventName: string, detail?: any) => {
    const event = new CustomEvent(eventName, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    rootElement.dispatchEvent(event);
    return event;
  };

  function disValue() {
    dispatch("onblur", {
      value: rootElement.value,
    });
  }
</script>

<div class="field">
  <input
    bind:this={rootElement}
    {type}
    {max}
    {id}
    {name}
    {placeholder}
    {maxlength}
    value={value ?? null}
    style={xstyle}
    on:blur={disValue}
  />

  <label for={id}>{label}</label>
</div>

<style type="text/scss">
  .field {
    display: inline-flex;
    flex-flow: column-reverse;
    margin: 0 1px;
  }

  label,
  input {
    transition: all 0.2s;
    touch-action: manipulation;
  }

  input {
    color: #808080;
    padding: 5px;
    border: 0.5px solid silver;
    border-radius: 4px;
    background: white;
    -webkit-appearance: none;
    cursor: text;
  }

  input:focus {
    outline: 0;
    // border-bottom: 1px solid #666;
  }

  label {
    background: white;
    padding: 0 2px;
    color: #808080;
    width: max-content;
  }

  /**
* Translate down and scale the label up to cover the placeholder,
* when following an input (with placeholder-shown support).
* Also make sure the label is only on one row, at max 2/3rds of the
* field—to make sure it scales properly and doesn't wrap.
*/
  input:placeholder-shown + label {
    cursor: text;
    max-width: 66.66%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transform-origin: left bottom;
    transform: translate(3px, 1.3rem) scale(1);
  }
  /**
* By default, the placeholder should be transparent. Also, it should 
* inherit the transition.
*/
  ::-webkit-input-placeholder {
    opacity: 0;
    transition: inherit;
  }
  /**
* Show the placeholder when the input is focused.
*/
  input:focus::-webkit-input-placeholder {
    opacity: 1;
  }
  /**
* When the element is focused, remove the label transform.
* Also, do this when the placeholder is _not_ shown, i.e. when 
* there's something in the input at all.
*/
  input:not(:placeholder-shown) + label,
  input:focus + label {
    transform: translate(3px, 7px) scale(0.9);
    // transform: translate(0, 0) scale(1);
    border-top: 0.5px solid silver;
    border-radius: 4px;
    width: max-content;
    cursor: pointer;
  }

  input[type="date"] {
    width: 135px;
    padding: 3px;
  }
  input[type="number"] {
    width: 63px;
  }
</style>
