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
package com.braintribe.devrock.zed.analyze;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.devrock.zed.analyze.dependency.ArtifactMatcher;
import com.braintribe.devrock.zed.analyze.dependency.DependencyExtracter;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.core.ZedArtifactAnalyzer;
import com.braintribe.devrock.zed.commons.ConversionHelper;
import com.braintribe.devrock.zed.commons.IdentificationWrapperCodec;
import com.braintribe.devrock.zed.commons.ZedException;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

public class BasicZedArtifactAnalyzer implements ZedArtifactAnalyzer {
	private static final String GENERIC_ENTITY = "com.braintribe.model.generic.GenericEntity";
	private static Logger log = Logger.getLogger(BasicZedArtifactAnalyzer.class);			

	@Override
	public Artifact analyzeArtifact(ZedAnalyzerContext context, List<String> classes) {	
	
		// process classes 
		try {
			analyzeInput( context, classes);
		} catch (Exception e) {
			String msg="error while analyzing the input classes";
			log.error( msg, e);
			throw new ZedException(msg, e);
		}
		
 		
		// analyze generic nature 
		try {
			analyzeGenericity( context);
		} catch (Exception e) {
			String msg="error while analyzing the GM relations ";
			log.error( msg, e);
			throw new ZedException(msg, e);
		}
		
		// analyze/wire type hierarchy 
		try {
			analyzeTypeHierarchy( context);
		} catch (Exception e) {
			String msg="error while analyzing the type hierarchy";
			log.error( msg, e);
			throw new ZedException(msg, e);
		}

		// create the container entity		
		Artifact artifact = context.terminalArtifact();
 
		artifact.getEntries().addAll( context.registry().population());
								
		// add declared dependencies : tries to find the solutions of the dependencies in the classpath to retrieve their solutions (the ACTUAL artifact!)		
		List<Artifact> declaredArtifactDependencies = collectDeclaredDependencies( context);				
		artifact.setDeclaredDependencies( declaredArtifactDependencies);
		
		// add actual dependencies via traversion
		List<Artifact> actualDependencies = new DependencyExtracter().collectDependencies(artifact);
		artifact.setActualDependencies(actualDependencies);
				

		return artifact;
	}
	/**
	 * @param context - {@link ZedAnalyzerContext} to use 
	 * @return - all {@link ZedEntity} that are still flagged as unscanned
	 */
	private Set<ZedEntity> getUnscannedEntities(ZedAnalyzerContext context) {
		return context.registry().population().stream().filter( z -> {
			return (!z.getScannedFlag());
		}).collect(Collectors.toSet());				
	}
		
	/**
	 * analyze the actual input (list of starting points)
	 * @param context - {@link ZedAnalyzerContext} to use 
	 * @param classnames - a {@link List} of {@link String} with the class names from the terminal
	 */
	private void analyzeInput(ZedAnalyzerContext context, List<String> classnames) {
		// 
		for (String className : classnames) {					
			ZedEntity zarathudEntity = context.registry().acquire(context, "L" + className.replace('.', '/') + ";", ZedEntity.T);
			zarathudEntity.setDefinedInTerminal(true);
		}
		
		Set<ZedEntity> unscanned = getUnscannedEntities(context);
		do {
			unscanned.stream().forEach( z -> {
				analyze( context, z);
			});
		} while (!(unscanned = getUnscannedEntities( context)).isEmpty());			
	}
	
	


	/**
	 * @param context - {@link ZedAnalyzerContext} to use
	 * @param z - an un-scanned {@link ZedEntity} to qualify (aka scan)
	 */
	private void analyze(ZedAnalyzerContext context, ZedEntity z) {
		List<Artifact> as = z.getArtifacts();
		Artifact ta = context.terminalArtifact();
		if (ArtifactMatcher.matchArtifactTo(ta, as)) {	
			log.debug( "qualifying " + z.getName() + " - " + ta.getGroupId() + ":" + ta.getArtifactId() + "#" + ta.getVersion());
			context.resolver().qualify(context, z);
		}
		else {
			z.setScannedFlag(true);			
		}
		
	}
	
	private void qualify(ZedAnalyzerContext context, ZedEntity z) {
		System.out.println( "forced qualifying " + z.getName());
		context.resolver().qualify(context, z);
		
	}
	
		
	/**
	 * @param z - the {@link InterfaceEntity} to test
	 * @return - true if a {@link GenericEntity}, false otherwise
	 */
	private boolean isGenericEntity( InterfaceEntity z, ZedAnalyzerContext context) {
		// cannot be in the runtime artifact
		if (z.getArtifacts().contains( context.artifacts().runtimeArtifact(context)))  {
			z.setGenericNature( false);
			return false;
		}
		
		if (z.getGenericNature() != null) {
			return z.getGenericNature();
		}
		// a) could be the generic entity thing itself
		if (z.getName().equalsIgnoreCase( GENERIC_ENTITY)) {
			z.setGenericNature( true);
			return true;
		}
		if (!z.getQualifiedFlag()) {
			qualify(context, z);
		}
		// b) could derive from one
		for (TypeReferenceEntity tr : z.getSuperInterfaces()) {
			InterfaceEntity referencedType = (InterfaceEntity) tr.getReferencedType();
			if (!referencedType.getQualifiedFlag()) {
				qualify(context, referencedType);
			}
			if (isGenericEntity( referencedType, context)) {
				z.setGenericNature( true);
				return true; 
			}
		}
		return false;		
	}

