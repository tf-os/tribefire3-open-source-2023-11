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
package com.braintribe.utils;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ThrowableToolsTest {

	@Test
	public void testGetLastMessage() {

		Throwable t = new Throwable("Hello, world");
		assertThat(ThrowableTools.getLastMessage(t)).isEqualTo("Hello, world");

		Throwable t1 = new Throwable("Hello, world");
		Throwable t2 = new Throwable("Some Test", t1);
		assertThat(ThrowableTools.getLastMessage(t2)).isEqualTo("Hello, world");

		Throwable t3 = new Throwable("Layer 3", t2);
		Throwable t4 = new Throwable("Layer 4", t3);
		Throwable t5 = new Throwable("Layer 5", t4);
		Throwable t6 = new Throwable("Layer 6", t5);
		Throwable t7 = new Throwable("Layer 7", t6);

		assertThat(ThrowableTools.getLastMessage(t7)).isEqualTo("Hello, world");

		Throwable t10 = new Throwable("Hello, world");
		t10.addSuppressed(new Throwable("Suppressed 1"));
		Throwable t11 = new Throwable("Some Test", t10);
		t11.addSuppressed(new Throwable("Suppressed 2"));
		assertThat(ThrowableTools.getLastMessage(t11)).isEqualTo("Hello, world");

		Throwable t20 = new Throwable("Hello, world");
		Throwable t21 = new Throwable(t20);
		Throwable t22 = new Throwable("Some Test", t21);
		assertThat(ThrowableTools.getLastMessage(t22)).isEqualTo("Hello, world");

		Throwable t30 = new Throwable();
		Throwable t31 = new Throwable("Hello, world", t30);
		Throwable t32 = new Throwable("Some Test", t31);
		assertThat(ThrowableTools.getLastMessage(t32)).isEqualTo("Hello, world");

		Throwable t40 = new Throwable();
		Throwable t41 = new Throwable("Hello, world", t40);
		Throwable t42 = new Throwable("Some Test", t41);
		// Create cause-loop
		t40.initCause(t42);
		assertThat(ThrowableTools.getLastMessage(t42)).isEqualTo("Hello, world");

	}
}
