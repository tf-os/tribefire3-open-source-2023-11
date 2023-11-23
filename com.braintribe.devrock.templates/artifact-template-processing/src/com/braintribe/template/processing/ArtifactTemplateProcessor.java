// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.template.processing;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.white;
import static com.braintribe.console.output.ConsoleOutputFiles.outputProjectionDirectoryTree;
import static com.braintribe.template.processing.helper.ConsoleOutputHelper.outTemplateResolvingResult;
import static com.braintribe.template.processing.helper.ConsoleOutputHelper.templateNameOutput;
import static com.braintribe.template.processing.helper.FileHelper.collectOverwritenRelativePaths;
import static com.braintribe.template.processing.helper.FileHelper.copyDir;
import static com.braintribe.template.processing.helper.FileHelper.createTempPath;
import static com.braintribe.template.processing.helper.FileHelper.deleteDir;
import static com.braintribe.template.processing.helper.FileHelper.ensureDirExists;
import static com.braintribe.template.processing.helper.FileHelper.unzipToTempDir;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocaterBuilder;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationLocatorContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.devrock.templates.model.ArtifactTemplateResponse;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.AlreadyExists;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.template.processing.api.ArtifactTemplateProjector;
import com.braintribe.template.processing.api.ArtifactTemplateRequestProjector;
import com.braintribe.template.processing.projection.support.TemplateSupport;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.scripting.deployment.model.GroovyScript;
import tribefire.extension.scripting.groovy.GroovyEngine;

/**
 * This {@link ArtifactTemplateProcessor processor} serves as an artifact template engine.
 * <p>
 * Based on the request, the artifact template is resolved by the use of the {@link ArtifactResolver resolver} and later on projected by the use of
 * the {@link ArtifactTemplateProjector projector}.
 */
public class ArtifactTemplateProcessor implements ReasonedServiceProcessor<ArtifactTemplateRequest, ArtifactTemplateResponse>, ArtifactTemplateConsts {

	private ArtifactTemplateRequestProjector requestProjector;
	private ArtifactTemplateProjector templateProjector;
	private VirtualEnvironment virtualEnvironment;
	private File useCaseRepositoryConfigurationLocation;
	private ModeledConfiguration modeledConfiguration;

	@Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Required
	public void setRequestProjector(ArtifactTemplateRequestProjector requestProjector) {
		this.requestProjector = requestProjector;
	}

	@Required
	public void setTemplateProjector(ArtifactTemplateProjector templateProjector) {
		this.templateProjector = templateProjector;
	}

	@Required
	@Configurable
	public void setModeledConfiguration(ModeledConfiguration config) {
		this.modeledConfiguration = config;
	}

	@Configurable
	public void setUseCaseRepositoryConfigurationLocation(File useCaseRepositoryConfigurationLocation) {
		this.useCaseRepositoryConfigurationLocation = useCaseRepositoryConfigurationLocation;
	}


	@Override
	public Maybe<ArtifactTemplateResponse> processReasoned(ServiceRequestContext requestContext, ArtifactTemplateRequest request) {
		Optional<DevEnvironment> devEnvironment = requestContext.findAttribute(DevEnvironment.class);

		File devEnvRoot = devEnvironment.map(DevEnvironment::getRootPath).orElse(null);

		try (WireContext<ArtifactDataResolverContract> wireContext = Wire.context(new TemplateProcessorWireModule(devEnvRoot))) {
			return new ArtifactTemplateProcess(requestContext, request, wireContext.contract()).run();
		}
	}

	public class TemplateProcessorWireModule implements WireTerminalModule<ArtifactDataResolverContract> {

		private final File devEnvFolder;

		public TemplateProcessorWireModule(File devEnvFolder) {
			super();
			this.devEnvFolder = devEnvFolder;
		}

		@Override
		public List<WireModule> dependencies() {
			return Lists.list( //
					ArtifactDataResolverModule.INSTANCE, //
					EnvironmentSensitiveConfigurationWireModule.INSTANCE //
			);
		}

