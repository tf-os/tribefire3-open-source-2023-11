repository policy
=================

A repository policy declares how the owning repository should be used, and especially, when it's index data needs to be updated. It also - and in case of non-persistant caches more importantly - declares whether it should be used at all for certain types of artifacts (or assets), and how much (if at all) it should check one the consitency of the downloaded files.

If you are looking for the differentiation of <i>RELEASE</i> and <i>SNAPSHOT</i>, see the respective text in the section about [Repository](./repository.md).

There is of course [Javadoc](javadoc:com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy) about the RepositoryPolicy.

properties
------------

- enabled   
A simple flag that activates or deactivates the repository's role in mode the policy is used for, i.e. either <i>RELEASE</i> or <i>SNAPSHOT</i>. Keep in mind that you need to set this flag to true explicitly.

- checksumPolicy   
This controls how (if at all) the respective features should check whether the files they downloaded are not corrupted (or in tech terms whether the hashes match). The value you enter here is an enum with 3 possible values: <b>fail</b>,<b>warn</b>,<b>ignore</b>. If you do not specifiy any checksumPolicy, it defaults to <b>ignore</b>. Keep in mind that setting it to something other than <b>ignore</b> will prompt the respective features to also download the hash stored remotely, calculate the hash from the downloaded file and compare them, so it slows down the feature.

- updatePolicy  
This controls when (if at all) the cached index information of a remote repository is to be refreshed. Obviously, this setting has no impact on you if you always request an empty local repository (aka cache in this respect). But if you reuse an existing one - and using one multiple times increases the speed of any of the extension's feature tremendously - the settings is of imporatance.  
Basically, you can set any of the following values : <b>always</b>,<b>never</b>, <b>daily</b>, <b>interval</b> and <b>dynamic</b>. See below of a detailed description.


- updatePolicyParameter  
This field is used to give addtional information that is required for the two updatePolicy options <b>interval</b> (the interval in minutes) or <b>dynamic</b> (the URL to query for updates). See below for a detailed description.


Update policy
-------------
All update policies below act on the remote repository within the role it is attached to, so either <i>RELEASE</i> or <i>SNAPSHOT</i>. See above for a link.

- never  
This means that the index information is never updated. Only if nothing is found locally, the index is downloaded from the remote repository. Changes that happen after that point in time are not reflected.

- always  
This in turn means that the index information is always updated, so even if there is local index information, the remote repository is accessed and its current index data is downloaded.

- daily  
Somehow boringly this means that the indices should be refreshed every 24 hours.

- interval  
This option sets an interval, i.e. that the index files should be updated when a certain amount of minutes has passed. <i>If this option is chosen, the actual number of minutes must be present in the <b>updatePolicyParameter</b></i>

- dynamic
This option is available if the remote repository reflects on changes on the artifact it contains. Standard (dumb) Maven repository can't do that, but our repositories (all hosted by BT) do. <i>If you choose this option, the URL where the reflecting service can be reached needs to be set in <b>updatePolicyParameter</b></i>
