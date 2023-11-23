import type { HighlightWords } from 'highlight-words';

export function highlightChunks(
  node: HTMLElement,
  chunks: HighlightWords.Chunk[],
) {
  function updateChunks(chunks: HighlightWords.Chunk[]) {
    node.innerHTML = "";
    const spans = chunks.map((chunk) => {
      const span = document.createElement('span');
      if (chunk.match) span.style.background = 'var(--primary-color)';
      span.appendChild(document.createTextNode(chunk.text));
      return span;
    });
    node.append(...spans);
  }

  return {
    update: updateChunks
  }
}

