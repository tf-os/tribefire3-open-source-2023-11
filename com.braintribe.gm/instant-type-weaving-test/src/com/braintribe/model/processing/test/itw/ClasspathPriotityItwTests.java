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
package com.braintribe.model.processing.test.itw;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.test.itw.entity.classpath_priority.EntityOnClassPath;
import com.braintribe.model.processing.test.itw.entity.classpath_priority.EntityOnClassPathRefererXyz;
import com.braintribe.model.processing.test.itw.entity.classpath_priority.SubNotOnClassPathXyz;
import com.braintribe.model.processing.test.itw.entity.classpath_priority.SuperEntityOnClassPath;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.StringTools;

/**
 * Tests that an entity type is woven from a classpath when a GmEntityType with same typeSignature but different structure is passed to ITW.
 * 
 * @author peter.gazdik
 */
public class ClasspathPriotityItwTests {

	private String nonCpSignature = null;

	@Test
	public void classpathIsPriority_Property() {
		GmMetaModel model = weaveXyzModel_Property();

		removeXyzSuffigFromModelTypes_Property(model);

		model.deploy();

		assertClasspathTypeStillOk_Property();
	}

	private GmMetaModel weaveXyzModel_Property() {
		GmMetaModel model = new NewMetaModelGeneration().buildMetaModel("test:classpath-conflicting-model", asList(EntityOnClassPathRefererXyz.T));
		assertThat(model.getTypes()).hasSize(2);
		return model;
	}

	private void removeXyzSuffigFromModelTypes_Property(GmMetaModel model) {
		for (GmType gmType : model.getTypes()) {
			String ts = gmType.getTypeSignature();
			assertThat(ts).endsWith("Xyz");

			String newTs = StringTools.removeSuffix(ts, "Xyz");
			assertThat(newTs).doesNotEndWith("Xyz");

			gmType.setTypeSignature(newTs);

			if (newTs.contains("Referer")) {
				nonCpSignature = newTs;

			} else {
				assertThat(newTs).isEqualTo(EntityOnClassPath.class.getName());

				// Chock property being woven is "nonCpName"

				List<GmProperty> gmProperties = ((GmEntityType) gmType).getProperties();
				assertThat(gmProperties).hasSize(1);

				GmProperty prop = first(gmProperties);
				assertThat(prop.getName()).isEqualTo("nonCpName");
			}
		}
	}

	private void assertClasspathTypeStillOk_Property() {
		EntityType<?> refererType = GMF.getTypeReflection().getType(nonCpSignature);

		GenericModelType type = refererType.getProperty("entity").getType();
		assertThat(type).isInstanceOf(EntityType.class);

		// Chock property actually woven is "cpName"

		EntityType<?> entityType = (EntityType<?>) type;
		List<Property> properties = entityType.getDeclaredProperties();

		assertThat(properties).hasSize(1);
		assertThat(first(properties).getName()).isEqualTo("cpName");
	}

	@Test
	public void classpathIsPriority_Super() {
		GmMetaModel model = weaveXyzModel_Super();

		removeXyzSuffigFromModelTypes_Super(model);

		model.deploy();

		assertClasspathTypeStillOk_Super();
	}

	private GmMetaModel weaveXyzModel_Super() {
		GmMetaModel model = new NewMetaModelGeneration().buildMetaModel("test:classpath-conflicting-model", asList(SubNotOnClassPathXyz.T));
		assertThat(model.getTypes()).hasSize(2);
		return model;
	}

	private void removeXyzSuffigFromModelTypes_Super(GmMetaModel model) {
		GmType superType = null;
		GmType subType = null;

		for (GmType gmType : model.getTypes()) {
			String ts = gmType.getTypeSignature();
			assertThat(ts).endsWith("Xyz");

			String newTs = StringTools.removeSuffix(ts, "Xyz");
			assertThat(newTs).doesNotEndWith("Xyz");

			gmType.setTypeSignature(newTs);

			if (newTs.contains("SubNotOnClassPath")) {
				nonCpSignature = newTs;
				subType = gmType;

			} else {
				superType = gmType;

				assertThat(newTs).isEqualTo(SuperEntityOnClassPath.class.getName());

				// Chock property being woven is "nonCpName"

				List<GmProperty> gmProperties = ((GmEntityType) gmType).getProperties();
				assertThat(gmProperties).hasSize(1);

				GmProperty prop = first(gmProperties);
				assertThat(prop.getName()).isEqualTo("nonCpName");
			}
		}

		model.setTypes(asLinkedSet(subType, superType));
	}

	private void assertClasspathTypeStillOk_Super() {
		EntityType<?> subType = GMF.getTypeReflection().getType(nonCpSignature);

		List<EntityType<?>> superTypes = subType.getSuperTypes();
		assertThat(superTypes).hasSize(1);

		EntityType<?> superType = first(superTypes);

		// Chock property actually woven is "cpName"

		List<Property> properties = superType.getDeclaredProperties();

		assertThat(properties).hasSize(1);
		assertThat(first(properties).getName()).isEqualTo("cpName");
	}
}
