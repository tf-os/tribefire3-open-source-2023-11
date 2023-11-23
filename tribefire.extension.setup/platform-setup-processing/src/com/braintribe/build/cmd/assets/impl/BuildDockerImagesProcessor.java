// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.println;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.process.ProcessException;
import com.braintribe.build.process.ProcessExecution;
import com.braintribe.build.process.ProcessResults;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.TribefireWebPlatform;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.platform.setup.api.BuildDockerImages;
import com.braintribe.model.platform.setup.api.PackagePlatformSetup;
import com.braintribe.model.platform.setup.api.SetupLocalTomcatPlatformForDocker;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.model.setuppackage.RuntimeContainer;
import com.braintribe.utils.ArrayTools;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;

/**
 * Processes {@link BuildDockerImages} requests to build tribefire Docker images.
 *
 * The processor first creates a base directory, by default called <code>docker</code> and then fetches all assets
 * required for the particular setup and creates a folder structure like this:
 * <ul>
 * <li>tribefire-base-image/: contains resources to build the base image such the respective Dockerfile, the startup wrapper or
 * the base tomcat (see below). (Note that this is not needed for tribefire versions efore August 2020, i.e. before
 * tribefire.cortex.services:tribefire-web-platform#2.0.172.)</li>
 * <li>tribefire-base-image/Dockerfile: the Dockerfile used to build the base image.</li>
 * <li>tribefire-base-image/tomcat/: the Tomcat installation without any (container specific) assets and configuration.</li>
 * <li>tomcat-package/: package directory from setup-local-tomcat-platform-for-docker (first setup where we don't add
 * assets to Tomcat installation). This is created during the setup of tribefire-base-image/tomcat/ and is not needed
 * otherwise.</li>
 * <li>package: package directory from package-platform-setup (second setup which we need for disjoint projection). This
 * contains the wep apps and further resources required for building the individual images (see below).</li>
 * </ul>
 *
 * The package directory contains a subfolder for each runtime container, i.e. each Docker image to be built. This
 * folder contains the respective webapp subfolder (<code>webapps/[webapp-name]</code>) and may also contain additional
 * assets and configuration, e.g. modules or runtime properties:
 * <ul>
 * <li>tribefire-control-center/Dockerfile/</li>
 * <li>tribefire-control-center/webapps/tribefire-control-center/</li>
 * <li>tribefire-explorer/Dockerfile/</li>
 * <li>tribefire-explorer/webapps/tribefire-explorer/</li>
 * <li>tribefire-master/Dockerfile/</li>
 * <li>tribefire-master/webapps/modules/</li>
 * <li>tribefire-master/webapps/storage/</li>
 * <li>tribefire-master/webapps/tribefire-services/</li>
 * </ul>
 *
 * Afterwards the processor just runs Docker <code>build</code> command for each <code>Dockerfile</code> (starting with
 * the base image build). Docker then builds the images using the resources listed above.
 *
 * @author michael.lafite
 */
public class BuildDockerImagesProcessor {

	private static final String BASE_IMAGE = "tribefire-base-image";
	private static final String DOCKERFILE = "Dockerfile";
	private static final String DOCKERFILE_TEMPLATE = DOCKERFILE + ".vm";
	private static final String DOCKERIGNORE = ".dockerignore";
	private static final String REWRITE_CONFIG = PlatformAssetDistributionConstants.FILENAME_REWRITECONFIG;

	private static final String PACKAGE = "package";
	private static final String TOMCAT = "tomcat";
	private static final String TOMCAT_PACKAGE = TOMCAT + "-" + PACKAGE;
	private static final String SETUP_INFO = PlatformAssetDistributionConstants.FILENAME_SETUP_INFO_DIR;
	private static final String UPDATE = PlatformAssetDistributionConstants.FILENAME_UPDATE_DIR;

	/* The Tomcat shutdown command. This is intentionally just a simple fixed string (and not a secret or UUID) because
	 * it doesn't make sense to have secrets in Docker images, since images may be shared with multiple customers. Also
	 * the port should anyway never be exposed. If needed, we can add the possibility to overwrite the command via ENV
	 * (at startup). */
	private static final String SHUTDOWN_COMMAND = "shutdown-tribefire";

	private static final String CONTAINER_NAME_TRIBEFIRE_JS = "tribefire-js";

