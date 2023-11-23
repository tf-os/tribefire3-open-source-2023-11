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
package tribefire.extension.hydrux.setup.processing;

import static com.braintribe.console.ConsoleOutputs.print;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.Set;

import com.braintribe.devrock.templates.model.artifact.CreateTsLibrary;
import com.braintribe.devrock.templates.support.compose.CompositeProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

import tribefire.cortex.assets.templates.model.CreateAggregator;
import tribefire.cortex.assets.templates.model.CreateModel;
import tribefire.cortex.assets.templates.model.CreateModule;
import tribefire.extension.hydrux.setup.model.CreateHydruxProject;
import tribefire.extension.hydrux.setup.model.HydruxProjectPart;
import tribefire.extension.hydrux.setup.model.HydruxSetupRequest;

/** Processor for {@link HydruxSetupRequest} */
public class HydruxSetupProcessor extends AbstractDispatchingServiceProcessor<HydruxSetupRequest, Object> {

	private File groupFolder;

	public void setGroupFolder(File groupFolder) {
		this.groupFolder = FileTools.getCanonicalFileUnchecked(groupFolder.getAbsoluteFile());
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<HydruxSetupRequest, Object> dispatching) {
		dispatching.register(CreateHydruxProject.T, this::createHydruxProject);
	}

	private Neutral createHydruxProject(ServiceRequestContext context, CreateHydruxProject request) {
		if (StringTools.isEmpty(request.getName()))
			outWarning("Cannot create Hydrux project, name was not specified!");

		new CreateHydruxProjectProcessor(context, request).run();

		return Neutral.NEUTRAL;
	}

	private class CreateHydruxProjectProcessor extends CompositeProcessor {

		private final Set<HydruxProjectPart> parts;

		public CreateHydruxProjectProcessor(ServiceRequestContext context, CreateHydruxProject request) {
			super(context, HydruxSetupProcessor.this.groupFolder, request.getName(), request.getOverwrite());
			this.parts = completePartDeps(request.getParts());
		}

		private Set<HydruxProjectPart> completePartDeps(Set<HydruxProjectPart> parts) {
			if (isEmpty(parts))
				return emptySet();

			if (parts.contains(HydruxProjectPart.all))
				return EnumSet.allOf(HydruxProjectPart.class);

			Set<HydruxProjectPart> result = newSet(parts);
			if (result.contains(HydruxProjectPart.setup))
				result.add(HydruxProjectPart.initializer);

			if (result.contains(HydruxProjectPart.setup) && result.contains(HydruxProjectPart.apiModel))
				result.add(HydruxProjectPart.module);

			return result;
		}

		// @formatter:off
		protected final String hxModuleName() { return baseName + "-hx-module";}
		protected final String hxModelName() { return baseName + "-hx-deployment-model";}
		protected final String initializerName() { return baseName + "-initializer";}
		protected final String moduleName() { return baseName + "-module";}
		protected final String setupName() { return baseName + "-setup";}
		protected final String apiModelName() { return baseName + "-api-model";}
		// @formatter:on

		@Override
		protected void initHandlers() {
			handlers.add(new UxModuleHandler());
			handlers.add(new UxModelHandler());

			if (parts.contains(HydruxProjectPart.initializer))
				handlers.add(new InitializerHandler());

			if (parts.contains(HydruxProjectPart.module))
				handlers.add(new ModuleHandler());

			if (parts.contains(HydruxProjectPart.setup))
				handlers.add(new SetupHandler());

			if (parts.contains(HydruxProjectPart.apiModel))
				handlers.add(new ApiModelHandler());
		}

		@Override
		protected void letHandlersHandleBro() {
			super.letHandlersHandleBro();

			createVsCodeSettingsIfNotExists();
		}

		private void createVsCodeSettingsIfNotExists() {
			String fileName = ".vscode/settings.json";
			File file = new File(groupFolder, fileName);
			if (file.exists())
				return;

			println(sequence(text("Creating: "), yellow(fileName)));
			println();

			try (InputStream is = getClass().getClassLoader().getResourceAsStream("settings.json")) {
				FileTools.write(file).fromInputStream(is);

			} catch (IOException e) {
				throw new UncheckedIOException("Error while writing " + fileName, e);
			}
		}

		private class UxModuleHandler extends PartHandler<CreateTsLibrary> {
			public UxModuleHandler() {
				super(hxModuleName(), CreateTsLibrary.T);
			}

			@Override
			protected void configureRequest() {
				request.setAsset(true);
				addDeps(locAssDep(hxModelName()), //
						jsDep("tribefire.extension.hydrux", "hydrux-api"), //
						assOnly(assDep("tribefire.extension.hydrux", "hydrux-module")) //
				);
			}
		}

		private class UxModelHandler extends PartHandler<CreateModel> {
			public UxModelHandler() {
				super(hxModelName(), CreateModel.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(assDep("tribefire.extension.hydrux", "hydrux-deployment-model"));
			}
		}

		private class InitializerHandler extends PartHandler<CreateModel> {
			public InitializerHandler() {
				super(initializerName(), CreateModel.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(locAssDep(hxModelName()), //
						assDep("tribefire.extension.hydrux", "hydrux-deployment-model"), //
						assOnly(locAssDep(hxModuleName())) //
				);

				if (parts.contains(HydruxProjectPart.module))
					addDeps(assOnly(locAssDep(moduleName())));
			}
		}

		private class ModuleHandler extends PartHandler<CreateModule> {
			public ModuleHandler() {
				super(moduleName(), CreateModule.T);
			}

			@Override
			protected void configureRequest() {
				if (parts.contains(HydruxProjectPart.apiModel))
					addDeps(locAssDep(apiModelName()));
			}
		}

		private class SetupHandler extends PartHandler<CreateAggregator> {
			public SetupHandler() {
				super(setupName(), CreateAggregator.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(assOnly(assDep("tribefire.setup.classic", "standard-setup")), //
						assOnly(locAssDep(initializerName())));

				if (parts.contains(HydruxProjectPart.apiModel))
					addDeps(locAssDep(apiModelName()));
			}
		}

		private class ApiModelHandler extends PartHandler<CreateModel> {
			public ApiModelHandler() {
				super(apiModelName(), CreateModel.T);
			}

			@Override
			protected void configureRequest() {
				addDeps(assDep("com.braintribe.gm", "service-api-model"));
			}
		}
	}

	private void outWarning(String text) {
		print(red(text));
	}

}
