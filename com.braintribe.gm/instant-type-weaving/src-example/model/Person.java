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
package model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.Transient;

/**
 * @author peter.gazdik
 */
@SelectiveInformation("Prefix_${name}_${father.name}_${#id}_Suffix")
public interface Person extends GenericEntity {

	String getName();
	void setName(String value);

	long getCount();
	void setCount(long value);

	Person getFather();
	void setFather(Person value);

	@Transient
	String getTransientName();
	void setTransientName(String transientName);
}
