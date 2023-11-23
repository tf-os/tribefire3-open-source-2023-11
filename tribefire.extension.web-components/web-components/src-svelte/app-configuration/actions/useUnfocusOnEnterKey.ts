export function unfocusOnEnterKey(node: HTMLElement, threshold = 500) {
  const handleKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Enter') {
      node?.blur();
    }
  }

  node.addEventListener('keydown', handleKeyDown);

  return {
    destroy() {
      node.removeEventListener('keydown', handleKeyDown);
    }
  };
}