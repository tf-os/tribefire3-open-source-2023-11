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
import static com.braintribe.utils.lcd.StringTools.isBlank;

import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.display.NameConversionStyle;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.utils.conversion.NameConverter;

/**
 * 
 */
public class CollectionPropertyDescriptor extends PropertyDescriptor {

	public String many2ManyTable;
	public String keyColumn;
	public Boolean keyColumnNotNull;
	public String keyPropertyRef;
	public String indexColumn;
	public String elementSignature;
	public String elementColumn;
	public String elementSimpleType;
	public String elementForeignKey;
	public boolean isSimpleCollection; // true if the elementType of this column is GmSimpleType
	public boolean isOneToMany; // defines if the (entity type) element must be mapped as <one-to-many> instead of the
									// default <many-to-many>
	public String manyToManyFetch;

	// map related properties
	public boolean isSimpleMapKey;
	public String mapKeySignature;
	public String mapKeyColumn;
	public String mapKeySimpleType;
	public String mapKeyExplicitType;
	public Long mapKeyLength;
	public String mapKeyForeignKey;

	// db naming strategy names to originate table/column names.
	public String collectionName;
	public String collectionIndexName;
	public String collectionKeyName;
	public String collectionElementName;
	public String collectionMapKeyName;

	public CollectionPropertyDescriptor(HbmXmlGenerationContext context, EntityDescriptor descriptor, GmProperty gmProperty,
			PropertyDescriptorMetaData metaData) {

		super(context, descriptor, gmProperty, metaData);
		this.initCollectionType(descriptor, gmProperty);
		this.applyCollectionMetaDatas();
		this.applyCollectionPropertyHint();
	}

	@Override
	public GmCollectionType getGmType() {
		return (GmCollectionType) super.getGmType();
	}
	
	private void initCollectionType(EntityDescriptor descriptor, GmProperty gmProperty) {
		GmType colType = gmProperty.getType();
		GmType elementType = null, keyType = null;
		if (colType instanceof GmLinearCollectionType) {
			elementType = ((GmLinearCollectionType) colType).getElementType();
		} else if (colType instanceof GmMapType) {
			elementType = ((GmMapType) colType).getValueType();
			keyType = ((GmMapType) colType).getKeyType();
		}

		initCollectionType(descriptor, colType, elementType, keyType);
	}

	private void initCollectionType(EntityDescriptor descriptor, GmType colType, GmType elementType, GmType keyType) {
		String ownerSimpleName = descriptor.getSimpleName();

		isSimpleCollection = !(elementType instanceof GmEntityType);
		if (isSimpleCollection) {
			elementSimpleType = elementType.getTypeSignature();
		}

		elementSignature = elementType.getTypeSignature();
		String eColumn = isSimpleCollection ? getName() : simpleName(elementSignature) + "Id";
		tag = keyType != null ? "map" : colType instanceof GmSetType ? "set" : "list";

		// db naming strategy names to originate table/column names.
		collectionName = ownerSimpleName + capitalize(getName());
		collectionIndexName = collectionName + "Idx";
		collectionKeyName = ownerSimpleName + "Id";
		collectionElementName = capitalize(eColumn);
		if (collectionElementName.equals(collectionKeyName))
			collectionElementName = "Other" + collectionElementName;

		if (metaData.nameConversion != null) {
			NameConversionStyle style = metaData.nameConversion.getStyle();
			collectionName = NameConverter.convert(collectionName, style);
			collectionIndexName = NameConverter.convert(collectionIndexName, style);
			collectionKeyName = NameConverter.convert(collectionKeyName, style);
			collectionElementName = NameConverter.convert(collectionElementName, style);
		}
		
		if (getIsMap()) {
			initMapKey(keyType);
		}
	}

