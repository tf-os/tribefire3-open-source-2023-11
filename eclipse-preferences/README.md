# Eclipse Preferences

## Introduction
This repository provides the Eclipse preferences which we use for tribefire development and related projects. They contain _Formatter_, _Compiler Warnings_, _Clean Up_ and other settings.

## Import 
To import the preferences, first download the latest [preferences file](eclipse-preferences.epf) from this repository. Afterwards in Eclipse navigate to _File / Import / General / Preferences_ and then select the file, choose _Import all_ and then _Finish_.

Note that this has to be done for each workspace separately.

## Changing the Preferences
It's important that we all use the same preferences. Otherwise developers would e.g. get different warnings or format the code in a different way. Therefore, if you have suggestions on how to improve the settings, don't just change your own settings. Instead let's have a discussion and then update the shared settings. In the past we e.g. agreed to increase the line length or to disable some warnings where we got too many false positives.
