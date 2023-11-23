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
package com.braintribe.gwt.smartmapper.client.experts;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class QualifiedPropertyElementsProvider extends AbstractMappingElementsProvider{

	public QualifiedPropertyElementsProvider(MappingElementKind mappingElementKind) {
		super(mappingElementKind);
	}

	@Override
	public Future<EntitiesProviderResult> apply(ParserArgument index) throws RuntimeException {
		PersistenceGmSession session  = propertyAssignmentContext.session;
		QualifiedPropertyAssignment qa = (QualifiedPropertyAssignment) propertyAssignmentContext.parentEntity;
		GmEntityType entityType = qa.getEntityType() != null ? qa.getEntityType() 
				: propertyAssignmentContext.mappedToEntityType;
		
		switch(mappingElementKind){
		case properties:
			return handleProperties(entityType, index);
		case types:
			return handleTypes(session, entityType, index);
		default:
			return new Future<EntitiesProviderResult>();
		}
	}
	
	@Override
	protected Future<EntitiesProviderResult> handleTypes(PersistenceGmSession session, GmEntityType entityType, ParserArgument index){
		Future<EntitiesProviderResult> future = new Future<>();
		String typeSignature = index.getValue();
		
		if(entityType != null){
			session.query().entities(EntityQueryBuilder.from(GmEntityType.class)
					.where().value(entityType.reference()).in().property("superTypes")
					.tc().negation().joker().done())
			.result(AbstractMappingElementsProvider.filterTypes(future, entityType, typeSignature));
			
			return future;
		}
		else return new Future<EntitiesProviderResult>();
	}

}
