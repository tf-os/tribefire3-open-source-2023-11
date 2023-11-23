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
package com.braintribe.model.access.hibernate;

import java.util.List;
import java.util.ListIterator;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.access.hibernate.hql.SelectHqlBuilder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;

/* package */ class HibernateAccessTools {

	public static GenericEntity deproxy(GenericEntity maybeProxy) {
		if (maybeProxy instanceof HibernateProxy) {
			HibernateProxy hibernateProxy = (HibernateProxy) maybeProxy;
			if (!Hibernate.isInitialized(maybeProxy))
				Hibernate.initialize(maybeProxy);

			return (GenericEntity) hibernateProxy.getHibernateLazyInitializer().getImplementation();
		} else {
			return maybeProxy;
		}
	}

	public static void ensureIdsAreGmValues(List<?> hqlResults) {
		replaceCompositeIdValusWithStrings(hqlResults);
	}

	public static void ensureIdsAreGmValues(SelectQuery query, List<?> hqlResults) {
		if (isSelectingId(query))
			replaceCompositeIdValusWithStrings(hqlResults);
	}

	private static boolean isSelectingId(SelectQuery query) {
		List<Object> selections = query.getSelections();
		if (selections == null)
			return false;

		for (Object selection : selections)
			if (isIdSelection(selection))
				return true;

		return false;
	}

	private static boolean isIdSelection(Object s) {
		return s instanceof PropertyOperand && GenericEntity.id.equals(((PropertyOperand) s).getPropertyName());
	}

	private static void replaceCompositeIdValusWithStrings(List<?> hqlResults) {
		ListIterator<Object> rows = (ListIterator<Object>) hqlResults.listIterator();
		while (rows.hasNext()) {
			Object row = rows.next();

			if (row == null)
				continue;

			if (row.getClass().isArray()) {
				Object[] vals = (Object[]) row;
				for (int i = 0; i < vals.length; i++)
					if (vals[i] instanceof CompositeIdValues)
						vals[i] = CompositeIdValues.encodeAsString(vals[i]);
				continue;
			}

			if (row instanceof CompositeIdValues)
				rows.set(CompositeIdValues.encodeAsString(row));
		}
	}

	public static void ensureTypeSignatureSelectedProperly(SelectHqlBuilder hqlBuilder, List<?> hqlResults) {
		if (needsTypeSignatureAdjusting(hqlBuilder))
			adjustTypeSignatures(hqlBuilder, hqlResults);
	}

	private static boolean needsTypeSignatureAdjusting(SelectHqlBuilder hqlBuilder) {
		return !hqlBuilder.entitySignaturePositions.isEmpty();
	}

	private static void adjustTypeSignatures(SelectHqlBuilder hqlBuilder, List<?> hqlResults) {
		ListIterator<Object> rows = (ListIterator<Object>) hqlResults.listIterator();
		while (rows.hasNext()) {
			Object row = rows.next();

			if (row == null)
				continue;

			if (row.getClass().isArray()) {
				Object[] vals = (Object[]) row;
				
				for (int i : hqlBuilder.entitySignaturePositions)
					vals[i] = toEntityTypeSignature(vals[i]);
				continue;
			}

			rows.set(toEntityTypeSignature(row));
		}
	}

	private static String toEntityTypeSignature(Object row) {
		return ((GenericEntity)row).entityType().getTypeSignature();
	}

	public static EntityReference createReference(GenericEntity entity) {
		EntityReference ref = entity.reference();
		
		Object id = ref.getRefId();
		if (id instanceof CompositeIdValues)
			ref.setRefId(((CompositeIdValues)id).encodeAsString());
		
		return ref;
	}
}
