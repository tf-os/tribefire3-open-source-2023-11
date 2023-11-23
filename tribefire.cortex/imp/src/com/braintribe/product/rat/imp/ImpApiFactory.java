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
package com.braintribe.product.rat.imp;

import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.tfconstants.TribefireComponent;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.processing.tfconstants.TribefireUrlBuilder;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.StringTools;

import tribefire.cortex.testing.user.UserRelatedTestApi;

/**
 * Builder class for {@link ImpApi}. As an <code>ImpApi</code> requires a session factory, you have the option
 * {@link #factory(PersistenceGmSessionFactory) to provide one}. Otherwise a session factory is created by the builder with
 * {@link #credentials(String, String)} and {@link #url(String)}. For any of these properties that is not provided, a default value is used
 * ({@link TribefireConstants#USER_CORTEX_NAME}, {@link TribefireConstants#USER_CORTEX_DEFAULT_PASSWORD}). A default property can be re-defined by
 * passing a system property or an environment variable for this property.
 * <p>
 * Supported properties:
 * <ol>
 * <li>QA_FORCE_USERNAME
 * <li>QA_FORCE_PASSWORD
 * <li>QA_FORCE_URL
 * </ol>
 * <p>
 * Examples of creating ImpApi with:
 * <ol>
 * <li>ImpApiFactory.with().credentials("john", "smith234").build(); // default url will be used
 * <li>ImpApiFactory.with().credentials("john", "smith234").url("https://localhost:8443/tribefire-services/").build();
 * <li>ImpApiFactory.with().factory(factory).build();
 * <li>ImpApiFactory.buildWithDefaultProperties()
 * </ol>
 */
public class ImpApiFactory implements Cloneable {

	private static Logger logger = Logger.getLogger(ImpApiFactory.class);

	private String url;
	private String username;
	private String password;
	private final String accessId = TribefireConstants.ACCESS_CORTEX;
	private PersistenceGmSessionFactory factory;
	private PersistenceGmSession session;

	/**
	 * Entrance point to the builder, that lets you specify properties for a session factory and build an instance of {@link ImpApi} with it.
	 *
	 * @see ImpApiFactory
	 */
	public static ImpApiFactory with() {
		return new ImpApiFactory();
	}

	/**
	 * The easiest way to build an instance of {@link ImpApi}, using default properties only.
	 *
	 * @see #with()
	 */
	public static ImpApi buildWithDefaultProperties() {
		return with().build();
	}

