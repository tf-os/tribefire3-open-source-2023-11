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
package com.braintribe.model.processing.deployment.processor.bidi.data;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysisException;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * 
 */
public class BidiPropertyTestModel {

	public static GmMetaModel enriched() {
		try {
			return loadModel();

		} catch (JavaTypeAnalysisException e) {
			throw new RuntimeException("", e);
		}
	}

	private static GmMetaModel loadModel() throws JavaTypeAnalysisException {
		JavaTypeAnalysis jta = new JavaTypeAnalysis();
		GmType c = jta.getGmType(Company.class);
		GmType p = jta.getGmType(Person.class);
		GmType f = jta.getGmType(Folder.class);

		GmMetaModel result = GmMetaModel.T.create();
		result.setName("test:BidiModel");
		result.getDependencies().add(NewMetaModelGeneration.rootModel().getMetaModel());
		result.setTypes(asSet(c, p, f));

		return result;
	}

}
