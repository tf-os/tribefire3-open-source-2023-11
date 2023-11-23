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
package com.braintribe.tribefire.jinni.support.template;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
// TODO Access Support
//import static com.braintribe.model.jinni.api.template.ExtensionPart.accessApiModel;
//import static com.braintribe.model.jinni.api.template.ExtensionPart.accessModel;
import static com.braintribe.model.jinni.api.template.ExtensionPart.apiModel;
import static com.braintribe.model.jinni.api.template.ExtensionPart.deploymentModel;
import static com.braintribe.model.jinni.api.template.ExtensionPart.doc;
import static com.braintribe.model.jinni.api.template.ExtensionPart.initializer;
import static com.braintribe.model.jinni.api.template.ExtensionPart.processing;
import static com.braintribe.model.jinni.api.template.ExtensionPart.processingTest;
import static com.braintribe.model.jinni.api.template.ExtensionPart.setup;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.devrock.templates.model.Property;
import com.braintribe.devrock.templates.model.artifact.CreateArtifactWithSamples;
import com.braintribe.devrock.templates.model.artifact.CreateLibrary;
import com.braintribe.devrock.templates.model.artifact.CreateParent;
import com.braintribe.devrock.templates.model.artifact.CreateServiceTest;
import com.braintribe.devrock.templates.support.compose.CompositeProcessor;
import com.braintribe.gm._EssentialReasonModel_;
import com.braintribe.gm._ServiceApi_;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.template.CreateArtifactsRequest;
import com.braintribe.model.jinni.api.template.CreateExtension;
import com.braintribe.model.jinni.api.template.ExtensionPart;
import com.braintribe.model.jinni.api.template.ExtensionSample;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

import tribefire.cortex._ServiceDeploymentModel_;
import tribefire.cortex.assets.templates.model.CreateAggregator;
import tribefire.cortex.assets.templates.model.CreateInitializer;
import tribefire.cortex.assets.templates.model.CreateMdDoc;
import tribefire.cortex.assets.templates.model.CreateModel;
import tribefire.cortex.assets.templates.model.CreateModule;

/**
 * @author peter.gazdik
 */
public class CreateArtifactsProcessor extends AbstractDispatchingServiceProcessor<CreateArtifactsRequest, Neutral> {

	private File groupFolder;

	@Required
	public void setGroupFolder(File groupFolder) {
		this.groupFolder = FileTools.getCanonicalFileUnchecked(groupFolder.getAbsoluteFile());
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<CreateArtifactsRequest, Neutral> dispatching) {
		dispatching.registerReasoned(CreateExtension.T, this::createExtension);
	}

	// ######################################################
	// ## . . . . . . . . Create Extension . . . . . . . . ##
	// ######################################################

	private Maybe<Neutral> createExtension(ServiceRequestContext c, CreateExtension r) {
		if (StringTools.isEmpty(r.getName()))
			return InvalidArgument.create("Cannot create extension, name was not specified!").asMaybe();

		new CreateExtensionProcessor(c, r).run();

		return Maybe.complete(Neutral.NEUTRAL);
	}

	private class CreateExtensionProcessor extends CompositeProcessor {

		private final Set<ExtensionSample> samples;
		private final Set<ExtensionPart> parts;

		public CreateExtensionProcessor(ServiceRequestContext context, CreateExtension request) {
			super(context, CreateArtifactsProcessor.this.groupFolder, request.getName(), request.getOverwrite());
			this.samples = completeSamples(request.getSamples());
			this.parts = completePartDeps(request.getParts());
		}

		private Set<ExtensionPart> completePartDeps(Set<ExtensionPart> parts) {
			if (parts.isEmpty() || parts.contains(ExtensionPart.all))
				return EnumSet.allOf(ExtensionPart.class);

			Set<ExtensionPart> result = newSet(parts);

			if (samples.contains(ExtensionSample.serviceProcessor))
				result.addAll(asList(apiModel, //
						deploymentModel, processing, processingTest, initializer, doc));

			// TODO Access Support
			// if (samples.contains(ExtensionSample.accessProcessor))
			// result.addAll(asList(accessModel, accessApiModel, //
			// deploymentModel, processing, processingTest, initializer));

			if (parts.contains(processingTest))
				result.add(processing);

			return result;
		}

