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
package com.braintribe.tribefire.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TfDriverTest {

	@Test
	public void testUrlAcceptance() throws Exception {
		
		TfDriver driver = new TfDriver();
		assertThat(driver.acceptsURL("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth")).isEqualTo(true);
		assertThat(driver.acceptsURL("jdbc:derby:../db;user=i2z;password=operating")).isEqualTo(false);
		
	}
	
}
