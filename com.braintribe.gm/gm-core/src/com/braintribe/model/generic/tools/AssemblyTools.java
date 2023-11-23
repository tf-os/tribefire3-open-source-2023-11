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
package com.braintribe.model.generic.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;

/**
 * @author peter.gazdik
 */
public class AssemblyTools {

	public static <T extends GenericEntity> Set<T> findAll(Object root, EntityType<T> entityType, Predicate<? super T> filter) {
		return findAll(root, e -> entityType.isInstance(e) && filter.test((T) e));
	}

	public static <T extends GenericEntity> Set<T> findAll(Object root, Predicate<? super GenericEntity> filter) {
		if (root == null) {
			return Collections.emptySet();
		}

		FilteringVisitor visitor = new FilteringVisitor(filter);

		GenericModelType type = GMF.getTypeReflection().getType(root);
		type.traverse(root, null, visitor);

		return (Set<T>) visitor.result;
	}

	private static class FilteringVisitor implements TraversingVisitor {
		public final Set<GenericEntity> result = newLinkedSet();
		private final Predicate<? super GenericEntity> filter;

		public FilteringVisitor(Predicate<? super GenericEntity> filter) {
			this.filter = filter;
		}
		
		@Override
		public void visitTraversing(TraversingContext traversingContext) {
			if (traversingContext.getCurrentCriterionType() == CriterionType.ENTITY) {
				GenericEntity entity = (GenericEntity) traversingContext.getObjectStack().peek();
				if (filter.test(entity))
					result.add(entity);
			}
		}
	}

	public static <T> LinkedHashSet<T> transitiveClosure(Collection<? extends T> roots,
			Function<? super T, Collection<? extends T>> neighborFunction) {

		LinkedHashSet<T> visited = newLinkedSet();

		for (T root : roots)
			addNode(root, visited, neighborFunction);

		return visited;
	}

	private static <T> void addNode(T node, LinkedHashSet<T> visited, Function<? super T, Collection<? extends T>> neighborFunction) {
		if (!visited.add(node))
			return;

		for (T neighbor : nullSafe(neighborFunction.apply(node))) {
			addNode(neighbor, visited, neighborFunction);
		}
	}

	public static <T> List<T> detectCycle(T root, Function<? super T, Collection<? extends T>> neighborFunction) {
		List<T> result = lookForCycle(root, neighborFunction, new LinkedHashSet<>());
		return result == null ? Collections.emptyList() : result;
	}

	private static <T> List<T> lookForCycle(T node, Function<? super T, Collection<? extends T>> neighborFunction, LinkedHashSet<T> visited) {
		if (!visited.add(node)) {
			ArrayList<T> result = new ArrayList<>(visited);
			result.add(node);
			return result;
		}

		for (T sibling : nullSafe(neighborFunction.apply(node))) {
			List<T> result = lookForCycle(sibling, neighborFunction, visited);
			if (result != null)
				return result;
		}

		visited.remove(node);
		return null;
	}

}
