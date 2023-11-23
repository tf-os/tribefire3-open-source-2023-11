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
package com.braintribe.model.processing.oracle;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.BasicEntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.processing.oracle.model.basic.mammal.Mammal;
import com.braintribe.model.processing.oracle.model.evaluable.GenericEntityEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.GenericEntityEvaluable2;
import com.braintribe.model.processing.oracle.model.evaluable.MammalEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.MammalPetEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.ObjectEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.StringEvaluable;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EntityTypeOracle
 * @see BasicEntityTypeOracle
 * 
 * @author peter.gazdik
 */
public class BasicEntityTypeOracle_Eval_Test {

	protected static GmMetaModel evalModel = ModelOracleModelProvider.evalModel();
	protected static ModelOracle oracle = new BasicModelOracle(evalModel);

	@Test
	public void checkBasicEvaluables() throws Exception {
		assertEvaluatesTo(GenericEntity.T, (GmType) null);

		assertEvaluatesTo(ServiceRequest.T, oracle.getGmBaseType());
		assertEvaluatesTo(ObjectEvaluable.T, oracle.getGmBaseType());
		assertEvaluatesTo(StringEvaluable.T, oracle.getGmStringType());
		
		assertEvaluatesTo(GenericEntityEvaluable.T, GenericEntity.T);
		assertEvaluatesTo(GenericEntityEvaluable2.T, GenericEntity.T);
		assertEvaluatesTo(MammalEvaluable.T, Mammal.T);
		assertEvaluatesTo(MammalPetEvaluable.T, Dog.T);
	}

	private void assertEvaluatesTo(EntityType<?> et, EntityType<?> evaluatesToEt) {
		assertEvaluatesTo(et, oracle.getEntityTypeOracle(evaluatesToEt).asGmEntityType());
	}

	private void assertEvaluatesTo(EntityType<?> et, GmType expectedType) {
		GmType evaluatesTo = getEvaluatesTo(et);
		Assertions.assertThat(evaluatesTo).isSameAs(expectedType);
	}
	
	private GmType getEvaluatesTo(EntityType<?> et) {
		return oracle.getEntityTypeOracle(et).getEvaluatesTo().orElse(null);
	}

	protected static EntityTypeOracle getEntityOracle(EntityType<?> et) {
		return oracle.getEntityTypeOracle(et);
	}

}
