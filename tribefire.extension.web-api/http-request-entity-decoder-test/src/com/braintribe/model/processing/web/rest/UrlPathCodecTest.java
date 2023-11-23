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
package com.braintribe.model.processing.web.rest;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.web.rest.model.TestEntityWithStringProperties;

public class UrlPathCodecTest {

	//@formatter:off
	private static final UrlPathCodec<GenericEntity> CODEC = UrlPathCodec.create()
			.mappedSegment("property1").constantSegment("c1").mappedSegment("property2");
	//@formatter:on

	//@formatter:off
	private static final UrlPathCodec<GenericEntity> CODEC_OPTIONAL = UrlPathCodec.create()
			.mappedSegment("property1", true).constantSegment("c1").mappedSegment("property2");
	//@formatter:on

	//@formatter:off
	private static final UrlPathCodec<GenericEntity> CODEC_LONG = UrlPathCodec.create()
			.mappedSegment("property1").constantSegment("c1").constantSegment("c2").constantSegment("c3")
			.mappedSegment("property2").constantSegment("c4").constantSegment("c5");
	//@formatter:on

	@Test
	public void decode() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();

		CODEC.decode(() -> entity, "aaa/bbb/ccc/ddd");

		Assert.assertEquals("aaa", entity.getProperty1());
		Assert.assertEquals("ccc", entity.getProperty2());
		Assert.assertNull(entity.getProperty3());
	}

	@Test
	public void decodeOptional() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();

		CODEC_OPTIONAL.decode(() -> entity, "aaa/bbb/ccc");

		Assert.assertEquals("aaa", entity.getProperty1());
		Assert.assertEquals("ccc", entity.getProperty2());
		Assert.assertNull(entity.getProperty3());
	}

	@Test
	public void decodeOptionalNotPresent() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();

		CODEC_OPTIONAL.decode(() -> entity, "aaa/bbb");

		Assert.assertNull(entity.getProperty1());
		Assert.assertEquals("bbb", entity.getProperty2());
		Assert.assertNull(entity.getProperty3());
	}
	
	@Test
	public void decodeLongUrl() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();

		CODEC.decode(() -> entity, "aaa/bbb/ccc/ddd/eee/fff/ggg");

		Assert.assertEquals("aaa", entity.getProperty1());
		Assert.assertEquals("ccc", entity.getProperty2());
		Assert.assertNull(entity.getProperty3());
	}

	@Test
	public void decodeTooManySegments() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();

		CODEC_LONG.decode(() -> entity, "aaa/bbb/ccc");

		Assert.assertEquals("aaa", entity.getProperty1());
		Assert.assertNull(entity.getProperty2());
		Assert.assertNull(entity.getProperty3());
	}

	@Test
	public void encode() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();
		entity.setProperty1("property1");
		entity.setProperty2("property2");

		String value = CODEC.encode(entity);

		Assert.assertEquals("property1/c1/property2", value);
	}

	@Test
	public void encodeNullProperty() {
		TestEntityWithStringProperties entity = TestEntityWithStringProperties.T.create();
		entity.setProperty2("property2");

		String value = CODEC.encode(entity);

		Assert.assertEquals("c1/property2", value);
	}
}
