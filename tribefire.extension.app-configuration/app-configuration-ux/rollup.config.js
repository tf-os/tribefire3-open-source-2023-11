import externals from "rollup-plugin-node-externals";
import { nodeResolve } from "@rollup/plugin-node-resolve";
import copy from "rollup-plugin-copy";

export default {
  input: "src-ts/contentView.js",
  output: {
    file: "src/contentView.js",
    format: "esm",
  },
  plugins: [
    externals({
      include: [/^..\/tribefire.extension?/],
    }),
    copy({
      targets: [{ src: "src-ts/public/*", dest: "src/" }],
    }),
    nodeResolve(),
  ],
};
