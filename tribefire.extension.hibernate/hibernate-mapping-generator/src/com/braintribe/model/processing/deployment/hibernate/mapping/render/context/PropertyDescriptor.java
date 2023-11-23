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
import static com.braintribe.utils.lcd.ReflectionTools.ensureValidJavaBeansName;
import static com.braintribe.utils.lcd.StringTools.isBlank;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbedded;
import com.braintribe.model.accessdeployment.jpa.meta.JpaPropertyMapping;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.display.NameConversion;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.PropertyHint;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.ResourceUtils;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.utils.conversion.NameConverter;

public class PropertyDescriptor extends AbstractDescriptor implements Comparable<PropertyDescriptor> {

	public final EntityDescriptor entityDescriptor;
	protected final GmProperty gmProperty;
	public final String name;
	protected final PropertyHint propertyHint;
	protected final PropertyDescriptorMetaData metaData;

	public String xml;
	public final String fkClass;
	public final boolean isIdProperty;
	public String idGenerator;
	public String explicitType;
	public String uniqueKey;
	public String index;
	public String lazy;
	public String cascade;
	public String fetch;
	public String referencedProperty;
	public String foreignKey;
	public String columnName;
	public final String foreignKeyNamePrefix;
	public final String uniqueKeyNamePrefix;
	public final String indexNamePrefix;
	public Boolean isUnique, isNotNull, isInvalidReferencesIgnored, isReadOnly, isOptimisticLock;
	public Long length, precision, scale;
	public String access;
	public String node;
	public String formula;
	public String sqlType;
	public String check;
	public String defaultValue;
	public String read;
	public String write;

	public static class PropertyDescriptorMetaData {
		public JpaPropertyMapping jpaPropertyMapping;
		public NameConversion nameConversion;
		public MaxLength maxLength;
	}

	public static PropertyDescriptor create(HbmXmlGenerationContext context, EntityDescriptor descriptor, GmProperty gmProperty) {
		PropertyDescriptorMetaData metaData = new PropertyDescriptorMetaData();

		PropertyMdResolver propertyMdResolver = context.propertyMd(descriptor.getGmEntityType(), gmProperty);

		metaData.jpaPropertyMapping = MappingHelper.resolveJpaPropertyMapping(context, descriptor.getGmEntityType(), gmProperty);
		metaData.nameConversion = propertyMdResolver.meta(NameConversion.T).exclusive();
		metaData.maxLength = propertyMdResolver.meta(MaxLength.T).exclusive();

		return create(context, gmProperty, descriptor, metaData);
	}

	/* package */ static PropertyDescriptor create(HbmXmlGenerationContext context, GmProperty gmProperty, EntityDescriptor descriptor,
			PropertyDescriptorMetaData metaData) {

		GmType propertyType = gmProperty.getType();

		if (propertyType.isGmCollection())
			return new CollectionPropertyDescriptor(context, descriptor, gmProperty, metaData);

		if (propertyType.isGmEnum())
			return new EnumDescriptor(context, descriptor, gmProperty, metaData);

		if (propertyType.isGmEntity() && MappingHelper.isEmbeddable((GmEntityType) propertyType, context)) {
			if (metaData.jpaPropertyMapping instanceof JpaEmbedded)
				return new ComponentDescriptor(context, descriptor, gmProperty, metaData);

			else if (!MappingHelper.isXmlSnippet(metaData.jpaPropertyMapping))
				throwMisconfiguredEmbeddableType(descriptor, gmProperty, metaData);
		}

		return new PropertyDescriptor(context, descriptor, gmProperty, metaData);
	}

	private static void throwMisconfiguredEmbeddableType(EntityDescriptor descriptor, GmProperty gmProperty, PropertyDescriptorMetaData metaData) {
		throw new IllegalStateException("Misconfigured hibernate mappings. Property " + descriptor.getGmEntityType().getTypeSignature() + "#"
				+ gmProperty.getName() + " has an embeddable type '" + gmProperty.getType().getTypeSignature()
				+ "', but it's property mapping is not a JpaEmbedded nor an XML snippet, but:" + metaData.jpaPropertyMapping);
	}

