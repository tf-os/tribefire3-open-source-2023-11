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
package com.braintribe.model.util.meta;

import static com.braintribe.model.util.meta.model.AnnotatedEntity.confidential;
import static com.braintribe.model.util.meta.model.AnnotatedEntity.initialized;
import static com.braintribe.model.util.meta.model.AnnotatedEntity.initializedToOverride;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.index;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.util.meta.model.AnnotatedEntity;
import com.braintribe.model.util.meta.model.AnnotatedEntitySub;

/**
 * Tests for {@link NewMetaModelGeneration}.
 */
public class NewMmgTest {

	private GmMetaModel model;

	@Test
	public void checkModel() {
		model = new NewMetaModelGeneration().buildMetaModel("mmg:TestModel", asList(AnnotatedEntitySub.T));

		assertThat(model).isNotNull();
		assertThat(model.getTypes()).hasSize(2);

		assertAnnotatedEntity();
		assertAnnotatedEntitySub();
	}

	private void assertAnnotatedEntity() {
		GmEntityType gmet = getGmEntityType(AnnotatedEntity.T);

		assertThat(gmet.getDeclaringModel()).isSameAs(model);

		Map<String, GmProperty> nameToProp = index(gmet.getProperties()).by(GmProperty::getName).unique();
		assertThat(nameToProp).containsKeys(initialized, initializedToOverride, confidential);

		GmProperty iP = nameToProp.get(initialized);
		GmProperty itoP = nameToProp.get(initializedToOverride);
		GmProperty confP = nameToProp.get(confidential);

		assertInitializer(iP, "I1");
		assertInitializer(itoP, "ITO");
		assertConfidential(confP);
	}

	private void assertAnnotatedEntitySub() {
		GmEntityType gmet = getGmEntityType(AnnotatedEntitySub.T);

		assertThat(gmet.getDeclaringModel()).isSameAs(model);

		Map<String, GmPropertyOverride> nameToProp = index(gmet.getPropertyOverrides()).by(gmp -> gmp.relatedProperty().getName()).unique();
		assertThat(nameToProp).containsKeys(initializedToOverride);

		GmPropertyOverride itoP = nameToProp.get(initializedToOverride);

		assertInitializer(itoP, "SUB-ITO");
	}

	private void assertInitializer(GmPropertyInfo p, String value) {
		assertThat(p.getInitializer()).as("Wrong initialzer for: " + p.nameWithOrigin()).isEqualTo(value);
	}

	private void assertConfidential(GmPropertyInfo p) {
		assertThat(findMd(p, Confidential.T)).as("Property is not confidential: " + p.nameWithOrigin()).isNotNull();
	}

	private <T extends MetaData> T findMd(HasMetaData mdOwner, EntityType<T> et) {
		return (T) mdOwner.getMetaData().stream() //
				.filter(et::isInstance) //
				.peek(md -> assertThat(md.getSelector()).isNull()) //
				.findFirst() //
				.orElse(null);
	}

	private GmEntityType getGmEntityType(EntityType<?> et) {
		return model.entityTypes() //
				.filter(gmEt -> gmEt.getTypeSignature().equals(et.getTypeSignature())) //
				.findFirst() //
				.orElseThrow(() -> new IllegalStateException("No GmEnttiyType found for: " + et.getTypeSignature()));
	}

}
