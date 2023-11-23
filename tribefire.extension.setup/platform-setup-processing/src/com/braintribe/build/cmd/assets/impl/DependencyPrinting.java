package com.braintribe.build.cmd.assets.impl;

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
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.setup.tools.TfSetupOutputs;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolution;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingContext;
import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

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
		}
		else {
			entryEdge = "---";
			edge = "+--";
			lastEdge = "+--";
			connector = "|  ";
		}
	}
	
	public void printAssetDependencyTree(PlatformAssetResolvingContext context, PlatformAssetResolution paResolution) {
		PlatformSetup platformSetup = context.session().findEntityByGlobalId("asset:setup");
		
		PlatformAsset asset = platformSetup.getTerminalAsset();
		
		StringBuilder padding = new StringBuilder();
		ConfigurableConsoleOutputContainer outputs = ConsoleOutputs.configurableSequence();
		Set<PlatformAsset> visited = new HashSet<>();

		outputs.append(brightBlack(entryEdge));
		Map<String, String> origins = new LinkedHashMap<String, String>();
		printAssetDependencyTree(asset, paResolution, outputs, padding, false, visited, origins);
		
		ConfigurableConsoleOutputContainer originsOutput = ConsoleOutputs.configurableSequence();
		
		originsOutput.append("\nResolved assets from:\n");
		
		for (Map.Entry<String, String> entry: origins.entrySet()) {
			originsOutput.append("  ");
			originsOutput.append(entry.getValue());
			originsOutput.append(brightBlack(sequence(text(" -> "), text(entry.getKey()))));
			originsOutput.append("\n");
		}
		
		
		ConsoleOutputs.println(originsOutput);
		
		ConsoleOutputs.println(outputs);
		
		boolean firstDeprecation = true;
		
		for (PlatformAssetSolution solution: paResolution.getSolutions()) {
			PlatformAsset deprecationAsset = solution.asset;
			Deprecated deprecated = findDeprecated(deprecationAsset);
			
			if (deprecated != null && deprecated.getMessage() != null) {
				if (firstDeprecation) {
					firstDeprecation = false;
					ConsoleOutputs.print("Deprecation messages:\n");
				}
				String version = deprecationAsset.getVersion() + "." + deprecationAsset.getResolvedRevision();
				
				ConsoleOutputs.println(sequence(text("\n  asset: "), TfSetupOutputs.solution(asset.getGroupId(), asset.getName(), version)));
				ConsoleOutputs.println(sequence(text("  message: "), ConsoleOutputs.brightYellow(deprecated.getMessage())));
			}
		}
	}
	
	private void printAssetDependencyTree(PlatformAsset asset, PlatformAssetResolution paResolution, ConfigurableConsoleOutputContainer outputs, StringBuilder padding, boolean hasSuccessors, Set<PlatformAsset> visited, Map<String, String> origins) {
		PlatformAssetSolution solution = paResolution.getSolutionForName(asset.qualifiedRevisionedAssetName());
		
		String version = null;
		if (solution != null) {
			version = solution.solution.getVersion();
		} else {
			version = asset.getVersion() + "." + asset.getResolvedRevision();
			
			outputs.append("New: ");
		}
		
		boolean alreadyTraversed = !visited.add(asset);
		
		outputs.append(assetNameColoredByType(asset, version, origins, alreadyTraversed)).append("\n");
		
		if (alreadyTraversed)
			return;

		List<PlatformAssetDependency> qualifiedDependencies = asset.getQualifiedDependencies();
		
		List<PlatformAsset> dependencies = qualifiedDependencies.stream()
				.filter(d -> !d.getSkipped())
				.map(PlatformAssetDependency::getAsset)
				.filter(a -> !a.getPlatformProvided())
				.collect(Collectors.toList());
		
		int dependencyCount = dependencies.size();
		
		int i = 0;
		for (PlatformAsset assetDependency: dependencies) {
			boolean hasChildSuccessors = i++ < (dependencyCount - 1);
			if (hasSuccessors) {
				// has successive siblings
				padding.append(connector);
			}
			else {
				// last dependency
				padding.append(empty);
			}
			
			outputs.append(brightBlack(padding.toString())).append(brightBlack(hasChildSuccessors? edge: lastEdge));
			printAssetDependencyTree(assetDependency, paResolution, outputs, padding, hasChildSuccessors, visited, origins);
			
			padding.setLength(padding.length() - 3);
		}
	}
	
	private ConsoleOutput assetNameColoredByType(PlatformAsset asset, String version, Map<String, String> origins, boolean alreadyTraversed) {

		ConfigurableConsoleOutputContainer solutionOutput = TfSetupOutputs.solution(null, asset.getName(), version);
		

		if (isDeprecated(asset))
			solutionOutput.append(ConsoleOutputs.brightYellow(" DEPRECATED"));
		
		ConfigurableConsoleOutputContainer detailInfo = ConsoleOutputs.configurableSequence();

		solutionOutput.append(brightBlack(detailInfo));
		
		detailInfo.append(" - ");
		detailInfo.append(asset.getGroupId());
		
		if (alreadyTraversed) {
			detailInfo.append(ConsoleOutputs.cyan(" ^ already traversed"));
		}
		else {
			detailInfo.append(" :");
	
			List<RepositoryOrigin> repositoryOrigins = asset.getRepositoryOrigins();
	
			for (RepositoryOrigin origin : repositoryOrigins) {
				detailInfo.append(" ");
				
				String name = origin.getName();
				detailInfo.append(name);
				
				origins.computeIfAbsent(name, k -> origin.getUrl());
			}
		}
		
		return solutionOutput;
	}

	private boolean isDeprecated(PlatformAsset asset) {
		return findDeprecated(asset) != null;
	}
	
	private Deprecated findDeprecated(PlatformAsset asset) {
		return (Deprecated) asset.getNature().getMetaData().stream().filter(m -> m.type() == Deprecated.T).findFirst().orElse(null);
	}

}