	protected PropertyDescriptor(HbmXmlGenerationContext context, EntityDescriptor entityDescriptor, GmProperty gmProperty,
			PropertyDescriptorMetaData metaData) {
		super(context);
		this.gmProperty = gmProperty;
		this.name = ensureValidJavaBeansName(gmProperty.getName());
		this.entityDescriptor = entityDescriptor;
		this.metaData = metaData;
		this.propertyHint = resolvePropertyHint(gmProperty, entityDescriptor);
		this.isIdProperty = gmProperty.isId();
		this.fkClass = resolveFkClass(gmProperty);
		this.tag = isIdProperty ? "id" : "property";
		this.foreignKeyNamePrefix = context.foreignKeyNamePrefix;
		this.uniqueKeyNamePrefix = context.uniqueKeyNamePrefix;
		this.indexNamePrefix = context.indexNamePrefix;

		applyMetaData();
		applyPropertyHint();
		applyTypeSpecification(gmProperty, entityDescriptor);

		if (isIdProperty)
			resolveIdGenerator(gmProperty);
	}

	/* This is just extracted so for now I can override this in ComponentDescriptor and return null - as I do not know
	 * how nor have the time to do figure out how to do it properly */
	protected PropertyHint resolvePropertyHint(GmProperty gmProperty, EntityDescriptor descriptor) {
		return MappingHelper.optionalPropertyHint(descriptor.getEntityHint(), gmProperty.getName()).orElse(null);
	}

	private void applyTypeSpecification(GmProperty gmProperty, EntityDescriptor entityDescriptor) {
		// A type or xml snipped was already resolved for this property. No need to proceed.
		if (explicitType != null || xml != null)
			return;

		GmType type = gmProperty.getType();

		if (!type.isGmScalar()) {

			// We resolve TypeSpecification.T only for non-scalar types
			GenericModelType resolveType = resolveType(entityDescriptor.getFullName(), gmProperty.getName());

			if (resolveType != null)
				explicitType = hibernateMappingTypeOf(resolveType);

			else if (gmProperty.isId())
				// Non-scalar ids are handled as Long when no TypeSpecification.T is resolved for it.
				explicitType = hibernateMappingTypeOf(SimpleTypes.TYPE_LONG);

		} else if (type.isGmSimple()) {
			// Here we ensure that simple types have an explicit type in the mapping to avoid the wrong
			// type being reflected by hibernate based on possible static members in the GenericEntity.

			explicitType = hibernateMappingTypeOf(type.reflectionType());
		}

	}

	private GenericModelType resolveType(String typeSignature, String propertyName) {
		TypeSpecification ts = context.getMetaData().entityTypeSignature(typeSignature).property(propertyName).meta(TypeSpecification.T).exclusive();

		return ts == null ? null : ts.getType().reflectionType();
	}

	private String hibernateMappingTypeOf(GenericModelType reflectionType) {
		switch (reflectionType.getTypeCode()) {
			case booleanType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
				return reflectionType.getTypeSignature();
			case decimalType:
				return "big_decimal";
			case dateType:
				return "timestamp";
			default:
				return null;
		}
	}

	protected void applyMetaData() {
		if (metaData.jpaPropertyMapping instanceof PropertyMapping)
			applyPropertyMappingMetaData((PropertyMapping) metaData.jpaPropertyMapping);

		if (metaData.nameConversion != null && getColumnName() == null)
			setColumnName(NameConverter.convert(getPropertyName(), metaData.nameConversion.getStyle()));

		if (metaData.maxLength != null)
			length = metaData.maxLength.getLength();
	}

