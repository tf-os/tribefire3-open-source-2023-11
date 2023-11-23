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
package com.braintribe.model.generic.manipulation.accessor;

public class ExpertKey {
	
	private Class<?> expertClass;
	private Class<?> handledClass;
	
	/**
	 * Creates a new <code>ExpertKey</code> instance.
	 * 
	 * @param expertClass
	 *            the expert (super) type. If there e.g. exists an interface <code>PersonExpert</code> and a concrete
	 *            implementation <code>EmployeeExpert</code> one would pass the <code>PersonExpert</code> class.
	 * @param handledClass
	 *            the class handled by the
	 */
	public ExpertKey(Class<?> expertClass, Class<?> handledClass) {
		this.expertClass = expertClass;
		this.handledClass = handledClass;
	}
	
	public Class<?> getExpertClass() {
		return expertClass;
	}
	
	public Class<?> getHandledClass() {
		return handledClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expertClass == null) ? 0 : expertClass.hashCode());
		result = prime * result
				+ ((handledClass == null) ? 0 : handledClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpertKey other = (ExpertKey) obj;
		if (expertClass == null) {
			if (other.expertClass != null)
				return false;
		} else if (expertClass != other.expertClass)
			return false;
		if (handledClass == null) {
			if (other.handledClass != null)
				return false;
		} else if (handledClass != other.handledClass)
			return false;
		return true;
	}
	
}
