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
package com.braintribe.transport.http;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.braintribe.utils.stream.MemoryThresholdBuffer;

public class MemoryThresholdBufferEntityTest {

	@Test
	public void testBuffer() throws Exception {
		
		byte[] bytes = "Hello, World!".getBytes("UTF-8");
		
		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer();
		buffer.write(bytes);
		buffer.close();
		
		MemoryThresholdBufferEntity entity = new MemoryThresholdBufferEntity(buffer);
		
		assertThat(entity.isRepeatable()).isTrue();
		assertThat(entity.isStreaming()).isTrue();
		assertThat(entity.getContentLength()).isEqualTo(bytes.length);
		assertThat(entity.getContent()).hasSameContentAs(new ByteArrayInputStream(bytes));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.writeTo(baos);
		assertThat(new ByteArrayInputStream(baos.toByteArray())).hasSameContentAs(new ByteArrayInputStream(bytes));
	}
	
	@Test
	public void testBufferThresholdExceeds() throws Exception {
		
		byte[] bytes = "Hello, World!".getBytes("UTF-8");
		
		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(5);
		buffer.write(bytes);
		buffer.close();
		
		MemoryThresholdBufferEntity entity = new MemoryThresholdBufferEntity(buffer);
		
		assertThat(entity.isRepeatable()).isFalse();
		assertThat(entity.isStreaming()).isTrue();
		assertThat(entity.getContentLength()).isEqualTo(bytes.length);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.writeTo(baos);
		assertThat(new ByteArrayInputStream(baos.toByteArray())).hasSameContentAs(new ByteArrayInputStream(bytes));
	}
}