		private Set<ExtensionSample> completeSamples(Set<ExtensionSample> samples) {
			if (samples.isEmpty())
				return emptySet();

			if (samples.contains(ExtensionSample.all))
				return EnumSet.allOf(ExtensionSample.class);

			return samples;
		}

		// @formatter:off
		protected final String parentName = "parent";
		protected final String initializerName = baseName + "-initializer";
		protected final String moduleName = baseName + "-module";
		protected final String processingName = baseName + "-processing";
		protected final String processingTestName = baseName + "-processing-test";
		protected final String setupName = baseName + "-setup";
		protected final String deploymentModelName = baseName + "-deployment-model";
		// TODO Access Support
//		protected final String accessModelName = baseName + "-access-model";
		protected final String apiModelName = baseName + "-api-model";
		protected final String docName = baseName + "-doc";
		// @formatter:on

		@Override
		protected void initHandlers() {
			if (!hasParent())
				handlers.add(new ParentHandler());

			handlers.add(new ModuleHandler());

			if (includes(initializer))
				handlers.add(new InitializerHandler());

			if (includes(deploymentModel))
				handlers.add(new DeploymentModelHandler());

			if (includes(apiModel))
				handlers.add(new ApiModelHandler());

			// TODO Access Support
			// if (includes(accessModel))
			// handlers.add(new AccesssModelHandler());

			if (includes(processing))
				handlers.add(new ProcessingHandler());

			if (includes(processingTest))
				handlers.add(new ProcessingTestHandler());

			if (includes(doc))
				handlers.add(new DocHandler());

			if (includes(setup))
				handlers.add(new SetupHandler());
		}

		private boolean hasParent() {
			String fileName = "parent";
			File file = new File(groupFolder, fileName);
			return file.exists();
		}

		@Override
		protected void letHandlersHandleBro() {
			super.letHandlersHandleBro();

			createGroupBuildXmlNotExists();
		}

		private void createGroupBuildXmlNotExists() {
			String fileName = "build.xml";
			File file = new File(groupFolder, fileName);
			if (file.exists())
				return;

			println(sequence(text("Creating: "), yellow(fileName)));
			println();

			try (InputStream is = loadResourceRelativeToThisClass("group.build.xml")) {
				FileTools.write(file).fromInputStream(is);

			} catch (IOException e) {
				throw new UncheckedIOException("Error while writing " + fileName, e);
			}
		}

		private class ModuleHandler extends PartWithSampleHandler<CreateModule> {
			public ModuleHandler() {
				super(moduleName, CreateModule.T);
			}

			@Override
			protected void configureRequest() {
				if (includes(deploymentModel))
					addDeps(locAssDep(deploymentModelName));
				// TODO Access Support
				// if (includes(accessModel))
				// addDeps(locAssDep(accessModelName));
				if (includes(apiModel))
					addDeps(locAssDep(apiModelName));
				if (includes(processing))
					addDeps(locLibDep(processingName));
			}
		}

		private abstract class PartWithSampleHandler<T extends CreateArtifactWithSamples> extends PartHandler<T> {
			public PartWithSampleHandler(String fileName, EntityType<T> requestType) {
				super(fileName, requestType);
			}
			@Override
			protected T newRequest(EntityType<T> entityType) {
				T result = super.newRequest(entityType);
				result.setServiceProcessorSample(samples.contains(ExtensionSample.serviceProcessor));
				return result;
			}
		}

		private class ParentHandler extends PartHandler<CreateParent> {
			public ParentHandler() {
				super(parentName, CreateParent.T);
			}

			@Override
			protected void configureRequest() {
				request.setVersion("1.0");

				List<Property> props = request.getProperties();
				fill_TF3_DepsVersions(props);
			}

