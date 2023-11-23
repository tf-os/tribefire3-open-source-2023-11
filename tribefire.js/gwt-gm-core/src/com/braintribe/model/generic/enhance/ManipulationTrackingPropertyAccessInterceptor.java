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
package com.braintribe.model.generic.enhance;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.absenting;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.localEntityProperty;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.session.GmSession;

@SuppressWarnings("unusable-by-js")
public class ManipulationTrackingPropertyAccessInterceptor extends PropertyAccessInterceptor {

	public boolean ignoredNoChangeAssignments;
	
	@Override
	public Object setProperty(Property property, GenericEntity entity, Object value, boolean isVd) {
		/* We assume entity is enhanced, otherwise there would be no session to be notified with the manipulation... */
		GmSession session = entity.session();

		// Note: this has to be done before setting the owner because in case of the id property change it would
		// otherwise create a PersistentEntityReference which is then wrong
		Owner entityProperty = newLocalOwner(entity, property);

		Object oldValue = next.setProperty(property, entity, value, isVd);

		if (session == null)
			return oldValue;

		if (isVd)
			// TODO what to do here - track as Absenting, whose property should be of type ValueDescriptor, AbesetingManipulator should be of type VD
			return oldValue;

		// oldValue being VD is no problem. We know "value" is not VD. If oldValue was VD, it is actually a VdHolder and this if is false
		if (ignoredNoChangeAssignments && (value == oldValue || (value != null && value.equals(oldValue))))
			return oldValue;

		ChangeValueManipulation manipulation = changeValue(property.getType().getValueSnapshot(value), entityProperty);

		AbsenceInformation ai = VdHolder.getAbsenceInfoIfPossible(oldValue);
		Manipulation im = ai != null ? absenting(ai, entityProperty) : changeValue(oldValue, entityProperty);

		manipulation.linkInverse(im);

		session.noticeManipulation(manipulation);

		return oldValue;
	}

	private static LocalEntityProperty newLocalOwner(GenericEntity entity, Property p) {
		return localEntityProperty(entity, p.getName());
	}

}
