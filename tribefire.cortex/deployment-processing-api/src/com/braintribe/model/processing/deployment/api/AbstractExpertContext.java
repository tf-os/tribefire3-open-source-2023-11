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
package com.braintribe.model.processing.deployment.api;

import com.braintribe.model.deployment.Deployable;

public abstract class AbstractExpertContext<D extends Deployable> implements ExpertContext<D> {

	@Override
	public int hashCode() {
		String externalId = getDeployableExternalId();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ExpertContext<?>))
			return false;

		ExpertContext<?> other = (ExpertContext<?>) obj;
		String externalId = getDeployableExternalId();
		String otherExternalId = other.getDeployableExternalId();
		if (externalId == null) {
			if (otherExternalId != null)
				return false;
		} else if (!externalId.equals(otherExternalId))
			return false;
		return true;
	}

}