	// initial version
	private static final String DOCKER_RESOURCES_VERSION__2_0_1 = "2.0.1";
	// no separate tribefire-master resources anymore
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_108 = "2.0.108";
	// refactored docker images with on-the-fly base image
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_172 = "2.0.172";
	// switch to JDK 15 (versions before require JDK 14, because they rely on Nashorn)
	// (core-stable-20210303 provides 2.0.217, i.e. tribefire 2.0 with OpenJDK 15 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_201 = "2.0.201";
	// switch to JDK 16 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (core-stable-20210625 provides 2.0.241, i.e. tribefire 2.1 with OpenJDK 16 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_218 = "2.0.218";
	// switch to JDK 17 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (core-stable-20211117 provides 2.0.248, i.e. tribefire 2.2 with OpenJDK 17 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_242 = "2.0.242"; //
	// switch to JDK 18 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (core-stable-20220414 provides 2.0.259, i.e. tribefire 2.3 with OpenJDK 18 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_249 = "2.0.249";
	// switch to JDK 19 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (2022-09-22 core-stable-20220414-p1 provides 2.0.265, i.e. tribefire 2.3 patch release with OpenJDK 19 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_265 = "2.0.265";
	// switch to JDK 20 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (tribefire-2-3-7 uses OpenJDK 20 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_267 = "2.0.267";
	// switch to JDK 21 (older versions of tribefire may also work with this JDK, but they haven't been tested with it)
	// (tribefire-2-3-10 uses OpenJDK 21 in Docker images)
	@SuppressWarnings("unused") // no version specific processor code required
	private static final String DOCKER_RESOURCES_VERSION__2_0_268 = "2.0.268";
	
	private static final Logger logger = Logger.getLogger(BuildDockerImagesProcessor.class);

	/**
	 * Holds various data required while processing a single container, i.e. while building a single Docker image.
	 */
	private static class RuntimeContainerDockerImageBuildContext {
		RuntimeContainer container;
		File containerDir;
		File webapp; // either file or directory
		String fullyQualifiedImageName;
		PackagedPlatformAsset packagedPlatformAsset;
		boolean hasCustomDockerFileOrResources;
		BuildDockerImages request;

		RuntimeContainerDockerImageBuildContext(RuntimeContainer container, BuildDockerImages request) {
			this.container = container;
			this.request = request;
		}
	}

	public static void process(ServiceRequestContext requestContext, BuildDockerImages request, OverridingEnvironment virtualEnvironment) {

		setupLocalTomcatPlatformWithoutContainerSpecifics(requestContext, request);

		/* Because local Tomcat Setup doesn't support disjoint projection, we need to run a second setup. Having run the
		 * full Tomcat setup before we already downloaded all assets though, thus we now switch to offline mode. This
		 * makes sure we don't get any new assets (which were just uploaded seconds ago). Update 2020-08-13: for now we
		 * can't switch to offline mode, because Maven metadata file may be missing after setup-local-tomcat-platform.
		 * This happened once for adx-setup. Assumption is that MC got a (second) Ravenhurst update when it performed
		 * the tribefire-web-platform resolution and thus deleted the adx-setup maven metadata file (+ others). Not
		 * being in offline mode is not ideal, because - if tomcat-asset and other assets are updated at the same time
		 * (i.e. within seconds) - it may happen that we still got the old tomcat-asset in the first setup, but now we
		 * will get newer versions of other assets in the second setup. It's very unlikely though that this happens
		 * (since there are not so many tomcat updates) and it also only happens when one doesn't work with a stable
		 * core. */
		// virtualEnvironment.setEnv(Constants.MC_CONNECTIVITY_MODE, Constants.MC_CONNECTIVITY_MODE_OFFLINE);

		PackagedPlatformSetup packagedPlatformSetup = packagePlatformSetup(requestContext, request);

		buildDockerImages(request, packagedPlatformSetup, virtualEnvironment);
	}

	private static void setupLocalTomcatPlatformWithoutContainerSpecifics(ServiceRequestContext requestContext, BuildDockerImages request) {

		println("Setting up local Tomcat platform as preparation for Docker image building.");

		SetupLocalTomcatPlatformForDocker setupLocalTomcatPlatformForDocker = SetupLocalTomcatPlatformForDocker.T.create();
		setupLocalTomcatPlatformForDocker.setSetupDependency(request.getSetupDependency());
		setupLocalTomcatPlatformForDocker.setTags(request.getTags());
		setupLocalTomcatPlatformForDocker.setPackageBaseDir(tomcatPackageDir(request));
		setupLocalTomcatPlatformForDocker.setDeletePackageBaseDir(false);
		setupLocalTomcatPlatformForDocker.setInstallationPath(tomcatDir(request));
		setupLocalTomcatPlatformForDocker.setShutdownCommand(SHUTDOWN_COMMAND);
		setupLocalTomcatPlatformForDocker.setAcceptorThreadCount(10);
		setupLocalTomcatPlatformForDocker.setConnectionUploadTimeout(1200000l);
		setupLocalTomcatPlatformForDocker.setMaxConnections(15000);
		setupLocalTomcatPlatformForDocker.setMaxThreads(5000);
		setupLocalTomcatPlatformForDocker.setInitialHeapSize("256m");
		setupLocalTomcatPlatformForDocker.setMaxHeapSize("2048m");
		setupLocalTomcatPlatformForDocker.setJvmOptions(
				"-Dtribefire.runtime.loglevel=${TRIBEFIRE_RUNTIME_LOGLEVEL} -Dsun.net.inetaddr.ttl=30 -Djava.security.egd=file:/dev/./urandom ${TRIBEFIRE_ADDITIONAL_OPTIONS} -server");
		setupLocalTomcatPlatformForDocker.setEnableVirtualThreads(request.getEnableVirtualThreads());
		setupLocalTomcatPlatformForDocker.eval(requestContext).get();

		println("Set up local platform setup.");
	}

