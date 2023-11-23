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
package com.braintribe.devrock.zed.analyze.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.zed.api.forensics.DependencyForensics;
import com.braintribe.devrock.zed.commons.ZedTokens;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.ClassOrInterfaceEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ParameterEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasTemplateParameters;

/**
 * java level dependency analyzer : runs through all entries of a terminal and extracts all references to other artifacts. 
 * Doesn't care for BT style ramifications (such as {@link ForwardDeclaration}, see {@link DependencyForensics} for that
 * @author pit
 *
 */
public class DependencyExtracter implements ZedTokens{
	
	private Set<ZedEntity> processedEntities;
	
	/**
	 * collect actual dependencies from the artifact by traversing the artifact's entries
	 * @param artifact - the {@link Artifact} to retrieve it from 
	 * @return - a {@link List} of {@link Artifact} (that are not the main one)
	 */
	public List<Artifact> collectDependencies(Artifact artifact) {
		processedEntities = new HashSet<>();
		
		Predicate<ZedEntity> entityFilter = new StandardFilter(artifact);
		Predicate<Artifact> artifactFilter = a -> !ArtifactMatcher.matchArtifactTo(artifact, a);
		
		Set<Artifact> referencedArtifacts = new HashSet<>();
		artifact.getEntries().stream().forEach( e -> {
			if (!processedEntities.contains( e)) {
				if (entityFilter.test(e)) {							
					processedEntities.add(e);
					
					List<Artifact> dependencies = getAllDependencies( e);
					Set<Artifact> filteredDependencies = dependencies.stream().filter( artifactFilter).collect(Collectors.toSet());					
					referencedArtifacts.addAll( filteredDependencies);
				}				
			}			
		});
		 		
		return new ArrayList<Artifact>( referencedArtifacts);
	}
	
	
	/**
	 * return all {@link Artifact} of the {@link ZedEntity}
	 * @param e - the {@link ZedEntity}
	 * @return - a {@link List} of found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( ZedEntity e) {		
		List<Artifact> result = new ArrayList<>();
		//System.out.println("collecting references of " + e.getName());
		// 
		if (e instanceof InterfaceEntity) {
			result.addAll( getAllDependencies((ClassOrInterfaceEntity) e));			 		
			result.addAll( getAllDependencies( (InterfaceEntity) e));			
		}
		else if (e instanceof ClassEntity) {
			result.addAll( getAllDependencies((ClassOrInterfaceEntity) e));
			result.addAll( getAllDependencies( (ClassEntity) e));
		}
		else if (e instanceof ClassOrInterfaceEntity) {
			result.addAll( getAllDependencies( (ClassOrInterfaceEntity) e));
		}
		else if (e instanceof AnnotationEntity) {
			AnnotationEntity ae = (AnnotationEntity) e;			
			result.addAll( getAllDependencies(ae));			
		}
		if (e instanceof HasTemplateParameters) {
			result.addAll( getAllDependencies( (HasTemplateParameters) e));
		}
		
		
		result.addAll( e.getArtifacts());
	
		return result;
	}
	
	private void trace( String tag, List<Artifact> artifacts) {
		if (artifacts.size() == 0)
			return;
		String v = artifacts.stream().map( a -> a.toVersionedStringRepresentation()).collect( Collectors.joining(","));
		//System.out.println("collected via " + tag + " " + v);
	}
	
	/**
	 * extract all dependencies from a thing with template parameters 
	 * @param e - the {@link HasTemplateParameters}
	 * @return - a {@link Collection} of {@link Artifact}
	 */
	private Collection<? extends Artifact> getAllDependencies(HasTemplateParameters e) {
		List<Artifact> result = new ArrayList<>();
		for (TypeReferenceEntity ref : e.getTemplateParameters().values()) {
			result.addAll( getAllDependencies(ref));
		}
		return result;
	}

