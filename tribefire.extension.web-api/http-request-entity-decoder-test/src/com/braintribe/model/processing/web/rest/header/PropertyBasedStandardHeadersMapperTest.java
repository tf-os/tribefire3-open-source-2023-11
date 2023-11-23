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
package com.braintribe.model.processing.web.rest.header;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.web.rest.MockHttpServletRequestBuilder;
import com.braintribe.model.processing.web.rest.model.CustomPropertiesEntity;
import com.braintribe.model.processing.web.rest.model.MixedPropertiesEntity;
import com.braintribe.model.processing.web.rest.model.StandardHttpHeadersEntity;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.MapTools;

public class PropertyBasedStandardHeadersMapperTest {

	@Test
	public void assignStringProperty() {
		CustomPropertiesEntity entity = assignFor(CustomPropertiesEntity.T, "eTag", "stringProperty", "E-Tag", "value");
		Assert.assertEquals("eTag should be mapped to stringProperty", "value", entity.getStringProperty());
	}

	@Test
	public void assignListProperty() {
		StandardHttpHeadersEntity entity = assignFor(StandardHttpHeadersEntity.T, "acceptRange", "accept", "Accept-Range", "ar1, ar2", "ar3");
		Assert.assertEquals("acceptRange should be mapped to accept", CollectionTools.getList("ar1", "ar2", "ar3"), entity.getAccept());
	}

	@Test
	public void assignSetProperty() {
		StandardHttpHeadersEntity entity = assignFor(StandardHttpHeadersEntity.T, "acceptRange", "acceptEncoding", "Accept-Range", "ar1, ar2", "ar3");
		Assert.assertEquals("acceptRange should be mapped to acceptEncoding", CollectionTools.getSet("ar1", "ar2", "ar3"), entity.getAcceptEncoding());
	}

	@Test
	public void assignIntProperty() {
		MixedPropertiesEntity entity = assignFor(MixedPropertiesEntity.T, "age", "intProperty", "Age", "12345");
		Assert.assertEquals("age should be mapped to intProperty", 12345, entity.getIntProperty());
	}

	@Test
	public void assignDateProperty() {
		StandardHttpHeadersEntity entity = assignFor(StandardHttpHeadersEntity.T, "date", "date", "date", "Wed, 15 Nov 1995 06:25:24 GMT");
		Assert.assertEquals("date should be mapped to date", DateTools.parseDate("1995-11-15T06:25:24.000+0000"), entity.getDate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIntHeader() {
		assignFor(CustomPropertiesEntity.T, "age", "stringProperty", "age", "12345");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkListHeader() {
		assignFor(CustomPropertiesEntity.T, "accept", "stringProperty", "accept", "value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkDateHeader() {
		assignFor(CustomPropertiesEntity.T, "date", "stringProperty", "date", "Wed, 15 Nov 1995 06:25:24 GMT");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkStringHeader() {
		assignFor(CustomPropertiesEntity.T, "contentLocation", "booleanProperty", "Content-Location", "value");
	}
	
	private <T extends GenericEntity> T assignFor(EntityType<T> entityType, String propertyKey, String propertyName, String headerName, String ...headerValues) {
		//@formatter:off
		Map<String, Property> properties = MapTools.getParameterizedMap(
				String.class, Property.class, propertyKey, entityType.getProperty(propertyName));
		//@formatter:on
		
		PropertyBasedStandardHeadersMapper<T> mapper = new PropertyBasedStandardHeadersMapper<>(properties);
		
		//@formatter:off
		HttpServletRequest request = request()
				.header(headerName, headerValues)
				.build();
		//@formatter:on
		
		T target = entityType.create();
		
		mapper.assign(request, headerName, target);
		
		return target;
	}

	private MockHttpServletRequestBuilder request() {
		return new MockHttpServletRequestBuilder();
	}
}
