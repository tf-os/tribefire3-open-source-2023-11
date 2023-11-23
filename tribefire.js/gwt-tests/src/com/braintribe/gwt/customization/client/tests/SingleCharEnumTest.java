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
import static com.braintribe.utils.lcd.CollectionTools2.removeFirst;

import com.braintribe.gwt.customization.client.tests.model.singleCharEnum.SingleCharEnum;
import com.braintribe.gwt.customization.client.tests.model.singleCharEnum.SingleCharEnumEntity;
import com.braintribe.gwt.genericmodel.client.reflect.TypePackage;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author peter.gazdik
 */
public class SingleCharEnumTest extends AbstractGwtTest {

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel);
		changeEnumToAllSingleChars(metaModel);
		ensureModelTypes(metaModel);

		assertOnlyConstantsInTfJs();
	}

	private void changeEnumToAllSingleChars(GmMetaModel model) {
		for (GmType gmType : model.getTypes())
			if (gmType.isGmEnum()) {
				GmEnumType get = (GmEnumType) gmType;
				GmEnumConstant prototype = removeFirst(get.getConstants());

				for (char c = 'a'; c <= 'z'; c++)
					addConstant(get, prototype, "" + c);
				for (char c = 'A'; c <= 'Z'; c++)
					addConstant(get, prototype, "" + c);

				addConstant(get, prototype, "equals");
				addConstant(get, prototype, "hashCode");
				addConstant(get, prototype, "getTypeSignature");
				addConstant(get, prototype, "getJavaType");
			}
	}

	private void addConstant(GmEnumType get, GmEnumConstant prototype, String name) {
		GmEnumConstant gmConstant = GmReflectionTools.makeShallowCopy(prototype);
		gmConstant.setName(name);
		get.getConstants().add(gmConstant);
	}

	private GmMetaModel generateModel() {
		log("generating meta model");

		return new NewMetaModelGeneration().buildMetaModel("test.gwt27.SingleCharEnumrModel", asList(SingleCharEnumEntity.T));
	}

	private void assertOnlyConstantsInTfJs() {
		String dynamicSignature = makeSignatureDynamic(SingleCharEnum.class.getName());
		int lastDot = dynamicSignature.lastIndexOf(".");
		String dynamicPackage = dynamicSignature.substring(0, lastDot);

		JavaScriptObject pakcageJsObject = TypePackage.acquireJsObjectForPackagePath(dynamicPackage);
		assertNotNull(pakcageJsObject, "jsObject for SingleCharEnum package");

		JavaScriptObject jsObject = getJsProperty(pakcageJsObject, SingleCharEnum.class.getSimpleName());
		assertNotNull(jsObject, "jsObject for SingleCharEnum");

		for (char c = 'a'; c <= 'z'; c++)
			doTest(jsObject, "" + c);
		for (char c = 'A'; c <= 'Z'; c++)
			doTest(jsObject, "" + c);

		log("All seems to be OK");
	}

	private void doTest(JavaScriptObject jsObject, String name) {
		JavaScriptObject dynamicConstant = getJsProperty(jsObject, name);
		assertNotNull(dynamicConstant, "constant SingleCharEnum." + name);
	}

}
