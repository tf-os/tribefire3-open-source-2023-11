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
package com.braintribe.codec.json.genericmodel;

import java.io.File;

import org.junit.Test;

import com.braintribe.utils.IOTools;


public class GenericModelJsonCodecAiTest {
	@Test
	public void testDumps() throws Exception {
		File dataFile = new File("./data/ai.json");
		
		String encodedData = IOTools.slurp(dataFile, "UTF-8");
		GenericModelJsonStringCodec<Object> codec = new GenericModelJsonStringCodec<Object>();
		codec.setAssignAbsenceInformationForMissingProperties(true);
		
		Object value = codec.decode(encodedData);
		
		String reencoded = codec.encode(value);
		System.out.println(reencoded);
	}
}
