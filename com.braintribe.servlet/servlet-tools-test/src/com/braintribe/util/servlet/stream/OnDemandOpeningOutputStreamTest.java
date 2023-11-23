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
package com.braintribe.util.servlet.stream;

import org.junit.Test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

public class OnDemandOpeningOutputStreamTest {

	@Test
	public void testOpenStream() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		
		odoos.write(13);
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(new byte[] {13});
		assertThat(response.internalGetOutputStream().isClosed()).isFalse();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}

	@Test
	public void testOpenStreamByteArray() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		
		String text = "Hello, world!";
		byte[] data = text.getBytes("UTF-8");
		odoos.write(data);
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(data);
		assertThat(response.internalGetOutputStream().isClosed()).isFalse();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}
	
	@Test
	public void testOpenStreamByteArrayWithParams() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		
		String text = "Hello, world!";
		byte[] data = text.getBytes("UTF-8");
		odoos.write(data, 0, data.length);
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(data);
		assertThat(response.internalGetOutputStream().isClosed()).isFalse();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}
	
	@Test
	public void testOpenStreamFlush() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		odoos.flush();
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(new byte[0]);
		assertThat(response.internalGetOutputStream().isClosed()).isFalse();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}
	
	@Test
	public void testOpenStreamClose() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		odoos.close();
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(new byte[0]);
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}
	
	@Test
	public void testOpenStreamString() throws Exception {

		FakeServletResponse response = new FakeServletResponse();
		OnDemandOpeningOutputStream odoos = new OnDemandOpeningOutputStream(response);
		
		assertThat(response.getOuputStreamOpened()).isFalse();
		
		String text = "Hello, world!"; //Not interested in the encoding here; not point of this test
		odoos.print(text);
		
		assertThat(response.getOuputStreamOpened()).isTrue();
		assertThat(response.internalGetOutputStream().getData()).isEqualTo(text.getBytes("UTF-8"));
		assertThat(response.internalGetOutputStream().isClosed()).isFalse();
		
		odoos.close();
		
		assertThat(response.internalGetOutputStream().isClosed()).isTrue();
	}
	
}
