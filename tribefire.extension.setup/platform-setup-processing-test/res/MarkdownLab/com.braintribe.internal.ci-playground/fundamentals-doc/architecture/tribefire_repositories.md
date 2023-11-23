# Tribefire Repositories

## General
Tribefire assets and artifacts are provided via Maven repositories, available on <https://artifactory.EXAMPLE.com/artifactory>. They include core components, such as:

* tribefire Services 
 * Control Center
  * Explorer
   * Modeler
 

Certain cartridges are provided as well, such as the Enablement Cartridges (i.e. <b>Simple Cartridge</b>, <b>Demo Cartridge</b>). Libraries required for cartridge development are also included.


<!--In addition to the dependencies for the Java API, any deployed models are also available through the target instance of the repository. The model is deployed via the action in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>, and it is deployed to the same container of tribefire Repository.-->

When setting up your development environment, ensure that you point to the correct repository in your Maven `settings.xml` file.
{%include tip.html content="For more information on setting up your IDE, see [Setting Up IDE](setting_up_ide.html)."%}


<!-- MLA: this is no longer true with Pathform Assets and Jinni. Almost everyone needs access to a repo now.
{%include note.html content="If you are building web applications using REST or Javascript, you do not need the repositories."%} -->

## Browsing the repos
You can browse the repositories via the Artifactory Web UI available at <https://artifactory.EXAMPLE.com/>. Artifactory is a unviversal repository manager which e.g. hosts Maven and Docker repositories. For general information on Artifactory please go to 
`https://www.jfrog.com/confluence/display/RTF/Welcome+to+Artifactory`.

## Repositories
There are multiple tribefire repositories. For most users/developers, we recommend to use the `core-stable` repository, where tested updates are provided every few weeks.

{% include image.html file="artifactory.png" max-width=600 %}


{%include note.html content="Which repositories you have access to depends on your account/permissions. For example, the repository `core-stable` provides latest releases of tribefire assets. Most users have access to it, whereas repository `core-dev` is mostly used internally by core developers at Braintribe, and therefore access is restricted."%}

Detailed information on tribefire repositories is available below:


### Repository `core-stable`
<b>URL:</b> <https://artifactory.EXAMPLE.com/artifactory/core-stable>

<b>Description:</b> the `core-stable` repository is used to share latest releases of core assets. This is the recommended repository for most users/developers.

<b>Users:</b> tribefire developers who want to use the latest released tribefire features.

<b>Tests:</b> further tests (including manual tests), in addition to the tests done for the `core-dev` repository.

<b>Update Mode:</b> full repository copy of `core-dev`; in the future we may switch to incremental updates.

<b>Update Frequency:</b> every few weeks or more frequently on demand (e.g. if there is a critical bug).

<b>Cleanup:</b> outdated revisions of artifacts may be removed at any time.

### Repository `core-stable-YYYYMMDD`

<b>URL:</b> <https://artifactory.EXAMPLE.com/artifactory/core-stable-YYYYMMDD>, e.g. <https://artifactory.EXAMPLE.com/artifactory/core-stable-20180927>

<b>Description:</b> these repositories have the same content as `core-stable` on that particular day. They never receive updates though and thus never change.

<b>Users:</b> developers who want to decide themselves when they get updates.

<b>Tests:</b> see `core-stable`.

<b>Update Mode:</b> see `core-stable`.

<b>Update Frequency:</b> every few weeks we create a new copy, e.g. `core-stable-20180927`. This copy then isn't modified anymore.

<b>Cleanup:</b> The content of the repositories never changes, thus there is no removal of outdated artifact revisions. Repositories older than three months are considered obsolete and removed. Note that we can keep the old repositories longer on demand, just let us know!

### Repository core-dev
<b>URL:</b> <https://artifactory.EXAMPLE.com/artifactory/core-dev>

<b>Description:</b> The `core-dev` repository is used by core developers to share the very latest versions of their assets. The repository content is updated incrementally and individual assets may be updated many times per hour.

<b>Users:</b> Core developers and other developers who need access to the very latest features and changes.

<b>Tests:</b> most unit tests (except for slow tests), integration tests for terminal artifacts run against Cloud based test environments.

<b>Update Mode:</b> incremental (individual artifacts are added all the time).

<b>Update Frequency:</b> very often (see description).

<b>Cleanup:</b> for each **major.minor** version, old versions are automatically deleted. For example, when `tribefire-services#2.0.273` gets published, `tribefire-services#2.0.267` may be removed. Note that this is no problem, because the latest revision will be fetched automatically.

### Repository `third-party`

<b>URL:</b> <https://artifactory.EXAMPLE.com/artifactory/third-party>

<b>Description:</b> The third-party repository is a Braintribe internal repository for third party libraries used by tribefire core components and for cartridge development. The repository content is updated incrementally, but already deployed versions won't be modified  (unless the library e.g. also changes on Maven Central).

Braintribe uses its own repository for third party libraries, because this e.g. enables us to check licenses, to be independent of a public repository such as Maven Central, and to provide libraries collected from multiple sources through a single repository. 

<b>Users:</b> Most Braintribe developers who work with tribefire and some development partners. Tribefire developers working at other companies would typically use their own company repository. If such a repository doesn't exist, one could use one of the public repositories, such as Maven Central or JCenter.

<b>Tests:</b> We assume third party libraries are tested before being pushed to a public repository, such as Maven Central. This means we usually do not test these libraries. However, we do make sure that the libraries work with tribefire through unit and integration tests (and thus we test the libraries indirectly, to some extent).

<!-- MLA: this section is outdated now IMO and could confuse readers. If we explain how to publish models we could/should do that for any asset.
## Exporting Models to tribefire repository
When developing cartridges, you must ensure that all sources, including custom models, are found in the repository.
{%include tip.html content="[For more information, see Exporting Custom Models to tribefire Repository](exporting_custom_models.html)"%} -->

