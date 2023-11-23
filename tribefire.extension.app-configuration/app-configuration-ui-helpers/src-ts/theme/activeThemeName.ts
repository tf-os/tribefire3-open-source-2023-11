import { writable } from "svelte/store";

export const activeThemeName = writable<string>(null);