		@Override
		public void configureContext(WireContextBuilder<?> contextBuilder) {
			WireTerminalModule.super.configureContext(contextBuilder);
			contextBuilder.bindContract(VirtualEnvironmentContract.class, () -> virtualEnvironment);
			contextBuilder.bindContract(DevelopmentEnvironmentContract.class, () -> devEnvFolder);
			
			RepositoryConfigurationLocaterBuilder repositoryConfigurationLocatorBuilder = RepositoryConfigurationLocators.build() //
				.addDevEnvLocation(UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_ARTIFACTS).push("repository-configuration-devrock.yaml")); 
			
			if (useCaseRepositoryConfigurationLocation != null)
				repositoryConfigurationLocatorBuilder.addLocation(useCaseRepositoryConfigurationLocation);
			
			repositoryConfigurationLocatorBuilder //
				.addDevEnvLocation(UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_ARTIFACTS).push(RepositoryConfigurationLocators.FILENAME_REPOSITORY_CONFIGURATION)) //
				.addLocationEnvVariable(RepositoryConfigurationLocators.ENV_DEVROCK_REPOSITORY_CONFIGURATION) //
				.addUserDirLocation(UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_DEVROCK).push(RepositoryConfigurationLocators.FILENAME_REPOSITORY_CONFIGURATION));
				
			RepositoryConfigurationLocator repositoryConfigurationLocator = repositoryConfigurationLocatorBuilder.done();
			
			contextBuilder.bindContract(RepositoryConfigurationLocatorContract.class, () -> repositoryConfigurationLocator);
		}

	}

	private class ArtifactTemplateProcess {

		private final ServiceRequestContext requestContext;
		private final ArtifactTemplateRequest request;

		private final DependencyResolver dependencyResolver;
		private final ArtifactPartResolver partResolver;

		private final GroovyEngine groovyEngine = new GroovyEngine();

		public ArtifactTemplateProcess(ServiceRequestContext requestContext, ArtifactTemplateRequest request,
				ArtifactDataResolverContract adrContract) {
			this.requestContext = requestContext;
			this.request = request;
			this.dependencyResolver = adrContract.dependencyResolver();
			this.partResolver = adrContract.artifactResolver();
		}

		public Maybe<ArtifactTemplateResponse> run() {
			boolean verboseOutput = requestContext.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();

			Path installationPath = Paths.get(request.getInstallationPath());
			ensureDirExists(installationPath);
			Path projectionPath = createTempPath("template-projection-" + UUID.randomUUID());

			if (verboseOutput)
				println(white("Projecting artifact template to the installation directory: " + installationPath));

			try {
				processTemplateRequest(request, projectionPath, verboseOutput);
			} catch (RuntimeException e) {
				throw Exceptions.unchecked(e, "Failed to project the requested artifact template");
			}

			println(white("Installing:"));
			outputProjectionDirectoryTree(projectionPath);

			if (!request.getOverwrite()) {
				AlreadyExists error = deleteProjectionIfInstallationExists(installationPath, projectionPath);
				if (error != null)
					return error.asMaybe();
			}

			copyDir(projectionPath, installationPath);
			deleteDir(projectionPath);

			return Maybe.complete(ArtifactTemplateResponse.T.create());
		}

		private void processTemplateRequest(ArtifactTemplateRequest request, Path installationPath, boolean verboseOutput) {
			if (verboseOutput) {
				println(white("Projecting '" + request.entityType().getTypeSignature() + "' property values"));
			}
			requestProjector.project(request);

			Path templateInstallationPath = installationPath.resolve(request.getDirectoryName() != null ? request.getDirectoryName() : "");
			ensureDirExists(templateInstallationPath);

			if (verboseOutput) {
				println(white("Resolving artifact template:"));
				println(templateNameOutput(request.template(), 1));
			}
			// resolve template zip, ignore dependencies
			ArchiveZip archiveZip = resolveTemplate(request);
			if (verboseOutput) {
				println(white("Found:"));
				outTemplateResolvingResult(archiveZip.artifact);
			}

			if (verboseOutput) {
				println(white("Unzipping artifact template:"));
				println(templateNameOutput(archiveZip.artifact, 1));
			}
			Path templatePath = unzipToTempDir(archiveZip.data.getResource(), "template-" + UUID.randomUUID());

			List<ArtifactTemplateRequest> templateDependencies = getTemplateDependencies(templatePath, request);

			for (ArtifactTemplateRequest td : templateDependencies) {
				if (!td.template().equals(request.template())) { // avoids potential infinite recursion
					processTemplateRequest(td, installationPath, verboseOutput);
				}
			}

			println(white("Projecting artifact template:"));
			println(templateNameOutput(archiveZip.artifact, 1));

			templateProjector.project(request, templatePath, templateInstallationPath);
			deleteDir(templatePath);
		}

		private ArchiveZip resolveTemplate(ArtifactTemplateRequest request) {
			String template = request.template();

			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parseAndRangify(template);

			Maybe<CompiledArtifactIdentification> maybeArtifact = dependencyResolver.resolveDependency(cdi);
			if (maybeArtifact.isUnsatisfied())
				throw new IllegalStateException("Unable to resolve template " + template + " of " + request.entityType().getShortName() + ". Reason: "
						+ maybeArtifact.whyUnsatisfied().stringify());

			CompiledArtifactIdentification artifact = maybeArtifact.get();
			ArtifactDataResolution data = requireArchiveZip(artifact);

			return new ArchiveZip(artifact, data);
		}

		private ArtifactDataResolution requireArchiveZip(CompiledArtifactIdentification cai) {
			Maybe<ArtifactDataResolution> resolutionMaybe = partResolver.resolvePart(cai, ARCHIVE_ZIP_PART);

			if (resolutionMaybe.isUnsatisfiedBy(NotFound.T))
				throw new IllegalStateException("Part '" + ARCHIVE_ZIP_PART.asString() + "' not found for artifact: " + cai.asString());

			return resolutionMaybe.get();
		}

		private List<ArtifactTemplateRequest> getTemplateDependencies(Path templatePath, ArtifactTemplateRequest request) {
			Path dependenciesScriptPath = templatePath.resolve(DEPENDENCIES_SCRIPT);
			if (!dependenciesScriptPath.toFile().exists())
				return Collections.emptyList();
			
			GroovyScript dependenciesScript = GroovyScript.T.create();
			Resource scriptResource = Resource.createTransient(() -> new FileInputStream(dependenciesScriptPath.toFile()));
			dependenciesScript.setSource(scriptResource);
			Map<String, Object> dataModel = asMap("request", request, "support", new TemplateSupport(modeledConfiguration));

			try {
				Maybe<Object> evaluateDependencies = groovyEngine.evaluate(dependenciesScript, dataModel);
				return (List<ArtifactTemplateRequest>) evaluateDependencies.get();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Failed to evaluate the template " + DEPENDENCIES_SCRIPT + " script");
			}
		}

		private AlreadyExists deleteProjectionIfInstallationExists(Path installationPath, Path projectionPath) {
			List<Path> overwrittenFilePaths = collectOverwritenRelativePaths(projectionPath, installationPath);
			if (overwrittenFilePaths.isEmpty())
				return null;

			deleteDir(projectionPath);
			return AlreadyExists.create("Failed to install the template projection as the following files would be overwritten: "
					+ overwrittenFilePaths + ". To enable overwritting, set request 'overwrite' flag to true.");
		}

	}

	private static class ArchiveZip {
		public CompiledArtifactIdentification artifact;
		public ArtifactDataResolution data;

		public ArchiveZip(CompiledArtifactIdentification artifact, ArtifactDataResolution data) {
			this.artifact = artifact;
			this.data = data;
		}
	}



}
