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
package com.braintribe.model.access.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.ldap.LdapAttribute;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

public class LdapQueryContext {
	
	protected static Logger logger = Logger.getLogger(LdapQueryContext.class);
	
	protected String entityTypeSignature = null;
	protected EntityType<GenericEntity> entityType = null;
	
	protected ModelMdResolver metaDataBuilder = null; 
	protected EntityMdResolver entityMetaDataBuilder = null;
	
	protected Set<String> ldapObjectClasses = null;
	protected Map<String,String> propertyToAttributeMap = new HashMap<String,String>();
	protected Map<String,Property> attributeToPropertyMap = new HashMap<String,Property>();
	protected Property idProperty = null;
	protected List<Property> propertyList = null;

	private BasicModelAccessory modelAccessory;

	
	public LdapQueryContext(ModelMdResolver metaData, BasicModelAccessory modelAccessory) {
		this.metaDataBuilder = metaData;
		this.modelAccessory = modelAccessory;
	}

	public void setEntityType(EntityType<GenericEntity> entityType) throws Exception {
		this.entityType = entityType;
		this.propertyList = this.entityType.getProperties();
		this.entityMetaDataBuilder = this.metaDataBuilder.entityType(this.entityType);

		for (Property property : this.propertyList) {
			LdapAttribute ldapAttribute = this.entityMetaDataBuilder.property(property).meta(LdapAttribute.T).exclusive();
			if (ldapAttribute != null) {
				String propertyName = property.getName();
				String attributeName = ldapAttribute.getAttributeName();
				this.propertyToAttributeMap.put(propertyName, attributeName);
				this.attributeToPropertyMap.put(attributeName.toLowerCase(), property);
			}
			
		}
		GenericModelType idType = modelAccessory.getIdType(entityType.getTypeSignature());
		this.idProperty = entityType.getIdProperty();
		
		if (idType.getTypeCode() != TypeCode.stringType) {
			logger.debug(() -> "Id property "+idProperty.getName()+" of type "+this.entityType+" is not of type String.");
			throw new Exception("The entity type "+this.entityType+" has no ID property of type String.");
		}
	}
}