	private static PackagedPlatformSetup packagePlatformSetup(ServiceRequestContext requestContext, BuildDockerImages request) {

		println("Packaging platform setup as preparation for Docker image building.");

		PackagePlatformSetup packagePlatformSetup = PackagePlatformSetup.T.create();
		packagePlatformSetup.setSetupDependency(request.getSetupDependency());
		packagePlatformSetup.setTags(request.getTags());
		packagePlatformSetup.setPackageBaseDir(packageDir(request));
		packagePlatformSetup.setDisjointProjection(true);

		PackagedPlatformSetup packagedPlatformSetup = packagePlatformSetup.eval(requestContext).get();

		println("Packaged platform setup.");

		return packagedPlatformSetup;
	}

	private static void buildDockerImages(BuildDockerImages request, PackagedPlatformSetup packagedPlatformSetup,
			VirtualEnvironment virtualEnvironment) {

		// get containers and sort them by name to get stable logging output
		List<RuntimeContainerDockerImageBuildContext> buildContexts = packagedPlatformSetup.getContainers().stream()
				.sorted(Comparator.comparing(RuntimeContainer::getName))
				// for now tribefire-js is not used/supported in cloud (see also tribefire manifest creation), thus we
				// skip building the respective Docker image
				.filter(container -> !container.getName().equals(CONTAINER_NAME_TRIBEFIRE_JS))
				.map(container -> new RuntimeContainerDockerImageBuildContext(container, request)).collect(Collectors.toList());

		/* choose proper version of docker resources (for now from 'docker' folder in Jinni) which fits our tribefire
		 * version. The respective version folder contains Dockerfile templates and resources which will be used to
		 * build Docker images. */
		SimpleArtifactVersion matchingDockerResourcesVersion;
		String sourceDockerResourcesDir;
		{
			File jinniRootDir = new File(virtualEnvironment.getEnv(Constants.JINNI_INSTALLATION_DIR_PROPERTY_NAME));

			File sourceDockerResourcesBaseDir = new File(jinniRootDir, "/docker/");

			if (!sourceDockerResourcesBaseDir.exists()) {
				throw new IllegalStateException(
						"Docker resources directory " + sourceDockerResourcesBaseDir.getAbsolutePath() + " (unexpectedly) doesn't exist!");
			}

			// different tribefire versions may require different Docker resources --> first get "tribefire version"
			SimpleArtifactVersion tribefireVersion = getTribefireVersionFromSetup(packagedPlatformSetup);

			// get available Docker resources versions from Docker resources folder
			List<SimpleArtifactVersion> dockerResourcesVersions = Arrays.asList(sourceDockerResourcesBaseDir.listFiles()).stream()
					.filter(file -> file.isDirectory()).filter(file -> SimpleArtifactVersion.isValidVersion(file.getName()))
					.map(file -> new SimpleArtifactVersion(file.getName())).collect(Collectors.toList());

			/* Get the most recent Docker resources version for our tribefire version. Example: If available resources
			 * versions are 2.0.1, 2.0.108, 2.0.355, 2.1.12 and 3.0.27 and our tribefire version is 2.0.157 (->
			 * tribefire-web-platform#2.0.157) this will give us 2.0.108. */
			matchingDockerResourcesVersion = tribefireVersion.getSameOrPredecessor(dockerResourcesVersions);
			sourceDockerResourcesDir = sourceDockerResourcesBaseDir.getPath() + "/" + matchingDockerResourcesVersion;
			println("Using Docker resources version " + matchingDockerResourcesVersion + " to prepare Dockerfiles");
		}

		File packageDir = new File(packageDir(request));

		// used for error message in case of duplicates
		Map<String, String> imageNamesToContainerNames = new HashMap<>();

		String tribefireBaseDockerRegistryHost = request.getTribefireBaseDockerRegistryHost() != null ? request.getTribefireBaseDockerRegistryHost()
				: request.getDockerRegistryHost();
		String dockerRegistryHost = request.getDockerRegistryHost();

		File masterContainerDir = null;

		for (RuntimeContainerDockerImageBuildContext buildContext : buildContexts) {
			RuntimeContainer container = buildContext.container;

			PackagedPlatformAssetsByNature packagedWebContexts = container.getAssets().get(WebContext.T.getTypeSignature());

			if (packagedWebContexts.getAssets().size() == 0) {
				throw new IllegalStateException("No packaged web context found for container " + container.getName() + "!");
			}

			if (packagedWebContexts.getAssets().size() > 1) {
				throw new IllegalStateException("Unexpectedly found multiple packaged web contexts for container " + container.getName() + ": "
						+ packagedWebContexts.getAssets());
			}

			buildContext.packagedPlatformAsset = CollectionTools.getFirstElement(packagedWebContexts.getAssets());

			buildContext.containerDir = new File(packageDir, container.getPathInPackage());

			if (container.getIsMaster()) {
				masterContainerDir = buildContext.containerDir;
			}

			if (!buildContext.containerDir.exists()) {
				throw new IllegalStateException("Container directory " + buildContext.containerDir.getAbsolutePath() + " for container "
						+ container.getName() + " not found!");
			}

			File webappsDir = new File(buildContext.containerDir, "webapps");
			if (!webappsDir.exists()) {
				throw new IllegalStateException("Directory '" + webappsDir + "' doesn't exist.");
			}

			File webapps[] = webappsDir.listFiles();
			if (webapps.length == 0) {
				throw new IllegalStateException("Directory '" + webappsDir + "' doesn't contain any webapp.");
			}

			if (webapps.length != 1) {
				throw new IllegalStateException("Expected exactly one webapp in '" + webappsDir.getAbsolutePath() + "' but found " + webapps.length
						+ " entries: " + Arrays.asList(webapps));
			}
			buildContext.webapp = webapps[0];

			// Docker image names are based on the container names; we may have to modify the name though, see below
			String imageName = buildContext.containerDir.getName();

			if (request.getShortenImageNames()) {
				/* in September 2018 tfcloud expected short image names (e.g. tribefire-services, tribefire-modeler) for
				 * core components. for cartridges we had also been using short image names as a convention. */
				if (imageName.contains(".")) {
					// tribefire.extension.demo.demo-cartridge --> demo-cartridge
					imageName = imageName.substring(imageName.lastIndexOf(".") + 1);
				}
			}

			if (imageNamesToContainerNames.containsKey(imageName)) {
				throw new IllegalStateException(
						"Docker image name '" + imageName + "' is not unique in this setup, see containers '" + buildContext.containerDir.getName()
								+ "' vs '" + imageNamesToContainerNames.get(imageName) + "'. In this case you can either use longer images names "
								+ " (see option 'shortenImageNames') or set a custom container name via a ContainerProjection asset.");
			}

			String dockerRegistrySubfolder = request.getDockerRegistrySubfolder() != null ? "/" + request.getDockerRegistrySubfolder() : "";
			String tag = request.getDockerImageTag();

			buildContext.fullyQualifiedImageName = dockerRegistryHost + dockerRegistrySubfolder + "/" + imageName + ":" + tag;
		}

		// ************************************************************************************************************
		// ************************************************************************************************************

		String deprecatedTribefireBaseImage = tribefireBaseDockerRegistryHost + "/tribefire-base:" + request.getTribefireBaseDockerImageTag();
		String tribefireBaseImage = "tribefire-base-for-" + StringTools.getSubstringBetween(request.getSetupDependency(), ":", "#") + ":"
				+ DateTools.getTimestampNumber();

		// *** Prepare Docker Files ***
		println("Preparing Dockerfiles and Docker resources.");

		boolean baseImageDeprecationModeEnabled = !new File(sourceDockerResourcesDir, BASE_IMAGE).exists();
		if (!baseImageDeprecationModeEnabled) {
			println("Preparing Dockerfile and related resources for base Docker image");
			// TODO: remove deprecated base image arguments when no longer needed
			createDockerfileAndResourcesForBaseImage(request, sourceDockerResourcesDir, tribefireBaseImage, deprecatedTribefireBaseImage);
		}

		buildContexts.forEach(buildContext -> {
			println("Preparing Dockerfile and related resources for Docker image " + buildContext.fullyQualifiedImageName + " for container "
					+ buildContext.container.getName());
			createDockerfileAndResourcesForContainer(buildContext, sourceDockerResourcesDir, tribefireBaseImage, deprecatedTribefireBaseImage);
		});
		println("Prepared Dockerfiles and Docker resources.");

		// *** Build Docker Images ***

		boolean noCache = !request.getUseCache();

		if (!baseImageDeprecationModeEnabled) {
			boolean pullBaseBaseImage = request.getPullUpdatedBaseImage();

			println("Building tribefire base Docker image " + tribefireBaseImage + (noCache ? " without cache" : "")
					+ (pullBaseBaseImage ? " (also pulling newer version of its base image, if available)" : ""));

			List<String> commandArguments = CollectionTools.getList("docker", "build", ".", "--tag", tribefireBaseImage);

			if (pullBaseBaseImage) {
				commandArguments.add("--pull");
			}

			if (noCache) {
				commandArguments.add("--no-cache");
			}

			if (request.getBaseImage() != null) {
				commandArguments.add("--build-arg");
				commandArguments.add("BASE_IMAGE=" + request.getBaseImage());
			}

			if (request.getJdkArchiveUrl() != null) {
				commandArguments.add("--build-arg");
				commandArguments.add("JDK_ARCHIVE_URL=" + request.getJdkArchiveUrl());
			}

			commandArguments.add("--build-arg");
			commandArguments.add("INSTALL_ANALYSIS_TOOLS=" + request.getInstallAnalysisTools());

			runCommand(new File(baseImageDir(request)), ArrayTools.toArray(commandArguments, String.class));
		}

		// whether or not we already pulled the standard base image
		// this is used for deprecated tribefire base image to not pull it twice.
		// if we just built the new base image, we can consider it to be already pulled.
		boolean pulledStandardBaseImage = !baseImageDeprecationModeEnabled;

		println("Building Docker images.");
		for (RuntimeContainerDockerImageBuildContext buildContext : buildContexts) {
			boolean pullBaseImage;
			if (request.getPullUpdatedBaseImage()) {
				// we may need to pull an updated base image
				if (buildContext.hasCustomDockerFileOrResources) {
					// this image may have a custom base image (probably not, but we can't know), thus always pull
					pullBaseImage = true;
				} else if (!pulledStandardBaseImage) {
					// this image uses the standard base image and we didn't pull it yet, thus pull now
					pullBaseImage = true;
					pulledStandardBaseImage = true;
				} else {
					// we already pulled that image seconds ago, no need to do it again
					pullBaseImage = false;
				}
			} else {
				// pulling base image disabled
				pullBaseImage = false;
			}
			println("Building Docker image " + buildContext.fullyQualifiedImageName + " for container " + buildContext.container.getName()
					+ (noCache ? " without cache" : "") + (pullBaseImage ? " (also pulling newer version of base image, if available)" : ""));

			List<String> commandArguments = CollectionTools.getList("docker", "build", "--build-arg",
					"FULLY_QUALIFIED_IMAGE=" + buildContext.fullyQualifiedImageName, ".", "--tag", buildContext.fullyQualifiedImageName);
			if (pullBaseImage) {
				commandArguments.add("--pull");
			}
			if (noCache) {
				commandArguments.add("--no-cache");
			}

			if (request.getContainerRegex() == null || buildContext.container.getName().matches(request.getContainerRegex())) {
				runCommand(buildContext.containerDir, ArrayTools.toArray(commandArguments, String.class));
			} else {
				println("Skip building container " + buildContext.container.getName()
						+ " because container name doesn't match configured container regex '" + request.getContainerRegex() + "'.");
			}
		}
		println("Built Docker images.");

		// *** Delete Base Image ***
		if (!baseImageDeprecationModeEnabled) {
			if (request.getDeleteIntermediateBaseImage()) {
				// delete base image (it was just built on-the-fly and won't be used anymore; also no need to push it)
				// we run the command in packageDir, but it actually doesn't matter from where we run "docker rmi"
				runCommand(packageDir, "docker", "rmi", tribefireBaseImage);
			} else {
				println("Keeping intermediate tribefire base image for analysis: " + tribefireBaseImage);
			}
		}

		// *** Push Docker Images ***
		if (request.getPush()) {
			println("Pushing Docker images.");
			buildContexts.forEach(buildContext -> {
				println("Pushing Docker image " + buildContext.fullyQualifiedImageName + " for container " + buildContext.container.getName());
				if (request.getContainerRegex() == null || buildContext.container.getName().matches(request.getContainerRegex())) {
					runCommand(buildContext.containerDir, "docker", "push", buildContext.fullyQualifiedImageName);
				} else {
					println("Skip pushing Docker image because container name doesn't match configured container regex '" + request.getContainerRegex()
							+ "'.");
				}
			});
			println("Pushed Docker images.");
		}

	}

