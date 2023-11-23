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
package com.braintribe.model.processing.smart.query.planner.tools;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import com.braintribe.model.accessdeployment.smart.meta.AsIs;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;

/**
 * @author peter.gazdik
 */
public class SmartMappingTools {

	/**
	 * @return {@link GmProperty} instance from given mapping that corresponds to given {@link PropertyAssignment}. It is the value directly
	 *         taken from the mapping, not adjusted to being smart or delegate, so that depends on the caller's context. In case of
	 *         {@link PropertyAsIs} this method returns <tt>null</tt>.
	 */
	public static QualifiedProperty getJoinedProperty(PropertyAssignment pa) {
		if (pa instanceof InverseKeyPropertyAssignment)
			return getJoinedPropertyFromIkpa((InverseKeyPropertyAssignment) pa);

		if (pa instanceof KeyPropertyAssignment)
			return getJoinedPropertyFromKpa((KeyPropertyAssignment) pa);

		if (pa instanceof CompositeInverseKeyPropertyAssignment)
			return getJoinedPropertyFromCikpa((CompositeInverseKeyPropertyAssignment) pa);

		if (pa instanceof CompositeKeyPropertyAssignment)
			return getJoinedPropertyFromCkpa((CompositeKeyPropertyAssignment) pa);

		if (pa instanceof LinkPropertyAssignment)
			return getJoinedPropertyFromLpa((LinkPropertyAssignment) pa);

		if (pa instanceof PropertyAsIs || pa instanceof AsIs)
			return null;

		if (pa instanceof QualifiedPropertyAssignment)
			return getJoinedPropertyFrom((QualifiedPropertyAssignment) pa);

		throw new SmartQueryPlannerException("Unsupported mapping: " + pa);
	}

	private static QualifiedProperty getJoinedPropertyFromCikpa(CompositeInverseKeyPropertyAssignment pa) {
		return getJoinedPropertyFromIkpa(first(pa.getInverseKeyPropertyAssignments()));
	}

	private static QualifiedProperty getJoinedPropertyFromCkpa(CompositeKeyPropertyAssignment pa) {
		return getJoinedPropertyFromKpa(first(pa.getKeyPropertyAssignments()));
	}

	private static QualifiedProperty getJoinedPropertyFromIkpa(InverseKeyPropertyAssignment pa) {
		return pa.getProperty();
	}

	private static QualifiedProperty getJoinedPropertyFromKpa(KeyPropertyAssignment pa) {
		return pa.getKeyProperty();
	}

	private static QualifiedProperty getJoinedPropertyFromLpa(LinkPropertyAssignment pa) {
		return pa.getOtherKey();
	}

	private static QualifiedProperty getJoinedPropertyFrom(QualifiedPropertyAssignment pa) {
		return pa;
	}

}
