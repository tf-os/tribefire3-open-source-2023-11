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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbedded;
import com.braintribe.model.accessdeployment.jpa.meta.JpaPropertyMapping;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.PropertyHint;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;

/**
 * Descriptor for an embedded property.
 * 
 * @author peter.gazdik
 */
public class ComponentDescriptor extends PropertyDescriptor {

	private final JpaEmbedded jpaEmbedded;
	private final List<PropertyDescriptor> embeddedProperties;

	public ComponentDescriptor(HbmXmlGenerationContext context, EntityDescriptor descriptor, GmProperty gmProperty,
			PropertyDescriptorMetaData metaData) {
		super(context, descriptor, gmProperty, metaData);

		this.jpaEmbedded = (JpaEmbedded) metaData.jpaPropertyMapping;
		this.embeddedProperties = createEmbeddedProperties();
	}

	private List<PropertyDescriptor> createEmbeddedProperties() {
		List<PropertyDescriptor> result = newList();
		GmEntityType embeddableType = (GmEntityType) gmProperty.getType();
		EntityTypeOracle embeddableTypeOracle = context.getModelOracle().findEntityTypeOracle(embeddableType);

		Map<String, JpaPropertyMapping> embeddedPropertyMappings = jpaEmbedded.getEmbeddedPropertyMappings();

		for (Entry<String, JpaPropertyMapping> entry : embeddedPropertyMappings.entrySet()) {
			String propertyName = entry.getKey();
			GmProperty gmProperty = embeddableTypeOracle.getProperty(propertyName).asGmProperty();

			PropertyDescriptorMetaData embeddedMetaData = new PropertyDescriptorMetaData();
			embeddedMetaData.jpaPropertyMapping = entry.getValue();

			PropertyDescriptor embeddedPropertyDescriptor = PropertyDescriptor.create(context, gmProperty, entityDescriptor, embeddedMetaData);
			result.add(embeddedPropertyDescriptor);
		}

		return result;
	}

	/** See the remark on the super-implementation. */
	@Override
	protected PropertyHint resolvePropertyHint(GmProperty gmProperty, EntityDescriptor descriptor) {
		return null;
	}

	@Override
	public boolean getIsEmbedded() {
		return true;
	}

	public String getIdPrefix() {
		return gmProperty.getType().getTypeSignature() + "#" + gmProperty.getName() + "#";
	}

	public String getOwnerIdQuotedColumnName() {
		PropertyDescriptor idProperty = entityDescriptor.getIdProperty();
		return Optional.ofNullable(idProperty.getQuotedColumnName()).orElse("id");
	}

	public List<PropertyDescriptor> getEmbeddedProperties() {
		return embeddedProperties;
	}

}
