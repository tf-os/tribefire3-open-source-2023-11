import svelte from "rollup-plugin-svelte";
import commonjs from "@rollup/plugin-commonjs";
import resolve from "@rollup/plugin-node-resolve";
import serve from "rollup-plugin-serve";
import typescript from "@rollup/plugin-typescript";
// import typescript from "rollup-plugin-typescript2";
import typescriptCompiler from "typescript";
import { terser } from "rollup-plugin-terser";
import livereload from "rollup-plugin-livereload";
import sveltePreprocessor from "svelte-preprocess";

import { preprocessOptions } from "./svelte.config";

const IS_DEV_MODE = process.env.NODE_ENV === "development";

const plugins = [
  svelte({
    compilerOptions: {
      ...preprocessOptions,
      dev: IS_DEV_MODE,
    },
    extensions: [".svelte"],
    preprocess: sveltePreprocessor(),
    emitCss: true,
    customeElemets: true,
  }),
  typescript({ typescript: typescriptCompiler }),
  commonjs({ include: "node_modules/**" }),
  resolve(),
  IS_DEV_MODE &&
  serve({
    contentBase: "demo",
    open: true,
    port: 3000,
  }),
  IS_DEV_MODE && livereload({ watch: "./src" }),
  !IS_DEV_MODE && terser(),
];

const webComponentPackages = [
  {
    input: "src-svelte/web-form/web-form.svelte",
    output: "web-form",
  },
  {
    input: "src-svelte/key-value-editor/key-value-editor.svelte",
    output: "key-value-editor",
  },
  {
    input: "src-svelte/app-configuration/app-configuration.svelte",
    output: "app-configuration",
  },
  {
    input: "src-svelte/headline-and-body/headline-and-body.svelte",
    output: "headline-and-body",
  },
  {
    input: "src-svelte/simple/wc-button.svelte",
    output: "wc-button",
  },
  {
    input: "src-svelte/simple/wc-checkbox.svelte",
    output: "wc-checkbox",
  },
  {
    input: "src-svelte/simple/wc-input.svelte",
    output: "wc-input",
  },
  {
    input: "src-svelte/simple/wc-info.svelte",
    output: "wc-info",
  },
  {
    input: "src-svelte/simple/wc-multi-select.svelte",
    output: "wc-multi-select",
  },
  {
    input: "src-svelte/simple/wc-single-select.svelte",
    output: "wc-single-select",
  },
  {
    input: "src-svelte/simple/wc-modal.svelte",
    output: "wc-modal",
  },
  {
    input: "src-svelte/simple/wc-editable-button.svelte",
    output: "wc-editable-button",
  },
  {
    input: "src-svelte/simple/wc-avatar.svelte",
    output: "wc-avatar",
  },
  {
    input: "src-svelte/simple/wc-delete-button.svelte",
    output: "wc-delete-button",
  },
  {
    input: "src-svelte/simple/wc-chevronup-button.svelte",
    output: "wc-chevronup-button",
  },
  {
    input: "src-svelte/simple/wc-chevrondown-button.svelte",
    output: "wc-chevrondown-button",
  },
  {
    input: "src-svelte/simple/wc-chevronleft-button.svelte",
    output: "wc-chevronleft-button",
  },
  {
    input: "src-svelte/simple/wc-chevronright-button.svelte",
    output: "wc-chevronright-button",
  },
  {
    input: "src-svelte/simple/wc-download-button.svelte",
    output: "wc-download-button",
  },
  {
    input: "src-svelte/simple/wc-upload-button.svelte",
    output: "wc-upload-button",
  },
  {
    input: "src-svelte/simple/wc-message-button.svelte",
    output: "wc-message-button"
  },
  {
    input: "src-svelte/simple/wc-edit-button.svelte",
    output: "wc-edit-button"
  },
  {
    input: "src-svelte/simple/wc-function-button.svelte",
    output: "wc-function-button"
  },
  {
    input: "src-svelte/simple/wc-message-button.svelte",
    output: "wc-message-button"
  },
  {
    input: "src-svelte/simple/wc-edit-button.svelte",
    output: "wc-edit-button"
  },
  {
    input: "src-svelte/simple/wc-function-button.svelte",
    output: "wc-function-button"
  },
  {
    input: "src-svelte/simple/wc-burger.svelte",
    output: "wc-burger"
  },



];

const buildConfigs = webComponentPackages.map((webComponentPackage) => {
  const output = [
    {
      file: `demo/lib/${webComponentPackage.output}.js`,
      format: "iife",
      name: webComponentPackage.output.split("-").join(""),
      sourcemap: true,
    },
  ];
  // if (!IS_DEV_MODE) {
  output.push({
    file: `src/${webComponentPackage.output}/index.js`,
    format: "esm",
    sourcemap: true,
  });
  // }
  return {
    input: webComponentPackage.input,
    output,
    plugins,
  };
});

export default buildConfigs;
