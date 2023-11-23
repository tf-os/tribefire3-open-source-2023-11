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
package com.braintribe.codec.marshaller.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BasicConfigurableMarshallerRegistryTest {

	@Test
	public void testRegistrySimple() {
		BasicConfigurableMarshallerRegistry registry = new BasicConfigurableMarshallerRegistry();

		registry.registerMarshaller("text/plain", DummyMarshaller.instance);

		assertThat(registry.getMarshaller("text/plain")).isSameAs(DummyMarshaller.instance);
	}

	@Test
	public void testRegistryWithParams() {
		BasicConfigurableMarshallerRegistry registry = new BasicConfigurableMarshallerRegistry();

		registry.registerMarshaller("text/plain", DummyMarshaller.instance);

		assertThat(registry.getMarshaller("text/plain; encoding=UTF-8")).isSameAs(DummyMarshaller.instance);
	}

	@Test
	public void testRegistryWithoutParams() {
		BasicConfigurableMarshallerRegistry registry = new BasicConfigurableMarshallerRegistry();

		registry.registerMarshaller("text/plain; encoding=UTF-8", DummyMarshaller.instance);

		assertThat(registry.getMarshaller("text/plain")).isSameAs(DummyMarshaller.instance);
	}

	@Test
	public void testRegistryWithParams2() {
		BasicConfigurableMarshallerRegistry registry = new BasicConfigurableMarshallerRegistry();

		registry.registerMarshaller("text/plain; encoding=UTF-8", DummyMarshaller.instance);

		assertThat(registry.getMarshaller("text/plain; encoding=ISO-8859-1")).isSameAs(DummyMarshaller.instance);
	}
	
	@Test
	public void testRegistryRegex() {
		BasicConfigurableMarshallerRegistry registry = new BasicConfigurableMarshallerRegistry();

		registry.registerMarshaller("text/plain", DummyMarshaller.instance);

		assertThat(registry.getMarshaller("text/*")).isSameAs(DummyMarshaller.instance);
	}
}
