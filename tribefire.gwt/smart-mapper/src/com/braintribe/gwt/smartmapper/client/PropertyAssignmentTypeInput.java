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
package com.braintribe.gwt.smartmapper.client;

import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.google.gwt.dom.client.Style.TextAlign;

public class PropertyAssignmentTypeInput extends PropertyAssignmentInput{
	
	protected AbstractMappingElementsProvider typesProvider;
	
	public PropertyAssignmentTypeInput() {
		super();
		getElement().setAttribute("placeholder", "EntityTypeName");
		getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		internalPropertyName = "entityType";
	}
	
	public void setTypesProvider(AbstractMappingElementsProvider typesProvider) {
		this.typesProvider = typesProvider;
	}
	
	@Override
	public void render(){
		if(pac.parentEntity != null){
			Property property = pac.parentEntity.entityType().getProperty(propertyNameOfAssignment);
			Object currentValue = property.get(pac.parentEntity);
			if(currentValue != null){
				if(currentValue instanceof GmEntityType){
					setText(getTypeName(((GmEntityType)currentValue).getTypeSignature()));
				}else if(currentValue instanceof QualifiedProperty){
					QualifiedProperty qpa = (QualifiedProperty) currentValue;
					if(qpa.getEntityType() != null){
						setText(getTypeName(qpa.getEntityType().getTypeSignature()));	
					}else{
						setText("");
					}				
				}
			}else{
				if(pac.parentEntity.entityType().isAssignableFrom(QualifiedPropertyAssignment.T)){
					QualifiedProperty qp = (QualifiedProperty)pac.parentEntity;
					if(qp.getEntityType() != null)
						setText(getTypeName(qp.getEntityType().getTypeSignature()));
					else if(qp.getProperty() != null && qp.getProperty().getDeclaringType() != null)
						setText(getTypeName(qp.getProperty().getDeclaringType().getTypeSignature()));
					else
						setText("");
				}else
					setText("");
			}
		}
		else
			setText("");
					
	}
	
	private String getTypeName(String typeSignature){
		return typeSignature.substring(typeSignature.lastIndexOf(".")+1, typeSignature.length());
	}
	
	@Override
	public EntityType<? extends GenericEntity> getType() {
		return gmEntityType;
	}
	
	@Override
	public boolean loadExisitingValues() {
		return false;
	}
	
	@Override
	public boolean loadTypes() {
		return false;
	}

	@Override
	public Function<ParserArgument, List<ParserResult>> simpleTypesValuesProvider() {
		return null;
	}

	@Override
	public Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider() {
		return typesProvider;
	}

}
