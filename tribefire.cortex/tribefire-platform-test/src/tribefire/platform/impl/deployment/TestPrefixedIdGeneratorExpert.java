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
package tribefire.platform.impl.deployment;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author christina.wilpernig
 */
public class TestPrefixedIdGeneratorExpert implements Supplier<String> {

	private String prefix;
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String get() {
		return prefix + "_" + UUID.randomUUID().toString();
	}

}
