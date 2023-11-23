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
package tribefire.extension.okta.templates.wire.space;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.okta.templates.util.NameShortening;
import tribefire.extension.okta.templates.wire.contract.OktaDbMappingsContract;

@Managed
public class OktaDbMappingsSpace extends AbstractInitializerSpace implements OktaDbMappingsContract {

	@Import
	private WireContext<?> wireContext;

	@Managed
	@Override
	public EntityMapping forceMapping() {
		EntityMapping bean = create(EntityMapping.T);
		bean.setForceMapping(true);
		return bean;
	}

	@Managed
	@Override
	public PropertyMapping clobProperty() {
		PropertyMapping bean = create(PropertyMapping.T);
		bean.setType("materialized_clob");
		return bean;
	}

	@Managed
	@Override
	public MaxLength maxLen2k() {
		MaxLength bean = create(MaxLength.T);
		bean.setLength(2000);
		return bean;
	}

	@Managed
	@Override
	public MaxLength maxLen4k() {
		MaxLength bean = create(MaxLength.T);
		bean.setLength(4000);
		return bean;
	}

	@Managed
	@Override
	public MaxLength maxLen10Meg() {
		MaxLength bean = create(MaxLength.T);
		bean.setLength(10_000_000);
		return bean;
	}

	@Override
	@Managed
	public MaxLength maxLen2GigProperty() {
		MaxLength bean = create(MaxLength.T);
		bean.setLength(Integer.MAX_VALUE);
		return bean;
	}

	@Override
	@Managed
	public MaxLength maxLen1k() {
		MaxLength maxLength = create(MaxLength.T);
		maxLength.setLength(1024);
		return maxLength;
	}

	@Override
	@Managed
	public EntityMapping entityUnmapped() {
		EntityMapping bean = create(EntityMapping.T);

		bean.setMapToDb(false);

		return bean;
	}

	// Helpers

	@Managed(Scope.prototype)
	private PropertyMapping lookupIndex(EntityType<?> entityType, String propertyName) {
		PropertyMapping bean = create(PropertyMapping.T);
		// global id
		InstanceConfiguration currentInstance = wireContext.currentInstancePath().lastElement().config();
		InstanceQualification instanceQualification = currentInstance.qualification();
		String prefix = initializerSupport.initializerId();

		String propertySignature = entityType.getTypeSignature() + "/" + propertyName;

		String indexName = NameShortening.shorten(propertySignature, propertyName);

		bean.setGlobalId("wire://" + prefix + "/" + instanceQualification.space().getClass().getSimpleName() + "/" + instanceQualification.name()
				+ "/" + indexName);

		bean.setIndex(indexName);
		return bean;
	}

	@Override
	public void applyIndex(ModelMetaDataEditor editor, EntityType<?> entityType, String propertyName) {
		editor.onEntityType(entityType).addPropertyMetaData(propertyName, lookupIndex(entityType, propertyName));
	}

	@Override
	public void applyIndices(ModelMetaDataEditor editor, EntityType<?> entityType, String... propertyNames) {
		EntityTypeMetaDataEditor onEntityType = editor.onEntityType(entityType);

		for (String propertyName : propertyNames) {
			onEntityType.addPropertyMetaData(propertyName, lookupIndex(entityType, propertyName));
		}
	}

}
