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

/**
 *
 */
public enum DefaultAttribute {

	// BEFORE execution
	ATTRIBUTE_TYPE("BEFORE"),
	ATTRIBUTE_ENTITY_TYPE("BEFORE"),
	ATTRIBUTE_REQUEST("BEFORE"),
	ATTRIBUTE_SESSION_ID("BEFORE"),
	ATTRIBUTE_DOMAIN_ID("BEFORE"),
	ATTRIBUTE_USER("BEFORE"),
	ATTRIBUTE_ROLES("BEFORE"),
	ATTRIBUTE_PARTITION("BEFORE"),
	ATTRIBUTE_INSTANCE_ID("BEFORE"),
	ATTRIBUTE_NODE_ID("BEFORE"),
	ATTRIBUTE_APPLICATION_ID("BEFORE"),
	ATTRIBUTE_HOST_ADDRESS_IPV4("BEFORE"),
	ATTRIBUTE_HOST_ADDRESS_IPV6("BEFORE"),
	ATTRIBUTE_TIMESTAMP("BEFORE"),
	ATTRIBUTE_TIMESTAMP_ISO8601("BEFORE"),

	// ERROR execution
	ATTRIBUTE_STACK("ERROR"),
	ATTRIBUTE_ERROR("ERROR", "error"),

	// AFTER execution
	ATTRIBUTE_RESULT("AFTER"),
	ATTRIBUTE_SERVICE_DURATION("AFTER"),
	ATTRIBUTE_TRACING_OVERHEAD("AFTER"),
	ATTRIBUTE_NOTIFICATION_MESSAGE("AFTER"),
	ATTRIBUTE_NOTIFICATION_DETAIL_MESSAGE("AFTER");

	// -----------------------------------------------------------------------

	private final String label;
	private final String value;

	private DefaultAttribute(String label) {
		this.label = label;
		this.value = null;
	}
	private DefaultAttribute(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public String label() {
		return label;
	}

	public String value() {
		if (value == null) {
			return this.toString();
		}
		return value;
	}

}
