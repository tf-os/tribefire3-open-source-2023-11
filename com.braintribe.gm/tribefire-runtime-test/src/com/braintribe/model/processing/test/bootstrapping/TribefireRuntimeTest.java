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
package com.braintribe.model.processing.test.bootstrapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.encryption.Cryptor;

public class TribefireRuntimeTest {
	
	private static final String DEFAULT_HOST = "localhost";
	
	@Test
	public void testRecursivePropertyResolution() {
		TribefireRuntime.setProperty("VAR1", "hello");
		TribefireRuntime.setProperty("VAR2", "-${VAR1}-");
		TribefireRuntime.setProperty("VAR3", "#${VAR2}#");
		TribefireRuntime.setProperty("VAR5", "#${VAR4}#");
		
		String value = TribefireRuntime.getResolvedProperty("VAR3").get();
		
		Assertions.assertThat(value).as("recursive resolution was not as expected").isEqualTo("#-hello-#");
		
		Assertions.assertThat(TribefireRuntime.getResolvedProperty("VAR5").isPresent()).as("missing variable not correctly detected").isEqualTo(false);
	}
	
	@Test
	public void testIsExtensionHost() {
		
		try {
			Assert.assertFalse(TribefireRuntime.isExtensionHost());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "NOT_DEFAULT_URL");
			
			Assert.assertFalse(TribefireRuntime.isExtensionHost());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, null);
			
