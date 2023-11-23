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
package com.braintribe.model.processing.deployment.hibernate.mapping.render.context;

import static com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityMappingContextTools.quoteIdentifier;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static com.braintribe.utils.lcd.StringTools.isBlank;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.accessdeployment.hibernate.meta.DbUpdateStatement;
import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.display.NameConversion;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.EntityHint;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.ResourceUtils;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.utils.conversion.NameConverter;
import com.braintribe.utils.lcd.CommonTools;

/**
 * 
 */
public class EntityDescriptor extends AbstractDescriptor {

	private static final int MAX_COMPOSITE_ID_COLUMNS = 30;

	public EntityDescriptor parent;
	public final HbmEntityType hbmEntityType;
	public final String fullName;
	public final EntityHint entityHint;
	public String simpleName;
	public String discriminatorName;
	public String discriminatorValue;
	public String discriminatorType;
	public String tableName;
	public String tableNameBase;
	public String discriminatorColumnName;
	public String discriminatorFormula;
	public String xml, hbmSuperType;
	public String schema, catalog;
	public final Boolean isAbstract, isTopLevel, hasSubTypes;
	public PropertyDescriptor idProperty;
	public JpaCompositeId compositeId;
	public Set<String> compositeIdColumns = emptySet();

	private final EntityDescriptorMetaData metaData;

	public final List<PropertyDescriptor> properties = newList();

	private static class EntityDescriptorMetaData {
		public EntityMapping em;
		public NameConversion nameConversion;
		public List<DbUpdateStatement> dbUpdateStatements;
	}

	public EntityDescriptor(EntityDescriptor parent, HbmEntityType et, EntityHint entityHint, HbmXmlGenerationContext context) {
		super(context);

		this.tag = et.getIsTopLevel() ? "class" : "subclass";

		this.parent = parent;
		this.entityHint = entityHint;
		this.hbmEntityType = et;
		this.fullName = et.getType().getTypeSignature();
		this.simpleName = simpleName();
		this.isAbstract = et.getType().getIsAbstract();
		this.isTopLevel = et.getIsTopLevel();
		this.hasSubTypes = et.getHasSubTypes();
		this.discriminatorName = simpleName + "Type";
		this.hbmSuperType = this.isTopLevel ? null : et.getSuperType().getType().getTypeSignature();
		this.schema = valueOrNullIfEmpty(context.defaultSchema);
		this.catalog = valueOrNullIfEmpty(context.defaultCatalog);
		this.metaData = resolveMetaData(context);

		applyMetaData();
		applyEntityHint(entityHint);
		resolveCompositeId(context);
	}

	private EntityDescriptorMetaData resolveMetaData(HbmXmlGenerationContext context) {
		EntityDescriptorMetaData result = new EntityDescriptorMetaData();

		EntityMdResolver mdResolver = context.getMetaData().entityType(hbmEntityType.getType());

		result.em = mdResolver.meta(EntityMapping.T).exclusive();
		result.nameConversion = mdResolver.meta(NameConversion.T).exclusive();
		result.dbUpdateStatements = mdResolver.meta(DbUpdateStatement.T).list();

		return result;
	}

	private static String valueOrNullIfEmpty(String s) {
		return CommonTools.isEmpty(s) ? null : s;
	}

	protected void applyUpdateStatemetns(List<DbUpdateStatement> statements) {
		metaData.dbUpdateStatements = newList(nullSafe(statements));
	}

	protected void applyMetaData() {
		if (metaData.em != null)
			applyEntityMetaData(metaData.em);

		if (metaData.nameConversion != null && tableName == null)
			tableNameBase = NameConverter.convert(simpleName, metaData.nameConversion.getStyle());
		else
			tableNameBase = simpleName;

		// Uncomment if name conversion should also be applied on discriminator column
		// It might make sense to also somehow do a conversion on discriminator column name, but the question is which MD should be relevant for that?

		// if (metaData.nameConversion != null)
		// discriminatorName = NameConverter.convert(discriminatorName, metaData.nameConversion.getStyle());
	}

	protected void applyEntityMetaData(EntityMapping entityMapping) {
		if (entityMapping == null)
			return;

		if (entityMapping.getXmlFileUrl() != null) {
			xml = ResourceUtils.loadResourceToString(entityMapping.getXmlFileUrl());
			if (xml != null) {
				return;
			}
		}

		if (entityMapping.getXml() != null) {
			xml = entityMapping.getXml();
			return;
		}

		if (!isBlank(entityMapping.getTableName()))
			tableName = prefixTableName(entityMapping.getTableName());

		if (entityMapping.getDiscriminatorValue() != null)
			discriminatorValue = entityMapping.getDiscriminatorValue();

		if (entityMapping.getDiscriminatorType() != null)
			discriminatorType = entityMapping.getDiscriminatorType();

		if (entityMapping.getDiscriminatorColumnName() != null)
			discriminatorColumnName = entityMapping.getDiscriminatorColumnName();

		if (entityMapping.getDiscriminatorFormula() != null)
			discriminatorFormula = entityMapping.getDiscriminatorFormula();

		if (entityMapping.getSchema() != null)
			schema = entityMapping.getSchema();

		if (entityMapping.getCatalog() != null)
			catalog = entityMapping.getCatalog();
	}

