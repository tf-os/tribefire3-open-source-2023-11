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
package com.braintribe.util.servlet.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.util.servlet.remote.FakeHttpServletRequest;

public class ServletToolsTest {

	@Test
	public void testAcceptHeaders() throws Exception {
	
		String[] accepts = new String[] {"a/b, c/d", "e/f"};
		FakeHttpServletRequest request = createRequest(accepts);
		List<String> types = ServletTools.getAcceptedMimeTypes(request);
		compareTypes(new String[] {"a/b", "c/d", "e/f"}, types);
		
		accepts = new String[] {"a/b", "c/d", "e/f"};
		request = createRequest(accepts);
		types = ServletTools.getAcceptedMimeTypes(request);
		compareTypes(new String[] {"a/b", "c/d", "e/f"}, types);

		accepts = new String[] {"a/b", "c/d", "e/f; q=0.5"};
		request = createRequest(accepts);
		types = ServletTools.getAcceptedMimeTypes(request);
		compareTypes(new String[] {"a/b", "c/d", "e/f"}, types);

		accepts = new String[] {"c/d; q=0.5, e/f; q=0.3", "a/b; q=1"};
		request = createRequest(accepts);
		types = ServletTools.getAcceptedMimeTypes(request);
		compareTypes(new String[] {"a/b", "c/d", "e/f"}, types);

		accepts = new String[] {"c/d; q=0.5, e/f", "a/b; q=1"};
		request = createRequest(accepts);
		types = ServletTools.getAcceptedMimeTypes(request);
		compareTypes(new String[] {"e/f", "a/b", "c/d"}, types);

	}
	
	private static void compareTypes(String[] expected, List<String> actual) {
		assertThat(actual.size()).isEqualTo(expected.length);
		for (int i=0; i<expected.length; ++i) {
			assertThat(actual.get(i)).isEqualTo(expected[i]);
		}
	}
	
	private static FakeHttpServletRequest createRequest(String... accepts) {
		FakeHttpServletRequest request = new FakeHttpServletRequest(null);
		for (String a : accepts) {
			request.addHeader("Accept", a);
		}
		return request;
	}
	
	@Test
	public void testParameters() throws Exception {
		FakeHttpServletRequest request = new FakeHttpServletRequest(null);
		request.addParameter("hello", "world");
		request.addParameters("key", new String[] {"value1", "value2"});
		
		assertThat(ServletTools.getSingleParameter(request, "hello", null)).isEqualTo("world");
		assertThat(ServletTools.getSingleParameter(request, "key", null)).isEqualTo("value1");
	}
}
