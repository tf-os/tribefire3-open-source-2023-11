SimpliedRepositoryConfiguration
===============================

The simplified repository configuration aims to reduce the amount of required configuration to the max but still support all relevant features.
As it is modelled, there's a [JavaDoc](javadoc:com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration) for this configuration, but there's also a more verbose description below.

properties
----------

 The only additional properties (other than what is inherited by the basic [RepositoryConfiguration](./configuration.md)) are:

<table>
    <tr>
        <td>localRepositoryExpression</td>
        <td>a relative path to your local repository. It will be created in the server's storage</td>
    </tr>
    <tr>
        <td>repositories</td>
        <td>A list of [Repository](./simplified/repository.md) entities</td>
    </tr>    
</table>
