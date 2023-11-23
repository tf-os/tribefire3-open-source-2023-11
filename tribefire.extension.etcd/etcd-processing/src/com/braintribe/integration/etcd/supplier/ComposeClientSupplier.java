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
package com.braintribe.integration.etcd.supplier;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.Arguments;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * {@link ClientSupplier} to get a etcd client with authentication and compose.io SSL functionality
 * 
 *
 */
public class ComposeClientSupplier extends ClientSupplier {

	protected static Logger logger = Logger.getLogger(ComposeClientSupplier.class);

	public static final String ETCD_CLIENT_CERTIFICATE = "ETCD_CLIENT_CERTIFICATE";

	private String authority;
	private String authorityPrefix;
	private String certificate;

	public ComposeClientSupplier(List<String> endpointUrls, String username, String password, String authority, String authorityPrefix,
			String certificate) {
		super(endpointUrls, username, password);

		this.authority = authority;
		this.authorityPrefix = authorityPrefix;
		this.certificate = certificate;
	}

	@Override
	public Client get() {
		if (CommonTools.isEmpty(certificate)) {
			// read certificate from environment
			certificate = System.getenv(ETCD_CLIENT_CERTIFICATE);
			if (CommonTools.isEmpty(certificate)) {
				throw new IllegalStateException("No certificate is set from the environment ('" + ETCD_CLIENT_CERTIFICATE
						+ "') or via configuration for endpointUrls: '" + StringTools.createStringFromCollection(endpointUrls, ",") + "' username: '"
						+ username + "' authority: '" + authority + "' authorityPrefix: '" + authorityPrefix + "'");
			}
		}
		Arguments.notEmptyWithNames(authority, "'authority' must not be empty", authorityPrefix, "'authorityPrefix' must not be empty", username,
				"'username' must not be empty", password, "'password' must not be empty");

		logger.debug(() -> "Prepare client for authority: '" + authority + "' authorityPrefix: '" + authorityPrefix + "' username: '" + username
				+ "' endpointUrls: '" + StringTools.createStringFromCollection(endpointUrls, ",") + "'");

		Client client;
		SslContext sslContext;

		try {
			try (InputStream certificateInputStream = StringTools.toInputStream(certificate)) {
				//@formatter:off
				sslContext = SslContextBuilder.forClient()
						.trustManager(certificateInputStream)
						.applicationProtocolConfig(new ApplicationProtocolConfig(
								ApplicationProtocolConfig.Protocol.ALPN,
								SelectorFailureBehavior.NO_ADVERTISE,
								SelectedListenerFailureBehavior.ACCEPT,
								ApplicationProtocolNames.HTTP_2,
								ApplicationProtocolNames.HTTP_1_1))
						.build();
				//@formatter:on
			}
		} catch (Exception e) {
			throw new GenericRuntimeException("Could not create SslContext for certificate: '" + certificate + "'", e);
		}

		ByteSequence bsUsername = ByteSequence.from(username, StandardCharsets.UTF_8);
		ByteSequence bsPassword = ByteSequence.from(password, StandardCharsets.UTF_8);

		// pick first endpoint - assume all have same sub domain
		String firstEndpoint = endpointUrls.get(0);
		URL url;
		try {
			url = new URL(firstEndpoint);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException("Could not create URL from first endpoint: '" + firstEndpoint + "'", e);
		}
		String host = url.getHost();
		String subDomain = host.replaceFirst("^.*?\\.", "");

		List<URI> endpointUris = endpointUrls.stream().map(u -> {
			try {
				return new URI(u);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		
		//@formatter:off
		client = Client.builder()
					.authority(authorityPrefix + subDomain) //works with each portal subdomain but also with the root of the according portal 
					.endpoints(endpointUris)
					.user(bsUsername)
					.password(bsPassword)
					.sslContext(sslContext)
				.build();
		//@formatter:on		

		return client;
	}
}
