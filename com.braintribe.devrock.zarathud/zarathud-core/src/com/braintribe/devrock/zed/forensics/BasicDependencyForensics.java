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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ClasspathForensics;
import com.braintribe.devrock.zed.api.forensics.DependencyForensics;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.devrock.zed.commons.ZedTokens;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.forensics.structure.DependencyStructureRegistry;
import com.braintribe.devrock.zed.forensics.structure.HasDependencyTagTokens;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.findings.ClasspathForensicIssueType;
import com.braintribe.zarathud.model.forensics.findings.DependencyForensicIssueTypes;

/**
 * class to run forensics on the classpath, i.e. dependency tree
 * finds missing and execess dependencies
 * @author pit
 *
 */
public class BasicDependencyForensics extends ZedForensicsCommons implements DependencyForensics, ZedTokens, HasDependencyTagTokens{

	private ClasspathForensics classpathForensics;
	private Predicate<Artifact> validImplictFilter = t -> false;
	
	
	public BasicDependencyForensics(ZedForensicsContext context) {
		super(context);
		classpathForensics = new BasicClasspathForensics( context);
		if (context.validImplicitArtifactReferenceFilter() != null) {
			validImplictFilter = context.validImplicitArtifactReferenceFilter();
		}
	}


	@Override
	public DependencyForensicsResult runForensics() {
		
		DependencyForensicsResult result = DependencyForensicsResult.T.create();
		result.setArtifact( shallowArtifactCopy( context.terminalArtifact()));
		
		// extract declared
		result.setDeclarations(terminal.getDeclaredDependencies());
		
		// extract actual
		result.setRequiredDeclarations( terminal.getActualDependencies());		
		
		// excess 
		result.setExcessDeclarations( extractExcess( result.getDeclarations(), result.getRequiredDeclarations()));
		
		// missing
		Artifact runtimeArtifact = context.artifacts().runtimeArtifact(context);
		List<Artifact> missingDependencyDeclarations = extractMissing( result.getDeclarations(), result.getRequiredDeclarations(), runtimeArtifact);
		result.setMissingDeclarations( missingDependencyDeclarations);
	
		// details of missing
		result.setMissingArtifactDetails( extractReferencesToMissingDeclarations( missingDependencyDeclarations));
		
		// post process		
		// process forward declarations.. result is now BT specific and not JAVA generic anymore
		result = postProcessDependenciesWithForwardDeclarations(result);
		
		// process references to pre-determined artifacts as valid.. result is now BT specific and not JAVA generic anymore
		result = postProcessDependenciesWithValidImplicitDeclarations(result);

		
		// one finger print per missing dependency declaration 
		List<FingerPrint> fps = result.getFingerPrintsOfIssues();		
		if (result.getMissingDeclarations().size() > 0) {
			result.getMissingDeclarations().stream()
												.map( a -> a.toStringRepresentation())
												.forEach( a -> fps.add(buildFingerPrint(terminal, DependencyForensicIssueTypes.MissingDependencyDeclarations, a)));						
		}
		// one finger print per excess dependency declaration
		if (result.getExcessDeclarations().size() > 0) {		
			result.getExcessDeclarations().stream()
												.map( a -> a.toStringRepresentation())
												.forEach( a -> fps.add(buildFingerPrint(terminal, DependencyForensicIssueTypes.ExcessDependencyDeclarations, a)));		
		}				
		return result;
	}
	
	/**
	 * @param terminal
	 * @param issueType
	 * @param issue
	 * @return
	 */
	private FingerPrint buildFingerPrint( Artifact terminal, DependencyForensicIssueTypes issueType, String issue) {
		// standard fingerprint build
		FingerPrint fingerPrint = FingerPrintExpert.build(terminal, issueType.toString(), Collections.singletonList(issue));
		// enrich with issue key slot (to make it able to be overriden)
		fingerPrint.getSlots().put( HasFingerPrintTokens.ISSUE_KEY, issue);
		return fingerPrint;		
	}
	
