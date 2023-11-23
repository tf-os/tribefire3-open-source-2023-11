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

import com.braintribe.model.generic.reflection.Property;

/**
 * The Class PropertyWrapper.
 *
 */
public class PropertyWrapper {

	private Property property;
	private String alias;
	private Object fixedValue;
	private String entityType;

	/**
	 * Instantiates a new property wrapper.
	 *
	 * @param p
	 *            the p
	 * @param alias
	 *            the alias
	 */
	public PropertyWrapper(Property p, String alias) {
		this(p, alias, null);
	}

	/**
	 * Instantiates a new property wrapper.
	 *
	 * @param alias
	 *            the alias
	 * @param fixedValue
	 *            the fixed value
	 */
	public PropertyWrapper(String alias, Object fixedValue) {
		this.alias = (alias == null ? fixedValue.toString() : alias);
		this.fixedValue = fixedValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PropertyWrapper [property=" + property + ", alias=" + alias + ", fixedValue=" + fixedValue
				+ ", entityType=" + entityType + "]";
	}

	/**
	 * Instantiates a new property wrapper.
	 *
	 * @param p
	 *            the p
	 * @param alias
	 *            the alias
	 * @param entityType
	 *            the entity type
	 */
	public PropertyWrapper(Property p, String alias, String entityType) {
		this.property = p;
		this.alias = alias != null ? alias : p.getName();
		this.entityType = entityType;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Object getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(Object fixedValue) {
		this.fixedValue = fixedValue;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

}
