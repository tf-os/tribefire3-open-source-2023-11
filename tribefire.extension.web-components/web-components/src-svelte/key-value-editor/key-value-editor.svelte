<svelte:options tag="key-value-editor" />

<script lang="ts">
  import { afterUpdate, get_current_component } from "svelte/internal";
  import type { HighlightWords } from "highlight-words";
  import highlightWords from "highlight-words";

  import { findEntry, removeButton } from "@app-configuration";
  import { highlightChunks } from "./actions/useHighlightChunks";
  import { validateJsonCode } from "./helpers/validateJsonCode";

  export let descriptors: Record<string, string> = null;
  export let entries;
  export let code: string;
  export let can_add_key: boolean = false;
  export let can_edit_key: boolean = false;
  export let mode: "form" | "code" = "form";

  interface FormChunk {
    key: string;
    keyChunks: HighlightWords.Chunk[];
    valueChunks: HighlightWords.Chunk[];
    descriptionChunks: HighlightWords.Chunk[];
  }
  let unfilteredFormChunks: FormChunk[] = [];
  let formChunks: FormChunk[] = [];
  let pendingKeyRename: [string, string] = null;

  $: if (mode === "form") {
    unfilteredFormChunks = entries
      ? entries.map(({ key, value }) => {
          const description = descriptors ? descriptors[key] || "" : "";
          return {
            key,
            keyChunks: highlightWords({ text: key, query }),
            valueChunks:
              typeof value === "string"
                ? highlightWords({ text: value, query })
                : [
                    {
                      key: value,
                      text: value,
                      match: false,
                    },
                  ],
            descriptionChunks:
              typeof description === "string"
                ? highlightWords({ text: description, query })
                : [
                    {
                      key: description,
                      text: description,
                      match: false,
                    },
                  ],
          };
        })
      : [];
  }

  $: formChunks = query
    ? unfilteredFormChunks.filter(
        ({ keyChunks, valueChunks, descriptionChunks }) =>
          keyChunks.some((chunk) => chunk.match) ||
          valueChunks.some((chunk) => chunk.match) ||
          descriptionChunks.some((chunk) => chunk.match)
      )
    : unfilteredFormChunks;

  // code-mode variables
  let codeChunks: HighlightWords.Chunk[] = [];

  $: if (mode === "code") {
    codeChunks = highlightWords({ text: code, query });
  }

  // search variables
  let query = "";

  // elementReferences
  let formRefs: { [key: string]: HTMLTableCellElement } = {};
  let codeEditorRef: HTMLPreElement;
  let focusedElementIdentifier: string;

  // callbacks
  const afterUpdateCallbacks = [];

  afterUpdate(() => {
    afterUpdateCallbacks.forEach((cb) => cb());
    afterUpdateCallbacks.length = 0;
  });

  // event handlers
  function handleKeyChanged(prevKey: string, event: FocusEvent) {
    const tableCell = event.target as HTMLTableCellElement;
    const nextKey = tableCell.innerText.trim();

    if (prevKey === null) {
      if (nextKey) {
        // add new key
        // const nextValue = formRefs['value:new'].innerText;
        // formRefs['value:new'].innerHTML = '';
        afterUpdateCallbacks.push(() => formRefs[`value:${nextKey}`]?.focus());
        dispatchEvent("create_entry", { key: nextKey, value: "" });
      }
      tableCell.innerHTML = "";
    } else if (!nextKey) {
      // delete key
      dispatchEvent("remove_entry", { key: prevKey });
    } else if (prevKey !== nextKey) {
      // edit existing key
      setTimeout(() => {
        // console.log({
        //   focusedElementIdentifier,
        //   formRef: formRefs[`value:${prevKey}`],
        //   hasFocus: focusedElementIdentifier === `value:${prevKey}`,
        // });
        pendingKeyRename = [prevKey, nextKey];
        const event = dispatchEvent("edit_entry_key", {
          oldName: prevKey,
          newName: nextKey,
        });
        if (event.defaultPrevented) {
          const cellElement = formRefs[`key:${prevKey}`];
          const entry = cellElement ? findEntry(entries, prevKey) : null;
          if (entry && cellElement) {
            console.log({ entry, cellElement });
            cellElement.innerText = entry.key;
            cellElement.focus();
            document.execCommand("selectAll", false, null);
          }
        } else if (focusedElementIdentifier === `value:${prevKey}`) {
          afterUpdateCallbacks.push(() =>
            formRefs[`value:${nextKey}`]?.focus()
          );
        }
      });

      (window as any).formRef = formRefs[`value:${prevKey}`];
    }
  }

  function handleValueChanged(key: string, event: FocusEvent) {
    if (pendingKeyRename && pendingKeyRename[0] === key) {
      pendingKeyRename = null;
    } else {
      const tableCell = event.target as HTMLTableCellElement;
      const nextValue = tableCell.innerText.trim();

      dispatchEvent("edit_entry_value", { key, value: nextValue });
    }
  }

  function handleDescriptorChanged(key: string, event: FocusEvent) {
    if (pendingKeyRename && pendingKeyRename[0] === key) {
      pendingKeyRename = null;
    } else {
      const tableCell = event.target as HTMLTableCellElement;
      const description = tableCell.innerText.trim();

      dispatchEvent("edit_description", { key, description });
    }
  }

  function handleCodeBlur(event: FocusEvent) {
    if (!document.hasFocus()) return;

    const codeElement = event.target as HTMLPreElement;
    const codeText = codeElement.innerText;
    try {
      const parsed = JSON.parse(codeText);
      const error = validateJsonCode(parsed);
      if (error) throw error;
      dispatchEvent("code_change", { parsed });
      mode = mode;
    } catch (e) {
      const continueEditing = confirm(
        `${
          e?.message || e
        }\n\nPress OK to continue editing or cancel to discard ALL the changes?`
      );
      if (continueEditing) {
        codeEditorRef.focus();
      } else {
        codeChunks = highlightWords({ text: code, query });
      }
    }
  }

  function handleKeyPress(event: KeyboardEvent, key?: string) {
    if (event.key === "Enter") {
      event.preventDefault();
      (event.target as HTMLElement)?.blur();
      if (key) {
        // focus next row
        const index = entries?.findIndex((entry) => entry.key === key) ?? -1;
        if (index >= 0) {
          const nextKeyToFocus = entries[index + 1]
            ? entries[index + 1].key
            : "new";
          formRefs[`key:${nextKeyToFocus}`]?.focus();
        }
      }
    }
  }

  function pastePlainTextOnly(e) {
    // cancel paste
    e.preventDefault();
    e.stopPropagation();
    // get text representation of clipboard
    var text = (e.originalEvent || e).clipboardData.getData("text/plain");
    // insert text manually
    document.execCommand("insertText", false, text);
  }

  const thisComponent = get_current_component() as HTMLDivElement;
  const dispatchEvent = (name: string, detail: any) => {
    const event = new CustomEvent(name, {
      bubbles: true,
      cancelable: true,
      composed: true,
      detail,
    });
    thisComponent.dispatchEvent(event);
    return event;
  };

  $: showDescriptionColumn = descriptors;
  let addNewItemRowHeight: number = 0;
