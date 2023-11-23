# What's this about?

This artifact enables legacy setups with plugable DCSA Shared Storage configured to run with a plugin-less module-based world.

If you have such a setup, you only have to add this artifact (actually a  platform library asset) to your setup.

This artifact will be placed on the main classpath, TF detects the relevant class (LegacyDssConverter) and with it's help creates a standard DCSA shared storage configuration based on the plugable one.   