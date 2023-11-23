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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.analyze.dependency.ArtifactMatcher;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.BasicZedForensics;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAnnotationsNature;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasMethodsNature;
import com.braintribe.zarathud.model.data.natures.HasTemplateParameters;

public class ZedForensicsCommons implements BasicZedForensics {
	
	protected ZedForensicsContext context;
	protected Artifact terminal;

	static final HashingComparator<Artifact> artifactComparator = EntityHashingComparator
			.build( Artifact.T)
			.addField( Artifact.groupId)
			.addField( Artifact.artifactId)			
			.done();

	public ZedForensicsCommons(ZedForensicsContext context) {
		this.context = context;
		this.terminal = context.terminalArtifact();		
	} 
	
	
	/**
	 * extracts all {@link ZedEntity} of a specific {@link Artifact} from the terminal's population list
	 * @param artifact - the {@link Artifact} to find its {@link ZedEntity}
	 * @return - a {@link List} of {@link ZedEntity} from this artifact 
	 */
	protected List<ZedEntity> getEntitiesOfArtifact( Artifact artifact) {
		List<ZedEntity> entitiesOfArtifact = terminal.getEntries().stream().filter( e -> {
			return ArtifactMatcher.matchArtifactTo(artifact, e.getArtifacts());
		}).collect(Collectors.toList());
		return entitiesOfArtifact;	
	}
	
	/**
	 * collects all {@link TypeReferenceEntity} *directly* used by the {@link ZedEntity} passed
	 * @param e - the {@link ZedEntity}
	 * @return - a {@link List} of {@link TypeReferenceEntity}
	 */
	protected List<TypeReferenceEntity> getTypeReferencesOfEntity( ZedEntity e) {
		List<TypeReferenceEntity> zeds = new ArrayList<>();
		if (e instanceof HasFieldsNature) {
			zeds.addAll( getReferencedEntities( (HasFieldsNature) e));
		}
		if (e instanceof HasMethodsNature) {
			zeds.addAll( getReferencedEntities( (HasMethodsNature) e));
		}
		if (e instanceof HasAnnotationsNature) {
			zeds.addAll( getReferencedEntities( (HasAnnotationsNature) e));
		}
		if (e instanceof HasTemplateParameters) {
			zeds.addAll( getReferencedEntities( (HasTemplateParameters) e));
		}
		if (e instanceof InterfaceEntity) {
			zeds.addAll( getReferencedEntities( (InterfaceEntity) e));
		}
		if (e instanceof ClassEntity) {
			zeds.addAll( getReferencedEntities( (ClassEntity) e));
		}		
		return zeds;
	}
	
	
	/**
	 * @param e - an {@link InterfaceEntity}
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within this {@link InterfaceEntity}
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(InterfaceEntity e) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		refs.addAll( e.getSuperInterfaces());				
		return refs;
	}
	/**
	 * @param e - a {@link ClassEntity}
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within this {@link ClassEntity}
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(ClassEntity e) {
		TypeReferenceEntity superType = e.getSuperType();
		if (superType != null) {
			return Collections.singleton(superType);
		}
		else {
			return Collections.emptyList();
		}
	}

	
	/**
	 * @param e - a {@link HasTemplateParameters}
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within this {@link HasTemplateParameters} 
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(HasTemplateParameters e) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		for (TypeReferenceEntity ref : e.getTemplateParameters().values()) {
			refs.add(ref);
		}
		return refs;
	}

	/**
	 * @param e
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(HasAnnotationsNature e) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		for (TypeReferenceEntity tf : e.getAnnotations()) {
			AnnotationEntity ae = (AnnotationEntity) tf.getReferencedType();
			refs.addAll( getReferencedEntities(ae));
		}
		return refs;
	}
	
	/**
	 * @param ae
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(AnnotationEntity ae) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		refs.add( ae.getDeclaringInterface());
		// container.. 
		Map<String, AnnotationValueContainer> members = ae.getMembers();
		for (AnnotationValueContainer avc : members.values()) {
			refs.addAll( getReferencedEntities(avc));
		}
		return refs;
	}
	
	/**
	 * @param avc
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(AnnotationValueContainer avc) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		List<AnnotationValueContainer> children = avc.getChildren();
		if (children != null && children.size()>0) {			
			children.stream().forEach( c -> refs.addAll( getReferencedEntities(c)));
		}
		return refs;
	}
	

	/**
	 * extract type references from method
	 * @param e
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(HasMethodsNature e) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		for (MethodEntity method : e.getMethods()) {
			refs.add( method.getReturnType());
			refs.addAll( method.getArgumentTypes());			
			refs.addAll( method.getBodyTypes());
			refs.addAll( getReferencedEntities( (HasAnnotationsNature) method));
		}		
		return refs;
	}

	/**
	 * extract type references from fields
	 * @param e
	 * @return - a {@link Collection} with the {@link TypeReferenceEntity} within
	 */
	protected Collection<TypeReferenceEntity> getReferencedEntities(HasFieldsNature e) {
		List<TypeReferenceEntity> refs = new ArrayList<>();
		for (FieldEntity field : e.getFields()) {
			refs.add( field.getType());
		}
		return refs;
	}
	
	/**
	 * @param ref
	 * @param e
	 * @return
	 */
	protected boolean referencesType( TypeReferenceEntity ref, ZedEntity e) {
		if (ref.getReferencedType() == e) {
			return true;
		}
		List<TypeReferenceEntity> parameterization = ref.getParameterization();
		for (TypeReferenceEntity pref : parameterization) {
			if (pref.getReferencedType() == e)
				return true;
		}
		return false;
	}
	