	/**
	 * Gets the "tribefire version" from the setup. This is required for choosing the corresponding Docker resources
	 * folder. Since there is no such thing as a "tribefire version", the method returns the version of the
	 * tribefire-web-platform asset. If the setup doesn't include the web platform (i.e. old tribefire-services), it
	 * returns {@value #DOCKER_RESOURCES_VERSION__2_0_1}, which is the lowest version for Docker resources are available.
	 */
	private static SimpleArtifactVersion getTribefireVersionFromSetup(PackagedPlatformSetup packagedPlatformSetup) {
		String version;
		PackagedPlatformAssetsByNature potentialNullPointerHelper = packagedPlatformSetup.getAssets().get(TribefireWebPlatform.T.getTypeSignature());
		Set<PackagedPlatformAsset> webPlatformAssets = potentialNullPointerHelper == null ? Collections.EMPTY_SET
				: potentialNullPointerHelper.getAssets();
		if (webPlatformAssets.size() == 1) {
			// get version from single web platform asset (this is the usual case)
			PlatformAsset asset = CollectionTools.getFirstElement(webPlatformAssets).getAsset();
			version = asset.getVersion() + "." + asset.getResolvedRevision();

			if (!SimpleArtifactVersion.isValidVersion(version)) {
				throw new IllegalStateException("Unsupported " + TribefireWebPlatform.T.getShortName() + " version: '" + version + "'!");
			}
		} else if (webPlatformAssets.size() > 1) {
			throw new IllegalStateException(
					"Unexpectedly found multiple " + TribefireWebPlatform.T.getShortName() + " assets in setup: " + webPlatformAssets);
		} else {
			// no web platform found -> probably just an old setup with master cartridge, i.e. tribefire-services
			potentialNullPointerHelper = packagedPlatformSetup.getAssets().get(MasterCartridge.T.getTypeSignature());
			Set<PackagedPlatformAsset> masterCartridgeAssets = potentialNullPointerHelper == null ? Collections.EMPTY_SET
					: potentialNullPointerHelper.getAssets();

			if (masterCartridgeAssets.size() == 1) {
				// as expected we found a single master cartridge asset.
				// therefore everything should be fine. it's just an old tribefire setup.
				// --> set (dummy) version to lowest supported version.
				version = DOCKER_RESOURCES_VERSION__2_0_1;
			} else if (masterCartridgeAssets.size() > 1) {
				throw new IllegalStateException(
						"Unexpectedly found multiple " + MasterCartridge.T.getShortName() + " assets in setup: " + masterCartridgeAssets);

			} else {
				throw new IllegalStateException("Unexpectedly found neither a " + TribefireWebPlatform.T.getShortName() + " nor a "
						+ MasterCartridge.T.getShortName() + " asset in setup!");
			}
		}

		return new SimpleArtifactVersion(version);
	}

