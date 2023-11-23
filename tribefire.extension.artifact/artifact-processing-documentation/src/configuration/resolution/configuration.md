resolution configuration
=========================

The resolution configuration is required to describe to the extension exactly how you want it to traverse the dependencies of an artifact, and what parts (aka Files) it should download. It is used in conjunction with the [RepositoryConfiguration](../repository/configuration.md).

Again, the [javadoc](javadoc:com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration) tells you all you need to know (besides the notes, see below).

- filtering dependencies  
Filtering is done in three distinct ways. You either specify a rule that acts on the type of a dependency, a rule that acts on tags, or you specify what scope a dependency needs to have to be processed.

  - type rule  
  a simple expression that follows this format description:
  ```
  [<classifier>:]<packaging>[,[<classifier>:]<packaging>,..].
  ```
A comma-delimited string of 'allowed' classifier/packaging tuples, so once you start listing them, you need to list all you want to use. If you do not specify the type rule, an automatic type rule for jar-packaging (or no packaging at all) is created and used.  

    An example would be "asset:man" or "war,zip,man".  

  - tag rule  
  a simple expression that follows this format description
  ```
  [!]<tag>[,[!]<tag>,..]
  ```
It's a comma delimited list of tags, where you can exclude tags by prefixing the tag with an exclamation mark.

    Wildcards \* (only dependencies that have a tag whatsoever are included) or !*  (only dependencies without any tag whatsoever are included) are allowed.

    An example would be "one,!two".

  - scopes  
  Scopes can also be used as filters and come in two flavours.
    - resolution scope  
    A [ResolutionScope](javadoc:/artifact-processing-access-model-1.0/src/com/braintribe/model/artifact/processing/cfg/resolution/ResolutionScope.java) combines several [FilterScope](javadoc:/artifact-processing-access-model-1.0/src/com/braintribe/model/artifact/processing/cfg/resolution/FilterScope.java) into a logical combination named after the usecase.
    Accordingly Its value can be <b>compile</b>, <b>runtime</b>, <b>test</b>. There is also the pass-through value <b>all</b>.
    - filter scope  
    A [FilterScope](javadoc:/artifact-processing-access-model-1.0/src/com/braintribe/model/artifact/processing/cfg/resolution/FilterScope.java) simply describes a single actual scope value. Scopes that extension (as it follows Maven here) <b>compile</b>, <b>provided</b>, <b>runtime</b>, <b>test</b> and <b>system</b>.  
    You can combine the scopes as you want, i.e. you can define either, both or none of types of scopes. <i>In case you define no scopes at all, the extension will switch to the pass-through filter.</>


- including/excluding optional dependencies  
You can either exclude dependencies marked as optional on the termial, or you can include them into the resolution. Default is not to include them.

- retrieving parts (enriching an artifact)   
As an artifact contains parts, i.e. several files like the pom, the binary code, the sources etc, you need to specify what parts you want the extension to download during the resolution. If you do not specify any, only the pom will be download (as it is required to identify the artifact and to enumerate its dependencies).
A part specification looks just as a type rule specification, i.e. its format is
      [<classifer>:]<type>
You can add any number of such constructs to the ResolutionConfiguration, if such a part is found by the extension, it will try to download it. The downloading process will make sure that only if all requested parts were successfully downloaded, the artifact's content will show up. Otherwise, if a single download fails, it wil remove any previously downloaded (during this enrichment process) parts.
