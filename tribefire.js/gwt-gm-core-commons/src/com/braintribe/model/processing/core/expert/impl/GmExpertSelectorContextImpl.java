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
package com.braintribe.model.processing.core.expert.impl;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.core.expert.api.GmExpertSelectorContext;

class GmExpertSelectorContextImpl implements GmExpertSelectorContext {
	
	private final Class<?> expertType;
	private final GenericModelType denotationType;
	private final GenericEntity denotationInstance;
	
	/**
	 * Creates a new <code>ExpertKey</code> instance.
	 * 
	 * @param expertClass
	 *            the expert (super) type. If there e.g. exists an interface <code>PersonExpert</code> and a concrete
	 *            implementation <code>EmployeeExpert</code> one would pass the <code>PersonExpert</code> class.
	 */
	public GmExpertSelectorContextImpl(Class<?> expertClass, GenericModelType denotationType, GenericEntity denotionationInstance) {
		this.expertType = expertClass;
		this.denotationType = denotationType;
		this.denotationInstance = denotionationInstance;
	}
	
	public Class<?> getExpertType() {
		return expertType;
	}
	
	@Override
	public GenericEntity getDenotationInstance() {
		return denotationInstance;
	}
	
	@Override
	public GenericModelType getDenotationType() {
		return denotationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expertType == null) ? 0 : expertType.hashCode());
		result = prime * result
				+ ((denotationType == null) ? 0 : denotationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		GmExpertSelectorContextImpl other = (GmExpertSelectorContextImpl) obj;
		return expertType == other.expertType && denotationType == other.denotationType;
	}

}
