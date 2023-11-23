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
package com.braintribe.model.processing.itw.synthesis.gm;

import java.util.List;
import java.util.StringJoiner;

import com.braintribe.model.processing.itw.tools.MetaModelValidator;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmType;

/**
 * @author peter.gazdik
 */
public class GmtsMetaModelValidator {

	public static void validate(ProtoGmMetaModel metaModel) throws GenericModelTypeSynthesisException {
		List<String> errors = MetaModelValidator.validate(metaModel);
		if (!errors.isEmpty())
			throwValidationException("Cannot weave model " + metaModel + ". Validation failed with following errors: ", errors);
	}

	public static void validate(ProtoGmType gmType) throws GenericModelTypeSynthesisException {
		List<String> errors = MetaModelValidator.validate(gmType);
		if (!errors.isEmpty())
			throwValidationException("Cannot weave type " + gmType + ". Validation failed with following errors: ", errors);
	}

	private static void throwValidationException(String message, List<String> errors) throws GenericModelTypeSynthesisException {
		StringJoiner sj = new StringJoiner("\n  ");
		sj.add(message);

		errors.forEach(sj::add);

		throw new GenericModelTypeSynthesisException(sj.toString());
	}

}
