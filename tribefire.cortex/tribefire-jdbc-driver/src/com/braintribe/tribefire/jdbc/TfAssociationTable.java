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
package com.braintribe.tribefire.jdbc;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import net.sf.jsqlparser.schema.Table;

/**
 * The Class TfAssociationTable.
 *
 */
public class TfAssociationTable {

	private String leftEntityName;
	private String rightEntityName;
	private String leftEntityType;
	private String rightEntityType;
	private String name;
	private String cardinality;
	private boolean primitive = false;
	private Property leftProperty;
	private String leftPropertyDisplayName;
	private Property rightProperty;
	private String rightPropertyDisplayName;
	private EntityType<?> leftEntity;
	private EntityType<?> rightEntity;
	private PersistenceGmSession sessionCortex;
	private TypeCode typeCode;
	private Set<Entry<String, String>> associationData = new HashSet<>();
	private Logger logger = Logger.getLogger(TfAssociationTable.class);

	
	
	/**
	 * Constructs a new virtual association table from an entity and its parameter.
	 *
	 * @param et
	 *            the entity
	 * @param property
	 *            the property
	 * @param sessionCortex
	 *            the session cortex
	 */
	public TfAssociationTable(EntityType<?> et, Property property, PersistenceGmSession sessionCortex) {
		this.leftEntityName = et.getShortName();
		this.leftEntityType = et.getTypeSignature();
		this.rightEntityType = ((CollectionType) property.getType()).getCollectionElementType().getTypeName();
		this.leftProperty = property;
		this.leftEntity = et;
		this.leftPropertyDisplayName = leftEntity.getShortName() + "." + leftEntity.getIdProperty().getName();

		String propName;
		if (property.getName().length() > 1) {
			propName = property.getName().substring(0, 1).toUpperCase() + property.getName().substring(1);
		} else {
			propName = property.getName().substring(0, 1).toUpperCase();
		}
		// System.out.println(property.getType() + " - " +
		// property.getType().getTypeCode());
		try {
			rightEntity = GMF.getTypeReflection().getEntityType(rightEntityType);
			this.rightEntityName = rightEntity.getShortName();
			this.rightProperty = rightEntity.getIdProperty();
			this.rightPropertyDisplayName = rightEntity.getShortName() + "." + rightEntity.getIdProperty().getName();
			this.name = leftEntityName + propName + "To" + rightEntityName;
		} catch (ClassCastException ccex) {
			// Not an entity type... this is fugly!

			this.primitive = true;
			this.rightEntityName = rightEntityType;
			this.rightPropertyDisplayName = "value";
			this.name = leftEntityName + propName;
			if (!this.name.endsWith("s")) { 
				this.name += "s";
			}
		}
		
		// TODO cardinality
		this.typeCode = property.getType().getTypeCode();
		this.sessionCortex = sessionCortex;

	}

	@Override
	public String toString() {
		return "TfAssociationTable [leftEntityName=" + leftEntityName + ", rightEntityName=" + rightEntityName
				+ ", leftEntityType=" + leftEntityType + ", rightEntityType=" + rightEntityType + ", name=" + name
				+ ", cardinality=" + cardinality + ", leftProperty=" + leftProperty + ", leftPropertyDisplayName="
				+ leftPropertyDisplayName + ", rightProperty=" + rightProperty + ", rightPropertyDisplayName="
				+ rightPropertyDisplayName + ", leftEntity=" + leftEntity + ", rightEntity=" + rightEntity
				+ ", sessionCortex=" + sessionCortex + ", typeCode=" + typeCode + ", associationData=" + associationData
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCardinality() {
		return cardinality;
	}

	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
	}

	public Set<Entry<String, String>> getAssociationData() {
		return associationData;
	}

	public void setAssociationData(Set<Entry<String, String>> associations) {
		this.associationData = associations;
	}

	public EntityType<?> getLeftEntity() {
		return leftEntity;
	}

	public EntityType<?> getRightEntity() {
		return rightEntity;
	}

	public Property getRightProperty() {
		return rightProperty;
	}

	public void setRightProperty(Property rightProperty) {
		this.rightProperty = rightProperty;
	}

	public TypeCode getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(TypeCode typeCode) {
		this.typeCode = typeCode;
	}

	public Property getLeftProperty() {
		return leftProperty;
	}

	public void setProperty(Property property) {
		this.leftProperty = property;
	}

	public String getLeftEntityName() {
		return leftEntityName;
	}

	public void setLeftEntityName(String leftEntityName) {
		this.leftEntityName = leftEntityName;
	}

	public String getRightEntityName() {
		return rightEntityName;
	}

	public void setRightEntityName(String rightEntityName) {
		this.rightEntityName = rightEntityName;
	}

	public String getLeftEntityType() {
		return leftEntityType;
	}

	public void setLeftEntityType(String leftEntityType) {
		this.leftEntityType = leftEntityType;
	}

	public String getRightEntityType() {
		return rightEntityType;
	}

	public void setRightEntityType(String rightEntityType) {
		this.rightEntityType = rightEntityType;
	}

	public EntityType<?> getEntity() {
		return leftEntity;
	}

	public void setEntity(EntityType<?> entity) {
		this.leftEntity = entity;
	}

	public PersistenceGmSession getSessionCortex() {
		return sessionCortex;
	}

	public void setSessionCortex(PersistenceGmSession sessionCortex) {
		this.sessionCortex = sessionCortex;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setLeftProperty(Property leftProperty) {
		this.leftProperty = leftProperty;
	}

	public String getRightPropertyDisplayName() {
		return rightPropertyDisplayName;
	}

	public void setRightPropertyDisplayName(String rightPropertyDisplayName) {
		this.rightPropertyDisplayName = rightPropertyDisplayName;
	}

	public String getLeftPropertyDisplayName() {
		return leftPropertyDisplayName;
	}

	public void setLeftPropertyDisplayName(String leftPropertyDisplayName) {
		this.leftPropertyDisplayName = leftPropertyDisplayName;
	}

	/**
	 * Checks if this virtual table is between given real entity types.
	 *
	 * @param leftTable
	 *            the left table
	 * @param rightTable
	 *            the right table
	 * @return true, if is between table names
	 */
	public boolean isBetweenTableNames(Table leftTable, Table rightTable) {
		if (leftTable == null || rightTable == null) {
			return false;
		}

		return (leftEntityName.equals(leftTable.getName()) && rightEntityName.equals(rightTable.getName()))
				|| (rightEntityName.equals(leftTable.getName()) && leftEntityName.equals(rightTable.getName()));
	}

	/**
	 * Checks if this association is for a real table and virtual table, either way.
	 *
	 * @param leftTable
	 *            the left table
	 * @param rightTable
	 *            the right table
	 * @return true, if is for table and virtual table
	 */
	public boolean isForTableAndVirtualTable(Table leftTable, Table rightTable) {
		if (leftTable == null || rightTable == null) {
			return false;
		}

		return ((leftEntityName.equals(leftTable.getName()) && name.equals(rightTable.getName()))
				|| (name.equals(leftTable.getName()) && leftEntityName.equals(rightTable.getName())))
				|| ((rightEntityName.equals(leftTable.getName()) && name.equals(rightTable.getName()))
				|| (name.equals(leftTable.getName()) && rightEntityName.equals(rightTable.getName())));
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

}