			private void fill_TF3_DepsVersions(List<Property> props) {
				props.add(groupRange("com.braintribe.wire", "(2.0,2.1]"));
				props.add(groupRange("com.braintribe.gm", "(2.0,2.1]"));
				props.add(groupRange("tribefire.cortex", "(3.0,3.1]"));
				props.add(groupRange("tribefire.setup.classic", "(3.0,3.1]"));
			}

			private Property groupRange(String groupId, String range) {
				return Property.create("V." + groupId, range);
			}

		}

		private class InitializerHandler extends PartWithSampleHandler<CreateInitializer> {
			public InitializerHandler() {
				super(initializerName, CreateInitializer.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(assOnly(locAssDep(moduleName)));

				if (includes(deploymentModel))
					addDeps(locAssDep(deploymentModelName));
				if (includes(apiModel))
					addDeps(locAssDep(apiModelName));
				// TODO Access Support
				// if (includes(accessModel))
				// addDeps(locAssDep(accessModelName));
			}
		}

		private class DeploymentModelHandler extends PartWithSampleHandler<CreateModel> {
			public DeploymentModelHandler() {
				super(deploymentModelName, CreateModel.T);
			}

			@Override
			protected void configureRequest() {
				request.setDeployment(true);

				if (samples.contains(ExtensionSample.serviceProcessor))
					addDeps(assDep(_ServiceDeploymentModel_.reflection));
			}
		}

		private class ApiModelHandler extends PartWithSampleHandler<CreateModel> {
			public ApiModelHandler() {
				super(apiModelName, CreateModel.T);
			}

			@Override
			protected void configureRequest() {
				request.setApi(true);

				// TODO Access Support
				// if (includes(accessModel))
				// addDeps(locAssDep(accessModelName));
			}
		}

		// TODO Access Support
		// private class AccesssModelHandler extends PartWithSampleHandler<CreateModel> {
		// public AccesssModelHandler() {
		// super(accessModelName, CreateModel.T);
		// }
		//
		// @Override
		// protected void configureRequest() {
		// addDeps(//
		// assDep("com.braintribe.gm", "root-model") //
		// );
		// }
		// }

		// TODO Access Support
		// private class AccessApiModelHandler extends PartWithSampleHandler<CreateModel> {
		// public AccessApiModelHandler() {
		// super(apiModelName, CreateModel.T);
		// }
		//
		// @Override
		// protected void configureRequest() {
		// addDeps(assDep("com.braintribe.gm", "access-request-model"));
		//
		// if (includes(accessModel))
		// addDeps(locAssDep(accessModelName));
		// }
		// }

		private class ProcessingHandler extends PartWithSampleHandler<CreateLibrary> {
			public ProcessingHandler() {
				super(processingName, CreateLibrary.T);
			}

			@Override
			protected void configureRequest() {
				// TODO Access Support
				// if (includes(accessModel))
				// addDeps(locAssDep(accessModelName));

				if (includes(apiModel))
					addDeps(locAssDep(apiModelName));

				if (samples.contains(ExtensionSample.serviceProcessor))
					addDeps(assDep(_EssentialReasonModel_.reflection), //
							libDep(_ServiceApi_.reflection));
			}
		}

		private class ProcessingTestHandler extends PartWithSampleHandler<CreateServiceTest> {
			public ProcessingTestHandler() {
				super(processingTestName, CreateServiceTest.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(locLibDep(processingName));
			}
		}

		private class DocHandler extends PartWithSampleHandler<CreateMdDoc> {
			public DocHandler() {
				super(docName, CreateMdDoc.T);
			}

			@Override
			protected void configureRequest() {
				// empty
			}
		}

		private class SetupHandler extends PartHandler<CreateAggregator> {
			public SetupHandler() {
				super(setupName, CreateAggregator.T);
			}

			@Override
			protected void configureRequest() {
				String moduleDepName = includes(initializer) ? initializerName : moduleName;

				addDeps(assOnly(assDep("tribefire.setup.classic", "standard-setup")), //
						assOnly(locAssDep(moduleDepName)) //
				);
			}
		}

		private boolean includes(ExtensionPart part) {
			return parts.contains(part);
		}

	}

}
