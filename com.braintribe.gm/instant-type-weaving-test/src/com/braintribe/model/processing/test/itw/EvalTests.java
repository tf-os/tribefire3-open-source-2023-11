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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.lang.reflect.Method;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.entity.TestEntity;
import com.braintribe.model.processing.test.itw.entity.eval.EvalServiceRequest;
import com.braintribe.model.processing.test.itw.entity.eval.EvalServiceRequest2;
import com.braintribe.model.processing.test.itw.entity.eval.EvalServiceRequest3;
import com.braintribe.model.processing.test.itw.entity.eval.EvalServiceRequestLRSub;
import com.braintribe.model.processing.test.itw.entity.eval.EvalServiceRequestSub;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @author peter.gazdik
 */
public class EvalTests extends ImportantItwTestSuperType {

	public static final String EVAL_VALUE = "Eval value";

	@Test
	public void entityType_evaluatesTo() {
		BtAssertions.assertThat(EvalServiceRequest.T.getEvaluatesTo()).isSameAs(SimpleType.TYPE_STRING);
		BtAssertions.assertThat(EvalServiceRequest.T.getEffectiveEvaluatesTo()).isSameAs(SimpleType.TYPE_STRING);
	}

	@Test
	public void entityType_effectiveEvaluatesTo_Sub() {
		BtAssertions.assertThat(EvalServiceRequestSub.T.getEffectiveEvaluatesTo()).isSameAs(SimpleType.TYPE_STRING);
	}

	@Test
	public void entityType_effectiveEvaluatesTo_Sub_MultiInherit() {
		BtAssertions.assertThat(EvalServiceRequestLRSub.T.getEffectiveEvaluatesTo()).isSameAs(TestEntity.T);
	}

	@Test
	public void checkWithExpresssiveEntity() {
		EvalServiceRequest entity = EvalServiceRequest.T.create();
		EvalContext<String> context = entity.eval(universalStringEvaluator);

		BtAssertions.assertThat(context.get()).isEqualTo(EVAL_VALUE);
	}

	@Test
	public void checkWithExpresssiveEntity2() {
		GenericModelType etType = EvalServiceRequest2.T.getEvaluatesTo();
		BtAssertions.assertThat(etType instanceof SetType).isTrue();

		SetType setType = etType.cast();
		BtAssertions.assertThat(setType.getCollectionElementType()).isSameAs(EvalServiceRequest.T);
	}

	@Test
	public void checkWithExpresssiveEntity3() {
		GenericModelType etType = EvalServiceRequest3.T.getEvaluatesTo();
		BtAssertions.assertThat(etType instanceof SetType).isTrue();

		SetType setType = etType.cast();
		BtAssertions.assertThat(setType.getCollectionElementType()).isSameAs(EvalServiceRequest.T);
	}

	@Test
	public void checkWithModelOnlyEntity() throws Exception {
		GmMetaModel gmMetaModel = new NewMetaModelGeneration().buildMetaModel("gm:ItwTest", asSet(EvalServiceRequest.T));

		for (GmType gmType : gmMetaModel.getTypes()) {
			if (!gmType.isGmEntity())
				continue;

			GmEntityType gmEntityType = (GmEntityType) gmType;
			String altSignature = alterSignature(gmEntityType.getTypeSignature());
			gmEntityType.setTypeSignature(altSignature);
		}

		gmMetaModel.deploy();

		EntityType<?> et = GMF.getTypeReflection().getEntityType(alterSignature(EvalServiceRequest.class.getName()));

		Method evalMethod = et.getJavaType().getDeclaredMethod("eval", Evaluator.class);
		BtAssertions.assertThat(evalMethod).isNotNull(); // not really needed, but this is the intention
		BtAssertions.assertThat(et.getEvaluatesTo()).isEqualTo(SimpleType.TYPE_STRING);
	}

	private String alterSignature(String typeSignature) {
		return typeSignature.replace(".itw.", ".itw.alt.");
	}

	private static final Evaluator<EvalServiceRequest> universalStringEvaluator = EvalTests::evalToString;

	private static <T> EvalContext<T> evalToString(GenericEntity evaluable) {
		return new EvalContext<T>() {
			@Override
			public T get() throws EvalException {
				return (T) EVAL_VALUE;
			}

			@Override
			public void get(AsyncCallback<? super T> callback) {
				throw new UnsupportedOperationException("Method not supported! Entity: " + evaluable);
			}
			
			@Override
			public <U, AA extends EvalContextAspect<? super U>> EvalContext<T> with(Class<AA> aspect, U value) {
				return this;
			}
		};
	}
}
