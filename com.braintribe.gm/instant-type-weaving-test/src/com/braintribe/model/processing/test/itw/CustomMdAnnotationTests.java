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
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertEntity;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.MdaRegistry;
import com.braintribe.model.generic.annotation.meta.api.MetaDataAnnotations;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.test.itw.entity.annotation.CustomMd;
import com.braintribe.model.processing.test.itw.entity.annotation.CustomMd_Annotation;
import com.braintribe.model.processing.test.itw.entity.annotation.CustomPredicate;
import com.braintribe.model.processing.test.itw.entity.annotation.CustomPredicate_Annotation;
import com.braintribe.model.processing.test.itw.entity.annotation.HasCustomMdProperties;

/**
 * Tests for custom MD annotations registered via a text file on classpath.
 * <p>
 * Currently only {@link Predicate}s are supported.
 * <p>
 * 
 * @see MdaHandler
 * @see CustomPredicate_Annotation
 */
public class CustomMdAnnotationTests extends ImportantItwTestSuperType {

	private static JavaTypeAnalysis jta = new JavaTypeAnalysis();
	private static GmEntityType gmType = (GmEntityType) jta.getGmType(HasCustomMdProperties.class);

	@Test
	public void customMd() {
		assertMdaRegistry(CustomPredicate_Annotation.class, CustomPredicate.T);
		assertMdaRegistry(CustomMd_Annotation.class, CustomMd.T);

		{
			CustomPredicate md = assertMdaUsedInJta("predicate", CustomPredicate.T);
			assertThat(md.getGlobalId()).isEqualTo("gid-predicate");
		}

		{
			CustomMd md = assertMdaUsedInJta("md", CustomMd.T);
			assertThat(md.getLength()).isEqualTo(42);
			assertThat(md.getName()).isEqualTo("DEFAULT_NAME");
		}

		{
			CustomMd md = assertMdaUsedInJta("mdWithName", CustomMd.T);
			assertThat(md.getLength()).isEqualTo(182);
			assertThat(md.getName()).isEqualTo("CUSTOM_NAME");
		}
	}

	private void assertMdaRegistry(Class<? extends Annotation> annoClass, EntityType<?> mdType) {
		MdaRegistry mdaRegistry = MetaDataAnnotations.registry();

		MdaHandler<?, ?> handler = mdaRegistry.annoToHandler().get(annoClass);
		assertThat(handler).isNotNull();

		assertThat(handler.annotationClass()).isSameAs(annoClass);
		assertThat(handler.metaDataClass()).isSameAs(mdType.getJavaType());
	}

	private <MD extends MetaData> MD assertMdaUsedInJta(String propertyName, EntityType<MD> mdType) {
		GmProperty joke = gmType.getProperties().stream() //
				.filter(p -> p.getName().equals(propertyName)) //
				.findFirst() //
				.get();

		Set<MetaData> mds = joke.getMetaData();
		assertThat(mds).hasSize(1);

		MetaData md = first(mds);
		assertEntity(md).isInstanceOf(mdType);

		return (MD) md;
	}

}
