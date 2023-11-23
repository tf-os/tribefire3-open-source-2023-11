
## General
The smart access is a specific type of `IncrementalAccess` that is used as a delegating layer, and controls the logic of mapped data between its defined smart model and <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">accesses</a> from where the data required for smart model elements is stored. Once deployed, the integration is abstracted and allows data consumers to access the smart model as an API (either Java or REST), following tribefire's <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model_as_API}}">model-as-API</a> paradigm.

{% include image.html file="SmartAccessOverview.png" max-width=500 %}

## Smart Mapper
The smart access works in tandem with the smart mapper, a tool accessible from <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>. The mapper provides the logic and the access carries out the actual functionality. This provides a level of data abstraction, since once the smart access is deployed, developers need only using one of the APIs (Java or REST) to reference the smart model, rather than trying to access the data directly.

During development, the smart access can also function in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.simulation_mode}}">simulation mode</a>, so that no real data needs to be integrated during development.