	/**
	 * evaluate all {@link ZedEntity} to see whether they are 'generic entities'
	 * @param context - {@link ZedAnalyzerContext} to use
	 */
	private void analyzeGenericity(ZedAnalyzerContext context) {
		for (ZedEntity zedEntity : context.registry().population()) {
			if ( zedEntity instanceof InterfaceEntity) {
				InterfaceEntity interfaceEntity = (InterfaceEntity) zedEntity;
				if (interfaceEntity.getGenericNature() == null) {
					interfaceEntity.setGenericNature( isGenericEntity( interfaceEntity, context));
				}
			}
		}
	}
	
	/**
	 * wire the type hierarchy, ie. add the 'back-links' to the {@link ZedEntity}
	 * @param context - the {@link ZedAnalyzerContext} to use
	 */
	private void analyzeTypeHierarchy(ZedAnalyzerContext context) {		
		
		for (ZedEntity zedEntity : context.registry().population()) {
			// class entities
			if (zedEntity instanceof ClassEntity) {
				ClassEntity classEntity = (ClassEntity) zedEntity;
				// only one super type 
				
				TypeReferenceEntity superTypeReference = classEntity.getSuperType();
				if (superTypeReference != null) {
					ClassEntity superType = (ClassEntity) superTypeReference.getReferencedType();
					Set<ClassEntity> subTypes = superType.getSubTypes();					
					log.debug( "adding [" + classEntity.getName() + "] as sub type to [" + superType.getName() + "]");
					subTypes.add( classEntity);
				}
				// implementing interfaces 
				Set<TypeReferenceEntity> implementedInterfaces = classEntity.getImplementedInterfaces();
				if (implementedInterfaces != null && implementedInterfaces.size() > 0) {
					for (TypeReferenceEntity implementedInterface : implementedInterfaces) {
						InterfaceEntity interfaceEntity = (InterfaceEntity) implementedInterface.getReferencedType();
						Set<ClassEntity> implementingClasses = interfaceEntity.getImplementingClasses();
						log.debug( "adding [" + classEntity.getName() + "] as implementing to [" + interfaceEntity.getName() + "]");
						implementingClasses.add( classEntity);
					}
				}		
				continue;
			}
			// interface 
			if (zedEntity instanceof InterfaceEntity) {
				InterfaceEntity interfaceEntity = (InterfaceEntity) zedEntity;
				Set<TypeReferenceEntity> superInterfaces = interfaceEntity.getSuperInterfaces();
				if (superInterfaces != null && superInterfaces.size() > 0) {
					for (TypeReferenceEntity superInterfaceRef : superInterfaces) {						
						InterfaceEntity superInterface = (InterfaceEntity) superInterfaceRef.getReferencedType();
						Set<InterfaceEntity> subInterfaces = superInterface.getSubInterfaces();						
						log.debug( "adding [" + interfaceEntity.getName() + "] as sub interface to [" + superInterface.getName() + "]");
						
						subInterfaces.add( interfaceEntity);
					}
				}
				continue;
			}
		}
	}
		
	/**
	 * use the classpath to resolve the direct dependencies of the terminal
	 * following assumptions: 
	 * 	all combinations of groupId and artifactId (ArtifactIdentification) are unique
	 *  all dependencies are reflected in the classpath   
	 * @param classpathSolutions - the solutions that make up the classpath
	 * @param declaredDependencies - the dependencies as declared in the pom
	 * @return - a {@link List} of {@link Artifact} that represent the resolved dependencies
	 */
	private List<Artifact> collectDeclaredDependencies(ZedAnalyzerContext context) {
	
		Collection<AnalysisArtifact> classpathSolutions = context.classpath();
		List<AnalysisDependency> declaredTerminalDependencies = context.declaredTerminalDependencies();
		List<AnalysisArtifact> additionsToClasspath = context.additionsToClasspath();
	
		Map<ArtifactIdentification, Artifact> map = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		
		classpathSolutions.stream().forEach( s -> {
			map.put( s, ConversionHelper.toArtifact(context, s));
		});
		if (additionsToClasspath != null) {
			additionsToClasspath.stream().forEach( s -> {
				map.put( s, ConversionHelper.toArtifact(context, s));
			});
		}
		
		
		return declaredTerminalDependencies.stream().map( d -> {
			return map.get(d);		
		}).filter( d -> d != null).collect(Collectors.toList());		
	}
	
	
}
