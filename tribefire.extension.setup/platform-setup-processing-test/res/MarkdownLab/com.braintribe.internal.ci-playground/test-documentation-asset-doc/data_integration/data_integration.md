
## General
Creating something new with tribefire is great, but it is useless without any data. The data integration features of tribefire allow you to connect third-party repositories of any kind – as long as they have an API to access them – to your tribefire instance.

## Means of Data Integration
The integration is done via connections and accesses, depending on the implementation. tribefire provides several connections and accesses out-of-the-box, however nothing is stopping you from building your own connections and accesses to provide custom data source integrations.



Data Integration Component    | Description  
------- | -----------
Connection | An object which creates an actual connection to the system that you wish to connect tribefire to. You can think of it as a kind of a pipe which data can be sent along. For more information, see [Available Connection Pools](available_connection_pools.html).   
[Access](access.html) | An object which retrieves data from an actual repository. A 'bridge' between a model and the data stored in a specific repository.
[Smart Access](smart_access.html)   | An object which controls from which integration accesses data is read from and written to. A delegating layer for accesses.    



The configuration of the integration can be managed via tribefire Control Center, while you can check the validity of your integrated data using  tribefire Explorer.
