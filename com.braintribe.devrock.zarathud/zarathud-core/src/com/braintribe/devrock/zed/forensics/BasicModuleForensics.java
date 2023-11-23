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
package com.braintribe.devrock.zed.forensics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ModuleForensics;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.data.ImportedModule;
import com.braintribe.zarathud.model.forensics.data.ModuleReference;

public class BasicModuleForensics extends ZedForensicsCommons implements ModuleForensics {
	
	public BasicModuleForensics(ZedForensicsContext context) {
		super(context);	
	}

	@Override
	public ModuleForensicsResult runModuleForensics() {
		Artifact terminalArtifact = context.terminalArtifact();
		List<Artifact> actualDependencies = terminalArtifact.getActualDependencies();
		
		// extract all module data from all actual dependencies			
		ModuleForensicsResult moduleForensicsResult = ModuleForensicsResult.T.create();
		for (Artifact dependency : actualDependencies) {
			ArtifactForensicsResult dependencyForensicsResult = extractArtifactForensics(dependency);
			
			Pair<ModuleReference, ImportedModule> moduleExtraction = extractModuleReference(dependencyForensicsResult);
			moduleForensicsResult.getModuleImports().add(moduleExtraction.first);
			moduleForensicsResult.getRequiredImportModules().add(moduleExtraction.second);
						
		}
		
		return moduleForensicsResult;
	}
	
	private static class ByLengthSorter implements Comparator<String> {				
		public int compare(String o1, String o2) {												
			int len1 = o1.length();
			int len2 = o2.length();
			if (len1 > len2) {
				return 1;
			}
			if (len1 < len2) {
				return -1;
			}
			return 0;
		}
	}												 		

	
	/**
	 * extracts the {@link ModuleForensicsResult} for the given terminal <-> artifact combination
	 * @param artifactForensicsResult -  the {@link ArtifactForensicsResult} of a depedency 
	 * @return - a {@link ModuleForensicsResult}
	 */
	public Pair<ModuleReference,ImportedModule> extractModuleReference( ArtifactForensicsResult artifactForensicsResult) {
		
		
		// sort both caller and callee lists by their packages ( 		
		Map<String, List<ZedEntity>> moduleStructureOfCallers = extractModuleStructure(artifactForensicsResult, ArtifactReference::getSource);					
		Map<String, List<ZedEntity>> moduleStructureOfCallees = extractModuleStructure(artifactForensicsResult, ArtifactReference::getTarget);
		// build the ModuleReference

		ModuleReference moduleReference = ModuleReference.T.create();
		Artifact artifact = artifactForensicsResult.getArtifact();
		String artifactName = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();
		moduleReference.setArtifactName( artifactName);
		
		// iterate over the artifact references
		Set<String> uniqueImportPackages = new HashSet<>();
		Set<String> uniqueExportPackages = new HashSet<>();
		
		for (ArtifactReference artifactReference : artifactForensicsResult.getReferences()) {
			ZedEntity source = artifactReference.getSource();
			if (!source.getDefinedInTerminal()) {
				System.out.println("unexpected : source not declared in terminal? " + source.getName());
				continue;
			}
			// find the module name of the caller
			String moduleNameOfSource = findModuleOf(moduleStructureOfCallers, source);
			if (moduleNameOfSource == null) {
				System.out.println("no module found for :" + source.getName());
				continue;
			}
			source.setModuleName(moduleNameOfSource);
						 			
			ZedEntity target = artifactReference.getTarget();
			// find the module name of the callee
			String moduleNameOfTarget = findModuleOf(moduleStructureOfCallees, target);			
			target.setModuleName(moduleNameOfTarget);
															
			uniqueImportPackages.add(moduleNameOfTarget);					
		}
		
		moduleReference.getRequiredPackages().addAll(uniqueImportPackages);
		
		for (Map.Entry<String, List<ZedEntity>> entry : moduleStructureOfCallees.entrySet()) {			
			uniqueExportPackages.addAll( entry.getValue().stream().map( ze -> dropToPackage(ze.getName())).collect( Collectors.toList()));			
		}
		ImportedModule importedModule = ImportedModule.T.create();
		importedModule.setArtifactName(artifactName);
		importedModule.getRequiredExports().addAll( uniqueExportPackages);
		
		return Pair.of( moduleReference, importedModule);
	}

	private String findModuleOf(Map<String, List<ZedEntity>> moduleStructure, ZedEntity ze) {
		String moduleName = null;
		for (Map.Entry<String, List<ZedEntity>> entry : moduleStructure.entrySet()) {
			if (entry.getValue().contains(ze)) {
				moduleName = entry.getKey();
				break;
			}
		}
		return moduleName;
	}
	
	private static String dropToPackage( String signature) {
		int p = signature.lastIndexOf('.');
		if (p > 0) {
			return signature.substring(0, p);
		}
		return signature;
	}

	private Map<String, List<ZedEntity>> extractModuleStructure(ArtifactForensicsResult artifactForensicsResult, Function<ArtifactReference,ZedEntity> supplier) {
		List<ZedEntity> collectedCallers = artifactForensicsResult.getReferences().stream().map( r -> supplier.apply(r)).collect( Collectors.toList());
		
		List<String> collectedCallerNames = collectedCallers
											.stream()
											.map( z -> z.getName())
											.collect(Collectors.toList());		
		
		// 
		
		List<String> longestCommonNamesOfCallers = extractLongestCommonNames( collectedCallerNames);		
		
		Map<String, List<ZedEntity>> typesOfModules = new HashMap<>();
		for (String longestCommonName : longestCommonNamesOfCallers) {
			for (ZedEntity ze : collectedCallers) {
				String name = ze.getName();
				if (name.startsWith( longestCommonName)) {
					List<ZedEntity> list = typesOfModules.computeIfAbsent(longestCommonName, lcm -> new ArrayList<>());
					list.add( ze);
				}								
			}
		}
		return typesOfModules;
	}
	
	private static List<String> extractLongestCommonNames(List<String> typeNames) {
		List<String> result = new ArrayList<>();
		List<String> packageNames = typeNames.stream().map( t -> dropToPackage( t)).collect( Collectors.toList());
		packageNames.sort( new ByLengthSorter());
		for (String packageName : packageNames) {
			if (result.size() == 0) {
				result.add( packageName);				
			} else {
				boolean match=false;
				for (String entry : result) {
					if (packageName.startsWith( entry)) {
						match = true;
						break;
					}
				}
				if (!match) {
					result.add( packageName);
				}				
			}			
		}
		
		return result;
	}

	/**
	 * creates an {@link ArtifactForensicsResult} of what's known of a NON-TERMINAL artifact
	 * @param artifact - the artifact to get the references
	 * @return - a {@link ArtifactForensicsResult}
	 */
	private ArtifactForensicsResult extractArtifactForensics(Artifact artifact) {
		ArtifactForensicsResult afr = ArtifactForensicsResult.T.create();
		afr.setArtifact( shallowArtifactCopy(artifact));			
		
		Artifact runtime = context.artifacts().runtimeArtifact(context);
		// references terminal -> artifact
		Map<ZedEntity, List<ZedEntity>> terminalReferencesToArtifact = getTerminalReferencesToArtifact(runtime, artifact);
		for (Entry<ZedEntity, List<ZedEntity>> entry : terminalReferencesToArtifact.entrySet()) {
			for (ZedEntity z : entry.getValue()) {
				ArtifactReference ar = ArtifactReference.T.create();
				ar.setSource( entry.getKey());
				ar.setTarget(z);
				afr.getReferences().add(ar);
			}
		}					
		return afr;		
	}
}
