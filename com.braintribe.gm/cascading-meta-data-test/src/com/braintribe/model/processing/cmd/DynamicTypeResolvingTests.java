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
package com.braintribe.model.processing.cmd;

import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;

import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.proxy.DynamicProperty;
import com.braintribe.model.generic.proxy.ProxyEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.cmd.test.provider.RawModelProvider;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;

/**
 * Tests for MD defined on {@link DynamicEntityType} and it's {@link DynamicProperty properties}.
 */
public class DynamicTypeResolvingTests extends MetaDataResolvingTestBase {

	private static final String DYNAMIC_TYPE_SIGNATURE = "com.bt.DynamicType";

	private final DynamicEntityType entityType = new DynamicEntityType(DYNAMIC_TYPE_SIGNATURE);

	// #######################################
	// ## . . . . . Default value . . . . . ##
	// #######################################

	@Test
	public void entityMdForType() {
		entityType.getMetaData().add(Visible.T.create());

		assertVisible(getMetaData().entityType(entityType).meta(Visible.T).exclusive());
	}

	@Test
	public void entityMdForType_Extended() {
		entityType.getMetaData().add(Visible.T.create());

		assertVisibleExtended(getMetaData().entityType(entityType).meta(Visible.T).exclusiveExtended());
	}

	@Test
	public void entityMdForInstance() {
		entityType.getMetaData().add(Visible.T.create());
		ProxyEntity entity = entityType.create();

		assertVisible(getMetaData().entity(entity).meta(Visible.T).exclusive());
	}

	@Test
	public void entityMdForInstance_Extended() {
		entityType.getMetaData().add(Visible.T.create());
		ProxyEntity entity = entityType.create();

		assertVisibleExtended(getMetaData().entity(entity).meta(Visible.T).exclusiveExtended());
	}

	@Test
	public void propertyMd_DefinedOnEntityType() {
		entityType.getPropertyMetaData().add(Visible.T.create());

		assertVisible(getMetaData().entityType(entityType).property(GenericEntity.globalId).meta(Visible.T).exclusive());
	}

	@Test
	public void propertyMd_DefinedOnEntityType_Extended() {
		entityType.getPropertyMetaData().add(Visible.T.create());

		assertVisibleExtended(getMetaData().entityType(entityType).property(GenericEntity.globalId).meta(Visible.T).exclusiveExtended());
	}

	@Test
	public void propertyMd_DefinedOnProperty() {
		String propertyName = "dynamicProperty";

		DynamicProperty property = entityType.addProperty(propertyName, BaseType.INSTANCE);
		property.getMetaData().add(Visible.T.create());

		assertVisible(getMetaData().entityType(entityType).property(propertyName).meta(Visible.T).exclusive());
	}

	@Test
	public void propertyMd_DefinedOnProperty_Extended() {
		String propertyName = "dynamicProperty";

		DynamicProperty property = entityType.addProperty(propertyName, BaseType.INSTANCE);
		property.getMetaData().add(Visible.T.create());

		assertVisibleExtended(getMetaData().entityType(entityType).property(propertyName).meta(Visible.T).exclusiveExtended());
	}

	private void assertVisible(MetaData md) {
		assertThat(md).isExactly(Visible.T);
	}

	private void assertVisibleExtended(MdDescriptor md) {
		assertThat(md.getResolvedValue()).isExactly(Visible.T);
		assertThat(md.getOwnerModel()).isNull();
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new RawModelProvider();
	}

}
