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
package com.braintribe.utils.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

public class TeePrintWriterTest {

	@Test
	public void testTeePrintWriter() {

		StringWriter mainRecipient = new StringWriter();
		StringWriter backupRecipient = new StringWriter();

		PrintWriter mainRecipientPrintWriter = new PrintWriter(mainRecipient);

		try (TeePrintWriter tw = new TeePrintWriter(mainRecipientPrintWriter, backupRecipient)) {
			tw.write("hello, world");
			tw.flush();
		}

		assertThat(mainRecipient.toString()).isEqualTo("hello, world");
		assertThat(backupRecipient.toString()).isEqualTo("hello, world");

	}

	@Test
	public void testTeePrintWriterWithStopTee() {

		StringWriter mainRecipient = new StringWriter();
		StringWriter backupRecipient = new StringWriter();

		PrintWriter mainRecipientPrintWriter = new PrintWriter(mainRecipient);

		try (TeePrintWriter tw = new TeePrintWriter(mainRecipientPrintWriter, backupRecipient)) {
			tw.write("hello");
			tw.stopTee();
			tw.write(", world");
			tw.flush();
		}

		assertThat(mainRecipient.toString()).isEqualTo("hello, world");
		assertThat(backupRecipient.toString()).isEqualTo("hello");

	}
}
