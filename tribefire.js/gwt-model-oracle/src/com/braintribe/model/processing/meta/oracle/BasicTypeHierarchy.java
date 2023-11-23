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

import static com.braintribe.utils.lcd.CollectionTools2.asDeque;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;

/**
 * @author peter.gazdik
 */
public class BasicTypeHierarchy implements TypeHierarchy {

	private final BasicModelOracle modelOracle;
	private final boolean superDemanded;
	private final GmEntityType gmEntityType;

	private boolean transitive;
	private boolean includeBase;
	private IncludeSelf includeSelf = IncludeSelf.no;
	private Instantiability instantiability = Instantiability.all;
	private Order order;

	public BasicTypeHierarchy(BasicModelOracle modelOracle, GmEntityType gmEntityType, HierarchyKind hierarchyKind) {
		this.modelOracle = modelOracle;
		this.gmEntityType = gmEntityType;
		this.superDemanded = hierarchyKind == HierarchyKind.superTypes;
	}

	@Override
	public TypeHierarchy transitive() {
		this.transitive = true;
		return this;
	}

	@Override
	public TypeHierarchy includeSelf() {
		this.includeSelf = IncludeSelf.yes;
		return this;
	}

	@Override
	public TypeHierarchy includeSelfForce() {
		this.includeSelf = IncludeSelf.force;
		return this;
	}

	@Override
	public TypeHierarchy onlyInstantiable() {
		this.instantiability = Instantiability.onlyInstantiable;
		return this;
	}

	@Override
	public TypeHierarchy onlyAbstract() {
		this.instantiability = Instantiability.onlyAbstract;
		return this;
	}

	@Override
	public TypeHierarchy includeBaseType() {
		this.includeBase = true;
		return this;
	}

	@Override
	@SuppressWarnings("unusable-by-js")
	public TypeHierarchy sorted(Order order) {
		this.order = order;
		return this;
	}

	@Override
	public <T extends GmType> Set<T> asGmTypes() {
		return Collections.unmodifiableSet((Set<T>) getGmTypes());
	}

	@Override
	public <T extends GenericModelType> Set<T> asTypes() {
		Set<T> result = newLinkedSet();

		Collection<? extends GmType> gmTypes = getGmTypes();
		for (GmType gmType : gmTypes)
			result.add(BasicModelOracle.typeReflection.<T> getType(gmType.getTypeSignature()));

		return Collections.unmodifiableSet(result);
	}

	@Override
	public Set<EntityTypeOracle> asEntityTypeOracles() {
		if (includeBase)
			throw new IllegalStateException("Cannot retrieve hierarchy as entity type oracles because base type was requested as well.");

		Set<EntityTypeOracle> result = newLinkedSet();

		Collection<GmEntityType> gmEntityTypes = (Collection<GmEntityType>) getGmTypes();
		for (GmEntityType gmEntityType : gmEntityTypes)
			result.add(modelOracle.getEntityTypeOracle(gmEntityType));

		return Collections.unmodifiableSet(result);
	}

	private boolean needsSpecialHandling() {
		return includeBase || transitive || includeSelf != IncludeSelf.no || instantiability != Instantiability.all;
	}

	private Set<? extends GmType> getGmTypes() {
		if (!needsSpecialHandling())
			return getDeclaredRelatives(gmEntityType);

		Set<GmType> result = newLinkedSet();

		addSelfOrBase(true, result);
		addDeclaredRelatives(result, gmEntityType);
		addSelfOrBase(false, result);

		return result;
	}

	private void addSelfOrBase(boolean beginning, Set<GmType> result) {
		if (beginning == demandsOrderFromRootType())
			maybeAddSelf(result);
		else
			maybeAddBaseType(result);
	}

	private void maybeAddSelf(Set<GmType> result) {
		if ((includeSelf == IncludeSelf.yes && instantiability.matches(gmEntityType)) || includeSelf == IncludeSelf.force)
			result.add(gmEntityType);
	}

	private void maybeAddBaseType(Set<GmType> result) {
		if (includeBase && superDemanded)
			// We can only include BaseType when retrieving super-types or a sub-type of BaseType itself.
			result.add(modelOracle.getGmBaseType());
	}

	private void addDeclaredRelatives(Set<GmType> result, GmEntityType gmEntityType) {
		if (!transitive && instantiability == Instantiability.all)
			result.addAll(getDeclaredRelatives(gmEntityType));
		else if (!transitive || order == null)
			addDeclaredRelativesAnyOrder(result, gmEntityType);
		else
			addTransitiveSortedRelatives(result, gmEntityType);
	}

	private void addDeclaredRelativesAnyOrder(Set<GmType> result, GmEntityType gmEntityType) {
		for (GmEntityType relatedType : getDeclaredRelatives(gmEntityType)) {
			if (instantiability.matches(relatedType))
				result.add(relatedType);

			if (transitive)
				addDeclaredRelativesAnyOrder(result, relatedType);
		}
	}

	private void addTransitiveSortedRelatives(Set<GmType> result, GmEntityType gmEntityType) {
		Set<GmType> processed = newSet();
		boolean orderFromRootType = demandsOrderFromRootType();
		Collection<GmType> localResult = orderFromRootType ? result : newList();

		Deque<GmEntityType> typesToProcess = asDeque(gmEntityType);
		while (!typesToProcess.isEmpty()) {
			GmEntityType typeToProcess = typesToProcess.removeFirst();
			if (!processed.add(typeToProcess))
				continue;

			for (GmEntityType relatedType : getDeclaredRelatives(typeToProcess)) {
				typesToProcess.addLast(relatedType);

				if (instantiability.matches(relatedType))
					localResult.add(relatedType);
			}
		}

		if (!orderFromRootType) {
			List<GmType> reversedList = (List<GmType>) localResult;
			Collections.reverse(reversedList);
			result.addAll(reversedList);
		}
	}

	private boolean demandsOrderFromRootType() {
		return order == null || superDemanded == (order == Order.subFirst);
	}

	private Set<GmEntityType> getDeclaredRelatives(GmEntityType gmEntityType) {
		return superDemanded ? modelOracle.getDirectSuper(gmEntityType) : modelOracle.getDirectSub(gmEntityType);
	}

	static enum IncludeSelf {
		no,
		yes,
		force
	}

	static enum Instantiability {
		all,
		onlyInstantiable,
		onlyAbstract;

		public boolean matches(GmEntityType gmType) {
			return this == all || ((this == onlyInstantiable) == isInstantiable(gmType));
		}

		private static boolean isInstantiable(GmEntityType gmType) {
			return !Boolean.TRUE.equals((gmType).getIsAbstract());
		}
	}
}

enum HierarchyKind {
	subTypes,
	superTypes,
}
