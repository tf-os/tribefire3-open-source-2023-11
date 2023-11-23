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
package com.braintribe.model.generic.validation.expert;

public class EntitySignatureAndPropertyName {
	
	private String entityTypeSignature;
	private String propertyName;
	
	public EntitySignatureAndPropertyName(String entityTypeSignature, String propertyName) {
		super();
		this.entityTypeSignature = entityTypeSignature;
		this.propertyName = propertyName;
	}
	
	public void setEntityTypeSignature(String entityTypeSignature) {
		this.entityTypeSignature = entityTypeSignature;
	}
	
	public String getEntityTypeSignature() {
		return entityTypeSignature;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityTypeSignature == null) ? 0 : entityTypeSignature.hashCode());
		result = prime * result
				+ ((propertyName == null) ? 0 : propertyName.hashCode());
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
		EntitySignatureAndPropertyName other = (EntitySignatureAndPropertyName) obj;
		if (entityTypeSignature == null) {
			if (other.entityTypeSignature != null)
				return false;
		} else if (!entityTypeSignature.equals(other.entityTypeSignature))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}

}
