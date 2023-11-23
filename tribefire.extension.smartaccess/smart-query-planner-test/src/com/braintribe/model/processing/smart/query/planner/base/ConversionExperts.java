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
package com.braintribe.model.processing.smart.query.planner.base;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DateToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.LongToString;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.query.smart.processing.eval.context.conversion.DateToStringExpert;
import com.braintribe.model.query.smart.processing.eval.context.conversion.LongToStringExpert;

/**
 * 
 */
public class ConversionExperts {

	public static final Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> DEFAULT_CONVERSION_EXPERTS = newMap();

	static {
		DEFAULT_CONVERSION_EXPERTS.put(DateToString.T, DateToStringExpert.INSTANCE);
		DEFAULT_CONVERSION_EXPERTS.put(LongToString.T, LongToStringExpert.INSTANCE);
	}

}
