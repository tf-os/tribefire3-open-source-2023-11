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
package tribefire.extension.messaging.model.comparison;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Diff extends GenericEntity {
	EntityType<Diff> T = EntityTypes.T(Diff.class);

	String propertyPath = "propertyPath";
	String oldValue = "oldValue";
	String newValue = "newValue";
	String valuesDiffer = "valuesDiffer";
	String descr = "descr";

	Object getNewValue();
	void setNewValue(Object newValue);

	Object getOldValue();
	void setOldValue(Object oldValue);

	boolean getValuesDiffer();
	void setValuesDiffer(boolean valuesDiffer);

	String getPropertyPath();
	void setPropertyPath(String propertyPath);

	String getDescription(); //Description!
	void setDescription(String description);

	static Diff createDiff(String propertyPath, Object first, Object second, boolean valuesDiffer, String description) {
		Diff diff = Diff.T.create();
		diff.setPropertyPath(propertyPath);
		diff.setOldValue(first);
		diff.setNewValue(second);
		diff.setValuesDiffer(valuesDiffer);
		diff.setDescription(description);
		return diff;
	}

	default String toStringRecord() {
		return "property path: " + getPropertyPath() + (getValuesDiffer() ? ", VALUE CHANGED, " : ", ") + getDescription() + ", was: " + getOldValue()
				+ ", now: " + getNewValue();
	}
}
