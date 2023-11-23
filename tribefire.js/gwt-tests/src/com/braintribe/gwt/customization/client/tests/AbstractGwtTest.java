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
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.customization.client.tests.model.keyword.KeywordEntity;
import com.braintribe.gwt.logging.client.ExceptionUtil;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.dom.client.Style.Unit;

/**
 * @author peter.gazdik
 */
public abstract class AbstractGwtTest {

	public static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public void run() {
		log("Running: " + this.getClass().getSimpleName());

		try {
			tryRun();

		} catch (Exception e) {
			logError("error: " + e.getMessage(), e);
		}

		logSeparator();
	}

	protected void tryRun() throws Exception {
		throw new UnsupportedOperationException("Method 'tryRun' (or just the 'run') should be implemented by sub-type!");
	}

	// ##########################################
	// ## . . . . . . Util methods . . . . . . ##
	// ##########################################

	protected EntityType<?> getDynamicCounterpart(EntityType<KeywordEntity> type) {
		return GMF.getTypeReflection().getEntityType(makeSignatureDynamic(type.getTypeSignature()));
	}

	protected EnumType getDynamicCounterpart(EnumType type) {
		return GMF.getTypeReflection().getEnumType(makeSignatureDynamic(type.getTypeSignature()));
	}

	protected void makeSignaturesDynamic(GmMetaModel metaModel) {
		makeSignaturesDynamic(metaModel, true);
	}

	protected void makeSignaturesDynamic(GmMetaModel metaModel, boolean alsoEnums) {
		for (GmType gmType : nullSafe(metaModel.getTypes())) {
			if (gmType.isGmEntity() || (alsoEnums && gmType.isGmEnum())) {
				String ts = gmType.getTypeSignature();
				if (ts.startsWith("com.braintribe.gwt.customization.client.tests.model") && !ts.contains("non_dynamic"))
					gmType.setTypeSignature(makeSignatureDynamic(ts));

				if (alsoEnums)
					if (gmType.isGmEntity())
						makeInitializersSignaturesDynamic((GmEntityType) gmType);
			}
		}
	}

	private void makeInitializersSignaturesDynamic(GmEntityType t) {
		makeInitializersSignaturesDynamic(t.getProperties());
		makeInitializersSignaturesDynamic(t.getPropertyOverrides());
	}

	private void makeInitializersSignaturesDynamic(List<? extends GmPropertyInfo> ps) {
		for (GmPropertyInfo p : ps) {
			Object i = p.getInitializer();
			if (i instanceof EnumReference) {
				EnumReference oldEr = (EnumReference) i;
				EnumReference newEr = EnumReference.T.create();
				newEr.setTypeSignature(makeSignatureDynamic(oldEr.getTypeSignature()));
				newEr.setConstant(oldEr.getConstant());
				p.setInitializer(newEr);
			}
		}
	}

	protected String makeSignatureDynamic(String ts) {
		int lastDot = ts.lastIndexOf(".");
		return ts.substring(0, lastDot) + ".dynamic" + ts.substring(lastDot);
	}

	protected void ensureModelTypes(GmMetaModel metaModel) throws GmfException {
		typeReflection.deploy(metaModel);
	}

	protected GmEntityType createDynamicSubType(GmEntityType gmEntityType) {
		GmEntityType subType = copy(gmEntityType);
		subType.setTypeSignature(makeSignatureDynamic(gmEntityType.getTypeSignature()));
		subType.setSuperTypes(asList(gmEntityType));

		return subType;
	}

	protected Map<String, GmEntityType> indexEntityTypes(GmMetaModel metaModel) {
		Map<String, GmEntityType> result = newMap();

		for (GmType gmType : metaModel.getTypes()) {
			if (gmType.isGmEntity()) {
				result.put(gmType.getTypeSignature(), (GmEntityType) gmType);
			}
		}

		return result;
	}

	protected GmEntityType copy(GmEntityType gmEntityType) {
		GmEntityType result = MetaModelBuilder.entityType(gmEntityType.getTypeSignature());
		result.setTypeSignature(gmEntityType.getTypeSignature());
		result.setSuperTypes(gmEntityType.getSuperTypes());
		result.setDeclaringModel(gmEntityType.getDeclaringModel());
		result.setIsAbstract(gmEntityType.getIsAbstract());
		result.setProperties(new ArrayList<GmProperty>());

		return result;
	}

	@SafeVarargs
	protected static GmMetaModel modelForTypes(EntityType<? extends GenericEntity>... types) {
		return new NewMetaModelGeneration().buildMetaModel("gm:Gwt27Model", asList(types));
	}

	protected static <T> T cast(Object o) {
		return (T) o;
	}

	// ##########################################
	// ## . . . . . Assertions . . . . . . . . ##
	// ##########################################

	protected void assertNotNull(Object o, String descriptor) {
		if (o == null)
			throw new RuntimeException("Object is null: " + descriptor);
	}

	// ##########################################
	// ## . . . . . . . tf.js . . . . . . . . .##
	// ##########################################

	protected final native JavaScriptObject getJsProperty(JavaScriptObject jso, String name) /*-{
		return jso[name];
	}-*/;

	protected final native <T> T getJsPropertyCasted(JavaScriptObject jso, String name) /*-{
		return jso[name];
	}-*/;

	// ##########################################
	// ## . . . . . . . Printing . . . . . . . ##
	// ##########################################

	protected void logSeparator() {
		Document document = Document.get();
		document.getBody().appendChild(document.createHRElement());
	}

	protected void log(String msg) {
		Document document = Document.get();
		PreElement preElement = document.createPreElement();
		preElement.appendChild(document.createTextNode(msg));
		preElement.getStyle().setMargin(0, Unit.PX);
		document.getBody().appendChild(preElement);
	}

	protected void logError(String msg, Throwable t) {
		Document document = Document.get();
		PreElement preElement = document.createPreElement();
		preElement.getStyle().setColor("red");
		preElement.getStyle().setMargin(0, Unit.PX);
		preElement.appendChild(document.createTextNode(msg + "\n" + ExceptionUtil.format(t)));
		document.getBody().appendChild(preElement);
	}

	protected void logError(String msg) {
		Document document = Document.get();
		PreElement preElement = document.createPreElement();
		preElement.getStyle().setColor("red");
		preElement.getStyle().setMargin(0, Unit.PX);
		preElement.appendChild(document.createTextNode(msg));
		document.getBody().appendChild(preElement);
	}

}