</script>

<main>
  <div class="toolbar">
    <div
      class="mode-toggle"
      class:active={mode === "form"}
      on:click={() => (mode = "form")}
    >
      form
    </div>
    <div
      class="mode-toggle"
      class:active={mode === "code"}
      on:click={() => (mode = "code")}
    >
      code
    </div>
    <div class="search-box">
      <label class="search-label" for="key-value-editor-search" />
      {#if query && mode === "form"}
        <div class="search-results-info">
          {formChunks.length} of {unfilteredFormChunks.length} displayed
        </div>
      {/if}
      <input
        bind:value={query}
        class="search-input"
        placeholder="Search ..."
        id="key-value-editor-search"
      />
      {#if query}
        <label
          class="clear-search"
          for="key-value-editor-search"
          on:click={() => (query = "")}
        />
      {/if}
    </div>
  </div>

  <div class="form-wrapper" class:hidden={mode !== "form"}>
    <table style="--add-new-item-row-hight: {addNewItemRowHeight}px;">
      {#if can_add_key}
        <tr bind:clientHeight={addNewItemRowHeight} class="add-new-item-row">
          <td class="key">
            <div
              bind:this={formRefs[`key:new`]}
              contentEditable
              on:paste={pastePlainTextOnly}
              on:blur={handleKeyChanged.bind(null, null)}
              on:keypress={(event) => handleKeyPress(event)}
            />
          </td>

          <td colspan={showDescriptionColumn ? 2 : 1}>
            <div />
          </td>
        </tr>
      {/if}

      <tr class="table-header-row">
        <th>key</th>
        <th>value</th>
        {#if showDescriptionColumn}
          <th>description</th>
        {/if}
      </tr>

      {#if !entries || entries.length === 0}
        <tr>
          <td colspan={showDescriptionColumn ? 3 : 2}>
            <div>
              {#if can_add_key}
                No items
              {:else}
                No items yet and your user account does not have sufficient
                privileges to create one.
              {/if}
            </div>
          </td>
        </tr>
      {/if}

      {#each formChunks as { key, keyChunks, valueChunks, descriptionChunks } (key)}
        <tr>
          <td class="key" on:click={() => formRefs[`key:${key}`]?.focus()}>
            <div
              bind:this={formRefs[`key:${key}`]}
              class="animate-by-remove-button"
              contentEditable={can_edit_key}
              use:highlightChunks={keyChunks}
              use:removeButton={{
                getAnimatedNodes: (node) =>
                  node?.parentElement?.parentElement?.querySelectorAll(
                    ":scope .animate-by-remove-button"
                  ),
              }}
              on:focus={() => (focusedElementIdentifier = `key:${key}`)}
              on:blur={handleKeyChanged.bind(null, key)}
              on:keypress={(event) => handleKeyPress(event, key)}
              on:paste={pastePlainTextOnly}
              on:remove={() => dispatchEvent("remove_entry", { key: key })}
            />
          </td>
          <td on:click={() => formRefs[`value:${key}`]?.focus()}>
            <div
              bind:this={formRefs[`value:${key}`]}
              class="animate-by-remove-button"
              contentEditable
              on:focus={() => (focusedElementIdentifier = `value:${key}`)}
              on:blur={handleValueChanged.bind(null, key)}
              on:paste={pastePlainTextOnly}
              use:highlightChunks={valueChunks}
            />
          </td>
          {#if showDescriptionColumn}
            <td
              class="description animate-by-remove-button"
              on:click={() => formRefs[`description:${key}`]?.focus()}
            >
              <div
                bind:this={formRefs[`description:${key}`]}
                contentEditable
                on:focus={() =>
                  (focusedElementIdentifier = `description:${key}`)}
                on:blur={handleDescriptorChanged.bind(null, key)}
                on:paste={pastePlainTextOnly}
                use:highlightChunks={descriptionChunks}
              />
            </td>
          {/if}
        </tr>
      {/each}
    </table>
  </div>

  <pre
    bind:this={codeEditorRef}
    class="code-editor"
    class:hidden={mode === "form"}
    contentEditable
    on:blur={handleCodeBlur}
    on:paste={pastePlainTextOnly}
    use:highlightChunks={codeChunks}
  />
</main>

<style type="text/scss">
  * {
    box-sizing: border-box;
  }
  main {
    min-width: 0;
    max-width: 100%;
    min-height: 0;
    max-height: 100%;
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr auto;
    & > * {
      min-height: 0;
      padding-left: var(--left-inset, 0);
    }
  }
  .toolbar {
    display: flex;
    align-items: center;
  }

  .mode-toggle {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0.25em 0.5em;
    min-height: 100%;
    margin-right: 0.5em;
    border: 1px solid var(--primary-color);
    cursor: pointer;
    &.active {
      background: var(--primary-color);
    }
    &:hover:not(.active) {
      background-color: var(--primary-color-tint);
    }
  }

  .search-box {
    position: relative;
    display: inline-block;
  }

  .search-input {
    border-radius: 4px;
    border: 1px solid silver;
    padding: 4px 28px;
  }

  .search-label {
    position: absolute;
    top: 0;
    left: 4px;
    display: block;
    width: 20px;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='rgba(0,0,0,.54)' d='M20.49 19l-5.73-5.73C15.53 12.2 16 10.91 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.41 0 2.7-.47 3.77-1.24L19 20.49 20.49 19zM5 9.5C5 7.01 7.01 5 9.5 5S14 7.01 14 9.5 11.99 14 9.5 14 5 11.99 5 9.5z'/%3E%3C/svg%3E");
    background-position: center;
    background-size: contain;
    background-repeat: no-repeat;
  }

  .clear-search {
    position: absolute;
    top: 0;
    right: 4px;
    display: block;
    width: 12px;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 352 512'%3E%3Cpath fill='rgba(0,0,0,.54)' d='M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z' class=''%3E%3C/path%3E%3C/svg%3E");
    background-position: center;
    background-size: contain;
    background-repeat: no-repeat;
  }

  .search-results-info {
    position: absolute;
    top: 100%;
    left: 28px;
    color: gray;
    font-size: 0.8em;
  }

  table {
    border-spacing: 1px;
  }

  tr {
    position: relative;
    background-color: rgb(234, 234, 234);
    &:nth-child(2n + 1) {
      background-color: rgb(240, 240, 240);
    }
    &.add-new-item-row > :focus-within {
      background-color: var(--primary-color);
    }
  }

  .add-new-item-row > *,
  .table-header-row > * {
    position: sticky;
    top: 0;
    z-index: 1;
    backdrop-filter: blur(2px);
  }

  .add-new-item-row > * {
    background-color: rgba(255, 255, 255, 0.95);
  }

  .table-header-row > * {
    top: var(--add-new-item-row-hight, 0);
    background-color: rgba(192, 192, 192, 0.95);
  }

  th {
    padding: 0.5em;
    text-align: left;
  }

  td {
    position: relative;
    vertical-align: top;
    & > * {
      padding: 0.5em;
      min-height: 100%;
    }
    &.key {
      width: 1%;
      white-space: nowrap;
      font-weight: 500;
      font-style: italic;

      &:not(:focus) > :empty::after {
        opacity: 0.5;
        font-style: italic;
        content: "Add new key ...";
      }
    }

    &.description {
      color: gray;
    }
  }

  .hidden {
    display: none;
  }

  .code-editor {
    font-family: monospace;
    white-space: pre-wrap;
    word-wrap: break-word;
    line-height: 2;
    outline: none;
    padding: 1em;
    background: rgb(246, 246, 246);
    &:focus {
      box-shadow: 0 0 2px 3px Highlight;
    }
  }

  .form-wrapper,
  .code-editor {
    overflow: auto;
    max-width: 100%;
    margin: 16px 0 0;
  }

  .code-editor {
    margin-left: var(--left-inset, 0);
  }
</style>
