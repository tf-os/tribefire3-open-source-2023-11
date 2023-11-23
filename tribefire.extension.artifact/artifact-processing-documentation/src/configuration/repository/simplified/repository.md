Repository
==========

A Repository reflects a remote repository for artifacts, such as Maven central or our artifactory based repositories such as core-dev, core-stable etc.

A repository is the home of two different types of artifacts (or assets):
- released artifacts or assets  
Released artifacts are per definition (and agreement) <i>binary stable</i>, that means if you downloaded a certain version, you will never need to download it again, as there is never an artifact with the same version yet a different content. Server-side and client-side contain the same files, i.e. abc:xyz#1.0 is the same locally and remotely. See below the part about the [repositoryPolicyForReleases](./repositorypolicy.md).

- snapshot artifacts or assets  
Snapshot artifacts however are not stable, that means that you will find numerous different content under same version. Server-side and client-side differ absolutely, so for instance locally you'll always see abc:xyz#1.0-SNAPSHOT.jar, but this file doesn't exist locally, but rather something like abc:xyz-1.0-20161220.105848-901454234.jar. What you have locally is always the newest version found on the server <i>at the time you were looking</i> See below the part about [repositoryPolicyForSnapshots](./repositorypolicy.md).

What needs to be configured are where to find it and how to access it.

again, it has its own [Javadoc](javadoc:com.braintribe.model.artifact.processing.cfg.repository.details.Repository).


properties
----------

- name  
A string containing the name (or ID) of the repository

- url  
A string containing the URL of the repository. <i>The string may contain variables that access either environment variables or system properties, so for instance ${env.\*} are supported.</i>

- user   
The name of the user that should be used to logon. <i>The string may contain variables that access either environment variables or system properties, so for instance ${env.\*} are supported</i>.

- password   
The password of the user. <i>The string may contain variables that access either environment variables or system properties, so for instance ${env.\*} are supported.</i>

- repositoryPolicyForReleases   
a [RepositoryPolicy](./repositorypolicy.md) concerning RELEASE. <i>If this property remains empty (or nulled), the repositry is automatically omitted for RELEASE access.</i>

- repositoryPolicyForSnapshots   
a [RepositoryPolicy](./repositorypolicy.md) concerning SNAPSHOTS.<i>If this property remains empty (or nulled), the repositry is automatically omitted for RELEASE access.</i>

- remoteIndexCanBeTrusted  
Set this to true if you are certain that the index information (maven-metdata.xml) from this repo is always correct. Our repositories ensure that, but repositories such as Maven Central do not. Leaving it at false (or setting it to false) makes the extension's features slower when they look for results. Default is <b>false</b>.

- allowsIndexing   
Set this to true if the repositories allows building an index of the files it hosts for a specific artifact (or asset). Our repositories allow that of course, but there are some repositories such as Maven central that do not like it and may put you (or rather the server with is IP) on a black list. Leaving it at false (or setting it to false) makes the extension's features slower when they look for files. Default is <b>false</b>.

- weaklyCertified  
Some repositories' HTTPS certificates are bad (or not properly certified). If left at false (or set to false), the extension's feature's HTTPS access will be lenient, but obviously less secure. Default is <b>false</b>.


Notes
-----

- RepositoryPolicy  
Make sure that you at least specify one RepositoryPolicy (either repositoryPolicyForReleases or repositoryPolicyForSnapshots), otherwise your repository will be inactive in any case.

- variables   
Keep in mind that without overriding the variables values in the configuration using [Overrides](../overrides.md) as described [here](../configuration.md), the environment (or system) of the server is used and that you most probably do not have any control over it. In other words: it will most likely not work as you expect it to.

- remoteIndexCanBeTrusted   
The impact of this option is only of interest if you are looking for a specific artifact (or asset). In that case, the extension's feature will ignore the index as reported by the repository and <b>still try to download the artifact</b>. You are resolving a range (say [1.0,1.1)), then you are doomed to trust the remote repository. <i>One of the reasons why the Maven comunity only sparingly use ranges, even though it's not clear what came first: the reluctance to use ranges and the subsequence poor enforcement of the indexes on Maven Central, or the poor enforcement which led to the comunity's reluctance.</>

- allowsIndexing   
The impact of this option is two-fold. If activated and the remote repository doesn't have any index information about an artifact (or asset), the feature will index the remote repository on its own (by enumerating the artifact's version folders and creating an index from this enumeration). On the part level, indexing will allow the features to know what files exist in the remote repository <b>prior</b> of trying to access them. If deactived, the feature will always try to download the part. Of course, it does still builds an index, but if you use an empty local repository, you wont profit from it.
