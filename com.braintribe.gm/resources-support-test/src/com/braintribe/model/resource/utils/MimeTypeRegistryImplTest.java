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
package com.braintribe.model.resource.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MimeTypeRegistryImplTest {

	@Test
	public void testRegistry() {

		MimeTypeRegistryImpl registry = new MimeTypeRegistryImpl();

		registry.registerMapping("audio/mp4", "m4a");
		registry.registerMapping("audio/mp4", "mp4a");

		assertThat(registry.getExtensions("audio/mp4")).hasSize(2);
		assertThat(registry.getExtensions("audio/mp4")).contains("m4a", "mp4a");
		assertThat(registry.getMimeTypes("m4a")).hasSize(1);
		assertThat(registry.getMimeTypes("m4a")).contains("audio/mp4");

		registry.registerMapping("video/x-mpeg", "mp3");
		registry.registerMapping("audio/mpeg", "mp3");
		registry.registerMapping("audio/mpeg3", "mp3");

		assertThat(registry.getMimeTypes("mp3")).hasSize(3);
		assertThat(registry.getMimeTypes("mp3")).contains("video/x-mpeg", "audio/mpeg", "audio/mpeg3");

	}

}
