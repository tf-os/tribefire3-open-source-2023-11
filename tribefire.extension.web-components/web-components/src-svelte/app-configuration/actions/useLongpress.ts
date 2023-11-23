export function longpress(node, enabled = true, threshold = 400) {
  let mousedownTimer = null;
  let touchDownTimer = null;

  let removeMouseUp = null;
  let removeTouchEnd = null;


  const emitEvent = () => {
    node.dispatchEvent(new CustomEvent('longpress'));
  }

  const handleMousedown = (event: MouseEvent) => {
    if (event.button === 0) {
      mousedownTimer = setTimeout(emitEvent, threshold);

      removeMouseUp = (event) => {
        if (!event || event.button === 0) {
          clearTimeout(mousedownTimer);
          mousedownTimer = null;
          node.removeEventListener('mouseup', removeMouseUp);
          removeMouseUp = null;
        }
      };
      
      node.addEventListener('mouseup', removeMouseUp);
    }
  }

  const handleTouchStart = (event: TouchEvent) => {
    if (event.touches.length === 1) {
      touchDownTimer = setTimeout(emitEvent, threshold);

      removeTouchEnd = (event: TouchEvent) => {
        if (event.touches.length === 0) {
          clearTimeout(touchDownTimer);
          touchDownTimer = null;
          node.removeEventListener('touchend', removeTouchEnd);
          removeTouchEnd = null;
        }
      };
      
      node.addEventListener('touchend', removeTouchEnd);
    }
  }

  const start = () => {
    if (enabled && node) {
      node.addEventListener('mousedown', handleMousedown);
      node.addEventListener('touchstart', handleTouchStart);
    }
  }

  const destroy = () => {
    if (node) {
      if (removeMouseUp) removeMouseUp();
      if (removeTouchEnd) removeTouchEnd();
      node.removeEventListener('mousedown', handleMousedown);
      node.removeEventListener('touchstart', handleTouchStart);
    }
  }

  start();

  return {
    destroy,
    update: (updatedEnabled = true, updatedThreshold = 400) => {
      destroy();
      enabled = updatedEnabled;
      threshold = updatedThreshold;
      start();
    },
  };
}