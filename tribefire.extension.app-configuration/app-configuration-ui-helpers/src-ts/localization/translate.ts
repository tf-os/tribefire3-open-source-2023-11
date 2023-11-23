import { derived } from "svelte/store";
import { format as i18nFormat } from "svelte-i18n";
import type { MessageObject } from "svelte-i18n/types/runtime/types"

const DEFAULT_INTERPOLATION = {
  values: {}
};

export const translate = derived(
  i18nFormat,
  (translateFn) => (
    key: string,
    values: MessageObject = null,
  ) => {
    try {
      return translateFn(key, values || DEFAULT_INTERPOLATION);
    } catch (error) {
      console.error(`"${key}" interpolation error: `, error);
      return key;
    }
  }
);

export const translateAsStore = (key, interpolation = null) => {
  return derived(translate, translateFn => translateFn(key, interpolation));
};
