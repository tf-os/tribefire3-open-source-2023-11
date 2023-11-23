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
package com.braintribe.tribefire.jinni.support;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.cyan;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.model.service.api.result.Neutral.NEUTRAL;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.GetConfig;
import com.braintribe.model.jinni.api.GetVersion;
import com.braintribe.model.jinni.api.Introduce;
import com.braintribe.model.jinni.api.JinniReflectionRequest;
import com.braintribe.model.jinni.api.PrintVersion;
import com.braintribe.model.jinni.api.ReflectLibraries;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.setup.tools.TfSetupOutputs;
import com.braintribe.tribefire.jinni.core.JinniTools;

public class JinniReflectionProcessor extends AbstractDispatchingServiceProcessor<JinniReflectionRequest, Object> {

	private File installationDir;

	// ░ ▒ ▓ █

	// @formatter:off
	protected static String titleBlockAsciArt[] = {
		"    ___       ___       ___       ___       ___   ",
		"   /\\  \\     /\\  \\     /\\__\\     /\\__\\     /\\  \\  ",
		"  _\\:\\  \\   _\\:\\  \\   /:| _|_   /:| _|_   _\\:\\  \\ ",
		" /\\/::\\__\\ /\\/::\\__\\ /::|/\\__\\ /::|/\\__\\ /\\/::\\__\\",
		" \\::/\\/__/ \\::/\\/__/ \\/|::/  / \\/|::/  / \\::/\\/__/",
		"  \\/__/     \\:\\__\\     |:/  /    |:/  /   \\:\\__\\  ",
		"             \\/__/     \\/__/     \\/__/     \\/__/  ",
	};
	

	protected static String titleSimpleAsci[] = {
		"   __  _  __  _  __  _  _  ",
		" __) || ||  \\| ||  \\| || | ",
		" \\___/|_||_|\\__||_|\\__||_| ",
	};

	protected static String titleImpossibleTriangle[] = {
		"      _",
		"     /_/\\",
		"    / /\\ \\",
		"   / / /\\ \\",
		"  / / /\\ \\ \\",
		" / /_/__\\ \\ \\",
		"/_/______\\_\\/\\",
		"\\_\\_________\\/",
	};
	
	protected static String titleMinAsciArt[] = {
			"   ┌─┬─┐       ┌─┐ ",
			" ┌─┤ ├─┼───┬───┼─┤ ",
			" │ ┘ │ │ │ │ │ │ │ ",
			" └───┴─┴─┴─┴─┴─┴─┘ ",
	};
	
	// ─ │ ┌ ┐ └ ┘ ├ ┤ ┬ ┴ ┼ 
	
	protected static String titleArabicSimple[] = {
			"        '  ┌───┐",
			" ┌    ┌─┴──────┘",
			" └────┘     ' ",
			"   ''        ",
	};
	
	protected static String titleArabicEdgy[] = {
			"                  ",
			"         °  ┌────┐",
			" ┌    ┌──┴───────┘",
			" └────┘        °  ",
			"   °°             ",
	};
	
	// ╭ ╮ ╯ ╰
	
	protected static String titleArabicRound[] = {
			"                  ",
			"         °  ╭────╮",
			" ╭    ╭──┴───────╯",
			" ╰────╯        °  ",
			"   °°             ",
	};
	
//   ┌──┬──┐  ╔══╦══╗ ╒══╤══╕ ╓──╥──╖
//	 │  │  │  ║  ║  ║ │  │  │ ║  ║  ║
//	 ├──┼──┤  ╠══╬══╣ ╞══╪══╡ ╟──╫──╢
//	 │  │  │  ║  ║  ║ │  │  │ ║  ║  ║
//	 └──┴──┘  ╚══╩══╝ ╘══╧══╛ ╙──╨──╜
	
	protected static String titleArabicEdgyLongDouble[] = {
			"                              ",
			"               °  ╔══════════╗",
			" ╔          ╔══╩═════════════╝",
			" ╚══════════╝              °  ",
			"     ° °                      ",
	};
	
	protected static String titleArabicEdgyLong[] = {
			"",
			"                °   ┌─────────┐",
			" ┌           ┌──┴─────────────┘",
			" └───────────┘              ° ",
			"     °  °     ",
	};
	
	protected static String titleAsciArt[] = {
	"  __  _             _", 
	"  \\ \\(_)_ __  _ __ (_)",
	"   \\ \\ | '_ \\| '_ \\| |",
	"/\\_/ / | | | | | | | |",
	"\\___/|_|_| |_|_| |_|_|"
	};
	// @formatter:on

