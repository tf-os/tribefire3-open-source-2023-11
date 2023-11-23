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
package com.braintribe.model.processing.test.tools.comparison;

import static org.assertj.core.api.Assertions.assertThat;

import com.braintribe.utils.lcd.StringTools;


public class ComparisonResult {
	private final String message;
	private String customMessage;
	private final boolean areEqual;
	private final Object first, second;
	
	public ComparisonResult(String message, boolean areEqual, Object first, Object second) {
		this.message = message;
		this.areEqual = areEqual;
		this.first = first;
		this.second = second;
	}
	
	public boolean asBoolean() {
		return areEqual;
	}
	
	public String asDetailedMessage() {
		return introduction() + message;
	}
	
	public void assertThatEqual() {
		assertThat(areEqual).as(introduction() + "Error when comparing following objects: " + comparedObjectsDescription() + message).isTrue();
	}
	
	public void assertThatNotEqual() {
		assertThat(areEqual).as(introduction() + "Expected objects to be different but they were equal: " + comparedObjectsDescription()).isFalse();
	}
	
	public ComparisonResult withMessage(String customMessage) {
		this.customMessage = customMessage;
		return this;
	}
	
	private String comparedObjectsDescription() {
		return "\n  >" + StringTools.getStringRepresentation(first) + "\n  >" + StringTools.getStringRepresentation(second) + "\n";
	}
	
	private String introduction() {
		return (customMessage == null ? "" : customMessage) + "\n";
	}
}
