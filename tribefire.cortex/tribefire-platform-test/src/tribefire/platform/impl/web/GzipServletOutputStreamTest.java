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
package tribefire.platform.impl.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class GzipServletOutputStreamTest {

	@Test
	public void testGZipServletOutputStream() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[(int) Numbers.MEGABYTE];
		new Random().nextBytes(input);


		GZipServletOutputStream out = new GZipServletOutputStream(baos, 16384, 0, null);
		out.write(input);
		out.close();

		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
		byte[] result = IOTools.slurpBytes(in);

		assertThat(result).isEqualTo(input);

	}

	@Test
	public void testGZipServletOutputStreamPerformance() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[(int) Numbers.MEGABYTE];
		new Random().nextBytes(input);

		int[] sizes = {16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
		int iterations = 5;
		for (int i=0; i<iterations; ++i) {
			System.out.println("Iteration: "+i);

			for (int s=0; s<sizes.length; ++s) {

				Instant start = NanoClock.INSTANCE.instant();
				GZipServletOutputStream out = new GZipServletOutputStream(baos, sizes[s], 0, null);
				out.write(input);
				out.close();
				System.out.println("Size: "+sizes[s]+": "+StringTools.prettyPrintDuration(start, true, ChronoUnit.NANOS));
			}
			System.out.println("\n\n");
		}
	}
	
	@Test
	public void testOverThreshold() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[(int) Numbers.MEGABYTE];
		new Random().nextBytes(input);


		GZipServletOutputStream out = new GZipServletOutputStream(baos, 16384, 1024, null);
		out.write(input);
		out.close();

		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
		byte[] result = IOTools.slurpBytes(in);

		assertThat(result).isEqualTo(input);
		
	}
	
	@Test
	public void testUnderThreshold() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[512];
		new Random().nextBytes(input);


		GZipServletOutputStream out = new GZipServletOutputStream(baos, 16384, 1024, null);
		out.write(input);
		out.close();

		assertThat(baos.toByteArray()).isEqualTo(input);
		
	}
	
	@Test
	public void testSwitchToZipCallback() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[2048];
		new Random().nextBytes(input);

		final AtomicInteger receivedZipEvent = new AtomicInteger(0);
		
		Consumer<Boolean> acceptor = b -> {
			receivedZipEvent.incrementAndGet();
		};

		GZipServletOutputStream out = new GZipServletOutputStream(baos, 16384, 1024, acceptor);
		out.write(input);
		out.close();

		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
		byte[] result = IOTools.slurpBytes(in);

		assertThat(result).isEqualTo(input);
		
		assertThat(receivedZipEvent.intValue()).isEqualTo(1);
	}
	
	@Test
	public void testNoSwitchToZipCallback() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] input = new byte[512];
		new Random().nextBytes(input);

		final AtomicInteger receivedZipEvent = new AtomicInteger(0);
		
		Consumer<Boolean> acceptor = b -> {
			receivedZipEvent.incrementAndGet();
		};

		GZipServletOutputStream out = new GZipServletOutputStream(baos, 16384, 1024, acceptor);
		out.write(input);
		out.close();

		assertThat(baos.toByteArray()).isEqualTo(input);
		
		assertThat(receivedZipEvent.intValue()).isEqualTo(0);
	}
}