	protected void applyEntityHint(EntityHint entityHint) {
		if (entityHint == null)
			return;

		if (!isBlank(entityHint.table))
			this.tableName = prefixTableName(entityHint.table);

		if (!isBlank(entityHint.discColumn))
			this.discriminatorColumnName = entityHint.discColumn;
	}

	private void resolveCompositeId(HbmXmlGenerationContext context) {
		compositeId = context.getMetaData().entityTypeSignature(fullName).property(GenericEntity.id).meta(JpaCompositeId.T).exclusive();

		if (compositeId == null)
			return;

		List<JpaColumn> columns = compositeId.getColumns();

		if (isEmpty(columns))
			throw new IllegalArgumentException("Id of entity '" + fullName + "' is configured as composite, but has no columns.");

		if (columns.size() > MAX_COMPOSITE_ID_COLUMNS)
			throw new IllegalArgumentException("Cannot map id property of '" + fullName
					+ ". It is mapped as composite, but the number of columns exceeds the maximum supported number of: " + MAX_COMPOSITE_ID_COLUMNS);

		compositeIdColumns = columns.stream() //
				.map(JpaColumn::getName) //
				.collect(Collectors.toSet());
	}

	protected boolean isCompositeIdColumn(String columnName) {
		return compositeIdColumns.contains(columnName);
	}

	protected JpaCompositeId getCompositeId() {
		return compositeId;
	}

	public GmEntityType getGmEntityType() {
		return hbmEntityType.getType();
	}

	public EntityHint getEntityHint() {
		return entityHint;
	}

	public HbmEntityType getHbmEntityType() {
		return hbmEntityType;
	}

	public String getFullName() {
		return fullName;
	}

	public String getQuotedTableName() {
		return quoteIdentifier(tableName);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableNameBase() {
		return tableNameBase;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public String getXml() {
		return xml;
	}

	private String simpleName() {
		return simpleName(fullName);
	}

	public Boolean getIsTopLevel() {
		return isTopLevel;
	}

	/**
	 * Hibernate class must be marked with abstract="true" only if GmEntityType.getIsAbstract() is true.
	 * 
	 * @return Whether the entity is abstract
	 */
	public boolean getAbstractFlag() {
		return Boolean.TRUE.equals(isAbstract);
	}

	public String abstractFlag() {
		return "" + getAbstractFlag();
	}

	public List<PropertyDescriptor> getProperties() {
		return this.properties;
	}

	public PropertyDescriptor getIdProperty() {
		return this.idProperty;
	}

	public void setIdProperty(PropertyDescriptor idProperty) {
		this.idProperty = idProperty;
	}

	public Boolean getHasSubTypes() {
		return hasSubTypes;
	}

	public String getDiscriminatorName() {
		return discriminatorName;
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}

	public String getDiscriminatorType() {
		return discriminatorType;
	}

	public String getQuotedDiscriminatorColumnName() {
		return quoteIdentifier(discriminatorColumnName);
	}

	public String getDiscriminatorColumnName() {
		return discriminatorColumnName;
	}

	public String getDiscriminatorFormula() {
		return discriminatorFormula;
	}

	public void setDiscriminatorColumnName(String discriminatorColumnName) {
		this.discriminatorColumnName = discriminatorColumnName;
	}

	public List<DbUpdateStatement> getUpdateStatements() {
		return metaData.dbUpdateStatements;
	}

	public String getHbmSuperType() {
		return hbmSuperType;
	}

	public void setHbmSuperType(String hbmSuperType) {
		this.hbmSuperType = hbmSuperType;
	}

	public String getSchema() {
		return schema;
	}

	public String getCatalog() {
		return catalog;
	}

	/**
	 * EntityDescriptors must be uniquely identifiable for persistence ends. Current implementation takes into consideration this.fullName only.
	 * DB/Run specific information retrievable from HbmXmlGenerationConfig could be part of this identifier.
	 * 
	 * @return An unique id for {@link EntityDescriptor} instances
	 */
	public Object getDescriptorId() {
		return this.fullName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDescriptorId() == null) ? 0 : getDescriptorId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityDescriptor other = (EntityDescriptor) obj;
		if (getDescriptorId() == null) {
			if (other.getDescriptorId() != null)
				return false;
		} else if (!getDescriptorId().equals(other.getDescriptorId()))
			return false;
		return true;
	}

}
