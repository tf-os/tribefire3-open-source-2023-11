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
package com.braintribe.model.generic.proxy;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GmtsEnhancedEntityStub;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;

@GmSystemInterface
public class ProxyEnhancedEntity extends GmtsEnhancedEntityStub implements ProxyEntity {

	private final AbstractProxyEntityType type;
	private final Map<AbstractProxyProperty, Object> properties = new HashMap<>();
	private GenericEntity actualEntity;

	public ProxyEnhancedEntity(AbstractProxyEntityType type, PropertyAccessInterceptor pai) {
		super(false);

		this.type = type;
		this.pai = pai;
	}

	/**
	 * The values in the map are instances of the following types:
	 * <ul>
	 * <li>SimpleType</li>
	 * <li>EnumType</li>
	 * <li>EntityType</li>
	 * </ul>
	 * 
	 * If a map, set, list or Escape is to be hold it needs to be wrapped by com.braintribe.model.generic.value.Escape
	 */
	@Override
	public Map<AbstractProxyProperty, Object> properties() {
		return properties;
	}

	@Override
	public void linkActualEntity(GenericEntity actualEntity) {
		this.actualEntity = actualEntity;
	}

	@Override
	public GenericEntity actualValue() {
		return actualEntity;
	}

	@Override
	public GenericModelType type() {
		return type;
	}

	@Override
	public AbstractProxyEntityType entityType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString(this);
	}

	// ######################################################
	// ## . . . . . . GenericEntity properties . . . . . . ##
	// ######################################################

	@Override
	public <T> T getId() {
		return type.getProperty(id).get(this);
	}

	@Override
	public void setId(Object value) {
		type.getProperty(id).set(this, value);
	}

	@Override
	public String getPartition() {
		return type.getProperty(partition).get(this);
	}

	@Override
	public void setPartition(String value) {
		type.getProperty(partition).set(this, value);
	}

	@Override
	public String getGlobalId() {
		return type.getProperty(globalId).get(this);
	}

	@Override
	public void setGlobalId(String value) {
		type.getProperty(globalId).set(this, value);
	}

}
