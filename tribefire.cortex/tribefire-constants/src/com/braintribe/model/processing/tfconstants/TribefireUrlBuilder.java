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
package com.braintribe.model.processing.tfconstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple helper class used to build URLs for {@link TribefireComponent}s dependent on some settings such as
 * {@link #hostname(String) hostname} or {@link #port(Integer) port}. Example:<br>
 * 
 * <pre>
 * TribefireUrlBuilder.with().https().hostname("myhost").port(18080).component(TribefireComponent.Services).build();
 * TribefireUrlBuilder.with().https().hostname("myhost").port(18080).buildFor(TribefireComponent.Services);
 * </pre>
 *
 * @author michael.lafite
 */
public class TribefireUrlBuilder {
	private String httpsPrefix = "https://";
	private String httpPrefix = "http://";
	private boolean httpsEnabled = true;
	private boolean defaultPortEnabled = true;
	private Integer port;
	private String hostname = "localhost";
	private final Map<TribefireComponent, String> componentUrlNames = new HashMap<>();
	private TribefireComponent component;
	private String suffix;
	private boolean relativeUrlEnabled = false;

	public static TribefireUrlBuilder with() {

		return new TribefireUrlBuilder();
	}

	public TribefireUrlBuilder() {
		for (final TribefireComponent component : TribefireComponent.values()) {
			this.componentUrlNames.put(component, TribefireConstants.getComponentUrlName(component));
		}
	}

	public TribefireUrlBuilder httpsPrefix(final String httpsPrefix) {
		this.httpsPrefix = httpsPrefix;
		return this;
	}

	public TribefireUrlBuilder httpPrefix(final String httpPrefix) {
		this.httpPrefix = httpPrefix;
		return this;
	}

	public TribefireUrlBuilder https() {
		return https(true);
	}

	public TribefireUrlBuilder http() {
		return https(false);
	}

	public TribefireUrlBuilder https(final boolean httpsEnabled) {
		this.httpsEnabled = httpsEnabled;
		return this;
	}

	public TribefireUrlBuilder port(final Integer port) {
		this.port = port;
		defaultPortEnabled = false;
		return this;
	}

	public TribefireUrlBuilder noPort() {
		port(null);
		return this;
	}

	public TribefireUrlBuilder hostname(final String hostname) {
		this.hostname = hostname;
		return this;
	}

	public TribefireUrlBuilder relativeUrl() {
		return relativeUrl(true);
	}

	public TribefireUrlBuilder absoluteUrl() {
		return relativeUrl(false);
	}

	public TribefireUrlBuilder relativeUrl(final boolean relativeUrlEnabled) {
		this.relativeUrlEnabled = relativeUrlEnabled;
		return this;
	}

	public TribefireUrlBuilder component(final TribefireComponent component) {
		this.component = component;
		return this;
	}

	public TribefireUrlBuilder suffix(final String suffix) {
		this.suffix = suffix;
		return this;
	}

	public TribefireUrlBuilder setComponentName(final TribefireComponent component, final String componentUrlName) {
		this.componentUrlNames.put(component, componentUrlName);
		this.component = component;
		return this;
	}

	public String build() {
		return buildFor(component);
	}

	public String buildFor(final TribefireComponent component) {
		final StringBuilder urlBuilder = new StringBuilder();

		if (!this.relativeUrlEnabled) {

			String portSuffix = "";
			{
				Integer portForUrl = null;
				if (defaultPortEnabled) {
					portForUrl = this.httpsEnabled ? TribefireConstants.DEFAULT_HTTPS_PORT : TribefireConstants.DEFAULT_HTTP_PORT;
				} else {
					portForUrl = port;
				}
				portSuffix = (portForUrl != null ? ":" + portForUrl : "");
			}

			urlBuilder.append((this.httpsEnabled ? this.httpsPrefix : this.httpPrefix) + this.hostname + portSuffix + "/");
		}

		if (component != null) {
			urlBuilder.append(this.componentUrlNames.get(component));
		}

		if (suffix != null) {
			urlBuilder.append(suffix);
		}

		final String result = urlBuilder.toString();
		return result;
	}
}
