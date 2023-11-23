package com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.setup.tools.TfSetupOutputs;

/**
 * This class is copied from {@code com.braintribe.build.cmd.assets.impl.DependencyPrinting} and it is adjusted in such
 * way so it can print the resolved views. Ideally we would reuse the code, however the original class is printing the
 * solutions of a {@code PlatformAssetResolution} and we want to print the solutions of a
 * {@code AnalysisArtifactResolution}.
 */
public class DependencyPrinting {
	private final String entryEdge;
	private final String edge;
	private final String lastEdge;
	private final String connector;
	private final String empty = "   ";

	public DependencyPrinting(boolean asci) {
		if (!asci) {
			entryEdge = "───";
			edge = "├──";
			lastEdge = "└──";
			connector = "│  ";
		} else {
			entryEdge = "---";
			edge = "+--";
			lastEdge = "+--";
			connector = "|  ";
		}
	}

	public void printAssetDependencyTree(AnalysisArtifactResolution resolution) {

		StringBuilder padding = new StringBuilder();
		ConfigurableConsoleOutputContainer outputs = ConsoleOutputs.configurableSequence();
		Set<AnalysisDependency> visited = new HashSet<>();

		Map<String, String> origins = new LinkedHashMap<String, String>();
		resolution.getTerminals().stream().forEach(terminal -> {
			outputs.append(brightBlack(entryEdge));
			printAssetDependencyTree((AnalysisDependency) terminal, outputs, padding, false, visited, origins);
		});

		ConfigurableConsoleOutputContainer originsOutput = ConsoleOutputs.configurableSequence();

		originsOutput.append("\nResolved views from:\n");

		for (Map.Entry<String, String> entry : origins.entrySet()) {
			originsOutput.append("  ");
			originsOutput.append(entry.getValue());
			originsOutput.append(brightBlack(sequence(text(" -> "), text(entry.getKey()))));
			originsOutput.append("\n");
		}
		ConsoleOutputs.println(originsOutput);
		ConsoleOutputs.println(outputs);
	}

	private void printAssetDependencyTree(AnalysisDependency dependency, ConfigurableConsoleOutputContainer outputs, StringBuilder padding,
			boolean hasSuccessors, Set<AnalysisDependency> visited, Map<String, String> origins) {
		AnalysisArtifact solution = dependency.getSolution();

		String version = solution.getVersion();

		boolean alreadyTraversed = !visited.add(dependency);

		outputs.append(assetNameColoredByType(dependency, version, alreadyTraversed)).append("\n");

		if (alreadyTraversed)
			return;

		List<AnalysisDependency> qualifiedDepsendencies = dependency.getSolution().getDependencies();

		int dependencyCount = qualifiedDepsendencies.size();

		int i = 0;
		for (AnalysisDependency assetDependency : qualifiedDepsendencies) {
			boolean hasChildSuccessors = i++ < (dependencyCount - 1);
			if (hasSuccessors) {
				// has successive siblings
				padding.append(connector);
			} else {
				// last dependency
				padding.append(empty);
			}

			outputs.append(brightBlack(padding.toString())).append(brightBlack(hasChildSuccessors ? edge : lastEdge));
			printAssetDependencyTree(assetDependency, outputs, padding, hasChildSuccessors, visited, origins);

			padding.setLength(padding.length() - 3);
		}
	}

	private ConsoleOutput assetNameColoredByType(AnalysisDependency dependency, String version, boolean alreadyTraversed) {

		ConfigurableConsoleOutputContainer solutionOutput = TfSetupOutputs.solution(null, dependency.getArtifactId(), version);

		ConfigurableConsoleOutputContainer detailInfo = ConsoleOutputs.configurableSequence();

		solutionOutput.append(brightBlack(detailInfo));

		detailInfo.append(" - ");
		detailInfo.append(dependency.getGroupId());

		if (alreadyTraversed) {
			detailInfo.append(ConsoleOutputs.cyan(" ^ already traversed"));
		} else {
			detailInfo.append(" :");
			final String repositoriesOrigins = dependency.getSolution().getParts().values().stream() //
					.map(part -> part.getRepositoryOrigin()) //
					.collect(Collectors.toSet()) //
					.stream().collect(Collectors.joining(","));
			detailInfo.append(repositoriesOrigins);
		}

		return solutionOutput;
	}
}
