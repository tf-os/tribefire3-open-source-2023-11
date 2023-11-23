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
package com.braintribe.util.servlet.remote;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.util.servlet.exception.InvalidForwardedHeader;
import com.braintribe.util.servlet.exception.RemoteHostNotTrustedException;

public class RemoteClientAddressResolverTest {

	@Test
	public void testRemoteAddressOnly() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");

		ar = createResolver(true, true, true, false, "127.0.0.1");
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");
		
		ar = createResolver(true, true, true, false, "127.0.0.2");
		try {
			verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");
			throw new AssertionError("The remote address should not have been trusted.");
		} catch(RemoteHostNotTrustedException nt) {
			//expected
		}
		
		ar = createResolver(true, true, true, true, "127.0.0.1");
		assertThat(ar.getRemoteIpLenient(null)).isNull();
	}

	@Test
	public void testXForwardedForAddress() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", "127.0.0.2, 127.0.0.3");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.2");

		
		ar = createResolver(false, true, true, false);
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", "127.0.0.2, 127.0.0.3");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");

	}
	
	@Test
	public void testCustomField() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		List<String> customFields = new ArrayList<>();
		customFields.add("X-Client-IP");
		ar.setCustomClientIpHeaders(customFields);
		
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", "127.0.0.2, 127.0.0.3");
		r.addHeader("X-Client-IP", "127.0.0.4");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.4");

	}

	@Test
	public void testXRealIpAddress() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Real-IP", "127.0.0.4");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.4");
		
		ar = createResolver(true, true, false, false);
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");

	}

	@Test
	public void testForwardedAddress() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=\"127.0.0.2\"");
		r.addHeader("Forwarded", "For=\"[2001:db8:cafe::17]:4711\"");
		r.addHeader("Forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
		r.addHeader("Forwarded", "for=192.0.2.43, for=198.51.100.17");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.2");

		
		ar = createResolver(true, false, true, false);
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.1");

	}

	@Test
	public void testXForwardedFor() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", "127.0.0.2, 127.0.0.3");
		
		RemoteAddressInformation rai = ar.getRemoteAddressInformation(r);
		List<String> expected = new ArrayList<>();
		expected.add("127.0.0.2");
		expected.add("127.0.0.3");
		assertThat(rai.getXForwardedFor()).isEqualTo(expected);
		

		r = new FakeHttpServletRequest("127.0.0.1");
		rai = ar.getRemoteAddressInformation(r);
		assertThat(rai.getXForwardedFor()).isNull();

		
		ar = createResolver(true, true, true, false);
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", ", 127.0.0.3");
		
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.3");
	}
	
	@Test
	public void testForwarded() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=\"127.0.0.2\"");
		r.addHeader("Forwarded", "For=\"[2001:db8:cafe::17]:4711\"");
		r.addHeader("Forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
		r.addHeader("Forwarded", "for=192.0.2.43;host=localhost, for=198.51.100.17");
		
		RemoteAddressInformation rai = ar.getRemoteAddressInformation(r);
		List<Forwarded> forwarded = rai.getForwarded();
		
		checkForwarded(forwarded.get(0), "127.0.0.2", null, null, null);
		checkForwarded(forwarded.get(1), "[2001:db8:cafe::17]:4711", null, null, null);
		checkForwarded(forwarded.get(2), "192.0.2.60", "203.0.113.43", null, "http");
		checkForwarded(forwarded.get(3), "192.0.2.43", null, "localhost", null);
		checkForwarded(forwarded.get(4), "198.51.100.17", null, null, null);
	}
	
	@Test
	public void testForwardedNonLenient() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=\"127.0.0.2");
		
		try {
			ar.getRemoteAddressInformation(r);
			throw new AssertionError("The Forwarded header should not have been parsed.");
		} catch(InvalidForwardedHeader ih) {
			//expected
		}
		
		
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=127.0.0.2;hello=world");
		
		try {
			ar.getRemoteAddressInformation(r);
			throw new AssertionError("The Forwarded header should not have been parsed.");
		} catch(InvalidForwardedHeader ih) {
			//expected
		}
		
		
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=127.0.0.2;helloworld");
		
		try {
			ar.getRemoteAddressInformation(r);
			throw new AssertionError("The Forwarded header should not have been parsed.");
		} catch(InvalidForwardedHeader ih) {
			//expected
		}

		
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=\"127.0.0.2;hello,by=world\"");
		verifyAddress(ar.getRemoteAddressInformation(r), "127.0.0.2;hello,by=world");
	}
	
	
	@Test
	public void testForwardedLenient() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, true);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=\"127.0.0.2");
		
		ar.getRemoteAddressInformation(r);
		
		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=127.0.0.2;hello=world");
		
		ar.getRemoteAddressInformation(r);

		r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("Forwarded", "for=127.0.0.2;helloworld");
		
		ar.getRemoteAddressInformation(r);

	}
	
	@Test
	public void testToString() throws Exception {
		
		StandardRemoteClientAddressResolver ar = createResolver(true, true, true, false);
		FakeHttpServletRequest r = new FakeHttpServletRequest("127.0.0.1");
		r.addHeader("X-Forwarded-For", "127.0.0.2, 127.0.0.3");
		r.addHeader("Forwarded", "for=\"127.0.0.2\"");
		r.addHeader("Forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
		r.addHeader("Forwarded", "for=192.0.2.43;host=localhost, for=198.51.100.17");
		
		RemoteAddressInformation rai = ar.getRemoteAddressInformation(r);
		String text = rai.toString();
		assertThat(text).isEqualTo("Direct address: 127.0.0.1, X-Forwarded-For: [127.0.0.2, 127.0.0.3], Forwarded: [{For: 127.0.0.2}, {For: 192.0.2.60, , By: 203.0.113.43, Proto: http}, {For: 192.0.2.43, Host: localhost}, {For: 198.51.100.17}]");
	}
	
	@Ignore
	protected void checkForwarded(Forwarded f, String forAddress, String byAddress, String host, String proto) {
		assertThat(f.getForAddress()).isEqualTo(forAddress);
		assertThat(f.getByAddress()).isEqualTo(byAddress);
		assertThat(f.getHost()).isEqualTo(host);
		assertThat(f.getProto()).isEqualTo(proto);
	}
	
	@Ignore
	protected static StandardRemoteClientAddressResolver createResolver(boolean includeXForwardedFor, boolean includeForwarded, boolean includeXRealIp, boolean lenientParsing, String... whiteListIps) {
		StandardRemoteClientAddressResolver r = new StandardRemoteClientAddressResolver();
		r.setIncludeXForwardedFor(includeXForwardedFor);
		r.setIncludeForwarded(includeForwarded);
		r.setIncludeXRealIp(includeXRealIp);
		r.setLenientParsing(lenientParsing);
		if (whiteListIps != null && whiteListIps.length > 0) {
			List<String> wl = new ArrayList<>();
			for (String w : whiteListIps) {
				if (w != null) {
					wl.add(w);
				}
			}
			r.setSourceWhiteList(wl);
		}
		return r;
	}
	
	
	@Ignore
	protected static void verifyAddress(RemoteAddressInformation rai, String expectation) {
		if (rai == null) {
			throw new AssertionError("RemoteAddressInformation is null. Expectation was "+expectation);
		}
		assertThat(rai.getRemoteIp()).isEqualTo(expectation);
	}
	
}
