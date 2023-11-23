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
package tribefire.platform.wire.space.system.servlets;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static com.braintribe.web.api.registry.WebRegistries.config;
import static com.braintribe.web.api.registry.WebRegistries.filter;
import static com.braintribe.web.api.registry.WebRegistries.servlet;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import com.braintribe.web.api.filter.SetCharacterEncodingFilter;
import com.braintribe.web.api.registry.Registration;
import com.braintribe.web.impl.registry.ConfigurableFilterRegistration;
import com.braintribe.web.impl.registry.ConfigurableWebRegistry;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.web.CompressionFilter;
import tribefire.platform.wire.space.security.servlets.SecurityServletSpace;

@Managed
public class WebRegistrySpace implements WireSpace {

	@Import
	private SecurityServletSpace security;

	@Import
	private SystemServletsSpace systemServlets;

	@Managed
	public ConfigurableWebRegistry webRegistry() {
		// @formatter:off
		ConfigurableWebRegistry bean = 
				config()
					.filters(
						compressionFilter(),
						filter()
							.name("servlet-response-capture-filter")
							.instance(systemServlets.captureFilter())
							.pattern("/*"),
						filter()
							.name("caller-info-filter")
							.instance(systemServlets.callerInfoFilter())
							.pattern("/*"),
						filter()
							.name("exception-filter")
							.instance(security.exceptionFilter())
							.pattern("/*"),
						threadRenamingFilter(),
						filter()
							.name("cors-filter")
							.instance(security.corsFilter())
							.pattern("/*"),
						strictAuthFilterConfiguration(),
						filter()
							.name("auth-strict-admin-filter")
							.instance(security.authFilterAdminStrict())
							.patterns(
									"/model-browser/*", 
									"/about/*", 
									"/logs/*", 
									"/deployment-summary/*"),
						lenientAuthFilterConfiguration(),
						filter()
							.name("set-character-encoding-filter")
							.type(SetCharacterEncodingFilter.class)
							.pattern("*")
							.initParams(
								map(
									entry("encoding", "utf-8")
								)
							)
					)
					.servlets(
						servlet()
							.name("about-servlet")
							.instance(systemServlets.aboutServlet())
							.pattern("/about/*")
							.multipart(),
						servlet()
							.name("deploymentreflection-servlet")
							.instance(systemServlets.deploymentReflectionServlet())
							.pattern("/deployment-summary/*")
							.multipart(),
						servlet()
							.name("logs-servlet")
							.instance(systemServlets.logsServlet())
							.pattern("/logs/*")
							.multipart(),
						servlet()
							.name("home-servlet")
							.instance(systemServlets.homeServlet())
							.pattern("/home/*")
							.multipart(),
						servlet()
							.name("login-servlet")
							.instance(security.loginServlet())
							.pattern("/login")
							.multipart(),
						servlet()
							.name("login-auth-servlet")
							.instance(security.loginAuthServlet())
							.pattern("/login/auth")
							.multipart(),
						servlet()
							.name("logout-servlet")
							.instance(security.logoutServlet())
							.pattern("/logout")
							.multipart(),
						servlet()
							.name("public-resource-servlet")
							.instance(systemServlets.publicResourceServlet())
							.pattern("/publicResource/dynamic/*")
							.multipart(),
						servlet()
							.name("component-servlet")
							.instance(systemServlets.componentServlet())
							.pattern("/component/*")
							.multipart(),
						servlet()
							.name("healthz-servlet")
							.instance(systemServlets.healthzServlet())
							.pattern("/healthz/*")
							.multipart(),
						servlet()
							.name("user-image-servlet")
							.instance(systemServlets.userImageServlet())
							.pattern("/user-image/*")
							.multipart(),
						servlet()
							.name("public-resources-servlet")
							.instance(systemServlets.publicResourcesServlet())
							.pattern("/res/*")
							.multipart()
	
			);
		// @formatter:on

		moduleWebRegistry().configure(bean);

		return bean;
	}

	@Managed
	public ConfigurableFilterRegistration compressionFilter() {
		return filter().name("compression-filter").type(CompressionFilter.class);
	}

	@Managed
	public ConfigurableFilterRegistration threadRenamingFilter() {
		return filter().name("thread-renamer-filter").instance(systemServlets.threadRenamerFilter()).patterns("/home/*", "/login/*", "/logout/*",
				"/model-browser/*", "/about/*", "/logs/*", "/deployment-summary/*", "/component/*", "/static/*", "/publicResource/*",
				"/user-image/*");
	}

	@Managed
	public ConfigurableFilterRegistration strictAuthFilterConfiguration() {
		return filter().name("auth-strict-api-filter").instance(security.authFilterStrict()).patterns("/user-image/*");
	}

	@Managed
	public ConfigurableFilterRegistration lenientAuthFilterConfiguration() {
		return filter().name("auth-lenient-filter").instance(security.authFilterLenient()).patterns("/home/*", "/component/*", "/logout/*");
	}

	@Managed
	public ModuleWebRegistry moduleWebRegistry() {
		return new ModuleWebRegistry();
	}

	/**
	 * This {@link ConfigurableWebRegistry} is used for binding hardwired servlets, filters and co from modules. The
	 * registered instances are then added to the {@link WebRegistrySpace#webRegistry()}.
	 * <p>
	 * Note that it is therefore important that the module-loading happens before the web
	 * {@link WebRegistrySpace#webRegistry()} is accessed the first time. But that's also necessary because the web registry
	 * triggers cortex-loading, so if modules are not loaded by that time, it's a problem anyway.
	 */
	private static class ModuleWebRegistry extends ConfigurableWebRegistry {

		public void configure(ConfigurableWebRegistry registry) {
			nullSafe(listeners).stream() //
					.forEach(registry::addListener);
			nullSafe(filters).stream() //
					.forEach(registry::addFilter);
			nullSafe(servlets).stream() //
					.forEach(registry::addServlet);
			nullSafe(websocketEndpoints).stream() //
					.forEach(registry::addWebsocketEndpoint);
		}

		@Override
		protected void ensureOrder(Registration registration) {
			// NOOP
		}

	}

}
