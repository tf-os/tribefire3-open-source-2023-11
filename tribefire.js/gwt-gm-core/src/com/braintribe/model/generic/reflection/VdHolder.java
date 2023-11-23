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
package com.braintribe.model.generic.reflection;

import java.util.Objects;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * This is (intended to be) used as a wrapper for a {@link ValueDescriptor} in case that is used instead of an actual
 * property value of an entity.
 * 
 * This is currently an experimental feature which we are planning to introduce later, and is not yet supported.
 * 
 * IMPORTANT: This class must remain final, as we are checking if something is {@link VdHolder} by doing
 * {@Code object.getClass() == VdHolder.class}.
 * 
 * @author peter.gazdik
 */
public final class VdHolder {

	public final ValueDescriptor vd;
	public final boolean isAbsenceInformation;

	public static final VdHolder standardAiHolder = new VdHolder(GMF.absenceInformation());

	public VdHolder(ValueDescriptor vd) {
		this.vd = Objects.requireNonNull(vd, "ValueDescriptor cannot be null!");
		this.isAbsenceInformation = vd instanceof AbsenceInformation;
	}

	public static VdHolder newInstance(ValueDescriptor vd) {
		return vd == GMF.absenceInformation() ? VdHolder.standardAiHolder : new VdHolder(vd);
	}

	public static AbsenceInformation getAbsenceInfoIfPossible(Object o) {
		if (!isVdHolder(o))
			return null;

		if (o == standardAiHolder)
			return (AbsenceInformation) standardAiHolder.vd;

		VdHolder vh = (VdHolder) o;
		return vh.isAbsenceInformation ? (AbsenceInformation) vh.vd : null;
	}

	public static ValueDescriptor getValueDescriptorIfPossible(Object o) {
		return isVdHolder(o) ? ((VdHolder) o).vd : null;
	}

	public static <T> T getValueIfPossible(Object o) {
		return (T) (isVdHolder(o) ? null : o);
	}

	public static boolean isAbsenceInfo(Object o) {
		return isVdHolder(o) && //
				(o == standardAiHolder || ((VdHolder) o).isAbsenceInformation);
	}

	public static boolean isVdHolder(Object o) {
		return o != null && o.getClass() == VdHolder.class;
	}

	public static void checkIsAbsenceInfo(Object vdHolder, GenericEntity entity, Property property) {
		VdHolder vh = (VdHolder) vdHolder;
		if (!(vh.isAbsenceInformation))
			throw new UnsupportedOperationException("Feature not supported. Cannot process ValueDescriptor other than AbsenceInformation. Property '"
					+ property.getName() + "' of: " + entity + ". ValueDescriptor: " + vh.vd);
	}

}
