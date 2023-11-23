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
package tribefire.extension.tracing.model.deployment.service;

public enum CustomValueAttribute {
	ATTRIBUTE_COMPONENT_NAME("BEFORE"),
	ATTRIBUTE_TENANT("BEFORE");

	// -----------------------------------------------------------------------

	private final String label;
	private final String value;

	private CustomValueAttribute(String label) {
		this.label = label;
		this.value = null;
	}
	private CustomValueAttribute(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public String label() {
		return label;
	}

	public String value() {
		return value;
	}

}
