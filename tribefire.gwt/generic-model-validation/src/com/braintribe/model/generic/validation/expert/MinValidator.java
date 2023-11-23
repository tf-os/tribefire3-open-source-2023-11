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
package com.braintribe.model.generic.validation.expert;

import java.util.Arrays;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.util.ValidatorUtil;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.processing.vde.evaluator.VDE;

public class MinValidator extends AbstractValidator<Min> {

	@Override
	public Future<ValidatorResult> validateValue(ValidationContext validationContext, GenericEntity entity,
			Min metaData, EntitySignatureAndPropertyName entitySignatureAndPropertyName, Object value) {
		String propertyName = entitySignatureAndPropertyName.getPropertyName();
		String propertyDisplayName = ValidatorUtil.getPropertyDisplayName(getGmSession(), entity, propertyName);
		
		ValidatorResult validatorResult = ValidatorUtil.prepareValidatorResult(propertyName);
		
		Object limit = metaData.getLimit();
		
		String message;
		com.braintribe.model.bvd.math.Min minVde = com.braintribe.model.bvd.math.Min.T.create();
		if (metaData.getExclusive()) {
			if (validationContext.isShortMessageStyle())
				message = LocalizedText.INSTANCE.stringGreatherThan(limit.toString());
			else	
				message = LocalizedText.INSTANCE.greater(propertyDisplayName, limit.toString());
			validatorResult.setMessageType(ValidatorMessageType.greatherThan);
			minVde.setOperands(Arrays.asList(value, limit)); //limit < value
		} else {
			if (validationContext.isShortMessageStyle())
				message = LocalizedText.INSTANCE.stringGreatherEqual(limit.toString());
			else	
				message = LocalizedText.INSTANCE.greaterEqual(propertyDisplayName, limit.toString());
			validatorResult.setMessageType(ValidatorMessageType.greatherEqual);
			minVde.setOperands(Arrays.asList(limit, value)); //limit <= value
		}
		
		Object compResult;
		if (value != null)
			compResult = VDE.evaluate(minVde);
		else {
			if (validationContext.isCompleteConfiguredValidation())
				compResult = null;
			else
				compResult = limit;
		}
		
		validatorResult.setMessageParameter(limit.toString());
		validatorResult.setResult(compResult == limit || (compResult != null && compResult.equals(limit)));
		validatorResult.setResultMessage(message);
		
		return new Future<>(validatorResult);
	}

}
