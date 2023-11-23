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
package tribefire.extension.drools.integration.impl;

import org.mvel2.integration.PropertyHandler;
import org.mvel2.integration.PropertyHandlerFactory;
import org.mvel2.integration.VariableResolverFactory;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

public class GmMvel2PropertyHandler implements PropertyHandler {
	
	private static final GmMvel2PropertyHandler INSTANCE = new GmMvel2PropertyHandler();
	
	private Property property(GenericEntity entity, String name) {
		return entity.entityType().getProperty(name);
	}

	@Override
	public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory, Object value) {
		GenericEntity entity = (GenericEntity) contextObj;
		Property property = property(entity, name);
		Object retVal = property.get(entity);
		property.set(entity, value);
		return retVal;
	}

	@Override
	public Object getProperty(String name, Object contextObj, VariableResolverFactory variableFactory) {
		GenericEntity entity = (GenericEntity) contextObj;
		Property property = property(entity, name);
		return property.get(entity);
	}
	
	public static void install() {
		PropertyHandlerFactory.registerPropertyHandler(GenericEntity.class, INSTANCE);
	}
}
