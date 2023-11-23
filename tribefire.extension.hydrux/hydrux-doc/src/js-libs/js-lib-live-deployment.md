# JS Libraries Live Deployment

**Live deployment** of `JS Libraries` means with proper configuration **the server** is able to **serve the code** directly **from our codebase**. This means we can **re-deploy** our app **by refreshing** the browser **without server restart**.

To achieve this we need a proper file system structure, so that `Jinni` can see our sources and link them from the server. We also need to tell `Jinni` on setup to do so.

## File Structure

Simply said, our file structure needs to be a `Dev Environment`.

For an example with a `JS Library` called `tribefire.tutorial.js:my-js-lib` this means:

```filesystem
root/
    git/
        tribefire.tutorial.js/
            my-js-lib/
                src/
                js-project.yml
    tf/
    dev-environment.yaml
```
Notes:
* The name `git` is important.
* `tf` name is not important, could be something else.
* `dev-environment.yaml` can be empty.
* `js-project.yml` marks a project as a `JS Library` and is created by `Jinni` with `create-js-library` or `create-ts-library`

## Setup

We simply do a local setup inside the `tf` folder with `--installationPath my-js-app` and `--debugJs true`:
```cli
> jinni setup-local-tomcat-platform ${SETUP_ARTIFACT} ... --installationPath my-js-app --debugJs true
```

## How it works

* When doing `Jinni` setup inside `tf/`, it detects `root/` as the root of our `Dev Environment` based on the existence of `dev-environment.yaml`.
* Jinni knows `git/` sub-folder contains sources.
* Since `debugJs` is true, `Jinni` scans the `git/` folder for all `JS Projects`, i.e. artifacts which contain a `js-project.yml` file.
* The `js-project.yml` content is ignored, `Jinni` simply knows the payload (`JS files`) for this `JS Library` lies inside the `src/` sub-folder.
* As part of setup, `Jinni` creates a `js-libraries/` sub-folder in the `context/` folder of `Tribefire Services`.
* `js-libraries/` contains all the `JS Libraries` of our application.
* each library is represented by a symbolic link to either the `src/` sub-folder of the corresponding project within `git/`, if such project exists, or to a `folder` inside local repository where the content of the artifact's `zip:js` part was extracted.