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
package com.braintribe.artifact.processing.backend.transpose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.processing.PlatformAssetResolution;
import com.braintribe.model.artifact.processing.ResolvedArtifactPart;
import com.braintribe.model.artifact.processing.ResolvedPlatformAsset;
import com.braintribe.model.artifact.processing.service.data.ResolvedPlatformAssets;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

import tribefire.cortex.asset.resolving.impl.PlatformAssetSolution;

public class PlatformAssetTransposer extends TransposerCommons {
	private static Logger log = Logger.getLogger(PlatformAssetTransposer.class);

	private Map<PlatformAssetSolution, ResolvedPlatformAsset> transposeMap = new ConcurrentHashMap<>();
	private PlatformAssetSolution terminal;

	/**
	 * @deprecated is not needed any more
	 */
	@Configurable
	@Required
	@Deprecated
	public void setPopulation(Collection<PlatformAssetSolution> pas) {
		// noop
	}

	@Configurable
	@Required
	public void setTerminal(PlatformAssetSolution terminal) {
		this.terminal = terminal;
	}

	/**
	 * @deprecated use {@link #transpose(RepositoryReflection, PlatformAssetSolution)} instead
	 */
	@Deprecated
	public static PlatformAssetResolution transpose(RepositoryReflection reflection, PlatformAssetSolution terminal, Collection<PlatformAssetSolution> solutions) {
		return transpose(reflection, terminal);
	}
	
	public static PlatformAssetResolution transpose(RepositoryReflection reflection, PlatformAssetSolution terminal) {
		PlatformAssetTransposer transposer = new PlatformAssetTransposer();
		transposer.setRepositoryReflection(reflection);
		transposer.setTerminal(terminal);
		
		PlatformAssetResolution result = PlatformAssetResolution.T.create();
		result.setResolvedPlatformAsset(transposer.transpose(terminal));
		result.setDependencies(transposer.transpose());
		return result;
	}
	
	

	public List<ResolvedPlatformAsset> transpose() {

		ResolvedPlatformAsset transposedTerminal = transpose(terminal);
		
		Set<ResolvedPlatformAsset> dependencies = new HashSet<>();
		for (ResolvedPlatformAsset dependency : transposedTerminal.getDependencies()) {
			collectDependencies( dependency, dependencies);
		}

		List<ResolvedPlatformAsset> results = new ArrayList<>( dependencies);

		return results;
	}

	private void collectDependencies(ResolvedPlatformAsset asset, Set<ResolvedPlatformAsset> dependencies) {
		dependencies.add( asset);
		for (ResolvedPlatformAsset dependency : asset.getDependencies()) {
			collectDependencies( dependency, dependencies);
		}		
	}

	private ResolvedPlatformAsset transpose(PlatformAssetSolution pas) {		
		//return transposeMap.computeIfAbsent(pas, this::_transpose);
		ResolvedPlatformAsset value = transposeMap.get(pas);
		if (value == null) {
			value = _transpose(pas);
			transposeMap.put( pas, value);
		}
		return value;
	}

	private ResolvedPlatformAsset _transpose(PlatformAssetSolution pa) {
		ResolvedPlatformAsset artifact = ResolvedPlatformAsset.T.create();
		artifact.setGroupId(pa.solution.getGroupId());
		artifact.setArtifactId(pa.solution.getArtifactId());
		artifact.setVersion(VersionProcessor.toString(pa.solution.getVersion()));
		artifact.getRepositoryOrigins().addAll(getRepositoryOrigins(pa.solution));
		artifact.setNature(pa.nature);

		for (PlatformAssetSolution dependency : pa.dependencies) {
			ResolvedPlatformAsset resolvedDependency = transpose(dependency);
			artifact.getDependencies().add(resolvedDependency);
		}

		for (Part part : pa.solution.getParts()) {
			ResolvedArtifactPart resolvedPart = transpose(part);
			artifact.getParts().add(resolvedPart);
		}

		return artifact;
	}

	public static ResolvedPlatformAssets transpose(RepositoryReflection reflection, List<PlatformAssetSolution> terminals) {
		PlatformAssetTransposer transposer = new PlatformAssetTransposer();
		transposer.setRepositoryReflection(reflection);
		//transposer.setTerminal(terminal);
		
		ResolvedPlatformAssets rtv = ResolvedPlatformAssets.T.create();

		Set<ResolvedPlatformAsset> dependencies = new HashSet<>();
		for (PlatformAssetSolution pa : terminals) {
			ResolvedPlatformAsset rpa = transposer.transpose( pa);
			for (ResolvedPlatformAsset dependency : rpa.getDependencies()) {
				transposer.collectDependencies( dependency, dependencies);
			}
			dependencies.add( rpa);
			rtv.getTerminalAssets().add( rpa);
		}
		rtv.getResolvedAssets().addAll( dependencies);				
		return rtv;
	}
}