	protected void applyPropertyMappingMetaData(PropertyMapping propertyMapping) {

		if (propertyMapping.getXmlFileUrl() != null) {
			this.xml = ResourceUtils.loadResourceToString(propertyMapping.getXmlFileUrl());
			if (this.xml != null)
				return;
		}

		if (propertyMapping.getXml() != null) {
			this.xml = propertyMapping.getXml();
			return;
		}

		// Overwrite idGenerator, if applicable
		if (isIdProperty) {

			if (propertyMapping.getIdGeneration() != null) {
				// An idGeneration strategy is specified, should overwrite the default value for the column type.
				this.idGenerator = propertyMapping.getIdGeneration();

			} else if (Boolean.TRUE.equals(propertyMapping.getAutoAssignable())) {
				// No idGeneration strategy was specified, but autoAssignable was.
				// Then "native" overwrites the default value for the column type.
				this.idGenerator = "native";
			}
		}

		if (propertyMapping.getColumnName() != null)
			this.setColumnName(propertyMapping.getColumnName());

		if (propertyMapping.getType() != null)
			this.explicitType = propertyMapping.getType();

		if (propertyMapping.getReferencedProperty() != null)
			this.referencedProperty = propertyMapping.getReferencedProperty();

		if (propertyMapping.getLength() != null)
			this.length = propertyMapping.getLength();

		if (propertyMapping.getPrecision() != null)
			this.precision = propertyMapping.getPrecision();

		if (propertyMapping.getScale() != null)
			this.scale = propertyMapping.getScale();

		// not used for generic properties, but in CollectionPropertyDescriptor :
		// propertyMapping.oneToMany
		// propertyMapping.table by copying to many2ManyTable
		// propertyMapping.keyType
		// propertyMapping.keyLength
		// propertyMapping.keyColumn;
		// propertyMapping.keyPropertyRef;
		// propertyMapping.indexColumn;
		// propertyMapping.mapKeyColumn;
		// propertyMapping.elementColumn;

		if (propertyMapping.getUnique() != null)
			this.isUnique = propertyMapping.getUnique();

		if (propertyMapping.getNotNull() != null)
			this.isNotNull = propertyMapping.getNotNull();

		if (!isBlank(propertyMapping.getUniqueKey()))
			this.uniqueKey = prefixUniqueKeyName(propertyMapping.getUniqueKey());

		if (!isBlank(propertyMapping.getIndex()))
			this.index = prefixIndexName(propertyMapping.getIndex());

		if (propertyMapping.getLazy() != null)
			this.lazy = propertyMapping.getLazy();

		if (propertyMapping.getCascade() != null)
			this.cascade = propertyMapping.getCascade();

		if (propertyMapping.getFetch() != null)
			this.fetch = propertyMapping.getFetch();

		if (propertyMapping.getInvalidReferencesIgnored() != null)
			this.isInvalidReferencesIgnored = propertyMapping.getInvalidReferencesIgnored();

		if (propertyMapping.getReadOnly() != null)
			this.isReadOnly = propertyMapping.getReadOnly();

		if (!isBlank(propertyMapping.getForeignKey()))
			this.foreignKey = prefixForeignKeyName(propertyMapping.getForeignKey());
	}

	/**
	 * {@link PropertyHint}(s) are applied last, thus, its settings are able to override settings parsed from the
	 * {@link GmProperty} or from {@link MetaData}(s).
	 * <p>
	 * e.g.:
	 * <p>
	 * {@link PropertyHint#type}, if set, will overwrite the type inferred from {@link GmProperty}.
	 * <p>
	 * {@link PropertyHint#length}, if set, will overwrite the equivalent configuration from a any {@link MaxLength}
	 * bound to the {@link GmProperty}.
	 * 
	 */
	private void applyPropertyHint() {
		if (propertyHint == null)
			return;

		if (!isBlank(propertyHint.type))
			this.explicitType = propertyHint.type;

		if (propertyHint.length != null && propertyHint.length > 0)
			this.length = propertyHint.length;

		if (propertyHint.precision != null && propertyHint.precision > -1)
			this.precision = propertyHint.precision;

		if (propertyHint.scale != null && propertyHint.scale > -1)
			this.scale = propertyHint.scale;

		if (!isBlank(propertyHint.column))
			this.setColumnName(propertyHint.column);

		if (propertyHint.unique != null)
			this.isUnique = propertyHint.unique;

		if (propertyHint.notNull != null)
			this.isNotNull = propertyHint.notNull;

		if (propertyHint.idGeneration != null)
			this.idGenerator = propertyHint.idGeneration;

		if (propertyHint.uniqueKey != null)
			this.uniqueKey = prefixUniqueKeyName(propertyHint.uniqueKey);

		if (propertyHint.index != null)
			this.index = prefixIndexName(propertyHint.index);

		if (!isBlank(propertyHint.lazy))
			this.lazy = propertyHint.lazy;

		if (!isBlank(propertyHint.fetch))
			this.fetch = propertyHint.fetch;

		if (!isBlank(propertyHint.foreignKey))
			this.foreignKey = prefixForeignKeyName(propertyHint.foreignKey);

	}

	/**
	 * Resolves the {@link #idGenerator} value, if not already set via metadata or hints.
	 */
	private void resolveIdGenerator(GmProperty gmProperty) {
		if (idGenerator == null) {
			// Currently the GmProperty type of id property is always Object, and a explicitType might have been already
			// defined at this stage, but we leave the GmProperty check here in case this changes again in the future...
			String idType = explicitType == null ? gmProperty.getType().getTypeSignature() : explicitType;

			idGenerator = isIntegerType(idType) ? "native" : "assigned";
		}
	}

	private static boolean isIntegerType(String typeSignature) {
		return typeSignature.equals(GenericModelTypeReflection.TYPE_INTEGER.getTypeSignature())
				|| typeSignature.equals(GenericModelTypeReflection.TYPE_LONG.getTypeSignature());
	}

	private static String resolveFkClass(GmProperty gmProperty) {
		GmType type = gmProperty.getType();
		if (type instanceof GmEntityType)
			return ((GmEntityType) type).getTypeSignature();
		else
			return null;
	}

