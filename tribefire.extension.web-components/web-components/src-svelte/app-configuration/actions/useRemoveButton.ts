interface RemoveButtonOptions {
  addToParent: boolean;
  getAnimatedNodes?: (node: HTMLElement) => HTMLElement[] | NodeListOf<Element>;
  position: 'left' | 'right';
  style: Record<string, string>;
}

const DEFAULT_OPTIONS: RemoveButtonOptions = {
  addToParent: true,
  position: 'left',
  style: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'absolute',
    padding: '0',
    background: 'none',
    fontStyle: 'normal',
    fontSize: '2em',
    cursor: 'pointer',
    filter: 'drop-shadow(1px 1px 4px var(--shadow-color, black))',
  }
}

export function removeButton(
  node: HTMLElement,
  { 
    addToParent = DEFAULT_OPTIONS.addToParent,
    getAnimatedNodes = DEFAULT_OPTIONS.getAnimatedNodes,
    position = DEFAULT_OPTIONS.position,
    style = {},
  }: Partial<RemoveButtonOptions> = {}
) {
  let cleanUp = null;
  let removeButton: HTMLSpanElement;
  let nodeRects: DOMRect[] = [];
  let nodesToAnimate: HTMLElement[];

  const emitEvent = (event: MouseEvent) => {
    if (event.button === 0) {
      node.dispatchEvent(new CustomEvent('remove'));
    }
  }

  const onMouseOver = () => {
    const buttonRect = removeButton.getBoundingClientRect();

    const nodesList = getAnimatedNodes && getAnimatedNodes(node);
    nodesToAnimate = nodesList ? Array.from(nodesList) as HTMLElement[] : [node];

    nodesToAnimate.forEach((node, index) => {
      if (!nodeRects[index] || window.getComputedStyle(node).opacity === '1') {
        nodeRects[index] = node.getBoundingClientRect();
      }

      const nodeRect = nodeRects[index];

      const scale = Math.min(1, buttonRect.width / nodeRect.width, buttonRect.height / nodeRect.height);
      const deltaX = (buttonRect.left + buttonRect.width / 2) - (nodeRect.left + nodeRect.width / 2);
      const deltaY = (buttonRect.top + buttonRect.height / 2) - (nodeRect.top + nodeRect.height / 2);
      node.style.transformOrigin = '50% 50%';
      node.style.transition = 'transform 150ms ease-in-out, opacity 20ms ease-out 150ms';
      node.style.opacity = '0';
      node.style.pointerEvents = 'none';
      node.style.transform = `translate(${deltaX}px, ${deltaY}px) scale(${scale})`;
      node.style.outline = 'none';
    });
    removeButton.style.transformOrigin = '50% 50%';
    removeButton.style.transition = 'transform 50ms ease-in-out 40ms';
    removeButton.style.transform = `scale(1.1)`;
  }

  const onMouseOut = () => {
    let delay = null;
    if (nodesToAnimate) {
      nodesToAnimate.forEach(node => {
        delay ??= window.getComputedStyle(node).opacity === '0' ? 200 : 0;
        node.style.transition = `transform 150ms ease-out ${delay}ms, opacity 50ms ease-in ${delay}ms`;
        node.style.transform = `none`;
        node.style.opacity = `1`;
        node.style.pointerEvents = `all`;
        node.style.removeProperty(`outline`);
      });
    }
    removeButton.style.transition = `transform 100ms ease-in ${(delay ?? 0) + 20}ms`;
    removeButton.style.transform = `none`;
  }

  const handleFocus = (event: FocusEvent) => {
    removeButton = document.createElement('span');
    removeButton.innerHTML = String.fromCodePoint(parseInt ("1f5d1", 16)) || '🗑️';
    const computedStyle = {
      ...DEFAULT_OPTIONS.style,
      ...style,
    };
    Object.entries(computedStyle).forEach(([key, value]) => removeButton.style[key] = value);
    removeButton.style.top = '0';
    removeButton.style.bottom = '0';
    removeButton.addEventListener('mousedown', emitEvent);
    removeButton.addEventListener('mouseover', onMouseOver);
    removeButton.addEventListener('mouseout', onMouseOut);

    const parentElement = addToParent ? node.parentElement : node;
    parentElement.appendChild(removeButton);

    if (position === 'left') {
      removeButton.style.left = `${-removeButton.getBoundingClientRect().width * 1.1}px`;
    } else{
      removeButton.style.right = `${-removeButton.getBoundingClientRect().width * 1.1}px`;
    }

    cleanUp = () => {
      setTimeout(() => {
        onMouseOut();
        removeButton.removeEventListener('mousedown', emitEvent);
        removeButton.removeEventListener('mouseover', onMouseOver);
        removeButton.removeEventListener('mouseout', onMouseOut);
        const parentElement = addToParent ? node.parentElement : node;
        if (parentElement.contains(removeButton)) {
          parentElement.removeChild(removeButton);
        }
        node.removeEventListener('blur', cleanUp);
        cleanUp = null;
      })
    };

    node.addEventListener('blur', cleanUp);
  }

  node.addEventListener('focus', handleFocus);

  return {
    destroy() {
      if (cleanUp) cleanUp();
      node.removeEventListener('focus', handleFocus);
    }
  };
}