	private void initMapKey(GmType keyType) {

		isSimpleMapKey = !(keyType instanceof GmEntityType);
		if (isSimpleMapKey) {
			mapKeySimpleType = keyType.getTypeSignature();
		}

		mapKeySignature = keyType.getTypeSignature();
		String eColumn = isSimpleMapKey ? getName() + "Key" : simpleName(mapKeySignature) + "Id";

		// db naming strategy names to originate table/column names.
		collectionMapKeyName = capitalize(eColumn);
		if (collectionMapKeyName.equals(collectionKeyName))
			collectionMapKeyName = "Other" + collectionMapKeyName;
	}

	private void applyCollectionMetaDatas() {
		if (metaData.jpaPropertyMapping instanceof PropertyMapping)
			applyCollectionPropertyMappingMetaData((PropertyMapping) metaData.jpaPropertyMapping);
	}

	private void applyCollectionPropertyMappingMetaData(PropertyMapping propertyMetaData) {
		if (propertyMetaData == null)
			return;

		if (!isBlank(propertyMetaData.getType()))
			this.elementSimpleType = propertyMetaData.getType();

		// common for collections
		if (propertyMetaData.getOneToMany() != null)
			this.isOneToMany = propertyMetaData.getOneToMany();

		if (!isBlank(propertyMetaData.getCollectionTableName()))
			this.many2ManyTable = prefixTableName(propertyMetaData.getCollectionTableName());

		if (propertyMetaData.getCollectionKeyColumn() != null)
			this.keyColumn = propertyMetaData.getCollectionKeyColumn();

		if (propertyMetaData.getCollectionKeyColumn() != null)
			this.keyColumn = propertyMetaData.getCollectionKeyColumn();
		
		if (propertyMetaData.getCollectionKeyColumnNotNull() != null)
			this.keyColumnNotNull = propertyMetaData.getCollectionKeyColumnNotNull();
		
		if (propertyMetaData.getCollectionKeyPropertyRef() != null)
			this.keyPropertyRef = propertyMetaData.getCollectionKeyPropertyRef();

		if (propertyMetaData.getCollectionElementColumn() != null)
			this.elementColumn = propertyMetaData.getCollectionElementColumn();

		if (!isBlank(propertyMetaData.getCollectionElementForeignKey()))
			this.elementForeignKey = prefixForeignKeyName(propertyMetaData.getCollectionElementForeignKey());

		if (propertyMetaData.getCollectionElementFetch() != null)
			this.manyToManyFetch = propertyMetaData.getCollectionElementFetch();

		// map
		if (propertyMetaData.getMapKeyColumn() != null)
			this.mapKeyColumn = propertyMetaData.getMapKeyColumn();

		if (!isBlank(propertyMetaData.getMapKeySimpleType())) {
			this.mapKeyExplicitType = propertyMetaData.getMapKeySimpleType();
			if (this.isSimpleMapKey)
				this.mapKeySimpleType = propertyMetaData.getMapKeySimpleType();
		}

		if (propertyMetaData.getMapKeyLength() != null)
			this.mapKeyLength = propertyMetaData.getMapKeyLength();

		if (!isBlank(propertyMetaData.getMapKeyForeignKey()))
			this.mapKeyForeignKey = prefixForeignKeyName(propertyMetaData.getMapKeyForeignKey());

		// list
		if (propertyMetaData.getListIndexColumn() != null)
			this.indexColumn = propertyMetaData.getListIndexColumn();
	}