	/**
	 * Represents an artifact version. This is a simplified implementation written only for the use case to order
	 * versions and {@link SimpleArtifactVersion#getSameOrPredecessor(Collection) find a version or its predecessor}.
	 * This class could probably be replaced by some more generic representation.
	 */
	static class SimpleArtifactVersion implements Comparable<SimpleArtifactVersion> {
		// we allow "-", but only after the revision
		private static final String VERSION_REGEX = "\\d+\\.\\d+\\.\\d+(-.+)?";

		Integer major;
		Integer minor;
		Integer revision;

		SimpleArtifactVersion(String versionString) {
			List<String> versionParts = Arrays.asList(versionString.split("\\."));
			major = Integer.parseInt(versionParts.get(0));
			minor = Integer.parseInt(versionParts.get(1));
			try {
				revision = Integer.parseInt(versionParts.get(2));
			} catch (NumberFormatException e) {
				// expected for e.g. 1.2.3-pc --> parse number before "-"
				// (for our use case we don't care about the -pc, we just need to decrement, see below)
				revision = Integer.parseInt(StringTools.getSubstringBefore(versionParts.get(2), "-"));
				// -1, since 1.2.3-pc is less than 1.2.3
				revision--;
			}
		}

		private static boolean isValidVersion(String versionString) {
			return versionString.matches(VERSION_REGEX);
		}

