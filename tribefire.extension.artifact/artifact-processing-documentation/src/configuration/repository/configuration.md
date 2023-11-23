configuration
-------------

All features of the extension require some configuration as they require information about the servers to access, how to access them and what credentials to use for the access. Furthermore, some configuration concerns the local cache (i.e. where the downloaded files reside locally).

- per maven settings file <br/>
  Basically, as the extension is Maven compatible (its repository declaration format and semantics at least), you could use a simple copy of a settings.xml as Maven uses. Still, some aspects of the file will be treated differently and are perhaps not functional (yet, if at ever).

- per model <br/>
  There is however a simplified way to declare the configuration, by means of a model.

Common to both configuration types are the following slots:

<table>
    <tr>
        <td>name</td>
        <td>the name of the configuration, i.e. how it is later to be identified by the respective feature's service processor</td>
    </tr>
    <tr>
        <td>description</td>
        <td>a string that contains some description of what the configuration wants to achieve</td>
    </tr>
    <tr>
        <td>environmentOverrides</td>
        <td>a list of [Override](overrides.md) types that allow overriding (or setting) environment variables or system properties.</td>
    </tr>
</table>

The two different configurations are described here

- [MavenRepositoryConfiguration](mavenconfiguration.md) : the resource based maven style configuration
- [SimpliedRepositoryConfiguration](simplifiedconfiguration.md) : the simplified (and spelled-out) configuration.
