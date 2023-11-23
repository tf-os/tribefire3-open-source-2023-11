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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ClasspathForensics;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.data.ClasspathDuplicate;
import com.braintribe.zarathud.model.forensics.findings.ClasspathForensicIssueType;

/**
 * 
 *  forensic expert for classpath 
 * 
 * <ul>forensics for the classpath 
 * <li>shadowing -> MAJOR_ISSUES</li>
 * </ul>
 * @author pit
 *
 */
public class BasicClasspathForensics extends ZedForensicsCommons implements ClasspathForensics {
	
	public BasicClasspathForensics(ZedForensicsContext context) {
		super(context);
	}

	@Override
	public ClasspathForensicsResult runForensics() {
		Set<ZedEntity> population = context.terminalArtifact().getEntries();
		ClasspathForensicsResult forensicsResult = extractForensicsOnPopulation(population);
		forensicsResult.setArtifact( shallowArtifactCopy(context.terminalArtifact()));
		return forensicsResult;
	}

	public ClasspathForensicsResult extractForensicsOnPopulation( Collection<ZedEntity> population) {
		Artifact runtimeArtifact = context.artifacts().runtimeArtifact(context);
		ClasspathForensicsResult result = ClasspathForensicsResult.T.create();
		
		List<Pair<ZedEntity, List<TypeReferenceEntity>>> pairs = new ArrayList<>(population.size());
		for (ZedEntity z : population) {
			if (!z.getDefinedInTerminal()) {
				continue;
			}
			List<TypeReferenceEntity> typeReferencesOfEntity = getTypeReferencesOfEntity(z);			
			pairs.add( Pair.of(z, typeReferencesOfEntity));
		}
						
	
		population.stream().forEach( z -> {
			if (z.getArtifacts().size() > 1) {
				ClasspathDuplicate duplicate = ClasspathDuplicate.T.create();				
				
				for (Pair<ZedEntity, List<TypeReferenceEntity>> pair : pairs)  {								
					TypeReferenceEntity trf = pair.second.stream().filter( r -> r.getReferencedType().equals(z)).findFirst().orElse( null);
					if (trf != null) {
						duplicate.getReferencersInTerminal().add( pair.first);						
					}
				}									
				duplicate.setType(z);
				duplicate.setDuplicates( z.getArtifacts());
				if (Comparators.contains( z.getArtifacts(), runtimeArtifact)) {
					duplicate.setShadowingRuntime( true);
				}
				result.getDuplicates().add(duplicate);
				// generate finger print, add 
				FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ClasspathForensicIssueType.ShadowingClassesInClasspath.toString(), Collections.singletonList( duplicate.toStringRepresentation()));
				result.getFingerPrintsOfIssues().add( fp);
			}
		});
		
		return result;
	}

	@Override
	public ClasspathForensicsResult extractForensicsOnPopulation(Artifact artifact) {
		Set<ZedEntity> population = context.terminalArtifact().getEntries();
		List<ZedEntity> subset = population.stream().filter( z -> Comparators.contains( z.getArtifacts(), artifact)).collect(Collectors.toList());
		ClasspathForensicsResult classpathForensicsResult = extractForensicsOnPopulation(subset);
		return classpathForensicsResult;
	}
	
	
}