	private void applyCollectionPropertyHint() {
		if (propertyHint == null)
			return;

		if (!isBlank(propertyHint.type))
			this.elementSimpleType = propertyHint.type;

		if (!isBlank(propertyHint.table))
			this.many2ManyTable = prefixTableName(propertyHint.table);

		if (!isBlank(propertyHint.keyType)) {
			this.mapKeyExplicitType = propertyHint.keyType;
			if (this.isSimpleMapKey)
				this.mapKeySimpleType = mapKeyExplicitType;
		}

		if (propertyHint.keyLength != null && propertyHint.keyLength > 0)
			this.mapKeyLength = propertyHint.keyLength;

		if (!isBlank(propertyHint.keyColumn))
			this.keyColumn = propertyHint.keyColumn;

		if (!isBlank(propertyHint.keyPropertyRef))
			this.keyPropertyRef = propertyHint.keyPropertyRef;

		if (!isBlank(propertyHint.indexColumn))
			this.indexColumn = propertyHint.indexColumn;

		if (!isBlank(propertyHint.mapKeyColumn))
			this.mapKeyColumn = propertyHint.mapKeyColumn;

		if (!isBlank(propertyHint.mapKeyForeignKey))
			this.mapKeyForeignKey = prefixForeignKeyName(propertyHint.mapKeyForeignKey);

		if (!isBlank(propertyHint.elementColumn))
			this.elementColumn = propertyHint.elementColumn;

		if (!isBlank(propertyHint.elementForeignKey))
			this.elementForeignKey = prefixForeignKeyName(propertyHint.elementForeignKey);

		if (propertyHint.oneToMany != null)
			this.isOneToMany = propertyHint.oneToMany;

		if (!isBlank(propertyHint.manyToManyFetch))
			this.manyToManyFetch = propertyHint.manyToManyFetch;

	}

	@Override
	public boolean getIsCollectionType() {
		return true;
	}

	public boolean getIsSimpleCollection() {
		return isSimpleCollection;
	}

	public boolean getIsOneToMany() {
		return isOneToMany;
	}

	public boolean getIsList() {
		return "list".equals(tag);
	}

	public boolean getIsMap() {
		return "map".equals(tag);
	}

	public String getQuotedKeyColumn() {
		return quoteIdentifier(keyColumn);
	}

	public String getKeyColumn() {
		return keyColumn;
	}

	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}

	public String getKeyPropertyRef() {
		return keyPropertyRef;
	}

	public String getQuotedMany2ManyTable() {
		return quoteIdentifier(many2ManyTable);
	}

	public String getMany2ManyTable() {
		return many2ManyTable;
	}

	public void setMany2ManyTable(String many2ManyTable) {
		this.many2ManyTable = many2ManyTable;
	}

	public String getQuotedElementColumn() {
		return quoteIdentifier(elementColumn);
	}

	public String getElementColumn() {
		return elementColumn;
	}

	public void setElementColumn(String elementColumn) {
		this.elementColumn = elementColumn;
	}

	public String getElementClass() {
		return elementSignature;
	}

	public String getElementSimpleType() {
		return elementSimpleType;
	}

	public String getElementForeignKey() {
		return elementForeignKey;
	}

	public String getQuotedIndexColumn() {
		return quoteIdentifier(indexColumn);
	}

	public String getIndexColumn() {
		return indexColumn;
	}

	public void setIndexColumn(String indexColumn) {
		this.indexColumn = indexColumn;
	}

	public boolean getIsSimpleMapKey() {
		return isSimpleMapKey;
	}

	public String getMapKeyClass() {
		return mapKeySignature;
	}

	public String getQuotedMapKeyColumn() {
		return quoteIdentifier(mapKeyColumn);
	}

	public String getMapKeyColumn() {
		return mapKeyColumn;
	}

	public void setMapKeyColumn(String mapKeyColumn) {
		this.mapKeyColumn = mapKeyColumn;
	}

	public String getMapKeySimpleType() {
		return mapKeySimpleType;
	}

	public String getMapKeyExplicitType() {
		return mapKeyExplicitType;
	}

	public void setMapKeyExplicitType(String mapKeyExplicitType) {
		this.mapKeyExplicitType = mapKeyExplicitType;
	}

	public Long getMapKeyLength() {
		return mapKeyLength;
	}

	public void setMapKeyLength(Long mapKeyLength) {
		this.mapKeyLength = mapKeyLength;
	}

	public String getMapKeyForeignKey() {
		return mapKeyForeignKey;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getCollectionIndexName() {
		return collectionIndexName;
	}

	public String getCollectionKeyName() {
		return collectionKeyName;
	}

	public String getCollectionElementName() {
		return collectionElementName;
	}

	public String getCollectionMapKeyName() {
		return collectionMapKeyName;
	}

	public String getManyToManyFetch() {
		return manyToManyFetch;
	}

}
