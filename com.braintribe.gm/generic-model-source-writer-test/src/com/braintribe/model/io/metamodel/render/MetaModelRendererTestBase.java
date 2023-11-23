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
package com.braintribe.model.io.metamodel.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Before;

import com.braintribe.model.io.metamodel.testbase.MetaModelBuilder;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.utils.StringTools;

public abstract class MetaModelRendererTestBase {

	protected MetaModelRenderer metaModelRenderer;

	protected String currentCode;

	private final SourceWriterContext context = new SourceWriterContext();

	@Before
	public void setUp() throws Exception {
		GmMetaModel gmMetaModel = new MetaModelBuilder().buildMetaModel();
		context.modelOracle = new BasicModelOracle(gmMetaModel);
		metaModelRenderer = new MetaModelRenderer(context);
	}

	protected String renderEntityType(GmEntityType gmEntityType) {
		return metaModelRenderer.renderEntityType(gmEntityType).sourceCode;
	}

	protected String renderEnumType(GmEnumType gmEnumType) {
		return metaModelRenderer.renderEnumType(gmEnumType).sourceCode;
	}

	protected void assertHasAnnotation(Class<? extends Annotation> annotation) {
		assertContainsSubstring("@" + annotation.getSimpleName());
		assertContainsSubstring("import " + annotation.getName());
	}

	protected void assertHasAnnotation(Class<? extends Annotation> annotation, String value) {
		assertContainsSubstring("@" + annotation.getSimpleName() + "(\"" + value + "\")");
		assertContainsSubstring("import " + annotation.getName());
	}

	protected void assertHasNoAnnotation(Class<? extends Annotation> annotation) {
		assertThat(currentCode.contains(annotation.getSimpleName())).as("Annotation should not be present: " + annotation).isFalse();
	}

	protected void assertHasProperty(String capitalizedName, String type) {
		String name = StringTools.uncapitalize(capitalizedName);
		
		assertContainsSubstring("String " + name + " = \"" + name + "\";");
		assertContainsSubstring("void set" + capitalizedName + "(" + type + " " + name + ");");
		assertContainsSubstring(type + " get" + capitalizedName + "()");
	}

	protected void assertHasEscapedProperty(String capitalizedName, String type, String literalName, String setterParamName) {
		String name = StringTools.uncapitalize(capitalizedName);

		assertContainsSubstring("String " + literalName + " = \"" + name + "\";");
		assertContainsSubstring("void set" + capitalizedName + "(" + type + " " + setterParamName + ");");
		assertContainsSubstring(type + " get" + capitalizedName + "()");
	}

	protected void assertDoesntHaveProperty(String name, String type) {
		assertNotContains("void set" + name + "(" + type + " value);");
		assertNotContains(type + " get" + name + "()");
	}

	protected void assertContainsSubstrings(String... strings) {
		for (String s : strings) {
			assertContainsSubstring(s);
		}
	}

	protected void assertContainsSubstring(String s) {
		assertThat(currentCode).contains(s);
	}

	protected void assertNotContains(String s) {
		assertThat(currentCode).doesNotContain(s);
	}

	protected GmEntityType getTypeBySignature(String signature) {
		return context.modelOracle.getEntityTypeOracle(signature).asGmType();
	}

	protected GmEnumType getEnumBySignature(String signature) {
		return context.modelOracle.getEnumTypeOracle(signature).asGmType();
	}

	protected void printCode() {
		System.out.println(currentCode);
	}

}