	/**
	 * extracts the artifact from the super type and implemented interfaces,
	 * and fields
	 * @param e - the {@link ClassEntity}
	 * @return - a {@link List} of found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( ClassEntity e) {
		List<Artifact> result = new ArrayList<>();
		// super type
		TypeReferenceEntity superTypeRef = e.getSuperType();
		if (superTypeRef != null) {
			result.addAll( getAllDependencies(superTypeRef));
		}
		// interfaces 
		e.getImplementedInterfaces().stream().forEach( i -> {
			result.addAll( getAllDependencies( i));
		});
		// fields
		e.getFields().stream().forEach( f -> {
			result.addAll( getAllDependencies( f.getType()));				
		});
		return result;
	}
	
	/**
	 * extracts the artifact from all super interfaces 
	 * @param e
	 * @return - a {@link List} of found {@link Artifact}	 
	 */
	private List<Artifact> getAllDependencies( InterfaceEntity e) {
		List<Artifact> result = new ArrayList<>();
		e.getSuperInterfaces().stream().forEach( i -> {			
			result.addAll( getAllDependencies(i));
		});
		return result;
	}
	
	
	/**
	 * extract the {@link Artifact} from interface and class,
	 * i.e. from methods and annotations 
	 * @param e - the {@link ZedEntity}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( ClassOrInterfaceEntity e) {
		List<Artifact> result = new ArrayList<>();
		e.getAnnotations().stream().forEach( a -> {
			result.addAll( getAllDependencies( a));
		});
	
		e.getMethods().stream().forEach( m -> {
			result.addAll( getAllDependencies( m));
		});	
		trace("class/interface", result);
		return result;
	}
	

	/**
	 * extract the {@link Artifact} information from an {@link AnnotationEntity}
	 * @param e - the {@link AnnotationEntity}
 	 * @return - a {@link List} of all found {@link Artifact}
	 */

	private List<Artifact> getAllDependencies( AnnotationEntity e) {
		List<Artifact> result = new ArrayList<>();
		result.addAll( e.getArtifacts());
		result.addAll( getAllDependencies(e.getDeclaringInterface()));
		for (Entry<String, AnnotationValueContainer> entry : e.getMembers().entrySet()) {
				result.addAll( getAllDependencies( entry.getValue()));
		}
		trace("annotation", result);
		return result;
	}

	
	/**
	 * extract the {@link Artifact} information from an {@link AnnotationValueContainer}
	 * @param c - the {@link AnnotationValueContainer}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( AnnotationValueContainer c) {
		List<Artifact> result = new ArrayList<>();
		switch (c.getContainerType()) {
			case annotation:
				result.addAll( c.getOwner().getArtifacts()); //TODO? annotation within a container only within container's artifact? 
				break;
			case collection:
				c.getChildren().stream().forEach( g -> result.addAll( getAllDependencies( g)));
				break;
			default:
				break;
		}
		trace("annotation value container", result);
		return result;
	}
	
	/**
	 * extract the {@link Artifact} from a {@link MethodEntity}, ie.e arguments, return type and exceptions
	 * @param m - the {@link MethodEntity}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( MethodEntity m) {
		List<Artifact> result = new ArrayList<>();
		// arguments
		m.getArgumentTypes().stream().forEach( t -> {
			if (t != null)
				result.addAll( getAllDependencies(t));
		});
		// return
		result.addAll( getAllDependencies( m.getReturnType()));					
		// exceptions
		m.getExceptions().stream().forEach( x -> result.addAll(x.getArtifacts()));
		// annotations
		m.getAnnotations().stream().forEach( t -> result.addAll( getAllDependencies(t)));
		// type references inside method body
		m.getBodyTypes().stream().forEach( t -> {
			if (t != null) {
				result.addAll( getAllDependencies(t));
			}
		});
		trace("method", result);
		return result;
	}

	private List<Artifact> getAllDependencies( TypeReferenceEntity ref) {
		List<Artifact> result = new ArrayList<>();
		ZedEntity referenced = ref.getReferencedType();
		
		//System.out.println("getting deps of [" + referenced.getName() + "]");
		
		if (referenced instanceof ParameterEntity || !processedEntities.contains( referenced)) {
			//processedEntities.add( referenced);
		
			result.addAll( referenced.getArtifacts());
			for (TypeReferenceEntity pRef : ref.getParameterization()) {				
				if (pRef == ref) // seems to repeat itself if it's a parameter
					continue;
				result.addAll( getAllDependencies(pRef));
			}
		}
		trace("typereference", result);
		return result;
	}
	
	
}
