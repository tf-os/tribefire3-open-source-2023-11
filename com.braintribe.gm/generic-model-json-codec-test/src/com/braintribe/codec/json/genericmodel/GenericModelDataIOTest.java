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
import java.io.FilenameFilter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;


public class GenericModelDataIOTest {
	@Test
	@Category(KnownIssue.class)
	public void testDumps() throws Exception {
		File dumpsDir = new File("./dumps");
		File files[] = dumpsDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		
		GenericModelJsonStringCodec<Object> codec = new GenericModelJsonStringCodec<Object>();
		
		long s,d;
		for (File file: files) {
			s = System.currentTimeMillis();
			String encodedJson = IOTools.slurp(file, "UTF-8");
			d = System.currentTimeMillis() - s;
			System.out.printf("%s read in %d ms\n", file.getName(), d);
			s = System.currentTimeMillis();
			Object value = codec.decode(encodedJson);
			d = System.currentTimeMillis() - s;
			System.out.printf("%s decoded in %d ms\n", file.getName(), d);
			s = System.currentTimeMillis();
			codec.encode(value);
			d = System.currentTimeMillis() - s;
			System.out.printf("%s encoded in %d ms\n", file.getName(), d);
		}
		{
			File file = files[0];
			String encodedJson = IOTools.slurp(file, "UTF-8");
			Object value = codec.decode(encodedJson);
			String reencodedJson = codec.encode(value);
			System.out.println(reencodedJson);
		}
	}
}
