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
package com.braintribe.model.processing.itw.synthesis.java.jar;

import org.junit.Test;

import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class EntityDebugInfoTests extends ImportantItwTestSuperType {

	StringBuilder sb = new StringBuilder();

	@Test
	public void checkWorks() {
		addLine("class X {");
		addLine("");
		addLine("  public String getValue(){");
		addLine("    return null;");
		addLine("  }");
		addLine("");
		addLine("  public void setValue(String otherValue ) {");
		addLine("    this.value = otherValue;");
		addLine("  }");
		addLine("");
		addLine("}");

		EntityDebugInfo info = new EntityDebugInfo(sb.toString());

		BtAssertions.assertThat(info.getMethodLine("getValue")).isEqualTo(4);
		BtAssertions.assertThat(info.getMethodLine("setValue")).isEqualTo(8);
		BtAssertions.assertThat(info.getSetterParameterName("setValue")).isEqualTo("otherValue");

	}

	private void addLine(String s) {
		sb.append(s);
		sb.append("\n");
	}

}
