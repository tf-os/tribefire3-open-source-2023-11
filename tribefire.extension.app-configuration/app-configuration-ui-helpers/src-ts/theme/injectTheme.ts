import { ThemeRecord } from "./types";
import { getThemeStore } from "./getThemeStore";

/**
 * @param themeName: string
 * injects css variables into HTMLElement
 * injects from from theme, where theme.name === @themeName
 * 
 * 
 * Usage with SvelteApp Root element:
 * 
 * <script>
 *   activeThemeName.set('light');
 * </script>
 * 
 * <main use:injectTheme={$activeThemeName}/>
 * 
 * 
 * 
 * Usage with window.document.documentElement
 * 
 * <script>
 *   activeThemeName.set('light');
 * </script>
 * 
 * <svelte:window use:injectTheme={$activeThemeName} />
 */
export function injectTheme(
  node: HTMLElement,
  themeName: string,
) {
  if ((node as any) === window || (node as any) === window.document) {
    node = window.document.documentElement;
  }

  let initialStyleProps = null;
  let lastInjectedTheme: [string, any][] = null;
  let unsubscriber;

  const injectStyleProps = (theme: ThemeRecord) => {
    if (theme) {
      const entriesToInject: [string, any][] = Object.entries(theme)
        .filter(([_key, value]) => typeof value === 'string' ? value.trim().length > 0 : true)
        .map(([key, value]) => key.startsWith('--')
          ? [key, value]
          : key.startsWith('-')
            ? ['-' + key, value]
            : ['--' + key, value]
        );

      // console.log('injectStyleProps', node, entriesToInject);
      entriesToInject.forEach(([key, value]) => {
        if (node.style.hasOwnProperty(key)) {
          initialStyleProps[key] = node.style.getPropertyValue(key);
        }
        node.style.setProperty(key, value);
      });

      lastInjectedTheme = entriesToInject;
    }
  }

  const removeStyleProps = (entriesToRemove: [string, any][]) => {
    // console.log('removeStyleProps', node, entriesToRemove);
    if (entriesToRemove) {
      entriesToRemove.forEach(([key, _value]) => {
        if (initialStyleProps?.hasOwnProperty(key)) {
          node.style.setProperty(key, initialStyleProps[key]);
        } else {
          node.style.removeProperty(key);
        }
      });
      lastInjectedTheme = null;
    }
  }

  const subsribeToTheme = (themeName: string) => {
    unsubscriber = getThemeStore(themeName).subscribe(
      theme => {
        if (lastInjectedTheme) removeStyleProps(lastInjectedTheme);
        injectStyleProps(theme);
      }
    );
  }

  // console.log('injectTheme', themeName, node);
  subsribeToTheme(themeName);

  return {
    update: (themeName: string) => {
      // remove props injected by previous theme
      removeStyleProps(lastInjectedTheme);
      // unsubsribe from previous theme changes
      unsubscriber();
      // subscribe to new theme and injectStyleProps to the node
      subsribeToTheme(themeName);
    },
    destroy: () => {
      // remove props injected by current theme
      removeStyleProps(lastInjectedTheme);
      // unsubsribe from current theme changes
      unsubscriber();
    }
  }
}
