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
package com.braintribe.model.processing.query.selection.experts;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.Source;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Based on the {@link SimpleAliasResolver}, but uses the {@link Name} metadata, if present.
 * @author michel.docouto
 *
 */
public class NameAliasResolver extends SimpleAliasResolver {
	
	private PersistenceGmSession gmSession;
	
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public String getAliasForSource(Source source) {
		EntityType<GenericEntity> entityType = getEntityType(source);
		if (entityType != null) {
			if (gmSession != null) {
				Name name = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType).meta(Name.T).exclusive();
				if (name != null && name.getName() != null)
					return I18nTools.getLocalized(name.getName());
			}
			
			return resolveTypeSignature(entityType.getTypeSignature());
		}
		
		return null;
	}
	
	@Override
	public String getPropertyNameForSource(Source source, String propertyName) {
		if (gmSession == null)
			return propertyName;
		
		EntityType<GenericEntity> entityType = getEntityType(source);
		if (entityType == null)
			return propertyName;
		
		Name name = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType).property(propertyName).meta(Name.T).exclusive();
		if (name == null || name.getName() == null)
			return propertyName;
		
		return I18nTools.getLocalized(name.getName());
	}

}
