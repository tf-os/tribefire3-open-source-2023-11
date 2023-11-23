package com.braintribe.build.cmd.assets.impl.js_modules;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContext;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.paths.PathList;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;
import tribefire.descriptor.model.UxModuleDescriptor;
import tribefire.descriptor.model.UxModulesDescriptor;

public class JsUxModuleCollector implements PlatformAssetCollector, PlatformAssetDistributionConstants {

	private final List<PlatformAssetSolution> solutions = new ArrayList<>();
	private final List<PlatformAssetSolution> jsLibraries = new ArrayList<>();

	public void addSolution(PlatformAssetSolution solution) {
		solutions.add(solution);
	}

	public void addLibrary(PlatformAssetSolution library) {
		jsLibraries.add(library);
	}

	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		new JsUxModuleTransfer(context).run();
	}

	private class JsUxModuleTransfer {

		private final PlatformAssetDistributionContext context;
		private final PlatformSetupConfig request;

		private JsLibraryLinkingContext jsLinkingContext;
		private File jsLibrariesFolder;
		private final List<CompiledTerminal> terminals = newList();

		private final List<UxModuleDescriptor> moduleDescriptors = newList();

		public JsUxModuleTransfer(PlatformAssetDistributionContext context) {
			this.context = context;
			this.request = context.request();
		}

		public void run() {
			printPreparingJsLibraries();

			buildJsLinkingContext();
			resolveAndEnsureJsLibrariesFolder();
			collectTerminals();

			doLibraryLinking();

			writeUxModulesYamlIfRelevant();
		}

		private void printPreparingJsLibraries() {
			ConsoleOutputs.println(ConsoleOutputs.text("\n    Preparing js-libraries"));
		}

		private void buildJsLinkingContext() {
			boolean debugJs = request.getDebugJs();
			jsLinkingContext = JsLibraryLinkingContext.build() //
					.useSymbolikLinks(debugJs) //
					.preferPrettyOverMin(debugJs) //
					.outputPrefix("        ") //
					.done();
		}

		private void resolveAndEnsureJsLibrariesFolder() {
			jsLibrariesFolder = resolveJsLibrariesParent().push(JS_LIBRARIES_FOLDER_NAME).toFile();

			if (jsLibrariesFolder.exists())
				FileTools.deleteRecursivelySymbolLinkAware(jsLibrariesFolder);

			jsLibrariesFolder.mkdirs();
		}

		private PathList resolveJsLibrariesParent() {
			String debugProjectAsStr = request.getDebugProject();
			if (debugProjectAsStr != null) {
				String[] debugProject = ModuleSetupHelper.parseCondensedArtifact(debugProjectAsStr);
				return context.projectionBaseFolder(false).push("debug").push(debugProject[1]).push("context");
			} else {
				return context.projectionBaseFolder(false).push(WEBAPP_FOLDER_NAME).push(TRIBEFIRE_SERVICES);
			}
		}

		private final Set<String> terminalDepNames = newSet();

		private void collectTerminals() {
			collectTerminals(solutions, true);
			collectTerminals(jsLibraries, false);
		}

		private void collectTerminals(List<PlatformAssetSolution> assetSolutions, boolean createModuleDescriptor) {
			for (PlatformAssetSolution assetSolution : assetSolutions)
				for (AnalysisDependency requestor : assetSolution.solution.getDependers()) {

					CompiledDependency origin = requestor.getOrigin();
					if (terminalDepNames.add(origin.asString())) {

						terminals.add(CompiledTerminal.from(origin));

						if (createModuleDescriptor)
							moduleDescriptors.add(createDescriptor(requestor, origin));
					}
				}
		}

		private UxModuleDescriptor createDescriptor(AnalysisDependency requestor, CompiledDependency origin) {
			UxModuleDescriptor md = UxModuleDescriptor.T.create();

			md.setArtifactId(requestor.getArtifactId());
			md.setGroupId(requestor.getGroupId());

			// create uncertain version
			String uncertainVersion = origin.getVersion().asShortNotation();
			md.setVersion(uncertainVersion);

			return md;
		}

		private void doLibraryLinking() {
			JsLibraryLinker jsLibraryLinker = context.artifactResolutionContext().jsLibraryLinker();
			jsLibraryLinker.linkLibraries(jsLinkingContext, terminals, jsLibrariesFolder);
		}

		private void writeUxModulesYamlIfRelevant() {
			// Only write ux-modules.yaml file if ux assets are in place
			if (!moduleDescriptors.isEmpty()) {
				UxModulesDescriptor modulesDescriptor = UxModulesDescriptor.T.create();
				modulesDescriptor.setUxModules(moduleDescriptors);

				File uxModulesDescFile = context.projectionBaseFolder(false) //
						.push(MODULES_DIR_NAME) //
						.push(UX_MODULES_YAML_NAME) //
						.toFile();

				FileTools.write(uxModulesDescFile).usingWriter(writer -> TfSetupTools.writeYml(modulesDescriptor, writer));
			}
		}

	}

	@Override
	public List<Class<? extends PlatformAssetCollector>> priorCollectors() {
		return Collections.singletonList(ModuleCollector.class);
	}
}
