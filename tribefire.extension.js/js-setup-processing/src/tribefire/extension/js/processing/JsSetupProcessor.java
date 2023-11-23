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
package tribefire.extension.js.processing;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.EntityFactory;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContext;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingResult;
import com.braintribe.devrock.mc.core.commons.SymbolicLinker;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.js.JsResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.js.contract.JsResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.js.model.api.request.AssembleJsDeps;
import tribefire.extension.js.model.api.request.JsSetupRequest;
import tribefire.extension.js.model.project.JsProjectDescriptor;
import tribefire.extension.js.model.project.JsSourceFolder;

public class JsSetupProcessor extends AbstractDispatchingServiceProcessor<JsSetupRequest, Object> {

	private static final String JS_PROJECT_YML = "js-project.yml";

	private final static String JS_WORKSPACE_YAML_NAME = "js-workspace.yaml";

	private VirtualEnvironment virtualEnvironment;

	@Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<JsSetupRequest, Object> dispatching) {
		dispatching.register(AssembleJsDeps.T, (ctx, req) -> new JsDepsAssembler(req).run());
	}

	private class JsDepsAssembler {

		private final AssembleJsDeps request;

		public JsDepsAssembler(AssembleJsDeps request) {
			this.request = request;
		}

		public Object run() {
			final String prioritizingMsg = " Prioritizing local project sources as dependency solutions over solutions within the repository.\n";

			File dir = CallerEnvironment.resolveRelativePath(request.getProjectPath());

			if (isWorkspace(dir)) {
				println(sequence( //
						text("\nDetected "), //
						yellow(JS_WORKSPACE_YAML_NAME), //
						text(prioritizingMsg)));

				assembleJsDepsForProject(dir, dir.listFiles());

			} else {
				if (!isJsProject(dir)) {
					println(sequence( //
							text("\n"), //
							yellow(dir.getAbsolutePath()), //
							red(" is neither marked as js-workspace nor js-project. A directory is marked by containing an empty file called "), //
							yellow(JS_WORKSPACE_YAML_NAME), //
							red(" or "), //
							yellow(JS_PROJECT_YML), //
							red(" respectively.")));
					return Neutral.NEUTRAL;
				}

				if (isWorkspace(dir.getParentFile()))
					println(sequence( //
							text("\nDetected "), //
							yellow(JS_WORKSPACE_YAML_NAME), //
							text(" in parent directory."), //
							text(prioritizingMsg)));

				assembleJsDepsForProject(dir.getParentFile(), dir);
			}

			return Neutral.NEUTRAL;
		}

		private void assembleJsDepsForProject(File workspaceDir, File... projectDirs) {
			try (WireContext<JsResolverContract> wireContext = jsResolverModule(workspaceDir)) {
				for (File projectDir : projectDirs)
					if (isJsProject(projectDir))
						assembleJsDeps(projectDir, wireContext.contract());
			}
		}

		// TODO redo once we have AggregatorWireModule
		private WireContext<JsResolverContract> jsResolverModule(File workspaceDir) {
			if (isWorkspace(workspaceDir)) {
				CodebaseRepositoryModule crm = new CodebaseRepositoryModule( //
						workspaceDir, "${artifactId}", emptySet(), singleton("model"));

				return Wire.context(JsResolverWireModule.INSTANCE, new MavenConfigurationWireModule(virtualEnvironment), crm);

			} else {
				return Wire.context(JsResolverWireModule.INSTANCE, new MavenConfigurationWireModule(virtualEnvironment));
			}
		}

		private void assembleJsDeps(File projectDir, JsResolverContract contract) {
			String projectDirAsStr = projectDir.getPath();

			println(sequence( //
					text("\nAssembling JS library dependencies for project in "), //
					yellow(projectDirAsStr)));

			JsProjectDescriptor projectDescriptor = readProjectDescriptor(projectDir);

			Map<File, String> linkFolders = resolveLinksOnSourceFolders(projectDir, projectDescriptor);

			JsLibraryLinkingContext jsLlCoolContext = JsLibraryLinkingContext.build() //
					.preferPrettyOverMin(!request.getPreferMinOverPretty()) //
					.linkFolders(linkFolders) //
					.useSymbolikLinks(true) //
					.done();

			JsLibraryLinker jsLibraryLinker = contract.jsLibraryLinker();

			JsLibraryLinkingResult resolverResult = jsLibraryLinker.linkLibraries(jsLlCoolContext, projectDir);

			// cleanup zombies in lib folder
			Set<File> resolvedLibs = newSet(resolverResult.getLibraryFolders());
			File libDir = new File(projectDir, "lib");

			for (File libFile : libDir.listFiles()) {
				if (!resolvedLibs.contains(libFile))
					libFile.delete();
			}

			// create "sources" folder, which is a symbolic link on "lib" folder
			File sourcesDir = new File(projectDir, "sources");
			SymbolicLinker.ensureSymbolicLink(sourcesDir, libDir, true);

			// update library references
			updateLibraryReferences(projectDir, resolverResult);
		}

		private JsProjectDescriptor readProjectDescriptor(File directory) {
			YamlMarshaller marshaller = new YamlMarshaller();

			File projectDescriptorFile = new File(directory, JS_PROJECT_YML);

			if (!projectDescriptorFile.exists())
				return JsProjectDescriptor.T.create();

			GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
					.setInferredRootType(JsProjectDescriptor.T) //
					.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
					.set(EntityFactory.class, EntityType::create) //
					.build();

			return FileTools.read(projectDescriptorFile) //
					.fromInputStream(in -> (JsProjectDescriptor) marshaller.unmarshall(in, options));
		}

		private Map<File, String> resolveLinksOnSourceFolders(File projectDir, JsProjectDescriptor projectDescriptor) {
			List<JsSourceFolder> sourceJsFolders = projectDescriptor.getSourceFolders();

			return sourceFolders(projectDir, sourceJsFolders) //
					.collect(Collectors.toMap(folder -> folder, File::getName));
		}

		private Stream<File> sourceFolders(File projectDir, List<JsSourceFolder> sourceJsFolders) {
			if (sourceJsFolders.isEmpty())
				return Stream.of(new File(projectDir, "src"));
			else
				return sourceJsFolders.stream() //
						.map(sf -> new File(projectDir, sf.getPath()));
		}

		private void updateLibraryReferences(File currentWorkingDirectory, JsLibraryLinkingResult resolverResult) {
			JsProjectDescriptor projectDescriptor = readProjectDescriptor(currentWorkingDirectory);

			List<JsSourceFolder> sourceFolders = projectDescriptor.getSourceFolders();

			List<JsSourceFolder> updateFolders = sourceFolders.stream().filter(JsSourceFolder::getUpdateResourceLinks).collect(Collectors.toList());

			if (!updateFolders.isEmpty()) {
				println(sequence(text("\nScanning source folders declared in "), //
						yellow(JS_PROJECT_YML), //
						text(" for resource references to be updated:") //
				));

				AnalysisArtifactResolution resolution = resolverResult.getResolution();
				AnalysisArtifact terminal = first(resolution.getTerminals());
				Map<String, List<CompiledDependency>> directDependenciesCandidates = getDirectDependencies(terminal);

				Set<String> dependencyCandidateIdentifications = getDependencyCandidateIdentifications(resolution.getSolutions());
				List<ReplacementUnit> replacementUnits = newList();

				UniversalPath rootPath = UniversalPath.empty();
				for (JsSourceFolder sourceFolder : updateFolders) {
					File folder = new File(currentWorkingDirectory, sourceFolder.getPath());
					UniversalPath sourcePath = rootPath.pushSlashPath(sourceFolder.getPath());
					println(sequence(text("  Scanning "), yellow(sourcePath.toFilePath())));
					collectReplacement(folder, replacementUnits, f -> false /* later use filter from JsSourceFolder configuration */, sourcePath);
				}

				println("");

				boolean replacementsFound = false;
				List<ReplacementUnit> erroneousUnits = newList();
				for (ReplacementUnit replacementUnit : replacementUnits) {
					determineReplacements(replacementUnit, dependencyCandidateIdentifications, directDependenciesCandidates);
					if (!replacementUnit.errors.isEmpty())
						erroneousUnits.add(replacementUnit);
					if (!replacementUnit.replacments.isEmpty())
						replacementsFound = true;
				}

				if (!erroneousUnits.isEmpty()) {
					StringBuilder errorMessageBuilder = new StringBuilder("Error(s) while updating resource links:\n\n");

					erroneousUnits.stream().flatMap(u -> u.errors.stream()).forEach(e -> {
						errorMessageBuilder.append(" - ");
						errorMessageBuilder.append(e);
						errorMessageBuilder.append('\n');
					});

					throw new IllegalStateException(errorMessageBuilder.toString());
				}

				if (replacementsFound) {
					println("Updating resource references:");

					for (ReplacementUnit replacementUnit : replacementUnits) {
						if (replacementUnit.replacments.isEmpty())
							continue;

						applyReplacements(replacementUnit);
					}
				} else {
					println("No resource reference required an update.");
				}

			}
		}

		private Set<String> getDependencyCandidateIdentifications(List<AnalysisArtifact> solutions) {
			Set<String> candidateIdentifications = newSet();
			Set<AnalysisArtifact> visited = newSet();

			for (AnalysisArtifact s : solutions)
				getDependencyCandidateIdentificationsRecursive(s, candidateIdentifications, visited);

			return candidateIdentifications;
		}

		private void getDependencyCandidateIdentificationsRecursive(AnalysisArtifact solution, Set<String> candidateIdentifications,
				Set<AnalysisArtifact> visited) {
			if (!visited.add(solution))
				return;

			candidateIdentifications.add(solution.getGroupId() + "." + solution.getArtifactId());

			for (AnalysisDependency dependency : solution.getDependencies()) {
				getDependencyCandidateIdentificationsRecursive(dependency.getSolution(), candidateIdentifications, visited);
			}
		}

		private Map<String, List<CompiledDependency>> getDirectDependencies(AnalysisArtifact solution) {
			Map<String, List<CompiledDependency>> directDependencies = newMap();
			for (AnalysisDependency dependency : solution.getDependencies()) {
				CompiledDependency cd = dependency.getOrigin();
				String identification = cd.getGroupId() + "." + cd.getArtifactId();
				directDependencies.computeIfAbsent(identification, k -> newList()).add(cd);
			}

			return directDependencies;
		}

		private boolean isWorkspace(File dir) {
			return new File(dir, JS_WORKSPACE_YAML_NAME).exists();
		}

		private boolean isJsProject(File dir) {
			return new File(dir, JS_PROJECT_YML).exists();
		}

	}

	private static class ReplacementUnit {
		List<String> errors = newList();
		List<Replacement> replacments = newList();
		File file;
		UniversalPath path;

		public ReplacementUnit(File file, UniversalPath path) {
			super();
			this.file = file;
			this.path = path;
		}

		void addReplace(int startIndex, int endIndex, String text, String groupId, String artifactId, String oldVersionExp, String newVersionExp) {
			Replacement r = new Replacement();
			r.startIndex = startIndex;
			r.endIndex = endIndex;
			r.text = text;
			r.groupId = groupId;
			r.artifactId = artifactId;
			r.newVersionExpression = newVersionExp;
			r.oldVersionExpression = oldVersionExp;
			replacments.add(r);
		}
	}

	private static class Replacement {
		public String oldVersionExpression;
		int startIndex;
		int endIndex;
		String groupId;
		String artifactId;
		String newVersionExpression;
		String text;
	}

	// private static final Pattern resourceReferencePattern = Pattern.compile("(import[\\W].*)?('|\"|\\(\\s*)..\\/(.*?)\\.(.*?)-(.*)\\/");
	private static final Pattern resourceReferencePattern = Pattern
			.compile("(import[\\W].*)?('|\"|\\(\\s*)..\\/(.*?)\\.(.*?)-(\\d+~|\\d+\\.\\d+.*?|[\\(\\[].*?)\\/");

	private static void collectReplacement(File folder, List<ReplacementUnit> units, Predicate<File> exclusionFilter, UniversalPath relativePath) {
		for (File file : folder.listFiles()) {
			UniversalPath filePath = relativePath.push(file.getName());

			if (file.isDirectory()) {
				collectReplacement(file, units, exclusionFilter, filePath);
			} else {
				if (exclusionFilter.test(file))
					continue;

				ReplacementUnit unit = new ReplacementUnit(file, filePath);
				units.add(unit);
			}
		}
	}

	private static void applyReplacements(ReplacementUnit replaceProtocol) {
		println(sequence(text("\nFile: "), ConsoleOutputs.yellow(replaceProtocol.path.toFilePath())));

		String text = FileTools.read(replaceProtocol.file).asString();

		FileTools.write(replaceProtocol.file).usingWriter(writer -> {

			int index = 0;
			int charPos = 0;
			for (Replacement replacement : replaceProtocol.replacments) {
				String originalFragment = text.substring(index, replacement.startIndex);
				writer.write(originalFragment);
				writer.write(replacement.text);
				index = replacement.endIndex;

				charPos += originalFragment.length();

				println(sequence(text("  Updated "), formatArtifactReference(replacement, false), text(" to "),
						formatArtifactReference(replacement, true), text(" at char position "), yellow(Integer.toString(charPos))));

				charPos += replacement.text.length();
			}

			writer.write(text.substring(index, text.length()));
		});
	}

	private static ConsoleOutput formatArtifactReference(Replacement replacement, boolean newVersion) {
		// @formatter:off
		return sequence(
			brightBlack(replacement.groupId), 
			brightBlack("."), 
			text(replacement.artifactId), 
			brightBlack("-"), 
			green(newVersion ? replacement.newVersionExpression : replacement.oldVersionExpression)
		);
		// @formatter:on
	}

	private static void determineReplacements(ReplacementUnit replacementUnit, Set<String> candidates,
			Map<String, List<CompiledDependency>> directDependenciesCandidates) {

		String text = FileTools.read(replacementUnit.file).asString();

		Matcher matcher = resourceReferencePattern.matcher(text);

		int index = 0;

		for (; matcher.find(index); index = matcher.end()) {
			boolean isImport = matcher.group(1) != null;
			String artifactIdentification = matcher.group(3) + "." + matcher.group(4);
			String oldVersionExpression = matcher.group(5);
			String artifact = artifactIdentification + "-" + oldVersionExpression;

			List<CompiledDependency> directDependencies = directDependenciesCandidates.get(artifactIdentification);

			if (isImport) {
				if (directDependencies == null)
					replacementUnit.errors
							.add("Missing direct dependency '" + artifact + "' for module import in " + replacementUnit.path.toFilePath());
			} else {
				if (!candidates.contains(artifactIdentification))
					continue;

				if (directDependencies == null)
					replacementUnit.errors
							.add("Missing direct dependency '" + artifact + "' for resource reference in " + replacementUnit.path.toFilePath());
			}

			VersionExpression versionExpression = parseSpecialVersionExpression(oldVersionExpression);
			String versionExpressionStr = versionExpression.asString();

			boolean found = false;

			for (CompiledDependency compiledDependency : directDependencies) {
				String dependencyVersionExpression = compiledDependency.getVersion().asString();

				if (dependencyVersionExpression.equals(versionExpressionStr)) {
					found = true;
					break;
				}
			}

			if (found)
				continue;

			// choosing the first dependency occurrence to patch a resource reference where the version could not be found in the dependencies
			CompiledDependency selectedDependency = directDependencies.get(0);

			StringBuilder substituteBuilder = new StringBuilder();

			if (isImport)
				substituteBuilder.append(matcher.group(1));

			String groupId = selectedDependency.getGroupId();
			String artifactId = selectedDependency.getArtifactId();
			String newVersionExpression = selectedDependency.getVersion().asShortNotation();

			substituteBuilder.append(matcher.group(2));
			substituteBuilder.append("../");
			substituteBuilder.append(groupId);
			substituteBuilder.append('.');
			substituteBuilder.append(artifactId);
			substituteBuilder.append('-');
			substituteBuilder.append(newVersionExpression);
			substituteBuilder.append('/');

			replacementUnit.addReplace(matcher.start(), matcher.end(), substituteBuilder.toString(), groupId, artifactId, oldVersionExpression,
					newVersionExpression);
		}

	}

	private static VersionExpression parseSpecialVersionExpression(String specialVersionExpression) {
		if (specialVersionExpression.endsWith("~")) {
			String versionStr = specialVersionExpression.substring(0, specialVersionExpression.length() - 1);

			Version version = Version.parse(versionStr);
			boolean minorRange = versionStr.indexOf('.') != -1;

			if (minorRange) {
				// 1.0~ -> [1.0,1.1)
				return VersionRange.from(version, false, Version.create(version.getMajor(), version.getMinor() + 1), true);
			} else {
				// 1~ -> [1,2)
				return VersionRange.from(Version.create(version.getMajor(), 0), false, Version.create(version.getMajor() + 1, 0), true);
			}
		} else {
			return VersionExpression.parse(specialVersionExpression);
		}
	}
}
