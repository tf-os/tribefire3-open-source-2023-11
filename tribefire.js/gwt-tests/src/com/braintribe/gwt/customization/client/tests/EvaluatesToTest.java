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
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.sort;

import java.util.List;
import java.util.Set;

import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.BoundWildcardEvalEntity;
import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.EvalEntity;
import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.ListEvalEntity;
import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.SubTypeEvalRequest;
import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.SubTypeEvalResult;
import com.braintribe.gwt.customization.client.tests.model.evaluatesTo.UnboundWildcardEvalEntity;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class EvaluatesToTest extends AbstractGwtTest {

	private final NewMetaModelGeneration mmg = new NewMetaModelGeneration();

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel);
		ensureModelTypes(metaModel);

		testEvaluatesToSetCorrectly(EvalEntity.class.getName(), SimpleType.TYPE_STRING);
		testEvaluatesToSetCorrectly(UnboundWildcardEvalEntity.class.getName(), BaseType.INSTANCE);
		testEvaluatesToSetCorrectly(ListEvalEntity.class.getName(), GMF.getTypeReflection().getListType(SimpleType.TYPE_STRING));
		testEvaluatesToSetCorrectly(BoundWildcardEvalEntity.class.getName(), GenericEntity.T);

		metaModel = generateModelWithSubTypeAsResult(metaModel);
		makeSignaturesDynamic(metaModel);
		ensureModelTypes(metaModel);

		testEvaluatesToSetCorrectly_Dynamic(SubTypeEvalRequest.class.getName(), SubTypeEvalResult.T);
	}

	private void testEvaluatesToSetCorrectly(String typeSignature, GenericModelType type) {
		testEvaluatesToHelper(typeSignature, type);
		testEvaluatesToHelper(makeSignatureDynamic(typeSignature), type);
	}

	private void testEvaluatesToSetCorrectly_Dynamic(String typeSignature, EntityType<?> type) {
		testEvaluatesToHelper(typeSignature, type);
		testEvaluatesToHelper(makeSignatureDynamic(typeSignature), typeReflection.getEntityType(makeSignatureDynamic(type.getTypeSignature())));
	}

	private void testEvaluatesToHelper(String typeSignature, GenericModelType type) {
		EntityType<GenericEntity> et = typeReflection.getEntityType(typeSignature);

		if (et.getEvaluatesTo() != type)
			logError("Wrong evaluatesTo for: " + typeSignature + " Expected: " + type + ", actual: " + et.getEvaluatesTo());
		else
			log("    evaluatesTo for: '" + typeSignature + "' [OK]");
	}

	private GmMetaModel generateModel() {
		log("generating model");

		return mmg.buildMetaModel("test.gwt.EvaluatesToTestModel",
				asList(EvalEntity.T, BoundWildcardEvalEntity.T, ListEvalEntity.T, UnboundWildcardEvalEntity.T));
	}

	private GmMetaModel generateModelWithSubTypeAsResult(GmMetaModel parentModel) {
		log("generating model with sub-type as eval result");

		GmMetaModel result = mmg.buildMetaModel("test.gwt.ExtendedEvaluatesToTestModel", asList(SubTypeEvalRequest.T), asList(parentModel));
		// Ordering by signature means order is: SubTypeEvalRequest, SubTypeEvalResult
		result.setTypes(sortBySignature(result.getTypes()));

		return result;
	}

	private Set<GmType> sortBySignature(Set<GmType> types) {
		List<GmType> typesInRequestResultOrder = sort(types, (et1, et2) -> et1.getTypeSignature().compareTo(et2.getTypeSignature()));

		return newLinkedSet(typesInRequestResultOrder);
	}

}
