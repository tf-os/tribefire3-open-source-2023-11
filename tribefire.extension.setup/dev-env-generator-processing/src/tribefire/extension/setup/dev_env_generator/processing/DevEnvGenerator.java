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
package tribefire.extension.setup.dev_env_generator.processing;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.white;
import static com.braintribe.console.output.ConsoleOutputFiles.outputProjectionDirectoryTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.AlreadyExists;
import com.braintribe.gm.model.reason.essential.Canceled;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.FileTools;

import tribefire.extension.setup.dev_env_generator.processing.eclipse.EclipseWorkspaceJdtUiPrefs;
import tribefire.extension.setup.dev_env_generator.processing.eclipse.EclipseWorkspaceNetJdtCorePrefs;
import tribefire.extension.setup.dev_env_generator.processing.eclipse.EclipseWorkspaceOrgJdtCorePrefs;
import tribefire.extension.setup.dev_env_generator.processing.eclipse.EclipseWorkspaceTomcatPrefs;
import tribefire.extension.setup.dev_env_generator.processing.eclipse.EclipseWorkspaceUiCorePrefs;
import tribefire.extension.setup.dev_env_generator_api.model.CreateDevEnv;
import tribefire.extension.setup.dev_env_generator_config.model.DevEnvGeneratorConfig;

/**
 * This {@link DevEnvGenerator processor} serves as an artifact template engine.
 */
public class DevEnvGenerator implements ReasonedServiceProcessor<CreateDevEnv, Neutral> {

	private final Logger logger = Logger.getLogger(DevEnvGenerator.class);
	private boolean verbose = false;
	private DevEnvGeneratorConfig config;

	@Required
	@Configurable
	public void setConfiguration(DevEnvGeneratorConfig config) {
		this.config = config;
	}

	@Override
	public Maybe<Neutral> processReasoned(ServiceRequestContext requestContext, CreateDevEnv request) {

		verbose = requestContext.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();

		if (verbose) {
			println("DevEnvGeneratorConfig read from dev-env-generator-config.yaml:");
			println("  - eclipse workspace template: \"" + config.getEclipseWorkspaceTemplate() + "\"");
			println("  - repository-config template: \"" + config.getRepoConfTemplate() + "\"");
		}

		String name = request.getName();
		File devEnv = new File(name);

		return createDirectories(devEnv) //
				.flatMap(r -> createDevEnv(devEnv)) //
				.flatMap(r -> createCommands(devEnv)) //
				.flatMap(r -> copyEclipseWorkspace(devEnv)) //
				.flatMap(r -> patchEclipseWorkspace(devEnv)) //
				.flatMap(r -> copyRepositoryConfiguration(devEnv)) //
				.flatMap(r -> output(devEnv, verbose));

	}