	/**
	 * if a filter has been passed, this can filter out any missings declarations
	 * @param ref
	 * @return
	 */
	private boolean test(ArtifactReference ref) {
		ZedEntity z = ref.getTarget();
		List<Artifact> droppable = z.getArtifacts().stream().filter(validImplictFilter).collect(Collectors.toList());
		List<Artifact> filtered = new ArrayList<>( z.getArtifacts());
		filtered.removeAll( droppable);
		if (filtered.size() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * correct result be passing entries thru a filter - some artifact references are valid ... 
	 * @param result
	 * @return
	 */
	private DependencyForensicsResult postProcessDependenciesWithValidImplicitDeclarations( DependencyForensicsResult result) {
		if (result.getMissingArtifactDetails().size() > 0) {
			// not AFRs we can drop afterwards 
			List<ArtifactForensicsResult> afrsToDrop = new ArrayList<>();
			// iterate
			for (ArtifactForensicsResult afr : result.getMissingArtifactDetails()) {
				// get references as stroed 
				List<ArtifactReference> references = afr.getReferences();
				List<ArtifactReference> droppables = references.stream().filter( this::test).collect( Collectors.toList());
				//System.out.println( droppables);
				droppables.stream().forEach( ar -> afr.getReferences().remove(ar));
				int numberOfReferences = afr.getReferences().size();
				afr.setNumberOfReferences( numberOfReferences);
				if (numberOfReferences == 0) {
					afrsToDrop.add( afr);
				}								
			}
			// drop any AFRs 
			for (ArtifactForensicsResult afrToDrop : afrsToDrop) {
				result.getMissingArtifactDetails().remove( afrToDrop);
				Comparators.remove(result.getMissingDeclarations(), afrToDrop.getArtifact());
			}
		}
				
		return result;
	}

	/**
	 * correct result by looking at 'forward declaration' annotated GEs
	 * @param result - the {@link DependencyForensicsResult} as determined
	 * @return
	 */
	private DependencyForensicsResult postProcessDependenciesWithForwardDeclarations(DependencyForensicsResult result) {
		// any missing, ie. details of them? 
		FingerPrint fp = null;
		if (result.getMissingArtifactDetails().size() > 0) {
			// not AFRs we can drop afterwards 
			List<ArtifactForensicsResult> afrsToDrop = new ArrayList<>();
			// iterate
			for (ArtifactForensicsResult afr : result.getMissingArtifactDetails()) {
				// get references as stroed 
				List<ArtifactReference> references = afr.getReferences();
				for (ArtifactReference rf : references) {
					
					ZedEntity referencedEntity = rf.getTarget();
					// a generic entity is an interface 
					if (referencedEntity instanceof InterfaceEntity) {
						InterfaceEntity ie = (InterfaceEntity) referencedEntity;
						// must be a generic entity 
						if (Boolean.TRUE.equals(ie.getGenericNature())) {
							// grap data (aka resolve it) 
							context.resolver().qualify(context, ie);
							// check whether it has a forward declaration annotation, if so, extract 
							Set<TypeReferenceEntity> annotations = ie.getAnnotations();
							for (TypeReferenceEntity tfr : annotations) {
								ZedEntity type = tfr.getReferencedType();
								if (type == null) 
									continue;
								AnnotationEntity ae = (AnnotationEntity) type;
								String name = ae.getDeclaringInterface().getReferencedType().getName();															
								if (name == null)
									continue;
								// is a ForwardDeclation
								if (name.equalsIgnoreCase(FORWARD_ANNOTATION_SIGNATURE)) {
									AnnotationValueContainer avc = ae.getMembers().get("value");
									if (avc != null) {
										String redirect = avc.getSimpleStringValue();																				
										// find the forwarded artifact
										Artifact redirection = findArtifactByName(redirect);
										// mark to drop 
										if (fp == null) {
											fp = FingerPrintExpert.build(context.terminalArtifact(), DependencyForensicIssueTypes.ForwardDeclarations.toString());
											result.getFingerPrintsOfIssues().add(fp);
										}
										// forward reference with no backing dependency trigger an FP
										if (redirection.getIsIncomplete()) {
											FingerPrint forwardDeclarationNotFound = FingerPrintExpert.build(context.terminalArtifact(), DependencyForensicIssueTypes.MissingForwardDependencyDeclarations.toString());
											result.getFingerPrintsOfIssues().add(forwardDeclarationNotFound);
										}
										result.getForwardedReferences().put( rf, redirection);																															
									}
								}							
							}
						}
					}									
				}
				// drop the redirected references from current AFR 
				for (Entry<ArtifactReference, Artifact> entry : result.getForwardedReferences().entrySet()) {
					afr.getReferences().remove( entry.getKey());
					Artifact artifact = entry.getValue();
					if (!Comparators.contains(result.getRequiredDeclarations(), artifact)) {
						if (artifact.getIsIncomplete()) {
							result.getMissingForwardDeclarations().add(artifact);
						}
						result.getRequiredDeclarations().add(artifact);
						Comparators.remove(result.getExcessDeclarations(), artifact);
					}
				}
				// check if AFR is empty -> mark to drop 
				int numberOfReferences = afr.getReferences().size();
				afr.setNumberOfReferences( numberOfReferences);
				if (numberOfReferences == 0) {
					afrsToDrop.add( afr);
				}				
			}	
			// drop any AFRs 
			for (ArtifactForensicsResult afrToDrop : afrsToDrop) {
				result.getMissingArtifactDetails().remove( afrToDrop);
			}
		}
		return result;
	}
	
	/**
	 * find the matching artifact via the map of URL to artifact 
	 * @param name - the condensed name (without version) 
	 * @return - a matching {@link Artifact}
	 */
	private Artifact findArtifactByName( String name) {
		int p = name.indexOf( ':');
		if (p <= 0) {
			throw new IllegalStateException("[" + name + "] is not a valid name for an artifact");			
		}
		String groupId = name.substring(0, p);
		String artifactId = name.substring( p+1);
		for (Artifact artifact : context.urlToArtifactMap().values()) {
			if (
					groupId.equalsIgnoreCase(artifact.getGroupId()) &&
					artifactId.equalsIgnoreCase( artifact.getArtifactId())					
			   ) {
				return artifact;
			}
		}		
		return context.artifacts().unknownArtifact(context, name);
		}

	/**
	 * analyzes the missing declarations  
	 * @param missingDeclarations
	 * @return
	 */
	private List<ArtifactForensicsResult> extractReferencesToMissingDeclarations(List<Artifact> missingDeclarations) {
		List<ArtifactForensicsResult> result = new ArrayList<>();
		for (Artifact artifact : missingDeclarations) {
			// check if it's runtime
			Artifact runtime = context.artifacts().runtimeArtifact(context);
			if (artifact == runtime) {
				continue;
			}
			ArtifactForensicsResult afr = extractArtifactForensics(artifact);
			result.add(afr);
		}
		return result;
	}
	
	
	/**
	 * creates an {@link ArtifactForensicsResult} of what's known of a NON-TERMINAL artifact
	 * @param artifact - the artifact to get the references
	 * @return - a {@link ArtifactForensicsResult}
	 */
	public ArtifactForensicsResult extractArtifactForensics(Artifact artifact) {
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
		
		afr.setNumberOfReferences( afr.getReferences().size());	
		// shadowing check 
		if (afr.getNumberOfReferences() == 0) {						
			ClasspathForensicsResult forensicsOnPopulation = classpathForensics.extractForensicsOnPopulation(artifact);
			afr.getDuplicates().addAll( forensicsOnPopulation.getDuplicates());
			FingerPrint fp = FingerPrintExpert.build(artifact, ClasspathForensicIssueType.ShadowingClassesInClasspath.toString());
			afr.getFingerPrintsOfIssues().add( fp);
		}
		return afr;		
	}
	
	
	
	/**
	 * extract the excess declarations, i.e. declared but never used.. respects tags as supported by {@link DependencyStructureRegistry}
	 * @param declarations - the declared {@link Artifact}s, i.e. what was in the pom 
	 * @param requiredDeclarations - the {@link Artifact}s Zed found referenced
	 * @return - a {@link List} of {@link Artifact}s that are declared yet not referenced
	 */
	private List<Artifact> extractExcess(List<Artifact> declarations, List<Artifact> requiredDeclarations) {
		String terminalKey = context.terminalArtifact().toVersionedStringRepresentation();
		List<Artifact> excesses = new ArrayList<>();
		for (Artifact declared : declarations) {
			String declaredKey = declared.toVersionedStringRepresentation();
			
			// is required : no excess  
			if (Comparators.contains(requiredDeclarations, declared)) {
				continue;
			}
			// somewhere marked as preserve : no excess 
			if (context.structuralRegistry().isMappedAs(declaredKey, PRESERVE)) {
				continue;
			}
			// is either an aggregate or an aggregator : no excess
			if (
					context.structuralRegistry().isMappedAs( declaredKey, terminalKey, AGGREGATE) ||
					context.structuralRegistry().isMappedAs(declaredKey, terminalKey, AGGREGATOR)
				) {
				continue;
			}
			excesses.add( declared);			
			
		}
		return excesses;		
	}

	/**
	 * extract the missing declarations, i.e. not declared, but used.. respects tags as supported by {@link DependencyStructureRegistry}
	 * @param declarations - the declared {@link Artifact}s, i.e. what was in the pom
	 * @param requiredDeclarations - the {@link Artifact}s Zed found referenced
	 * @param runtimeArtifact 
	 * @return
	 */
	private List<Artifact> extractMissing(List<Artifact> declarations, List<Artifact> requiredDeclarations, Artifact runtimeArtifact) {
		String terminalKey = context.terminalArtifact().toVersionedStringRepresentation();
		List<Artifact> missing = new ArrayList<>();
		for (Artifact required : requiredDeclarations) {
			
			// runtime doesn't need to be declared
			if (required == runtimeArtifact) {
				continue;
			}
			String requiredKey = required.toVersionedStringRepresentation();		
			// is required : not missing  
			if (Comparators.contains(declarations, required)) {
				continue;
			}
			// neither mapped as aggregate nor aggregator NOWHERE in tree -> missing
			if (
					!context.structuralRegistry().isMappedAs(requiredKey, AGGREGATE) &&
					!context.structuralRegistry().isMappedAs( requiredKey, AGGREGATOR)
					) {
				missing.add( required);			
				continue;
			}

			// if tagged by terminal is either an aggregate or an aggregator : not missing 
			if (
					context.structuralRegistry().isMappedAs(requiredKey, terminalKey, AGGREGATE) ||
					context.structuralRegistry().isMappedAs( requiredKey, terminalKey, AGGREGATOR)
				) {
				continue;
			}
			
			// can a pure aggregate/aggregator path be followed up to the terminal?
			if (context.structuralRegistry().hasAggregationLineage( context.terminalArtifact().toVersionedStringRepresentation(), requiredKey)) {
				continue;
			}
			
			missing.add( required);	
		}
		return missing;
				
	}

	


	
}