	private final String title[] = titleArabicEdgy;
	
	private ModeledConfiguration modeledConfiguration;
	private Map<String, EntityType<?>> typeShortcuts;

	@Override
	protected void configureDispatching(DispatchConfiguration<JinniReflectionRequest, Object> dispatching) {
		dispatching.register(Introduce.T, (c, r) -> introduce());
		dispatching.register(ReflectLibraries.T, (c, r) -> reflectLibraries());
		dispatching.register(PrintVersion.T, (c, r) -> printVersion());
		dispatching.register(GetVersion.T, (c, r) -> getVersion());
		dispatching.registerReasoned(GetConfig.T, this::getConfig);
	}

	@Configurable
	@Required
	public void setInstallationDir(File installationDir) {
		this.installationDir = installationDir;
	}

	@Required
	public void setModeledConfiguration(ModeledConfiguration modeledConfiguration) {
		this.modeledConfiguration = modeledConfiguration;
	}

	@Required
	public void setTypeShortcuts(Map<String, EntityType<?>> typeShortcuts) {
		this.typeShortcuts = typeShortcuts;
	}
	
	private Neutral introduce() {
		if (Boolean.TRUE.toString().equals(System.getProperty("jinni.legacy.options")))
			introduceLegacy();
		else
			introduceNew();

		return NEUTRAL;
	}

	private void introduceLegacy() {
		String version = determineVersion();

		println(brightBlack(generateBlockGraphic(title)));

		println(sequence(brightWhite("Jinni"), text(" - version: "), version.endsWith("-pc") ? yellow(version) : green(version)));

		println(sequence(yellow("\nusage: "), brightWhite("jinni "), cyan("<request> "), text("property1"), text("="), cyan("value1 "),
				text("property2"), text("="), cyan("value2 "), text("... "), text("-option1 "), cyan("x "), text("-option2 "), cyan("y")));

		println(sequence(yellow("help: "), brightWhite("jinni "), cyan("help")));
		println(sequence(yellow("reflect libraries: "), brightWhite("jinni "), cyan("reflect-libraries")));
	}

	private String determineVersion() {
		String version = PackagedSolution.readSolutionsFrom(installationDir).stream() //
				.filter(s -> s.groupId.equals("tribefire.extension.setup") && s.artifactId.equals("jinni")) //
				.map(s -> s.version).findFirst().orElse("unknown");
		return version;
	}

	private void introduceNew() {
		String version = determineVersion();
		// out(ConsoleOutputs.brightBlack(generateBlockGraphic(title)));
		println(sequence(brightBlack(generateBlockGraphic(title)),
				sequence(text("Jinni"), text(" - version: "), JinniTools.isSnapshotVersion(version) ? yellow(version) : green(version))));

		println(sequence(yellow("get help with: "), brightWhite("jinni "), cyan("help")));

		ConsoleOutputs.print("\n\n");
	}

	private static ConsoleOutput generateBlockGraphic(String lines[]) {
		ConfigurableConsoleOutputContainer sequence = ConsoleOutputs.configurableSequence();

		for (String line : lines) {
			sequence.append(line);
			sequence.append("\n");
		}

		return sequence;
	}

	private Neutral reflectLibraries() {

		Set<PackagedSolution> packagedSolutions = PackagedSolution.readSolutionsFrom(installationDir);

		for (PackagedSolution packagedSolution : packagedSolutions) {
			String groupId = packagedSolution.groupId;
			String artifactId = packagedSolution.artifactId;
			String version = packagedSolution.version;
			println(TfSetupOutputs.solution(groupId, artifactId, version));
		}

		return Neutral.NEUTRAL;
	}

	private String getVersion() {
		return determineVersion();
	}
	
	private Maybe<Object> getConfig(ServiceRequestContext context, GetConfig request) {
		
		String name = request.getName();
		
		if (name == null)
			Reasons.build(InvalidArgument.T).text("GetConfig.name must not be empty");
		
		EntityType<?> entityType = typeShortcuts.get(name);
		
		if (entityType == null) {
			entityType = GMF.getTypeReflection().findEntityType(name);
		}
		
		if (entityType == null)
			return Reasons.build(NotFound.T).text("GetConfig.name [" + name + "] could not be resolved to a config type").toMaybe();
			
		
		return modeledConfiguration.configReasoned(entityType).cast();
	}

	private Neutral printVersion() {
		String version = determineVersion();

		println(TfSetupOutputs.version(version));

		return Neutral.NEUTRAL;
	}

}
