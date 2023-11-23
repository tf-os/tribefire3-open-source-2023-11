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
package com.braintribe.devrock.zarathud.test.structure;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.zarathud.test.utils.ClasspathResolvingUtil;
import com.braintribe.devrock.zed.forensics.structure.DependencyStructureRegistry;
import com.braintribe.devrock.zed.forensics.structure.HasDependencyTagTokens;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.zarathud.model.data.Artifact;

@Category(KnownIssue.class)
public class DependencyStructureRegistryLab implements HasDependencyTagTokens {
	public static Maybe<Pair<AnalysisArtifact,Collection<AnalysisArtifact>>> getClasspath(String solution) {
		ClasspathResolutionContext context = ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).done();
		Maybe<AnalysisArtifactResolution> resolutionMaybe = ClasspathResolvingUtil.runAsDependency(solution, context, null);
		
		if (resolutionMaybe.isSatisfied()) {
			AnalysisArtifactResolution resolution = resolutionMaybe.get();
			 AnalysisTerminal analysisTerminal = resolution.getTerminals().get(0);
			AnalysisArtifact analysisArtifact = resolution.getSolutions().stream().filter( s -> s.compareTo(analysisTerminal) == 0).findFirst().orElse(null);
			if (analysisArtifact != null) {
				return Maybe.complete(Pair.of(analysisArtifact, resolution.getSolutions()));
			}
			else {
				return Maybe.empty( Reasons.build(NotFound.T).text("cannot find corresponding artifact in solution list of passed terminal:" + solution).toReason());
			}
		}
		else {
			return resolutionMaybe.emptyCast();
		}
		

	}
	

	/**
	 * @param condensedName
	 * @return
	 */
	public static Artifact toArtifact( String condensedName) {
		String [] values = condensedName.split( ":#");
		if (values.length < 3) {
			throw new IllegalArgumentException("passed value [" + condensedName + "] is not a valid solution name");
		}		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( values[0]);
		artifact.setArtifactId( values[1]);
		artifact.setVersion( values[2]);
		
		return artifact;
	}
	
	/**
	 * @param solution
	 * @return
	 */
	public static Artifact toArtifact( AnalysisArtifact solution) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( solution.getVersion());		
		return artifact;
	}
	
	
	private Maybe<DependencyStructureRegistry> test(String terminal) {
					
		Maybe<Pair<AnalysisArtifact,Collection<AnalysisArtifact>>> mcresultMaybe = getClasspath( terminal);
		if (mcresultMaybe.isSatisfied()) {
			Pair<AnalysisArtifact, Collection<AnalysisArtifact>> pair = mcresultMaybe.get();			
			DependencyStructureRegistry registry = DependencyStructureRegistry.buildRegistry( pair.second.stream().map( aa -> aa.getOrigin()).collect( Collectors.toList()));		
			return Maybe.complete(registry);
		}
		else return mcresultMaybe.emptyCast();
	}
	
	private void validate(  DependencyStructureRegistry registry, String parent, String suspect, String tag) {
		validate(registry, parent, suspect, tag, false);
	}
	private void validate(  DependencyStructureRegistry registry, String parent, String suspect, String tag, boolean reversed) {
		boolean tagged = registry.isMappedAs(suspect, parent, tag);
		if (!reversed) {
			Assert.assertTrue("expected [" + suspect + "] to be tagged with " + tag + " via [" + parent + "]", tagged);
		}
		else {
			Assert.assertTrue("expected [" + suspect + "] NOT to be tagged with " + tag + " via [" + parent + "]", !tagged);
		}
	}
	
	/**
	 * test a terminal with a direct dependency to an aggregator with two aggregates
	 */
	@Test
	public void test_direct_aggregator_terminal() {
		
		String terminalName = "com.braintribe.devrock.test.zarathud:z-direct-aggregator-terminal#1.0.1-pc";
		String aggregator = "com.braintribe.devrock.test.zarathud:z-aggregator-two#1.0.1-pc";

		Maybe<DependencyStructureRegistry> registryMaybe = test( terminalName);
		
		if (registryMaybe.isSatisfied()) {
			DependencyStructureRegistry registry = registryMaybe.get();

			validate( registry, terminalName, aggregator, AGGREGATOR);
			
			String aggregateName3 = "com.braintribe.devrock.test.zarathud:z-aggregate-three#1.0.1-pc";
			validate( registry, aggregator, aggregateName3, AGGREGATE);
			
			String aggregateName4 = "com.braintribe.devrock.test.zarathud:z-aggregate-four#1.0.1-pc";
			validate( registry, aggregator, aggregateName4, AGGREGATE);
		}
		else {
			Assert.fail("test failed as resolution failed : " + registryMaybe.whyUnsatisfied().stringify());
		}
	
	}
	
	/**
	 * test a terminal with a indirect dependency to an aggregator and two aggregates and another aggregator with two further aggregates
	 */
	@Test
	public void test_indirect_aggregator_terminal() {		
		String terminalName = "com.braintribe.devrock.test.zarathud:z-indirect-aggregator-terminal#1.0.1-pc";
		String aggregator1 = "com.braintribe.devrock.test.zarathud:z-aggregator-one#1.0.1-pc";
		String aggregator2 = "com.braintribe.devrock.test.zarathud:z-aggregator-two#1.0.1-pc";
		
		Maybe<DependencyStructureRegistry> registryMaybe = test( terminalName);
		
		if (registryMaybe.isSatisfied()) {
			DependencyStructureRegistry registry = registryMaybe.get();

			validate( registry, terminalName, aggregator1, AGGREGATOR);
			validate( registry, terminalName, aggregator2, AGGREGATOR, true);
			
			String aggregateName3 = "com.braintribe.devrock.test.zarathud:z-aggregate-three#1.0.1-pc";
			validate( registry, aggregator2, aggregateName3, AGGREGATE);
			
			String aggregateName4 = "com.braintribe.devrock.test.zarathud:z-aggregate-four#1.0.1-pc";
			validate( registry, aggregator2, aggregateName4, AGGREGATE);
			
			
			validate( registry, aggregator1, aggregator2, AGGREGATOR);
			
			String aggregateName1 = "com.braintribe.devrock.test.zarathud:z-aggregate-one#1.0.1-pc";
			validate( registry, aggregator1, aggregateName1, AGGREGATE);
			
			String aggregateName2 = "com.braintribe.devrock.test.zarathud:z-aggregate-two#1.0.1-pc";
			validate( registry, aggregator1, aggregateName2, AGGREGATE);
		}
		else {
			Assert.fail("test failed as resolution failed : " + registryMaybe.whyUnsatisfied().stringify());
		}
	}
	
	
	@Test
	public void test_perserve() {
		String terminalName = "com.braintribe.devrock.test.zarathud:z-preserving-terminal#1.0.1-pc";
		String preservingName = "com.braintribe.devrock.test.zarathud:z-preserving-one#1.0.1-pc";
		String aggregateName = "com.braintribe.devrock.test.zarathud:z-aggregate-one#1.0.1-pc";
		
		Maybe<DependencyStructureRegistry> registryMaybe = test( terminalName);
		if (registryMaybe.isSatisfied()) {
			DependencyStructureRegistry registry = registryMaybe.get();			
		
			validate( registry, terminalName, aggregateName, PRESERVE, true);
			validate( registry, preservingName, aggregateName, PRESERVE);
		}
		else {
			Assert.fail("test failed as resolution failed : " + registryMaybe.whyUnsatisfied().stringify());
		}				
		
	}
	
	
	

}
