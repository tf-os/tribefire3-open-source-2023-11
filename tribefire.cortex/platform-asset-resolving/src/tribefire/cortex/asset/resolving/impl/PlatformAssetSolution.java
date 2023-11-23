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
package tribefire.cortex.asset.resolving.impl;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public class PlatformAssetSolution implements Comparable<PlatformAssetSolution> {
	public Solution solution;
	public PlatformAssetNature nature;
	public PlatformAsset asset;
	
	public List<Dependency> filteredDependencies = new ArrayList<>();
	public List<PlatformAssetSolution> dependencies = new ArrayList<>();
	
	public PlatformAssetSolution(Solution solution, PlatformAssetNature nature, ManagedGmSession session, List<RepositoryOrigin> origins, boolean useFullyQualifiedGlobalAssetId) {
		this.solution = solution;
		
		Version version = solution.getVersion();
		String versionAsStr = VersionProcessor.toString(version);
		
		com.braintribe.model.version.Version v = com.braintribe.model.version.Version.parse(versionAsStr);
		
		String id = useFullyQualifiedGlobalAssetId?
				NameParser.buildName(solution):
				solution.getGroupId() + ":" + solution.getArtifactId() + "#" + v.getMajor() + "." + v.getMinor(); // TODO change to stringified v?
		
		this.nature = nature;
		
		this.asset = session.create(PlatformAsset.T);
		
		this.asset.setGlobalId(this.asset.entityType().getShortName() + ":" + id);
		this.asset.setNature(this.nature);
		this.asset.setGroupId(solution.getGroupId());
		this.asset.setName(solution.getArtifactId());
		
		
		String majorMinorAsStr = v.getMajor() + "." + v.getMinor();
		String revisionAsStr = versionAsStr.substring(majorMinorAsStr.length() + 1);

		this.asset.setVersion(majorMinorAsStr);
		ConfigurableCloningContext.build().supplyRawCloneWith(session);
		this.asset.setRepositoryOrigins(EssentialCollectionTypes.TYPE_LIST.clone(ConfigurableCloningContext.build().supplyRawCloneWith(session).done(), origins, StrategyOnCriterionMatch.skip));
		this.asset.setResolvedRevision(revisionAsStr);
		
	}
	

	@Override
	public int compareTo(PlatformAssetSolution o) {
		Solution o1 = this.solution;
		Solution o2 = o.solution;
		
		int res = ArtifactProcessor.compare(o1, o2);
		
		if (res == 0)
			return res;

		return o1.getOrder().compareTo(o2.getOrder());
	}
	
	@Override
	public String toString() {
		return NameParser.buildName(solution);
	}
}
