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
package tribefire.cortex.asset.resolving.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

/**
 * a validator for the most common cases - uses a {@link RepoletContent} as 'expectation description' to check a set of {@link PlatformAssetSolution}
 * 
 * @author pit
 *
 */
public class Validator {	
	
	/**
	 * main validation function 
	 * @param yaml - the {@link File} that contains the YAML formatted expectation {@link RepoletContent}
	 * @param resolution - the {@link AnalysisArtifactResolution} 
	 */
	public static void validate( File yaml, Collection<PlatformAssetSolution> pas) {
		try {
			RepoletContent content = RepositoryGenerations.unmarshallConfigurationFile(yaml);
			validate( content, pas);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("exception thrown while unmarshalling [" + yaml.getAbsolutePath() + "]");
		}
	}
	
	private static String collateMissingDependencies( List<com.braintribe.devrock.model.repolet.content.Dependency> missing) {
		return collate( new ArrayList<VersionedArtifactIdentification>( missing));
	}	
	private static String collateArtifacts( List<Artifact> missing) {
		return collate( new ArrayList<VersionedArtifactIdentification>( missing));
	}
	private static String collate( List<VersionedArtifactIdentification> missing) {
		return missing.stream().map( d -> d.asString()).collect(Collectors.joining(","));
	}
	private static String collateDependencies( List<AnalysisDependency> missing) {
		return missing.stream().map( d -> d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getSolution().getVersion()).collect(Collectors.joining(","));
	}
	private static String collateAnalysisArtifacts( List<AnalysisArtifact> excess) {
		return excess.stream().map( d -> d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getVersion()).collect(Collectors.joining(","));
	}
	
	/**
	 * @param expectedArtifact - expected {@link Artifact} from the {@link RepoletContent}
	 * @param foundArtifact - the {@link AnalysisArtifact} from the resolution
	 */
	private static void validate( Artifact expectedArtifact, AnalysisArtifact foundArtifact) {

		if (expectedArtifact.getOrder() != null) {
			Assert.assertTrue("expected order [" + expectedArtifact.getOrder() + "], but found [" + foundArtifact.getDependencyOrder() + "] for [" + expectedArtifact.asString() + "]", expectedArtifact.getOrder() == foundArtifact.getDependencyOrder());
		}
		
		List<AnalysisDependency> dependencies = foundArtifact.getDependencies();				
		List<com.braintribe.devrock.model.repolet.content.Dependency> missing = new ArrayList<>();
		List<AnalysisDependency> matching = new ArrayList<>();
		for (com.braintribe.devrock.model.repolet.content.Dependency expected : expectedArtifact.getDependencies()) {
			AnalysisDependency found = null;
			for (AnalysisDependency suspect : dependencies) {
				if (
						suspect.getGroupId().equals( expected.getGroupId()) &&
						suspect.getArtifactId().equals( expected.getArtifactId()) &&
						suspect.getSolution().getVersion().equals( expected.getVersion())
					) {
					found = suspect;
					break;
				}
			}
			if (found == null) {
				missing.add( expected);
			}
			else {
				matching.add( found);				
			}			
		}
		String name = expectedArtifact.asString();
		Assert.assertTrue( "artifact : " + name + " -> missing dependencies [" + collateMissingDependencies( missing) + "]", missing.size() == 0);
		
		List<AnalysisDependency> excess = new ArrayList<>( dependencies);
		excess.removeAll( matching);		
		Assert.assertTrue( "artifact : " + name + " -> excess dependencies [" + collateDependencies( excess) + "]", excess.size() == 0);		
		
	}
	
	/**
	 * @param repoletContent - the expectation as {@link RepoletContent}
	 * @param resolution - the {@link AnalysisArtifactResolution} to check
	 */
	private static void validate( RepoletContent repoletContent, Collection<PlatformAssetSolution> pas) {
		List<Artifact> expectedArtifacts = repoletContent.getArtifacts();
		List<AnalysisArtifact> solutions = pas.stream().map( p -> p.solution).collect(Collectors.toList());
		
		// compare any artifacts..
		List<Artifact> missing = new ArrayList<>();
		List<AnalysisArtifact> matching = new ArrayList<>();
		for (Artifact expected : expectedArtifacts) {			
			AnalysisArtifact found = null;
			for (AnalysisArtifact suspect : solutions) {
				if (
						suspect.getGroupId().equals( expected.getGroupId()) && 
						suspect.getArtifactId().equals( expected.getArtifactId()) &&
						suspect.getVersion().equals( expected.getVersion())
					) {
					found = suspect;
					break;
				}
			}
			if (found == null) {
				missing.add(expected);
			}
			else {				
				matching.add(found);
				validate( expected, found);							
			}			
		}
		
		Assert.assertTrue( "missing artifacts [" + collateArtifacts( missing) + "]", missing.size() == 0);		
		
		List<AnalysisArtifact> excess = new ArrayList<>( solutions);
		excess.removeAll( matching);
		Assert.assertTrue( "excess artifacts [" + collateAnalysisArtifacts( excess) + "]", missing.size() == 0);			
	}
}