		@Override
		public String toString() {
			return major + "." + minor + "." + revision;
		}

		@Override
		public boolean equals(Object other) {
			return this == other || (other instanceof SimpleArtifactVersion && compareTo((SimpleArtifactVersion) other) == 0);
		}

		@Override
		public int hashCode() {
			return major + minor + revision;
		}

		public boolean isNewerThanOrEqual(String other) {
			return isNewerThanOrEqual(new SimpleArtifactVersion(other));
		}

		public boolean isNewerThanOrEqual(SimpleArtifactVersion other) {
			return compareTo(other) >= 0;
		}

		public boolean isOlderThan(String other) {
			return isOlderThan(new SimpleArtifactVersion(other));
		}

		public boolean isOlderThan(SimpleArtifactVersion other) {
			return compareTo(other) < 0;
		}

		/**
		 * Compares by major / minor / revision.
		 */
		@Override
		public int compareTo(SimpleArtifactVersion other) {
			int result = major.compareTo(other.major);
			if (result == 0) {
				result = minor.compareTo(other.minor);
				if (result == 0) {
					result = revision.compareTo(other.revision);
				}
			}
			return result;
		}

		/**
		 * From the collection of passed <code>versions</code> returns the version which is the same as the one
		 * represented by this object or the predecessor.
		 */
		SimpleArtifactVersion getSameOrPredecessor(Collection<SimpleArtifactVersion> versions) {
			List<SimpleArtifactVersion> sortedVersions = new ArrayList<>(versions);
			Collections.sort(sortedVersions);
			Collections.reverse(sortedVersions);

			for (SimpleArtifactVersion supportedVersion : sortedVersions) {
				if (isNewerThanOrEqual(supportedVersion)) {
					// this is the highest version which is not higher than our version -> take it
					return supportedVersion;
				}
			}

			throw new IllegalArgumentException("Version " + this + " is older than any of the supported versions: " + sortedVersions);
		}
	}

