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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.core.commons.EntityReferenceWrapperCodec;

public class EntityReferenceResolver {
	
	private final Map<EntityReference, PersistentEntityReference> referenceTranslations;
	
	private static final Logger log = Logger.getLogger(EntityReferenceResolver.class);
	
	public EntityReferenceResolver() {
		this.referenceTranslations = CodingMap.createHashMapBased(new EntityReferenceWrapperCodec());
	}
	
	public PersistentEntityReference resolvePreliminaryEntityReference(PreliminaryEntityReference preliminaryEntityReference) {
		return referenceTranslations.get(preliminaryEntityReference);
	}
	
	/**
	 * Maps an EntityReference used in Manipulations to a persistentEntityReference constructed based on a id value change manipulations.
	 * <p>
	 * This enables the localization of EntityReferences (retrieval of the correspondent GenericEntity from the persistent session) in cases where 
	 * the id property value in the EntityReference no longer matches current value of the id property in the persistent session.
	 */
	public void registerReferenceTranslation(EntityReference entityReference, PersistentEntityReference persistentEntityReference) {

		referenceTranslations.put(entityReference, persistentEntityReference);
		
		if (log.isTraceEnabled()) {
			log.trace("Registered translation from [ "+entityReferenceToString(entityReference)+" ] to [ "+entityReferenceToString(referenceTranslations.get(entityReference))+" ]");
		}
	}
	
    private static String entityReferenceToString(EntityReference entityReference) {
    	
    	if (entityReference == null)
    		return "null";
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(entityReference.getClass().getSimpleName()).append("[");
		sb.append("hashCodeReference=");
		sb.append(EntRefHashingComparator.INSTANCE.computeHash(entityReference));
		sb.append(",id=");
		if (entityReference.getRefId() == null) {
			sb.append("null");
		} else {
			sb.append(entityReference.getRefId());
		}
		sb.append(",typeSignature=");
		sb.append(entityReference.getTypeSignature());
    	sb.append("]");

    	return sb.toString();
    }
    
    public void registerReferenceTranslations(Manipulation... manipulations) {
    	CompoundManipulation compoundManipulation = compound(new ArrayList<>(Arrays.asList(manipulations)));
    	registerReferenceTranslations(compoundManipulation);
    }
    
    public void registerReferenceTranslations(Manipulation manipulation) {
    	
    	List<AtomicManipulation> manipulations = manipulation.inline();
    	registerReferenceTranslations(manipulations);
    }
    
    public void registerReferenceTranslations(List<AtomicManipulation> manipulations) {
    	for (AtomicManipulation atomicManipulation: manipulations) {
    		if (atomicManipulation instanceof PropertyManipulation) {
    			PropertyManipulation propertyManipulation = (PropertyManipulation)atomicManipulation;
    			registerReferenceTranslation(propertyManipulation);
    		}
    	}
    }
    
    public void registerReferenceTranslation(PropertyManipulation propertyManipulation) {

		if (propertyManipulation.manipulationType() != ManipulationType.CHANGE_VALUE || !(propertyManipulation.getOwner() instanceof EntityProperty))
			return;
		
		EntityProperty entityProperty = (EntityProperty)propertyManipulation.getOwner();
		
		if (entityProperty.getReference() == null)
			return;

		if (entityProperty.getPropertyName().equals(GenericEntity.id)) {
			
			PersistentEntityReference persistentEntityReference = PersistentEntityReference.T.create();
			persistentEntityReference.setTypeSignature(entityProperty.getReference().getTypeSignature());
			persistentEntityReference.setRefId(((ChangeValueManipulation)propertyManipulation).getNewValue());
			
			registerReferenceTranslation(entityProperty.getReference(), persistentEntityReference);
		}
	}

    
}
