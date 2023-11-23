# Exporting Custom Models to tribefire Repository

When developing cartridges, you must ensure that all sources, including custom models, are found in the repository.

## General

Any custom models that you wish to manipulate or query must be deployed to the repository, which can be done using Control Center.

## Target repository configuration in Control Center

Follow the below procedure to configure the destination repository for your tribefire deployables:

1. Open the CortexConfiguration entity type (use `Quick Access` or the `Entity Types` query).
2. Highlight the `artifactRepository` property.
3. Assign the repository `URL`. Provide authentication credentials if necessary.
4. Commit. That's it - your items should now be deployed to the assigned repository.

> When not configured, target repository defaults to `your_tribefire_installation/storage/repository`.

## Deploying models to the repository

<iframe width="560" height="315" src="https://www.youtube.com/embed/5jUY3A1h7y0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
