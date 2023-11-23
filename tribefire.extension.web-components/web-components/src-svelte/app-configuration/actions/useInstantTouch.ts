export function instantTouch(node: HTMLElement) {
  const handleMouseDown = (event: MouseEvent) => {
    if (event.button === 0) {
      node.dispatchEvent(new CustomEvent('instanttouch'));
    }
  }

  const handleTouchStart = (event: TouchEvent) => {
    if (event.touches.length === 1) {
      node.dispatchEvent(new CustomEvent('instanttouch'));
    }
  }

  node.addEventListener('mousedown', handleMouseDown);
  node.addEventListener('touchstart', handleTouchStart);

  return {
    destroy() {
      node.addEventListener('mousedown', handleMouseDown);
      node.addEventListener('touchstart', handleTouchStart);
    }
  };
}