	private static void createDockerfileAndResourcesForBaseImage(BuildDockerImages request, String dockerResourcesDir, String tribefireBaseImage,
			String deprecatedTribefireBaseImage) {

		File sourceBaseImageDockerResources = new File(dockerResourcesDir, BASE_IMAGE);
		File targetBaseImageResourcesDir = new File(baseImageDir(request));

		FileTools.copyDirectoryUnchecked(sourceBaseImageDockerResources, targetBaseImageResourcesDir);

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("tribefireBaseImage", tribefireBaseImage);
		velocityContext.put("deprecatedTribefireBaseImage", deprecatedTribefireBaseImage);

		createDockerFileFromTemplate(targetBaseImageResourcesDir, velocityContext);
		String tomcatHostDir = TOMCAT + "/runtime/host";
		// @formatter:off
		createDockerIgnoreFile(targetBaseImageResourcesDir,
				// Windows only
				tomcatHostDir + "/bin/*.bat",
				tomcatHostDir + "/bin/*.dll",
				tomcatHostDir + "/bin/*.exe",
				// custom tribefire start/stop scripts are not used
				tomcatHostDir + "/bin/tribefire-*.sh",
				// replaced by startup wrapper
				tomcatHostDir + "/conf/logging.properties",
				// e.g. tribefire-services_logging.properties or [WebappName]_logging.properties -> not needed
				tomcatHostDir + "/conf/*_logging.properties",
				// manager apps are not used (so far) with Docker/Cloud
				tomcatHostDir + "/webapps/host-manager",
				tomcatHostDir + "/webapps/manager",
				// added later (only in master image) for Docker layer/cache optimization
				TOMCAT + "/" + SETUP_INFO,
				// not needed after Tomcat config has been updated based on templates
				TOMCAT + "/" + UPDATE);
		// @formatter:on
	}

	private static void createDockerfileAndResourcesForContainer(RuntimeContainerDockerImageBuildContext buildContext, String dockerResourcesDir,
			String tribefireBaseImage, String deprecatedTribefireBaseImage) {
		String webappName = FileTools.getNameWithoutExtension(buildContext.webapp.getName());

		if (buildContext.webapp.isDirectory()) {
			// the webapp is a already folder and not a war file
			// (that's the case with tribefire-web-platform)
		} else {
			// the webapp is a war file -> extract to folder and delete
			logger.info("Extracting " + buildContext.webapp + " ...");
			extractAndDeleteWarFile(buildContext.webapp, webappName);
		}

		// copy docker resources
		String containerName = buildContext.container.getName();

		File dockerResources = new File(dockerResourcesDir);

		File containerDockerResources = new File(dockerResources, containerName);

		if (containerDockerResources.exists()) {
			logger.info("Using custom " + DOCKERFILE + " and resources from " + containerDockerResources);
			buildContext.hasCustomDockerFileOrResources = true;
		} else {
			containerDockerResources = new File(dockerResources, "default");
		}

		FileTools.copyDirectoryUnchecked(containerDockerResources, buildContext.containerDir);

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("containerDir", baseDirRelativePath(buildContext.request, buildContext.containerDir.getPath()));
		velocityContext.put("webappName", webappName);
		velocityContext.put("tribefireBaseImage", tribefireBaseImage);
		velocityContext.put("deprecatedTribefireBaseImage", deprecatedTribefireBaseImage);

		// *** Permissions ***
		velocityContext.put("enableRootPermissions", buildContext.request.getEnableRootPermissions());
		velocityContext.put("enableGosu", buildContext.request.getEnableGosu());

		// *** Custom Docker Instructions ***
		velocityContext.put("customInstructionsBeforeAddingAssets",
				buildContext.request.getCustomInstructionsBeforeAddingAssets().get(containerName));
		velocityContext.put("customInstructionsAfterAddingAssets", buildContext.request.getCustomInstructionsAfterAddingAssets().get(containerName));

		// *** Modules ***
		velocityContext.put("modulesProvided", new File(buildContext.containerDir, "modules").exists());

		// *** Storage ***
		velocityContext.put("storageProvided", new File(buildContext.containerDir, "storage").exists());

		// *** Plugins ***
		File pluginsFolder = new File(buildContext.containerDir, "plugins");
		velocityContext.put("pluginsProvided", pluginsFolder.exists());

		Set<String> pluginFolderNames = new HashSet<>();

		if (pluginsFolder.isDirectory()) {
			// plugins directory exists --> get list of plugin folder names (to be passed to Velocity)
			// (since the base image may already have plugins, we have to add them individually instead of just adding
			// the full folder)
			Arrays.stream(pluginsFolder.listFiles()).filter(File::isDirectory).map(File::getName).forEach(pluginFolderNames::add);
		}
		velocityContext.put("pluginFolderNames", pluginFolderNames);

		// *** Tribefire Properties ***
		velocityContext.put("tribefirePropertiesProvided", new File(buildContext.containerDir, "environment/tribefire.properties").exists());

		// *** Rewrite Config ***
		String rewriteConfig = buildContext.request.getUrlRewriteConfigs().get(containerName);
		if (rewriteConfig != null) {
			FileTools.writeStringToFile(new File(buildContext.containerDir, REWRITE_CONFIG), rewriteConfig);
		}
		velocityContext.put("rewriteConfigProvided", rewriteConfig != null);

		// *** Setup Info ***
		// add setup info only to master to increase probability that we can use the cache for the other images.
		velocityContext.put("setupInfoProvided", buildContext.container.getIsMaster());
		File setupInfoDir = new File(tomcatDir(buildContext.request), SETUP_INFO);
		if (!setupInfoDir.exists()) {
			// probably something was changed in setup-local-tomcat-platform and this code here needs to be adapted.
			throw new IllegalStateException("Setup info directory " + setupInfoDir + " (unexpectedly) does not exist.");
		}
		// the tomcat directory is not part of our build context (and we don't want it to be), since we only need the
		// setup info. thus we copy that single directory only.
		FileTools.copyDirectoryUnchecked(setupInfoDir, new File(buildContext.containerDir, SETUP_INFO));

		// ************************************************************************************************************
		createDockerFileFromTemplate(buildContext.containerDir, velocityContext);
		createDockerIgnoreFile(buildContext.containerDir);
	}

