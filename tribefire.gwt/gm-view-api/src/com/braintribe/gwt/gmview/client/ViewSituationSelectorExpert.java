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
package com.braintribe.gwt.gmview.client;

import java.util.Set;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.ModelPathElementInstanceKind;
import com.braintribe.model.generic.path.ModelPathElementType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ViewSituationSelectorExpert extends AbstractViewSelectorExpert<ViewSituationSelector> {
	
	private PersistenceGmSession gmSession;
	
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public boolean doesSelectorApply(ViewSituationSelector viewSituationSelector, ModelPathElement modelPathElement) {
		String metadataTypeSignature = viewSituationSelector.getMetadataTypeSignature();
		if (metadataTypeSignature != null) {
			GenericModelType type = modelPathElement.getType();
			if (!type.isEntity())
				return false;
			
			return getEntityTypeMetadata((GenericEntity) modelPathElement.getValue(), (EntityType<?>) type, metadataTypeSignature,
					viewSituationSelector.getUseCases()) != null;
		}
		
		boolean typeConditionMatched = viewSituationSelector.getValueType().matches(modelPathElement.<GenericModelType>getType());
		ModelPathElementType selectorPathElementType = viewSituationSelector.getPathElementType();
		if (!typeConditionMatched || (selectorPathElementType != null && !selectorPathElementType.equals(modelPathElement.getPathElementType())))
			return false;
		
		Object value = modelPathElement.getValue();
		if (!(value instanceof GenericEntity) || viewSituationSelector.getElementInstanceKind() == null)
			return true;
		
		GenericEntity entity = (GenericEntity) value;
		EntityReference ref = entity.reference();
		ModelPathElementInstanceKind ik = viewSituationSelector.getElementInstanceKind();
		if (ModelPathElementInstanceKind.any.equals(ik))
			return true;
		
		if (ModelPathElementInstanceKind.preliminary.equals(ik) && ref instanceof PreliminaryEntityReference)
			return true;
		
		if (ModelPathElementInstanceKind.persistent.equals(ik) && ref instanceof PersistentEntityReference)
			return true;
		
		return false;
	}

	private EntityTypeMetaData getEntityTypeMetadata(GenericEntity entity, EntityType<?> entityType, String metadataTypeSignature,
			Set<String> useCases) {
		EntityType<? extends EntityTypeMetaData> metadataEntityType = GMF.getTypeReflection().getEntityType(metadataTypeSignature);
		EntityMdResolver entityMdResolver;
		if (entity != null)
			entityMdResolver = GmSessions.getMetaData(entity).lenient(true).entity(entity);
		else
			entityMdResolver = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType);
		
		if (useCases != null && !useCases.isEmpty())
			entityMdResolver = entityMdResolver.useCases(useCases);
		
		return entityMdResolver.meta(metadataEntityType).exclusive();
	}

}
