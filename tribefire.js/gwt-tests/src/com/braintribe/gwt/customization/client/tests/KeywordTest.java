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
package com.braintribe.gwt.customization.client.tests;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.gwt.customization.client.tests.model.keyword.KeywordEntity;
import com.braintribe.gwt.customization.client.tests.model.keyword.KeywordEnum;
import com.braintribe.gwt.customization.client.tests.model.keyword.KeywordEnumOwner;
import com.braintribe.gwt.customization.client.tests.model.keyword.with.non_dynamic.KeywordPackageEntity;
import com.braintribe.gwt.genericmodel.client.reflect.TypePackage;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.context.JsKeywords;

/**
 * @author peter.gazdik
 */
public class KeywordTest extends AbstractGwtTest {

	private static final Set<String> jsKeywords = jsKeywordsWithout_Class();

	// class is omitted as that cannot be used as a property - getClass wouldn't work
	private static Set<String> jsKeywordsWithout_Class() {
		Set<String> result = newSet(JsKeywords.jsKeywords);
		result.remove("class");

		return result;
	}

	@Override
	protected void tryRun() throws GmfException {
		testKeywordPackage();

		testKeywordEntity(KeywordEntity.T);
		testKeywordEnum(KeywordEnum.arguments.type());

		deployDynamicModel();

		testKeywordEntity(getDynamicCounterpart(KeywordEntity.T));
		testKeywordEnum(getDynamicCounterpart(KeywordEnum.arguments.type()));
	}

	private void testKeywordPackage() {
		// this actually tests everything that's needed
		tfjcNewKpe();
		log("Tesing instantiation of entity with keyword in the package. [OK] ");
	}

	private native KeywordPackageEntity tfjcNewKpe() /*-{
		return $wnd.$T.com.braintribe.gwt.customization.client.tests.model.keyword.with_.non_dynamic.KeywordPackageEntity.create();
	}-*/;

	private void testKeywordEntity(EntityType<?> type) {
		log("Testing Keyword type: " + type.getTypeSignature());

		GenericEntity e = type.create();
		fillKeywordEntity(e);

		for (Property p : type.getProperties()) {
			if (p.isIdentifying() || p.isGlobalId())
				continue;

			Object v = p.get(e);
			if (p.getType() == SimpleTypes.TYPE_STRING) {
				if (!p.getName().equals(v))
					logError("Property: " + p.getName() + " has unexpected value: " + v);
			} else if (!(v instanceof KeywordPackageEntity))
				logError("Property: " + p.getName() + " has unexpected value: " + v);
		}
	}

	private native void fillKeywordEntity(GenericEntity e) /*-{
		e.arguments_ = "arguments";
		e.await_ = "await";
		e.break_ = "break";
		e.case_ = "case";
		e.catch_ = "catch";
		e.const_ = "const";
		e.continue_ = "continue";
		e.debugger_ = "debugger";
		e.default_ = "default";
		e.delete_ = "delete";
		e.do_ = "do";
		e.else_ = "else";
		e.enum_ = "enum";
		e.eval_ = "eval";
		e.export_ = "export";
		e.extends_ = "extends";
		e.false_ = "false";
		e.finally_ = "finally";
		e.for_ = "for";
		e.function_ = "function";
		e.if_ = "if";
		e.implements_ = "implements";
		e.import_ = "import";
		e.in_ = "in";
		e.instanceof_ = "instanceof";
		e.interface_ = "interface";
		e.let_ = "let";
		e.new_ = "new";
		e.null_ = "null";
		e.package_ = "package";
		e.private_ = "private";
		e.protected_ = "protected";
		e.prototype_ = "prototype";
		e.public_ = "public";
		e.return_ = "return";
		e.static_ = "static";
		e.super_ = "super";
		e.switch_ = "switch";
		e.this_ = "this";
		e.throw_ = "throw";
		e.true_ = "true";
		e.try_ = "try";
		e.typeof_ = "typeof";
		e.var_ = "var";
		e.void_ = "void";
		e.while_ = "while";
		e.with_ = "with";
		e.yield_ = "yield";
		e.yield__ = "yield_";
		e.keywordPackage = $wnd.$T.com.braintribe.gwt.customization.client.tests.model.keyword.with_.non_dynamic.KeywordPackageEntity.create();
	}-*/;

	private void testKeywordEnum(EnumType enumType) {
		log("Testing Keyword enum: " + enumType.getTypeSignature());

		JavaScriptObject typeJso = resolvTypeJso(enumType);

		for (Enum<?> e : enumType.getEnumValues()) {
			Enum<?> tfjsE = getJsPropertyCasted(typeJso, e.name() + "_");
			if (e != tfjsE)
				logError("Constant " + e.name() + " could not be resolved with name _" + e.name() + ". Actaully resolved: " + tfjsE);
		}
	}

	private JavaScriptObject resolvTypeJso(EnumType enumType) {
		JavaScriptObject jso = TypePackage.getRoot();

		for (String part : enumType.getTypeSignature().split("\\."))
			jso = getJsProperty(jso, part);

		return jso;
	}

	private void deployDynamicModel() {
		GmMetaModel metaModel = generateModel();
		makeSignaturesDynamic(metaModel);
		ensureModelTypes(metaModel);
	}

	private GmMetaModel generateModel() {
		log("generating meta model");

		return new NewMetaModelGeneration().buildMetaModel("test:KeywordModel", asList(KeywordEntity.T, KeywordEnumOwner.T));
	}

}
