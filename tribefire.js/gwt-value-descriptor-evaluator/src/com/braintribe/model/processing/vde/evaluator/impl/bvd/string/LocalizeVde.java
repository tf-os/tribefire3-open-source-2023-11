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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.string;

import com.braintribe.model.bvd.string.Localize;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.utils.i18n.I18nTools;

/**
 * {@link ValueDescriptorEvaluator} for {@link Localize}
 * 
 */
public class LocalizeVde implements ValueDescriptorEvaluator<Localize> {

	@Override
	public VdeResult evaluate(VdeContext context, Localize valueDescriptor) throws VdeRuntimeException {

		Object localeObject = valueDescriptor.getLocale();
		Object localisedStringObject = valueDescriptor.getLocalizedString();

		if (localisedStringObject instanceof LocalizedString && localeObject instanceof String) {
			LocalizedString localisedString = (LocalizedString) localisedStringObject;
			String locale = (String) localeObject;
			return new VdeResultImpl(I18nTools.get(localisedString, locale), false);
		}
		throw new VdeRuntimeException("Localize is valid for LocalizedString and String and not :" + localisedStringObject + " " + localeObject);
	}

}
