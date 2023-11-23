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
package tribefire.extension.audit.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityRelatedMdResolver;
import com.braintribe.utils.lcd.LazyInitialized;

import tribefire.extension.audit.model.deployment.meta.Audited;
import tribefire.extension.audit.model.deployment.meta.AuditedPreserved;

public class TypeUsageInfo {
	private static final HashingComparator<PersistentEntityReference> referenceComparator = EntityHashingComparator // 
			.build(PersistentEntityReference.T) // 
			.addField(PersistentEntityReference.refId) // 
			.addField(PersistentEntityReference.refPartition) //
			.done();

	private String typeSignature;
	private Set<PersistentEntityReference> preserveReferences = null;
	private EntityMdResolver typeMdResolver;
	private LazyInitialized<TrackMode> lifeCycleTrackMode = new LazyInitialized<TrackMode>(() -> resolveTrackMode(typeMdResolver));
	private Map<String, PropertyInfo> propertyInfos = new HashMap<>();
	private CmdResolver cmdResolver;
	private List<String> preservedProperties = null;
	private List<EntityProperty> preservedCollectionProperties = null;

	private Map<PersistentEntityReference, GenericEntity> existingEntities = referenceComparator.newHashMap();
	private EntityType<?> entityType;
	
	public TypeUsageInfo(CmdResolver cmdResolver, String typeSignature) {
		super();
		this.cmdResolver = cmdResolver;
		this.typeSignature = typeSignature;
		this.entityType = EntityTypes.get(typeSignature);
		typeMdResolver = cmdResolver.getMetaData().entityTypeSignature(typeSignature);
	}
	
	public String getTypeSignature() {
		return typeSignature;
	}
	
	public EntityType<?> getEntityType() {
		return entityType;
	}
	
	public Set<PersistentEntityReference> getPreserveReferences() {
		return preserveReferences;
	}
	
	public List<EntityProperty> getPreservedCollectionProperties() {
		return preservedCollectionProperties;
	}
	
	public GenericEntity getExistingEntity(PersistentEntityReference reference) {
		return existingEntities.get(reference);
	}
	
	public GenericEntity acquireExistingEntity(Object id, String partition) {
		PersistentEntityReference ref = PersistentEntityReference.T.create();
		ref.setTypeSignature(entityType.getTypeSignature());
		ref.setRefId(id);
		ref.setRefPartition(partition);
		return acquireExistingEntity(ref);
	}
	public GenericEntity acquireExistingEntity(PersistentEntityReference reference) {

		return existingEntities.computeIfAbsent(reference, r -> {
			GenericEntity entity = entityType.createRaw();
			entity.setId(reference.getRefId());
			entity.setPartition(reference.getRefPartition());
			return entity;
		});
		
	}
	
	List<String> getAccumulatedPreservedNonCollectionProperties() {
		if (preservedProperties == null) {
			preservedProperties = propertyInfos.entrySet().stream() //
				.filter(e -> e.getValue().getTrackMode() == TrackMode.PRESERVE && //
						!e.getValue().getProperty().getType().isCollection()) //
				.map(Map.Entry::getKey) //
				.collect(Collectors.toList());
			
		}

		return preservedProperties;
	}
	
	private TrackMode resolveTrackMode(EntityRelatedMdResolver<?> resolver) {
		Audited audited = resolver.meta(Audited.T).exclusive();
		
		if (audited != null) {
			boolean preserve = audited instanceof AuditedPreserved;
			
			if (preserve)
				return TrackMode.PRESERVE;
			
			// if audited is an erasure (= Unaudited) #isTrue will be false
			return audited.isTrue()? TrackMode.BASIC: TrackMode.NONE;
		}
		else {
			return TrackMode.NONE;
		}
	}
	
	public void manageTrackedProperty(String property, EntityReference reference, boolean incrementalCollectionManipulation) {
		PropertyInfo propertyInfo = propertyInfos.computeIfAbsent(property, this::buildProperyInfo);
		
		if (incrementalCollectionManipulation) {
			propertyInfo.incrementalValueChangeOccured();
		} else {
			propertyInfo.changeValueOccured();
		}
		
		TrackMode trackMode = propertyInfo.getTrackMode();
		
		if (trackMode == TrackMode.PRESERVE && reference.referenceType() == EntityReferenceType.persistent) {
			
			if (propertyInfo.getProperty().getType().isCollection()) {
				if (preservedCollectionProperties == null) {
					// We use a list here as we expect the normalization to have removed duplicates
					preservedCollectionProperties = new ArrayList<>();
				}
				EntityProperty entityProperty = EntityProperty.T.create();
				entityProperty.setReference(reference);
				entityProperty.setPropertyName(property);
				preservedCollectionProperties.add(entityProperty);
				
			} else {
				
				if (preserveReferences == null) {
					preserveReferences = referenceComparator.newHashSet();
				}
				
				preserveReferences.add((PersistentEntityReference) reference);
				
			}

		}
	}
	
	private PropertyInfo buildProperyInfo(String propertyName) {
		Property property = entityType.getProperty(propertyName);
		TrackMode trackMode = resolveTrackMode(typeMdResolver.property(property));
		return new PropertyInfo(trackMode, property);
	}
	
	public void manageLifeCycleTracked(EntityReference reference) {
		lifeCycleTrackMode.get();	
	}
	
	public PropertyInfo getPropertyInfo(String property) {
		return propertyInfos.get(property);
	}
	
	public TrackMode getLifeCycleTrackMode() {
		return lifeCycleTrackMode.get();
	}
	
	@Override
	public String toString() {
		return "TypeUsageInfo["+typeSignature + ", propertyInfos=" +  propertyInfos + "]";
	}
}