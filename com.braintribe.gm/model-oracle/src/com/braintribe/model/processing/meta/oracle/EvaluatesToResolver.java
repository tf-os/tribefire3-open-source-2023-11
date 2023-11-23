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
package com.braintribe.model.processing.meta.oracle;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.flat.FlatEntityType;

/**
 * @author peter.gazdik
 */
/* package */ class EvaluatesToResolver {

	public static Optional<GmType> resolveEvaluatesTo(BasicEntityTypeOracle entityTypeOracle) {
		return new EvaluatesToResolver(entityTypeOracle) //
				.resolveEvaluatesTo();
	}

	private final BasicEntityTypeOracle entityTypeOracle;
	private final BasicModelOracle modelOracle;
	private final FlatEntityType flatEntityType;

	public EvaluatesToResolver(BasicEntityTypeOracle entityTypeOracle) {
		this.entityTypeOracle = entityTypeOracle;
		this.modelOracle = entityTypeOracle.modelOracle;
		this.flatEntityType = entityTypeOracle.flatEntityType;
	}

	private Optional<GmType> resolveEvaluatesTo() {
		Optional<GmType> directEvaluatesTo = Optional.ofNullable(flatEntityType.type.getEvaluatesTo());

		Map<GmType, EntityTypeOracle> resultCandidates = newMap();
		// We cannot collect the Map on the Stream directly, as there might be duplicate keys We don't care which key we
		// end up picking
		entityTypeOracle.getSuperTypes().asEntityTypeOracles().stream() //
				.filter(EntityTypeOracle::isEvaluable) //
				.forEach(o -> resultCandidates.put(o.getEvaluatesTo().get(), o));

		if (directEvaluatesTo.isPresent())
			resultCandidates.put(directEvaluatesTo.get(), entityTypeOracle);

		if (resultCandidates.isEmpty())
			return directEvaluatesTo;

		EntityTypeOracle resultOwner = findCommonSubTypeOwner(resultCandidates);

		if (resultOwner == entityTypeOracle)
			return directEvaluatesTo;

		Optional<GmType> result = resultOwner.getEvaluatesTo();

		if (directEvaluatesTo.isPresent() && !directEvaluatesTo.equals(result))
			throw new GenericModelException("TODO");

		return result;
	}

	private EntityTypeOracle findCommonSubTypeOwner(Map<GmType, EntityTypeOracle> types) {
		if (types.size() > 1)
			types.remove(modelOracle.getGmBaseType());

		if (types.size() == 1)
			return first(types.values());

		// we have multiple types and none of them is Object
		// if at least one of the types is not a GmEntityType, we have a problem

		checkAllTypesAreGmEntityTypes(types);

		Set<GmEntityType> gmEntityTypes = (Set<GmEntityType>) (Set<?>) types.keySet();

		List<GmEntityType> commonSubTypes = newList();
		for (GmEntityType gmEntityType : gmEntityTypes)
			if (!hasSubType(gmEntityType, gmEntityTypes))
				commonSubTypes.add(gmEntityType);

		if (commonSubTypes.size() > 1) {
			GmEntityType t1 = commonSubTypes.get(0);
			GmEntityType t2 = commonSubTypes.get(1);

			throwIncompatibleTypesException(t1, t2, types);
		}

		return types.get(first(commonSubTypes));
	}

	private void checkAllTypesAreGmEntityTypes(Map<GmType, EntityTypeOracle> types) {
		Iterator<GmType> it = types.keySet().iterator();

		GmType prev = it.next();
		boolean onlyEntities = prev.isGmEntity();

		while (it.hasNext()) {
			GmType next = it.next();
			onlyEntities &= next.isGmEntity();

			if (!onlyEntities)
				throwIncompatibleTypesException(prev, next, types);

			prev = next;
		}
	}

	private boolean hasSubType(GmEntityType gmEntityType, Set<GmEntityType> gmEntityTypes) {
		for (GmEntityType maybeSubType : gmEntityTypes)
			if (isFirstSuperTypeOfSecond(gmEntityType, maybeSubType))
				return true;

		return false;
	}

	private boolean isFirstSuperTypeOfSecond(GmEntityType et2, GmEntityType et1) {
		return modelOracle.getEntityTypeOracle(et1).getSuperTypes().transitive().asGmTypes().contains(et2);
	}

	private void throwIncompatibleTypesException(GmType t1, GmType t2, Map<GmType, EntityTypeOracle> types) {
		EntityTypeOracle owner1 = types.get(t1);
		EntityTypeOracle owner2 = types.get(t2);
		
		StringBuilder message = new StringBuilder();
		message.append("Incompatible evaluate-to types: ").append(t1.getTypeSignature()).append(", ").append(t2.getTypeSignature())
				.append(". The respective evaluable types are ").append(owner1.asGmEntityType().getTypeSignature()).append(" and ")
				.append(owner2.asGmEntityType().getTypeSignature()).append(".");

		throw new GenericModelException(message.toString());
	}

}
