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
package com.braintribe.tribefire.jdbc.statement;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

/**
 * The Class TargetExpression.
 *
 */
public class TargetExpression {

	private Type type;
	private String alias;
	private String name;
	private Object value;
	private Integer index;
	
	/**
	 * The Enum Type.
	 */
	public enum Type { value, property, namedparameter, indexedparameter }
	
	/**
	 * Instantiates a new target expression.
	 *
	 * @param type
	 *            the type
	 */
	public TargetExpression(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Property getProperty(EntityType<?> entityType) {		
		if (name.contains(".")) {
			// workaround for FK-ID properties
			return entityType.getProperty(name.substring(0, name.indexOf(".")));
		} else {
			return entityType.getProperty(name);
		}
	}
	
}
