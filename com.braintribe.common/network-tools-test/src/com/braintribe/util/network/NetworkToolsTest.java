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
package com.braintribe.util.network;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;


public class NetworkToolsTest {

	@BeforeClass
	public static void beforeClass() throws Exception {
		Instant start = NanoClock.INSTANCE.instant();
		NetworkTools.getIPv4NetworkInterface();
		System.out.println("First run: "+StringTools.prettyPrintDuration(start, true, null));

		start = NanoClock.INSTANCE.instant();
		NetworkTools.getIPv4NetworkInterface();
		System.out.println("Second run: "+StringTools.prettyPrintDuration(start, true, null));

	}
	
	@Test
	public void testGetIPv4NetworkInterface() {
		InetAddress inetAddress = NetworkTools.getIPv4NetworkInterface();
		
		assertThat(inetAddress).isNotNull();
		assertThat(inetAddress).isInstanceOf(Inet4Address.class);
	}

	@Test
	public void testGetIPv6NetworkInterface() {
		InetAddress inetAddress = NetworkTools.getIPv6NetworkInterface();
		
		if (inetAddress != null) {
			assertThat(inetAddress).isInstanceOf(Inet6Address.class);
		}
	}
	
	@Test
	public void printNetworks() {
		List<NetworkDetectionContext> networkDetectionContexts = NetworkTools.getNetworkDetectionContexts();
		
		assertThat(networkDetectionContexts).isNotNull();
		assertThat(networkDetectionContexts).isNotEmpty();
		
		for (NetworkDetectionContext context : networkDetectionContexts) {
			System.out.println(context.toString());
		}
	}
}