	/**
	 * Determines if this property has hbm2ddl options attributes justifying the creation of a column child element.
	 * 
	 * @return Whether this property has attributes justifying the creation of a <column/> child element
	 */
	public boolean getHasColumnAttributes() {
		return !isEmpty(sqlType) || !isEmpty(check) || !isEmpty(defaultValue) || !isEmpty(read) || !isEmpty(write);
	}

	/** Valid JavaBeans property name, e.g. XML instead of xML, which would be a GM property name. */
	public String getName() {
		return name;
	}

	public String getPropertyName() {
		return gmProperty.getName();
	}
	
	public String getQuotedColumnName() {
		return quoteIdentifier(columnName);
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;

		makeColumnReadOnlyIfNeeded();
	}

	/**
	 * Currently, the only case where column property absolutely must be mapped as read-only is when that column is part
	 * of a composite id.
	 */
	private void makeColumnReadOnlyIfNeeded() {
		if (entityDescriptor.isCompositeIdColumn(columnName))
			this.isReadOnly = true;
	}

	public Boolean getIsIdProperty() {
		return isIdProperty;
	}

	public boolean getIsCompositeIdProperty() {
		return isIdProperty && getCompositeId() != null;
	}

	public boolean getIsEmbedded() {
		return false; // overridden in sub-type
	}

	public JpaCompositeId getCompositeId() {
		return entityDescriptor.getCompositeId();
	}

	public Boolean getIsUnique() {
		return isUnique;
	}

	public Boolean getIsNotNull() {
		return isNotNull;
	}

	public String getGeneratorClass() {
		return idGenerator;
	}

	public String getXml() {
		return xml;
	}

	public String getFkClass() {
		return fkClass;
	}

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	public Long getLength() {
		return length;
	}

	public Long getPrecision() {
		return precision;
	}

	public Long getScale() {
		return scale;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public String getIndex() {
		return index;
	}

	public String getExplicitType() {
		return explicitType;
	}

	public String getLazy() {
		return lazy;
	}

	public String getFetch() {
		return fetch;
	}

	public Boolean getIsInvalidReferencesIgnored() {
		return isInvalidReferencesIgnored;
	}

	public Boolean getIsReadOnly() {
		return isReadOnly;
	}

	public String getReferencedProperty() {
		return referencedProperty;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public boolean getIsCollectionType() {
		return false;
	}

	public boolean getIsEnumType() {
		return false;
	}

	public Boolean getIsOptimisticLock() {
		return isOptimisticLock;
	}

	public String getAccess() {
		return access;
	}

	public String getNode() {
		return node;
	}

	public String getFormula() {
		return formula;
	}

	public String getSqlType() {
		return sqlType;
	}

	public String getCheck() {
		return check;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getWrite() {
		return write;
	}

	public GmType getGmType() {
		return gmProperty.getType();
	}

	@Override
	public int compareTo(PropertyDescriptor other) {
		return name.compareTo(other.name);
	}

	/**
	 * <p>
	 * Applies the constraint prefix as given by {@link HbmXmlGenerationContext#indexNamePrefix} to index names
	 * configured via metadata or hints.
	 * 
	 * @param hintedIndexName
	 *            Constraint name configured via metadata or hints.
	 * @return The constraint name possibly prefixed.
	 */
	protected String prefixIndexName(String hintedIndexName) {
		return indexNamePrefix != null ? indexNamePrefix + hintedIndexName : hintedIndexName;
	}

	/**
	 * <p>
	 * Applies the constraint prefix as given by {@link HbmXmlGenerationContext#uniqueKeyNamePrefix} to unique key names
	 * configured via metadata or hints.
	 * 
	 * @param hintedUniqueKeyName
	 *            Constraint name configured via metadata or hints.
	 * @return The constraint name possibly prefixed.
	 */
	protected String prefixUniqueKeyName(String hintedUniqueKeyName) {
		return uniqueKeyNamePrefix != null ? uniqueKeyNamePrefix + hintedUniqueKeyName : hintedUniqueKeyName;
	}

	/**
	 * <p>
	 * Applies the constraint prefix as given by {@link HbmXmlGenerationContext#foreignKeyNamePrefix} to foreign key
	 * names configured via metadata or hints.
	 * 
	 * @param hintedForeignKeyName
	 *            Constraint name configured via metadata or hints.
	 * @return The constraint name possibly prefixed.
	 */
	protected String prefixForeignKeyName(String hintedForeignKeyName) {
		return foreignKeyNamePrefix != null ? foreignKeyNamePrefix + hintedForeignKeyName : hintedForeignKeyName;
	}

}
