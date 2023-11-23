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
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.processing.vde.evaluator.VDE;

public class MaxValidator extends AbstractValidator<Max> {

	@Override
	public Future<ValidatorResult> validateValue(ValidationContext validationContext, GenericEntity entity,
			Max metaData, EntitySignatureAndPropertyName entitySignatureAndPropertyName, Object value) {
		String propertyName = entitySignatureAndPropertyName.getPropertyName();
		String propertyDisplayName = ValidatorUtil.getPropertyDisplayName(getGmSession(), entity, propertyName);
		
		ValidatorResult validatorResult = ValidatorUtil.prepareValidatorResult(propertyName);
		
		Object limit = metaData.getLimit();
		if (limit == null) {
			validatorResult.setResult(true);
			return new Future<>(validatorResult);
		}
		
		String message;
		com.braintribe.model.bvd.math.Max maxVde = com.braintribe.model.bvd.math.Max.T.create();
		if (metaData.getExclusive()) {
			if (validationContext.isShortMessageStyle())
				message = LocalizedText.INSTANCE.stringLesserThan(limit.toString());
			else	
				message = LocalizedText.INSTANCE.less(propertyDisplayName, limit.toString());
			validatorResult.setMessageType(ValidatorMessageType.lessThan);
			maxVde.setOperands(Arrays.asList(value, limit)); //limit > value
		} else {
			if (validationContext.isShortMessageStyle())
				message = LocalizedText.INSTANCE.stringLesserEqual(limit.toString());
			else	
				message = LocalizedText.INSTANCE.lessEqual(propertyDisplayName, limit.toString());
			validatorResult.setMessageType(ValidatorMessageType.lessEqual);
			maxVde.setOperands(Arrays.asList(limit, value)); //limit >= value
		}
		
		Object compResult;
		if (value != null)
			compResult = VDE.evaluate(maxVde);
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