	private Maybe<Neutral> copyEclipseWorkspace(File devEnv) {
		String template = config.getEclipseWorkspaceTemplate();
		if (template != null) {
			File wspc = new File(template);
			if (wspc.exists()) {
				FileTools.copy(wspc) //
						.as(new File(devEnv + "/eclipse-workspace")) //
						.please();
			} else {
				return Reasons.build(InvalidArgument.T).text("The eclipseWorkspaceTemplate at \"" + template + "\" not found.").toMaybe();
			}
		} else {
			println(red("No \"eclipseWorkspaceTemplate\" configured in \"dev-env-generator-config.yaml\". Only minimal workspace created. "));
		}

		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<Neutral> patchEclipseWorkspace(File devEnv) {
		if (!devEnv.isDirectory()) {
			return Reasons.build(Canceled.T).text("Error while creating dev-env. Directory \"" + devEnv + "\" not created.").toMaybe();
		}
		File cfgdir = new File(devEnv, "eclipse-workspace/.metadata/.plugins/org.eclipse.core.runtime/.settings");
		if (!cfgdir.exists()) {
			if (verbose)
				println("WARNING: Need to re-create eclipse workspace.");
			cfgdir.mkdirs();
		}

		String DEVENV = espaceWindowsPathForEclipse(devEnv.getAbsolutePath());

		String TOMCAT_HOME = DEVENV + "/tf-setups/main/runtime/host";

		EclipseWorkspaceNetJdtCorePrefs netJdt = new EclipseWorkspaceNetJdtCorePrefs(devEnv);
		if (verbose)
			println("cfg file: " + netJdt.getCfgFile() + " exists: " + netJdt.exists());
		if (netJdt.exists()) {
			Maybe<Neutral> content = netJdt.patch(//
					"org.eclipse.jdt.core.classpathVariable.TOMCAT_HOME=" + TOMCAT_HOME, //
					Map.ofEntries(Map.entry("(?mi).*\\.tomcat_home=.*", ""))); // remove line
			if (!content.isSatisfied())
				return content.whyUnsatisfied();
		} else {
			// create it
			Maybe<Neutral> result = netJdt.create(Map.ofEntries(Map.entry("@TOMCAT_HOME@", TOMCAT_HOME)));
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		}

		EclipseWorkspaceOrgJdtCorePrefs orgJdt = new EclipseWorkspaceOrgJdtCorePrefs(devEnv);
		if (verbose)
			println("cfg file: " + orgJdt.getCfgFile() + " exists: " + orgJdt.exists());
		if (orgJdt.exists()) {
			Maybe<Neutral> content = orgJdt.patch(//
					"org.eclipse.jdt.core.classpathVariable.TOMCAT_HOME=" + TOMCAT_HOME, //
					Map.ofEntries(Map.entry("(?mi).*\\.tomcat_home=.*", ""))); // remove line
			if (!content.isSatisfied())
				return content.whyUnsatisfied();
		} else {
			// create it
			Maybe<Neutral> result = orgJdt.create(Map.ofEntries(Map.entry("@TOMCAT_HOME@", TOMCAT_HOME)));
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		}

		EclipseWorkspaceJdtUiPrefs jdtUi = new EclipseWorkspaceJdtUiPrefs(devEnv);
		if (verbose)
			println("cfg file: " + jdtUi.getCfgFile() + " exists: " + jdtUi.exists());
		if (!jdtUi.exists()) {
			// create it
			Maybe<Neutral> result = jdtUi.create(Map.ofEntries(Map.entry("@DEVENV@", DEVENV)));
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		}

		EclipseWorkspaceTomcatPrefs tomcat = new EclipseWorkspaceTomcatPrefs(devEnv);
		if (verbose)
			println("cfg file: " + tomcat.getCfgFile() + " exists: " + tomcat.exists());
		if (tomcat.exists()) {
			// patch existing
			Maybe<Neutral> result = tomcat.patch( //
					"contextsDir=" + DEVENV + "/tf-setups/main/runtime/host/conf/Catalina/localhost\n" + //
							"tomcatConfigFile=" + DEVENV + "/tf-setups/main/runtime/host/conf/server.xml\n" + //
							"tomcatDir=" + DEVENV + "/tf-setups/main/runtime/host\n",
					Map.ofEntries(//
							Map.entry("(?mi)^\s*contextsDir=.*", ""), // remove line
							Map.entry("(?mi)^\s*tomcatConfigFile=.*", ""), // remove line
							Map.entry("(?mi)^\s*tomcatDir=.*", ""))); // remove line
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		} else {
			// create it
			Maybe<Neutral> result = tomcat.create(Map.ofEntries(Map.entry("@DEVENV@", DEVENV)));
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		}

		EclipseWorkspaceUiCorePrefs ui = new EclipseWorkspaceUiCorePrefs(devEnv);
		if (verbose)
			println("cfg file: " + ui.getCfgFile() + " exists: " + ui.exists());
		if (ui.exists()) {
			Maybe<Neutral> content = ui.patch(//
					"WORKSPACE_NAME=" + devEnv, //
					Map.ofEntries(Map.entry("(?mi)^\s*workspace_name=.*", ""))); // remove line
			if (!content.isSatisfied())
				return content.whyUnsatisfied();
		} else {
			// create it
			Maybe<Neutral> result = ui.create(Map.ofEntries(Map.entry("@THENAME@", devEnv.toString())));
			if (!result.isSatisfied())
				return result.whyUnsatisfied();
		}

		return Maybe.complete(Neutral.NEUTRAL);
	}

	private String espaceWindowsPathForEclipse(String path) {
		return path.replace("\\", "/").replace(":", "\\:");
	}

	private Maybe<Neutral> copyRepositoryConfiguration(File devEnv) {
		String template = config.getRepoConfTemplate();
		Path target = Paths.get(devEnv.toString(), "/artifacts/repository-configuration.yaml");
		if (template != null) {
			File cfg = new File(template);
			if (cfg.exists()) {
				if (!cfg.isFile())
					return Reasons.build(InvalidArgument.T)
							.text("The repository configuration can only be a yaml file, but \"" + template + "\" was given.").toMaybe();
				try {
					Files.copy(cfg.toPath(), target);
					return Maybe.complete(Neutral.NEUTRAL);
				} catch (IOException e) {
					return Reasons.build(IoError.T).text("Error while copying repository configuration: \"" + template + "\" was given.") //
							.toMaybe();
				}
			} else {
				return Reasons.build(InvalidArgument.T).text("The repository configuration \"" + template + "\" not found.").toMaybe();
			}
		}

		return Reasons.build(InvalidArgument.T).text("No \"repoConfTemplate\" configured in \"dev-env-generator-config.yaml\". Requires fix.")
				.toMaybe();
	}

	private Maybe<Neutral> createCommands(File devEnv) {
		String fileName = devEnv + "/commands/setup-main.yaml";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			writer.write("!com.braintribe.model.platform.setup.api.SetupLocalTomcatPlatform {\n");
			writer.write("\tsetupDependency: \"<your-dependency>\",\n");
			writer.write("\tinstallationPath: \"${config.base}/../tf-setups/main\",\n");
			writer.write("}\n");
			writer.close();

		} catch (IOException e) {
			return Reasons.build(IoError.T).text("Error while creating dev-environment: \"" + fileName + "\".") //
					.toMaybe();
		}
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<Neutral> createDevEnv(File devEnv) {
		Path path = Paths.get(devEnv + "/dev-environment.yaml");
		try {
			Files.createFile(path);
		} catch (IOException e) {
			return Reasons.build(IoError.T).text("Error while creating dev-environment: \"" + path + "\".") //
					.toMaybe();
		}
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<Neutral> createDirectories(File devEnv) {
		List<File> dirs = new ArrayList<>();
		dirs.add(devEnv);
		dirs.add(new File(devEnv, "artifacts"));
		dirs.add(new File(devEnv, "artifacts/inst"));
		dirs.add(new File(devEnv, "git"));
		dirs.add(new File(devEnv, "commands"));
		dirs.add(new File(devEnv, "eclipse-workspace"));
		dirs.add(new File(devEnv, "tf-setups"));
		dirs.add(new File(devEnv, "tf-setups/main"));

		// first check
		for (File dir : dirs) {
			if (dir.exists()) {
				return Reasons.build(AlreadyExists.T).text("dev-env \"" + devEnv + "\", dir \"" + dir + "\" already exists.").toMaybe();
			}
		}
		// then create
		for (File dir : dirs)
			dir.mkdirs();

		return Maybe.complete(Neutral.NEUTRAL);
	}

	public Maybe<Neutral> output(File devEnv, boolean verboseOutput) {
		println(white("Installing:"));
		List<Path> foldPaths = new ArrayList<>();
		if (!verboseOutput)
			foldPaths.add(Paths.get(devEnv.toString(), "eclipse-workspace"));
		outputProjectionDirectoryTree(devEnv.toPath(), foldPaths);
		return Maybe.complete(Neutral.NEUTRAL);
	}
}