	private static void createDockerFileFromTemplate(File directory, VelocityContext velocityContext) {
		// *** Create Dockerfile content from template ***
		StringWriter writer = new StringWriter();
		VelocityEngine velocity = new VelocityEngine();
		velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, directory.getAbsolutePath());
		velocity.init();
		Template template = velocity.getTemplate(DOCKERFILE_TEMPLATE);
		template.merge(velocityContext, writer);

		// remove empty lines at the beginning or end of file and end with a line separator
		String dockerfileContent = writer.toString().trim() + "\n";

		// not more than one empty line in a row
		dockerfileContent = StringTools.limitConsecutiveOccurrences(dockerfileContent, "\n", 2);

		// *** Write Dockerfile ***
		FileTools.writeStringToFile(new File(directory, DOCKERFILE), dockerfileContent);
	}

	private static void createDockerIgnoreFile(File directory, String... additionalPathsToIgnore) {
		List<String> lines = new ArrayList<>();
		lines.add("# ignore Docker files");
		lines.add(DOCKERFILE);
		lines.add(DOCKERFILE_TEMPLATE);
		lines.add(DOCKERIGNORE);
		if (!CommonTools.isEmpty(additionalPathsToIgnore)) {
			lines.add("# additional paths to ignore");
			lines.addAll(Arrays.asList(additionalPathsToIgnore));
		}

		String fileContent = StringTools.join("\n", lines) + "\n";
		FileTools.writeStringToFile(new File(directory, DOCKERIGNORE), fileContent);
	}

	private static void runCommand(File directory, String... parts) {
		List<String> cmd = CollectionTools.getList(parts);
		String cmdStringForLogging = cmd.stream().collect(Collectors.joining(" "));
		logger.info("In directory " + directory + " running command: " + cmdStringForLogging);

		try {
			ProcessResults result = ProcessExecution.runCommand(cmd, directory, null, null);
			println(result.getNormalText());
			println(result.getErrorText());
			if (result.getRetVal() != 0) {
				throw new ProcessException("Process execution terminated with exit code " + result.getRetVal() + ".");
			}
		} catch (ProcessException e) {
			throw new RuntimeException("Error while running command '" + cmdStringForLogging + "' in directory " + directory + "!", e);
		}

		logger.debug("Successfully ran command.");
	}

	private static void extractAndDeleteWarFile(File warFile, String webappName) {
		File targetWebappsDir = new File(warFile.getParentFile(), webappName);
		targetWebappsDir.mkdir();

		try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(warFile))) {
			ZipEntry zipEntry = null;
			while ((zipEntry = inputStream.getNextEntry()) != null) {
				File targetFile = new File(targetWebappsDir, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					// create directory because it may be empty and it would be an information loss otherwise
					targetFile.mkdirs();
				} else {
					targetFile.getParentFile().mkdirs();

					try (OutputStream outputStream = new FileOutputStream(targetFile)) {
						IOTools.transferBytes(inputStream, outputStream, IOTools.BUFFER_SUPPLIER_8K);
					}
				}

				inputStream.closeEntry();
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while extracting war file: " + warFile);
		}

		warFile.delete();
	}

	private static String baseDirRelativePath(BuildDockerImages request, String baseDirSubPath) {
		String baseDirPrefix = request.getBaseDir() + "/";
		if (!baseDirSubPath.startsWith(baseDirPrefix)) {
			throw new IllegalStateException("Cannot get base dir relative path for '" + baseDirSubPath
					+ "', since the path string (unexpectedly) does not start with path '" + baseDirPrefix + "'!");
		}

		return StringTools.getSubstringAfter(baseDirSubPath, baseDirPrefix);
	}

	private static String packageDir(BuildDockerImages request) {
		return request.getBaseDir() + "/" + PACKAGE;
	}

	private static String tomcatPackageDir(BuildDockerImages request) {
		return request.getBaseDir() + "/" + TOMCAT_PACKAGE;
	}

	private static String baseImageDir(BuildDockerImages request) {
		return request.getBaseDir() + "/" + BASE_IMAGE;
	}

	private static String tomcatDir(BuildDockerImages request) {
		return baseImageDir(request) + "/" + TOMCAT;
	}
}
