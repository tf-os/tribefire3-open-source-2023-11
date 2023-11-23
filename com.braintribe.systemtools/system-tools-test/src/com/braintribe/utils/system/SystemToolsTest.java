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
package com.braintribe.utils.system;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class SystemToolsTest {

	@Test
	public void testCpuCount() {
		assertThat(0).isLessThan(SystemTools.getAvailableProcessors());
	}

	@Test
	public void testMemory() {
		assertThat(0L).isLessThan(SystemTools.getFreeMemory());
		assertThat(0L).isLessThan(SystemTools.getTotalMemory());
	}

	@Test
	public void testOs() {
		String osInformation = SystemTools.getOperatingSystem();
		assertThat(osInformation).isNotNull();
		assertThat(osInformation).isNotEqualTo("");
	}


	@Test
	public void testPrettyPrintFileSizeBinary() {

		assertThat(SystemTools.prettyPrintBytesBinary(0)).isEqualTo("0 B");
		assertThat(SystemTools.prettyPrintBytesBinary(1024)).isEqualTo("1.0 KiB");
		assertThat(SystemTools.prettyPrintBytesBinary(1048576)).isEqualTo("1.0 MiB");
		assertThat(SystemTools.prettyPrintBytesBinary(1048577)).isEqualTo("1.0 MiB");
		assertThat(SystemTools.prettyPrintBytesBinary(512)).isEqualTo("512 B");
		assertThat(SystemTools.prettyPrintBytesBinary(513)).isEqualTo("513 B");
		assertThat(SystemTools.prettyPrintBytesBinary(1023)).isEqualTo("1023 B");
		assertThat(SystemTools.prettyPrintBytesBinary(1200000)).isEqualTo("1.1 MiB");

	}

	@Test
	public void testPrettyPrintFileSizeDecimal() {

		assertThat(SystemTools.prettyPrintBytesDecimal(0)).isEqualTo("0 B");
		assertThat(SystemTools.prettyPrintBytesDecimal(1000)).isEqualTo("1.0 kB");
		assertThat(SystemTools.prettyPrintBytesDecimal(1001)).isEqualTo("1.0 kB");
		assertThat(SystemTools.prettyPrintBytesDecimal(1000000)).isEqualTo("1.0 MB");
		assertThat(SystemTools.prettyPrintBytesDecimal(512)).isEqualTo("512 B");
		assertThat(SystemTools.prettyPrintBytesDecimal(513)).isEqualTo("513 B");
		assertThat(SystemTools.prettyPrintBytesDecimal(1023)).isEqualTo("1.0 kB");
		assertThat(SystemTools.prettyPrintBytesDecimal(1200000)).isEqualTo("1.2 MB");

	}

	@Test
	public void testGetPrettyPrintFreeSpaceOnDiskDevice() throws IOException {
		File tempFile = File.createTempFile("test", ".txt");
		try {
			String freeSpace = SystemTools.getPrettyPrintFreeSpaceOnDiskDevice(tempFile);
			System.out.println(freeSpace);
			assertThat(freeSpace).isNotNull();
		} finally {
			tempFile.delete();
		}

	}
}
