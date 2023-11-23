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
package com.braintribe.model.access.hibernate.base.tools;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbeddable;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.MetaData;

/**
 * @author peter.gazdik
 */
public class HibernateMappings {

	public static final PropertyMapping UNMAPPED_P = unmappedProperty();

	public static EntityTypeMetaData embeddable() {
		return JpaEmbeddable.T.create("hbm:entity:embeddable");
	}

	public static EntityMapping unmappedEntity() {
		EntityMapping result = EntityMapping.T.create("hbm:entity:unmapped");
		result.setMapToDb(false);

		return result;
	}

	public static PropertyMapping unmappedProperty() {
		PropertyMapping result = PropertyMapping.T.create("hbm:property:unmapped");
		result.setMapToDb(false);

		return result;
	}

	public static PropertyMapping mappedProperty() {
		return PropertyMapping.T.create("hbm:property:mapped");
	}
	
	public static MetaData compositeIdMapping() {
		String globalId = "hbm:CompositeId#id";

		JpaCompositeId result = JpaCompositeId.T.create(globalId);
		result.setColumns(asList( //
				hbmColumn(globalId, "id_long", "long"), //
				hbmColumn(globalId, "id_string", "string") //
		));
		return result;
	}

	public static JpaColumn hbmColumn(String globalIdPrefix, String name, String type) {
		JpaColumn result = JpaColumn.T.create(globalIdPrefix + ":" + name);
		result.setName(name);
		result.setType(type);

		return result;
	}

	public static MetaData compositeIdMapping_XmlSnippet() {
		PropertyMapping result = PropertyMapping.T.create("hbm:ComposuteIdEntity#id");
		// @formatter:off
		result.setXml(
			"<composite-id name=\"id\" class=\"com.braintribe.model.access.hibernate.gm.CompositeIdValues\">" +
				"<key-property name=\"value0\" column=\"id_long\" type=\"long\" />" +
				"<key-property name=\"value1\" column=\"id_string\" type=\"string\" />" +
			"</composite-id>");
		// @formatter:on
		return result;
	}

	public static PropertyMapping propertyMapping(String globalIdPrefix, String columnName, String type) {
		PropertyMapping result = PropertyMapping.T.create(globalIdPrefix + ":" + columnName);
		result.setColumnName(columnName);
		result.setType(type);

		return result;
	}


}
