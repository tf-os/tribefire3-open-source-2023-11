import commonjs from "@rollup/plugin-commonjs";
import resolve from "@rollup/plugin-node-resolve";
import typescript from "@rollup/plugin-typescript";
import externals from "rollup-plugin-node-externals";
// import { terser } from "rollup-plugin-terser";

const production = !process.env.ROLLUP_WATCH;

export default {
  input: ["src-ts/svelte.ts"],
  output: {
    dir: "src",
    format: "esm",
    sourcemap: true,
  },
  plugins: [
    resolve({
      browser: true,
      // dedupe: ["svelte"],
    }),
    commonjs(),
    externals({
      include: [/^..\/tribefire.extension?/],
    }),
    typescript({
      sourceMap: true,
      inlineSources: !production,
    }),
    // production && terser(),
  ],
};