			Assert.assertFalse(TribefireRuntime.isExtensionHost());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_IS_EXTENSION_HOST, "true");
			
			Assert.assertTrue(TribefireRuntime.isExtensionHost());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_IS_EXTENSION_HOST, "false");
			
			Assert.assertFalse(TribefireRuntime.isExtensionHost());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_IS_EXTENSION_HOST, null);
			
			Assert.assertFalse(TribefireRuntime.isExtensionHost());
			
		} finally {
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, null);
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_IS_EXTENSION_HOST, null);
		}
		
	}
	
	//@Test
	@Deprecated
	/**
	 * Deprecated: getLocalBaseUrl is not supported anymore.
	 */
	public void testGetLocalBaseUrl() {
		
		try {
			
			Assert.assertEquals("http://"+DEFAULT_HOST+":8080", TribefireRuntime.getLocalBaseUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, "https");
			
			Assert.assertEquals("https", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("https://"+DEFAULT_HOST+":8443", TribefireRuntime.getLocalBaseUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			
			// This assumption is not correct anymore since we ensure a fixed default for certain properties. "http" in this case.
			//Assert.assertNull(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			Assert.assertEquals("http", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("http://"+DEFAULT_HOST+":8080", TribefireRuntime.getLocalBaseUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOCAL_BASE_URL, "//custom.host:8888");
			Assert.assertEquals("http://custom.host:8888", TribefireRuntime.getLocalBaseUrl());

			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, "gopher");
			
			Assert.assertEquals("gopher", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("gopher://custom.host:8888", TribefireRuntime.getLocalBaseUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOCAL_BASE_URL, "ftp://custom.host:8888");
			
			Assert.assertEquals("ftp://custom.host:8888", TribefireRuntime.getLocalBaseUrl());

			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOCAL_BASE_URL, null);

			Assert.assertEquals("http://"+DEFAULT_HOST+":8080", TribefireRuntime.getLocalBaseUrl());
			
		} finally {
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOCAL_BASE_URL, null);
		}
		
	}
	
	//@Test
	@Deprecated
	/**
	 * Deprectated: services url has no "magic" default anymore. either null or configured explicitly (absolute)
	 */
	public void testGetServicesUrl() {
		
		try {
			
			Assert.assertEquals("http://"+DEFAULT_HOST+":8080/tribefire-services", TribefireRuntime.getServicesUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, "https");
			
			Assert.assertEquals("https", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("https://"+DEFAULT_HOST+":8443/tribefire-services", TribefireRuntime.getServicesUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			
			// This assumption is not correct anymore since we ensure a fixed default for certain properties. "http" in this case.
			// Assert.assertNull(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			Assert.assertEquals("http", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("http://"+DEFAULT_HOST+":8080/tribefire-services", TribefireRuntime.getServicesUrl());

			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "/test-context");

			Assert.assertEquals("http://"+DEFAULT_HOST+":8080/test-context", TribefireRuntime.getServicesUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "//custom.host:8888/tribefire-services");
			
			Assert.assertEquals("http://custom.host:8888/tribefire-services", TribefireRuntime.getServicesUrl());

			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, "gopher");
			
			Assert.assertEquals("gopher", TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA));
			
			Assert.assertEquals("gopher://custom.host:8888/tribefire-services", TribefireRuntime.getServicesUrl());
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "ftp://custom.host:8888");
			
			Assert.assertEquals("ftp://custom.host:8888", TribefireRuntime.getServicesUrl());

			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, null);

			Assert.assertEquals("http://"+DEFAULT_HOST+":8080/tribefire-services", TribefireRuntime.getServicesUrl());
			
		} finally {
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_DEFAULT_SCHEMA, null);
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, null);
		}
		
	}

	
	@Test
	public void testHasExplicitPropertyUponPropertySet() {
		Assert.assertFalse(TribefireRuntime.hasExplicitProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL));
			
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "http://custom.host:9000");
		
		
		Assert.assertEquals(TribefireRuntime.getServicesUrl(), "http://custom.host:9000" );
		Assert.assertTrue(TribefireRuntime.hasExplicitProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL));
	}

	@Test
	public void testHasExplicitPropertyUponSystemPropertySet() {

		try {
			Assert.assertFalse(TribefireRuntime.hasExplicitProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL));
				
			System.setProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL, "http://custom.host:9000");
			
			Assert.assertEquals(TribefireRuntime.getServicesUrl(), "http://custom.host:9000" );
			
			Assert.assertTrue(TribefireRuntime.hasExplicitProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL));
		
		} finally {
			System.clearProperty(TribefireRuntime.ENVIRONMENT_SERVICES_URL);
		}

	}
	
	
	@Test
	public void testDecryption() {
		
		String secret = "c36e99ec-e108-11e8-9f32-f2801f1b9fd1";
		String pwd = "cortex";
		
		String encryptedPlain = Cryptor.encrypt(secret, null, null, null, pwd);
		
		Arrays.asList(new String[] {"'", "\""}).forEach(quote -> {
			String encrypted = "${decrypt("+quote+encryptedPlain+quote+")}";
			TribefireRuntime.setProperty("test-pwd", encrypted);
			String decrypted = TribefireRuntime.getProperty("test-pwd");
			assertThat(decrypted).isEqualTo(pwd);
		});
	}
	
	@Test
	public void testHostAndIp() throws Exception {
		
		String hostname = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_HOSTNAME);
		System.out.println("Hostname: "+hostname);
		assertThat(hostname).describedAs("Hostname: "+hostname).isNotBlank();
		
		String ipAddress = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_IP_ADDRESS);
		System.out.println("IP Address: "+ipAddress);
		assertThat(ipAddress).describedAs("IP Address: "+ipAddress).isNotBlank();
		
		String ip4Address = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_IP4_ADDRESS);
		System.out.println("IPv4 Address: "+ip4Address);
		assertThat(ip4Address).describedAs("IPv4 Address: "+ip4Address).isNotBlank();
		
		String ip6Address = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_IP6_ADDRESS);
		System.out.println("IPv6 Address: "+ip6Address);
		//assertThat(ip6Address).describedAs("IPv6 Address: "+ip6Address).isNotBlank();
				
		if (ipAddress != null) {
			assertThat(ipAddress).isEqualTo(ip4Address);
		}
		
		
		TribefireRuntime.setProperty("test", "Hello, ${TRIBEFIRE_HOSTNAME}!");
		String eval = TribefireRuntime.getProperty("test");
		assertThat(eval).isEqualTo("Hello, "+hostname+"!");
	}
	
	@Test
	public void testPrivateProp() throws Exception {
		
		String name = "thisIsPrivate";
		String value = "***";
		
		TribefireRuntime.setProperty(name, value);
		assertThat(TribefireRuntime.getProperty(name)).isEqualTo(value);
		
		TribefireRuntime.setPropertyPrivate(name);
		assertThat(TribefireRuntime.isPropertyPrivate(name)).isTrue();
		
	}

}
