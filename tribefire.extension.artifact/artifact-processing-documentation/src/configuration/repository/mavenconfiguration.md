MavenRepositoryConfiguration
============================

The Maven repository configuration consists of a resource that contains a settings.xml as one would find in a Maven setup on a computer. While the extension fully supports the features that can be configured with such a settings.xml, some ramifications were needed to make it compatible with the environment where the extension runs.

references to the local filesystem
------------------------------
 It is obvious that you cannot control the absolute path to the filesystem as you as a caller cannot know where the filesystem available to the extension is actually located. Therefore, what ever you specify in the respective places of the settings.xml is parsed and modified by the extension.

 - local repository
 - activation based on files


 Other than that, all features are supported (even all Devrock ramifications), so basically, you can upload your trusted settings.xml and let the extension use that.


 There is also [JavaDoc](javadoc:com.braintribe.model.artifact.processing.cfg.repository.MavenRepositoryConfiguration) for this configuration entity.

Properties
----------

 The only additional property (other than what is inherited by the basic [RepositoryConfiguration](./configuration.md)) is:

 <table>
     <tr>
         <td>settingsAsResource</td>
         <td>A Resource instance containing the settings.xml</td>
     </tr>    
 </table>
