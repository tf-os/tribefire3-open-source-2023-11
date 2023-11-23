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
package com.braintribe.model.processing.service.impl.weaving.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.service.impl.weaving.test.model.SubRequest1;
import com.braintribe.model.processing.service.impl.weaving.test.model.SubRequest2;
import com.braintribe.model.processing.service.impl.weaving.test.model.SubRequest3;
import com.braintribe.model.processing.service.impl.weaving.test.model.SubSubRequest2;


public class DispatchTest {
	@Test
	public void testDirect() throws Exception {
		TestServiceProcessor processor = new TestServiceProcessor();
		String result1 = processor.process(null, SubRequest1.T.create());
		String result2 = processor.process(null, SubRequest2.T.create());
		
		assertThat(result1).isEqualTo("subRequest1");
		assertThat(result2).isEqualTo("subRequest2");
	}
	
	@Test
	public void testPolymorphic() throws Exception {
		TestServiceProcessor processor = new TestServiceProcessor();
		String result = processor.process(null, SubSubRequest2.T.create());
		
		assertThat(result).isEqualTo("subRequest2");
	}
	
	@Test(expected=UnsupportedRequestTypeException.class)
	public void testUnmapped() throws Exception {
		TestServiceProcessor processor = new TestServiceProcessor();
		processor.process(null, SubRequest3.T.create());
	}
}
