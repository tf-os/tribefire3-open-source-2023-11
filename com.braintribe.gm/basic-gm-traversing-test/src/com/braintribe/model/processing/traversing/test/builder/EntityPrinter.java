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
package com.braintribe.model.processing.traversing.test.builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.test.model.NamableEntity;

/**
 * 
 */
public class EntityPrinter {

	private final Set<NamableEntity> visited = new HashSet<NamableEntity>();
	private int level;
	private boolean prefixApplied = false;

	public static void print(NamableEntity entity) {
		new EntityPrinter().printHelper(entity);
	}

	private void printHelper(NamableEntity entity) {
		if (visited.contains(entity)) {
			printVisited(entity);
			return;
		}

		visited.add(entity);

		printFirstVisit(entity);
	}

	private void printVisited(NamableEntity entity) {
		EntityType<GenericEntity> et = entityType(entity);
		println(et.getShortName() + "(" + number(entity) + ") <-");
	}

	private void printFirstVisit(NamableEntity entity) {
		EntityType<GenericEntity> et = entityType(entity);
		println(et.getShortName() + "(" + number(entity) + ") [");
		level++;

		for (Property p: et.getProperties()) {
			print(p.getName() + ": ");

			AbsenceInformation ai = p.getAbsenceInformation(entity);
			if (ai != null) {
				println("---");
				continue;
			}

			GenericModelType type = p.getType();
			Object value = p.get(entity);

			printValue(value, type);

		}

		level--;
		println("]");
	}

	private void printValue(Object value, GenericModelType type) {
		if (value == null) {
			println("null");
			return;
		}

		switch (type.getTypeCode()) {
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case enumType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
				println(value);
				return;

			case entityType:
				printReferencedEntity((GenericEntity) value, (EntityType<?>) type);
				return;
			case listType:
				printList((List<?>) value, ((CollectionType) type).getParameterization()[0]);
				return;
			case mapType:
				printMap((Map<?, ?>) value, ((CollectionType) type).getParameterization()[0],
						((CollectionType) type).getParameterization()[1]);
				return;
			case setType:
				printSet((Set<?>) value, ((CollectionType) type).getParameterization()[0]);
				return;
			
			default:
				return;
		}
	}

	private void printReferencedEntity(GenericEntity entity, EntityType<?> type) {
		if (NamableEntity.class.isAssignableFrom(type.getJavaType())) {
			printHelper((NamableEntity) entity);
		} else {
			println(entity);
		}
	}

	@SuppressWarnings("unused")
	private void printMap(Map<?, ?> value, GenericModelType keyType, GenericModelType valueType) {
		println("MAP - TODO");
	}

	private void printList(List<?> value, GenericModelType elementType) {
		printCollection(value, elementType);
	}

	private void printSet(Set<?> value, GenericModelType elementType) {
		printCollection(value, elementType);
	}

	private void printCollection(Collection<?> value, GenericModelType elementType) {
		println("[");

		level++;
		for (Object o: value) {
			printValue(o, elementType);
		}
		level--;
	}

	private EntityType<GenericEntity> entityType(NamableEntity entity) {
		return entity.entityType();
	}

	private void println(Object s) {
		print(s + "\n");
		prefixApplied = false;
	}

	private void print(Object s) {
		if (!prefixApplied) {
			for (int i = 0; i < level; i++) {
				System.out.print("    ");
			}
			prefixApplied = true;
		}

		System.out.print(s);
	}

	private int number(NamableEntity entity) {
		return System.identityHashCode(entity);
	}

}
