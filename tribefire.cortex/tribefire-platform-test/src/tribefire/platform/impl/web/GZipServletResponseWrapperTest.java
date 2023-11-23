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
import java.io.OutputStream;
import java.time.Instant;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.servlet.test.mock.MockHttpServletResponse;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class GZipServletResponseWrapperTest {

	@Test
	public void testHeaderWhenZipping() throws Exception {

		MockHttpServletResponse response = new MockHttpServletResponse();

		byte[] input = new byte[2048];
		new Random().nextBytes(input);

		GZipServletResponseWrapper wrapper = new GZipServletResponseWrapper(response, 16384, 1024);

		OutputStream out = wrapper.getOutputStream();
		out.write(input);
		out.close();


		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(response.getBytes()));
		byte[] result = IOTools.slurpBytes(in);

		assertThat(result).isEqualTo(input);

		assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");


	}

	@Test
	@Ignore
	public void labTestVariousThresholds() throws Exception {

		Random rnd = new Random();
		int runs = 10;

		for (int j=0; j<runs; ++j) {
			System.out.println("\n\nRun: "+j);
			int[] thresholds = new int[] {256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
			int iterations = 100;
			byte[][] data = new byte[iterations][];
			for (int i=0; i<iterations; ++i) {
				int size = rnd.nextInt(250000); 
				data[i] = new byte[size];
				rnd.nextBytes(data[i]);

			}

			for (int threshold : thresholds) {

				Instant start = NanoClock.INSTANCE.instant();
				for (int i=0; i<iterations; ++i) {


					MockHttpServletResponse response = new MockHttpServletResponse();
					GZipServletResponseWrapper wrapper = new GZipServletResponseWrapper(response, 16384, threshold);

					OutputStream out = wrapper.getOutputStream();
					out.write(data[i]);
					out.close();

				}
				System.out.println(""+threshold+":\t"+StringTools.prettyPrintDuration(start, true, null));
			}

		}

	}


	@Test
	public void testNoHeaderWhenNotZipping() throws Exception {

		MockHttpServletResponse response = new MockHttpServletResponse();

		byte[] input = new byte[512];
		new Random().nextBytes(input);

		GZipServletResponseWrapper wrapper = new GZipServletResponseWrapper(response, 16384, 1024);

		OutputStream out = wrapper.getOutputStream();
		out.write(input);
		out.close();

		assertThat(response.getBytes()).isEqualTo(input);

		assertThat(response.getHeader("Content-Encoding")).isNull();


	}
}
