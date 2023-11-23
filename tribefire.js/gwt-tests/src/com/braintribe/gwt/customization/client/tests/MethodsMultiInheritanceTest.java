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

import java.util.Date;

import com.braintribe.gwt.customization.client.tests.model.methodsMultiInheritance.BaseWithMethods;
import com.braintribe.gwt.customization.client.tests.model.methodsMultiInheritance.BaseWithProps;
import com.braintribe.gwt.customization.client.tests.model.methodsMultiInheritance.SubTypeWithPropsAndMethods;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * To really test this DO NOT USE DRAFT COMPILE OPTION!!!
 * 
 * @author peter.gazdik
 */
public class MethodsMultiInheritanceTest extends AbstractGwtTest {

	@Override
	protected void tryRun() throws GmfException {
		GmMetaModel metaModel = generateModel();

		makeSignaturesDynamic(metaModel);
		ensureModelTypes(metaModel);

		runEvals();
	}

	private GmMetaModel generateModel() {
		log("generating meta model");

		NewMetaModelGeneration mmg = new NewMetaModelGeneration();
		GmMetaModel baseModel = mmg.buildMetaModel("test.gwt.EvalBaseModel", asList(BaseWithMethods.T, BaseWithProps.T));
		GmMetaModel bothModel = mmg.buildMetaModel("test.gwt.EvalBothModel", asList(SubTypeWithPropsAndMethods.T), asList(baseModel));
		return bothModel;
	}

	private void runEvals() {
		log("Running eval tests");

		runEval(SubTypeWithPropsAndMethods.T);
	}

	private void runEval(EntityType<? extends BaseWithMethods> et) {
		runEvalHelper(et.getTypeSignature());
		runEvalHelper(makeSignatureDynamic(et.getTypeSignature()));
	}

	private void runEvalHelper(String typeSignature) {
		EntityType<? extends BaseWithMethods> et = typeReflection.getEntityType(typeSignature);
		BaseWithMethods instance = et.create();
		Evaluator<BaseWithMethods> evaluator = getEvaluator();

		log("Calling eval on: " + typeSignature);
		instance.eval(evaluator);

		log("Calling default method on: " + typeSignature);
		log(instance.print());
	}

	// private native void evalJs(GenericEntity instance, Evaluator<?> evaluator)
	// /*-{
	// instance.@Evaluable::eval(Lcom/braintribe/model/generic/eval/Evaluator;)(evaluator);
	// }-*/;

	private Evaluator<BaseWithMethods> getEvaluator() {
		if (new Date().getTime() < 10)
			return this::evaluatorImpl1;
		else
			return this::evaluatorImpl2;
	}

	<T> EvalContext<T> evaluatorImpl1(BaseWithMethods evaluable) {
		log("Evaluator called for: " + evaluable.entityType().getTypeSignature());
		return null;
	}

	<T> EvalContext<T> evaluatorImpl2(BaseWithMethods evaluable) {
		log("Evaluator called for: " + evaluable.entityType().getTypeSignature());
		return null;
	}

}
