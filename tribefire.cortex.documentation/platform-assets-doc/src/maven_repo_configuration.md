
You can configure tribefire to work with:

* a local filesystem-based repository (for example `file://host/path`) without authentication
> Note than if you are on a different operating system, the url to a local file looks differently. For example, on iOS, the url to a local file starts with `file:///path/to/file`.
* a remote HTTP based repository, usually with some form of authentication


>Both repositories must be Maven-based. It is best if the `<localRemoteRepository>` you specified in your `settings.xml` does not point to the `.m2` directory.
>For more information about setting up Maven, see [Quick Installation](asset://tribefire.cortex.documentation:development-environment-doc/quick_installation_devops.md).

### Maven Repository for Deploy Transfer

To configure a platform assets repository:

1. If you haven't already, start tribefire and open Control Center.
2. Navigate to the **System -> General -> Cortex Configuration** entry point or click the **Cortex Configuration** icon on the main page of Control Center.
3. Assign a new instance of `ArtifactRepository` to the **artifactRepository** property. Make sure to provide the `username`, `password`, and `url` to your Maven-compatible repository. 
   * if your `url` points to your file system, for example: `file:/ABSOLUTE-PATH-TO/tf-setups/repository`, tribefire will use the provided location as the local repository. In this case, make absolutely sure that the value of the `<localRemoteRepository>` in your `settings.xml` is the same as the path you provide in this step.
   * if your `url` points to a URL, for example: `https://repository.yourCompanyName.com/repo/`, tribefire will use the provided location as the remote repository
4. Click **Apply** and commit your changes.

> For more information on how to use platform assets, see [Working with Platform Assets](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/working_with_platform_assets.md).

### Maven Repository for Install Transfer

For maven install operations the local repository is used. It is configured the standard Maven way: [https://maven.apache.org/settings.html](https://maven.apache.org/settings.html)



