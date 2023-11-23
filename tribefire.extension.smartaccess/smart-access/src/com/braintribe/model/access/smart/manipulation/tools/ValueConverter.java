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
package com.braintribe.model.access.smart.manipulation.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.manipulation.conversion.ResolveDelegateValueConversion;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EnumMapping;
import com.braintribe.model.processing.smartquery.eval.api.ConversionDirection;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;

/**
 * 
 */
public class ValueConverter {

	private final SmartManipulationProcessor smp;
	private final ModelExpert modelExpert;
	private final ReferenceManager referenceManager;
	private final Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> experts;
	private final Map<SmartConversion, SmartConversionExpert<SmartConversion>> expertsForValues;

	public ValueConverter(SmartManipulationProcessor smp, Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> experts) {

		this.smp = smp;
		this.modelExpert = smp.modelExpert();
		this.referenceManager = smp.referenceManager();
		this.experts = experts;
		this.expertsForValues = newMap();
	}

	@FunctionalInterface
	public static interface DelegateRefToEmResolver extends Function<EntityReference, EntityMapping> {
		// nothing to add
	}

	public Object convertToDelegate(Object value, SmartConversion conversion, boolean convertMapKeys) {
		return convert(ConversionDirection.smart2Delegate, value, conversion, convertMapKeys, null);
	}

	public Object convertToSmart(Object value, SmartConversion conversion, DelegateRefToEmResolver emResolver, boolean convertMapKeys) {
		return convert(ConversionDirection.delegate2Smart, value, conversion, convertMapKeys, emResolver);
	}

	private Object convert(ConversionDirection direction, Object value, SmartConversion conversion, boolean convertMapKeys,
			DelegateRefToEmResolver emResolver) {
		if (value == null)
			return null;

		if (value instanceof Collection<?>)
			if (value instanceof List<?>)
				return convertCollection(direction, newList(), (List<?>) value, conversion, emResolver);
			else
				return convertCollection(direction, newSet(), (Set<?>) value, conversion, emResolver);

		if (value instanceof Map<?, ?>)
			return convertMap(direction, (Map<?, ?>) value, conversion, convertMapKeys, emResolver);

		/* This must go first, and handling of EntityReference is second. The reason is that for KPA, we create an artificial conversion that takes
		 * entity and gives it's key property back. So if we did check for EntityReference first, we would simply convert it to delegate entity.
		 * 
		 * Also, this has to be before the enum check, because in case there is a conversion for property, the automatic enum conversion based on
		 * mapping is ignored. */
		if (conversion != null) {
			SmartConversionExpert<SmartConversion> conversionExpert = getExpertFor(conversion);
			return conversionExpert.convertValue(conversion, value, direction);
		}

		if (value instanceof EntityReference) {
			EntityReference ref = (EntityReference) value;
			if (direction == ConversionDirection.smart2Delegate)
				return referenceManager.acquireDelegateReference(ref);
			else
				return referenceManager.acquireSmartReference(ref, emResolver.apply(ref));
		}

		if (value instanceof Enum<?>)
			return convertEnum((Enum<?>) value);

		return value;
	}

	private Object convertCollection(ConversionDirection direction, Collection<Object> converted, Collection<?> original, SmartConversion conversion,
			DelegateRefToEmResolver emResolver) {

		for (Object o : original)
			converted.add(convert(direction, o, conversion, false, emResolver));

		return converted;
	}

	private Object convertMap(ConversionDirection direction, Map<?, ?> value, SmartConversion conversion, boolean convertMapKeys,
			DelegateRefToEmResolver emResolver) {

		Map<Object, Object> result = newMap();

		SmartConversion keyConversion = convertMapKeys ? conversion : null;

		for (Entry<?, ?> e : value.entrySet())
			result.put(convert(direction, e.getKey(), keyConversion, false, emResolver),
					convert(direction, e.getValue(), conversion, false, emResolver));

		return result;
	}

	private Object convertEnum(Enum<?> value) {
		EnumMapping em = modelExpert.resolveEnumMapping(value.getClass().getName());

		return em.convertToDelegateValaue(value);
	}

	private SmartConversionExpert<SmartConversion> getExpertFor(SmartConversion conversion) {
		SmartConversionExpert<SmartConversion> result = expertsForValues.get(conversion);

		if (result == null) {
			result = findExpertFor(conversion);
			expertsForValues.put(conversion, result);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private SmartConversionExpert<SmartConversion> findExpertFor(SmartConversion conversion) {
		if (conversion instanceof ResolveDelegateValueConversion)
			/* This is not normalized, the expert could be in the map with others, but as we cannot have a singleton, we do it this way, so we only
			 * instantiate it when necessary. */
			return acquireDelegateValueConversionExpert();

		EntityType<?> conversionType = conversion.entityType();
		SmartConversionExpert<SmartConversion> result = (SmartConversionExpert<SmartConversion>) experts.get(conversionType);

		if (result == null)
			throw new RuntimeQueryEvaluationException("No expert found for conversion:" + conversionType.getTypeSignature());

		return result;
	}

	private SmartConversionExpert<SmartConversion> delegateValueConversionExpert;

	private SmartConversionExpert<SmartConversion> acquireDelegateValueConversionExpert() {
		if (delegateValueConversionExpert == null) {
			Object o = new ResolveDelegateValueConversionExpert(smp, this);
			delegateValueConversionExpert = (SmartConversionExpert<SmartConversion>) o;
		}

		return delegateValueConversionExpert;
	}

}
