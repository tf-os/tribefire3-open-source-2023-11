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
package com.braintribe.model.processing.query.eval.context.function;

import java.util.Map;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.api.function.aspect.LocaleQueryAspect;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.utils.i18n.I18nTools;

/**
 * 
 */
public class LocalizeExpert implements QueryFunctionExpert<Localize> {

	public static final LocalizeExpert INSTANCE = new LocalizeExpert();

	private LocalizeExpert() {
	}

	@Override
	public Object evaluate(Tuple tuple, Localize queryFunction, Map<Object, Value> operandMappings, QueryEvaluationContext context) {
		LocalizedString ls = context.resolveValue(tuple, operandMappings.get(queryFunction.getLocalizedStringOperand()));
		if (ls == null)
			return null;

		String locale = getLocale(queryFunction, context);

		return I18nTools.get(ls, locale);
	}

	private String getLocale(Localize queryFunction, QueryEvaluationContext context) {
		String locale = queryFunction.getLocale();
		if (locale != null)
			return locale;

		return context.getFunctionAspect(LocaleQueryAspect.class);
	}

}