	/**
	 * @param ref
	 * @param es
	 * @return
	 */
	protected List<Pair<TypeReferenceEntity, ZedEntity>> referencesArtifact( TypeReferenceEntity ref, List<ZedEntity> es) {
		List<Pair<TypeReferenceEntity, ZedEntity>> result = new ArrayList<>();
		for (ZedEntity e : es) {
			if (referencesType(ref, e)) {
				result.add( Pair.of( ref, e));
			}
		}
		return result;
	}
	
	/**
	 * filter out all {@link ZedEntity} that are declared in the runtime artifact 
	 * @param runtime
	 * @param entities
	 * @return
	 */
	protected List<ZedEntity> filterRuntimeEntities(Artifact runtime, List<ZedEntity> entities) {
		return entities.stream().filter( e -> {
			return !ArtifactMatcher.matchArtifactTo( runtime, e.getArtifacts());
		}).collect(Collectors.toList());
	}

	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.forensics.BasicZedForensics#getTerminalReferencesToArtifact(com.braintribe.model.zarathud.data.Artifact, com.braintribe.model.zarathud.data.Artifact)
	 */
	@Override
	public Map<ZedEntity, List<ZedEntity>> getTerminalReferencesToArtifact( Artifact runtime, Artifact artifact) {				
		
		List<ZedEntity> artifactEntities = getEntitiesOfArtifact( artifact);
		if (artifactEntities == null || artifactEntities.size() == 0)
			return java.util.Collections.emptyMap();
			
		artifactEntities = filterRuntimeEntities( runtime, artifactEntities);
		
		Map<ZedEntity, List<ZedEntity>> result = new HashMap<>();
		List<ZedEntity> terminalEntities = getEntitiesOfArtifact(terminal);
		for (ZedEntity terminalEntity : terminalEntities) {
			List<TypeReferenceEntity> typeReferencesOfEntity = getTypeReferencesOfEntity(terminalEntity);
			for (TypeReferenceEntity ref : typeReferencesOfEntity) {
				List<Pair<TypeReferenceEntity, ZedEntity>> pairs = referencesArtifact(ref, artifactEntities);
				for (Pair<TypeReferenceEntity, ZedEntity> pair : pairs) {
					result.computeIfAbsent(terminalEntity, s -> new ArrayList<ZedEntity>()).add( pair.getSecond());				
				}
			}
		}		
		return result;
	}
	

	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.forensics.BasicZedForensics#extractUndeclaredDependencies(java.util.Collection, java.util.Collection)
	 */
	@Override
	public Set<Artifact> extractUndeclaredDependencies( Collection<Artifact> declared, Collection<Artifact> found) {
		Set<Artifact> foundSet = artifactComparator.newHashSet();
		foundSet.addAll(found);
		List<Artifact> referenced = new ArrayList<>();
		List<Artifact> notReferenced = new ArrayList<>();
		for (Artifact artifact : declared) {
			if (foundSet.contains( artifact)) {
				referenced.add( artifact);
			}
			else {
				notReferenced.add(artifact);
			}
		}
		Set<Artifact> undeclared = artifactComparator.newHashSet();
		undeclared.addAll( foundSet);
		undeclared.removeAll(referenced);
		
		return undeclared;		
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.forensics.BasicZedForensics#getEntitiesWithMultipleSources(java.util.List)
	 */
	@Override
	public List<ZedEntity> getEntitiesWithMultipleSources(List<ZedEntity> population) {
		return population.stream().filter( e -> e.getArtifacts().size() == 0).collect(Collectors.toList());
	}
	
	private String createStableKey( List<Artifact> artifacts) {
		List<String> strings = artifacts.stream().map( a -> {
			return a.toString();
		}).sorted().collect( Collectors.toList());
		
		String key = strings.stream().collect( Collectors.joining( ","));
		return key;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zed.forensics.BasicZedForensics#collectEntitiesWithSameMultipleSources(java.util.List)
	 */
	@Override
	public Map<String, List<ZedEntity>> collectEntitiesWithSameMultipleSources( List<ZedEntity> multiSourcePopulation) {
		Map<String, List<ZedEntity>> result = new HashMap<>();
		for (ZedEntity e : multiSourcePopulation) {
			List<Artifact> sourceArtifacts = e.getArtifacts();		
			String key = createStableKey(sourceArtifacts);
			List<ZedEntity> storedEntitiesForCombination = result.computeIfAbsent( key, k -> new ArrayList<ZedEntity>());
			storedEntitiesForCombination.add(e);			
		}
		return result;
	}
	
	
	
	/**
	 * @param z
	 * @param signature
	 * @return - true if the {@link ZedEntity} has a matching name aka signature
	 */
	protected boolean matches( ZedEntity z, String signature) {
		return z.getName().equalsIgnoreCase(signature);
	}
	
	/**
	 * @param m
	 * @param rt
	 * @param at
	 * @return
	 */
	protected boolean matchesSignature( MethodEntity m, ZedEntity rt, ZedEntity at) {
		if (rt != null) {
			if (Comparators.entity().compare(m.getReturnType().getReferencedType(), rt) != 0)
				return false;
		}
		if (at != null) {
			if (m.getArgumentTypes().size() != 1)
				return false;
			if (Comparators.entity().compare(m.getArgumentTypes().get(0).getReferencedType(), rt) != 0)
				return false;
		}
		return true;
	}
	
	/**
	 * @param from
	 * @return
	 */
	protected Artifact shallowArtifactCopy( Artifact from) { 
		Artifact shallow = Artifact.T.create();
		shallow.setGroupId( from.getGroupId());
		shallow.setArtifactId( from.getArtifactId());
		shallow.setVersion( from.getVersion());
		return shallow;	
	}
	

}
 