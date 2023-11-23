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
package com.braintribe.model.processing.deployment.hibernate.data;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.model.tools.MetaModelTools;

/**
 * 
 */
public class HibernateDeploymentTestModel {

	// @formatter:off
	public static final List<EntityType<?>> classes = Arrays.asList(
			Car.T,
			Card.T,
			Company.T,
			CardCompany.T,
			CreditCard.T,
			DebitCard.T,
			TravelPrePaidDebitCard.T,
			Bank.T,
			Employee.T,
			Person.T
	);
	// @formatter:on

	public static GmMetaModel enriched() {
		GmMetaModel originalMetaModel = MetaModelTools.provideRawModel(classes);
		return enrichMetaModel(originalMetaModel);
	}

	private static GmMetaModel enrichMetaModel(GmMetaModel gmMetaModel) {
		@SuppressWarnings("unused")
		ModelMetaDataEditor model = new BasicModelMetaDataEditor(gmMetaModel);

		// TODO: use modelEditor to apply hibernate metadata do entities and properties

		return gmMetaModel;
	}

}
