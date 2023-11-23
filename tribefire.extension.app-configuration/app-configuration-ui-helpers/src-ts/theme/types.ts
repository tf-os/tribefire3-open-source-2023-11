import type { Writable } from "svelte/store";

export type ThemeRecord = Record<string, any>;

export type ThemeStore = Writable<ThemeRecord>;