	@Override
	public ImpApiFactory clone() {
		try {
			return (ImpApiFactory) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Lets you pass username and password which will be used by the created session factory.
	 */
	public ImpApiFactory credentials(String username, String password) {
		ImpApiFactory clonedCave = this.clone();
		Arguments.notNullWithNames("username", username, "password", password);

		clonedCave.username = username;
		clonedCave.password = password;
		return clonedCave;
	}

	/** Lets you pass the tribefire-services url which will be used by the created session factory. */
	public ImpApiFactory url(String url) {
		ImpApiFactory clonedCave = this.clone();
		Arguments.notNullWithNames("url", url);

		clonedCave.url = url;
		return clonedCave;
	}

	/** Lets you pass the tribefire-services url which will be used by the created session factory. */
	public ImpApiFactory url(TribefireUrlBuilder builder) {
		ImpApiFactory clonedCave = this.clone();

		clonedCave.url = builder.build();
		return clonedCave;
	}

	/** Returns the tribefire-services url that currently would be used for creating a session factory */
	public String getURL() {
		return getConfigProperty(url, this::defaultServicesUrl, //
				"TRIBEFIRE_SERVICES_URL", "TRIBEFIRE_PUBLIC_SERVICES_URL", "QA_FORCE_URL");
	}

	/** Typicaly returns "localhost:8080/tribefire-services" */
	private String defaultServicesUrl() {
		return new TribefireUrlBuilder().http().buildFor(TribefireComponent.Services);
	}

	/** Returns the base URL for the servlet container hosting tribefire. */
	public String getBaseURL() {
		return StringTools.getSubstringBeforeLast(getURL(), "/");
	}

	/**
	 * Lets you specify the session factory that will be used to create an instance of {@link ImpApi}. If you set session factory specific properties
	 * like {@link #url(String)} or {@link #credentials(String, String)} before, they will be ignored in that case. However you can still use
	 * {@link #session(PersistenceGmSession)}
	 */
	public ImpApiFactory factory(PersistenceGmSessionFactory factory) {
		ImpApiFactory clonedCave = this.clone();
		Arguments.notNullWithNames("factory", factory);
		clonedCave.factory = factory;
		return clonedCave;
	}

	/** Let's you specify the session that will be used to create an instance of {@link ImpApi} */
	public ImpApiFactory session(PersistenceGmSession session) {
		ImpApiFactory clonedCave = this.clone();
		Arguments.notNullWithNames("session", session);
		clonedCave.session = session;
		return clonedCave;
	}

	private boolean factoryWasConfigured() {
		return url != null || username != null || password != null || factory != null;
	}
	/**
	 * Builds a new {@link PersistenceGmSessionFactory} with the previously provided credentials and url (or standard values if not)
	 *
	 * @return this new factory instance
	 */
	public PersistenceGmSessionFactory buildSessionFactory() {
		if (!factoryWasConfigured() && UserRelatedTestApi.userSessionFactory != null) {
			return UserRelatedTestApi.userSessionFactory;
		}
		getTribefirePropertyValues();
		Arguments.notEmptyWithNames("username", username, "password", password, "url", url, "accessId", accessId);

		try {
			PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote(url).authentication(username, password).done();
			return sessionFactory;
		} catch (GmSessionFactoryBuilderException e) {
			throw new RuntimeException("Could not create a session to " + url + " using user " + username, e);
		}
	}

	/**
	 * Builds a new {@link PersistenceGmSessionFactory} with the previously provided credentials and url (or standard values if not) and creates a new
	 * session using this very factory and provided accessExternalId
	 */
	public PersistenceGmSession newSessionForAccess(String accessExternalId) {
		return buildSessionFactory().newSession(accessExternalId);
	}

	/**
	 * Builds an instance of {@link ImpApi}.
	 *
	 * @see ImpApiFactory
	 */
	public ImpApi build() {
		PersistenceGmSession sessionToUse = this.session;
		PersistenceGmSessionFactory factoryToUse = this.factory;

		if (factoryToUse == null) {
			factoryToUse = buildSessionFactory();
		}

		if (sessionToUse != null) {
			logger.info("Building an Imp for access '" + sessionToUse.getAccessId() + "' ...");
		} else {
			logger.info("Building an Imp for access '" + accessId + "' ...");
			Arguments.notEmptyWithNames("accessId", accessId);
			sessionToUse = factoryToUse.newSession(accessId);
		}

		return new ImpApi(this, factoryToUse, sessionToUse);
	}

	private void getTribefirePropertyValues() {
		username = getConfigProperty(username, "QA_FORCE_USERNAME", TribefireConstants.USER_CORTEX_NAME);
		password = getConfigProperty(password, "QA_FORCE_PASSWORD", TribefireConstants.USER_CORTEX_DEFAULT_PASSWORD);
		url = getURL();
		logger.info("Initialized properties " + CommonTools.getParametersString("user", username, "password", password, "url", url));
	}

	private static String getConfigProperty(String propertyValue, String propertyName, String defaultPropertyValue) {
		if (!CommonTools.isEmpty(propertyValue))
			return propertyValue;

		String sysOrEnv = getSysOrEnv(propertyName);
		if (sysOrEnv != null)
			return sysOrEnv;

		return defaultPropertyValue;
	}

	private static String getConfigProperty(String propertyValue, Supplier<String> defaultValueSuplier, String... propertyNames) {
		if (!CommonTools.isEmpty(propertyValue))
			return propertyValue;

		for (String propertyName : propertyNames) {
			String result = getSysOrEnv(propertyName);
			if (result != null)
				return result;
		}

		return defaultValueSuplier.get();
	}

	private static String getSysOrEnv(String propertyName) {
		String systemPropertyName = propertyName.toLowerCase().replace("_", ".");

		String result = System.getProperty(systemPropertyName);
		if (CommonTools.isEmpty(result))
			result = System.getenv(propertyName);

		return CommonTools.isEmpty(result) ? null : result;
	}

}