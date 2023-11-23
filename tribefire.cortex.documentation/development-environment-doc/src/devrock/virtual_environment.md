# Virtual Environment

Virtual Environment is a plugin which helps you maintain different development setups by overriding system properties and environment variables.

## General

Virtual Environment (also referred to as VE) works in conjunction with Artifact Container as part of Braintribe's development tools. VE is used to override system and environmental variables within a workspace, allowing you to have different development setups depending on your requirements.

> Note that there is a global on/off switch for VE which is **off** by default.

## Usage

You can find the UI if you navigate to **Window->Preferences->DevRock Virtual Environment Preferences**. That is the exact place where you can place your overrides for system properties and environment variables.

When you create an override, for each token, you must enter the name of the new override. As soon as you entered it, VE will show you the current value as it’s set by the system, if any. You then enter the value you want to override the system’s setting with and you activate it. For each entry, there is also an on/off switch for convenience.

The other plugins are wired into the VE, i.e. they listen to changes in the virtual environment. Greyface reacts instantly, and will show now the target repository as defined by the Maven settings file in the overridden directory (that means, GF’s view can stay active, no need to close it when manipulating the VE).
