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
package com.braintribe.product.rat.imp.impl.utils;

import java.io.IOException;
import java.io.InputStream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.resource.Resource;
import com.braintribe.product.rat.imp.AbstractHasSession;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.Arguments;

/**
 * The entrance point for the general utilities chapter of the {@link ImpApi}
 */
public class GeneralGmUtils extends AbstractHasSession {

	private final ImpApiFactory impApiFactory;

	public GeneralGmUtils(ImpApiFactory impApiFactory, PersistenceGmSession session) {
		super(session);
		this.impApiFactory = impApiFactory;
	}

	/**
	 * Creates a new {@link Resource} with the specified name and content.
	 */
	public Resource createResource(final String resourceName, final String resourceContent) throws IOException {
		logger.info("Creating a resource with name '" + resourceName + "' and content: " + resourceContent);
		final InputStream inputStream = StringTools.toInputStream(resourceContent);
		return createResource(resourceName, inputStream);
	}

	/**
	 * Creates a new {@link Resource} with the specified name and content.
	 */
	public Resource createResource(String resourceName, InputStream inputStream) throws IOException {
		logger.info("Creating a resource with name '" + resourceName + "'");
		Resource resource;
		try {
			resource = session().resources().create().name(resourceName).store(inputStream);
		} finally {
			inputStream.close();
		}

		return resource;
	}

	/**
	 * Creates a {@link LocalizedString} using a pair of <code>locale</code> and <code>displayName</code> values.
	 */
	public static LocalizedString createLocalizedString(PersistenceGmSession session, final String locale, final String displayName) {
		logger.info("Creating a localized string for locale '" + locale + "' and value '" + displayName + "'");
		final LocalizedString localizedString = session.create(LocalizedString.T);
		localizedString.setLocalizedValues(MapTools.getStringMap(locale, displayName));
		return localizedString;
	}

	/**
	 * Forces the session to load the most current state for this very entity from the server by setting the
	 * AbsenceInformation on each property of the given entity (except identifiers (property.isIdentifying())). If there
	 * is no session attached to the entity, no refresh happens.
	 */
	public static void refreshEntity(GenericEntity entity) {
		Arguments.notNullWithName("entity", entity);

		if(entity.session() == null) {
			return;
		}
		
		if (entity.getId() == null) {
			throw new ImpException("The 'id' property of passed entity is null. Please commit before refreshing: " + entity);
		}

		for (Property property : entity.entityType().getProperties()) {
			if (!property.isIdentifying()) {
				property.setAbsenceInformation(entity, GMF.absenceInformation());
				// TODO: check if needed property.get(entity);
				property.get(entity);
			}
		}
	}

	/**
	 * Forces the session to load the most current state for this very property from the server by setting the
	 * AbsenceInformation on given property of the given entity
	 */
	public static void refreshProperties(GenericEntity entity, String... propertyNames) {
		for (String propertyName : propertyNames) {
			entity.entityType().getProperty(propertyName).setAbsenceInformation(entity, GMF.absenceInformation());
		}
	}

	/**
	 * Calls {@link #tomcat(String, String) tomcat(TribefireConstants.USER_CORTEX_NAME,
	 * TribefireConstants.USER_CORTEX_DEFAULT_PASSWORD)}
	 */
	public TomcatManagerHelper tomcat() {
		return new TomcatManagerHelper(getBaseUrl(), TribefireConstants.USER_CORTEX_NAME, TribefireConstants.USER_CORTEX_DEFAULT_PASSWORD);
	}

	/**
	 * Goes to a deeper level of the api with TomcatManager related utility methods. For example one can start, stop,
	 * reload tribefire services.
	 */
	public TomcatManagerHelper tomcat(String managerUser, String managerPassword) {
		return new TomcatManagerHelper(getBaseUrl(), managerUser, managerPassword);
	}

	protected String getBaseUrl() {
		return impApiFactory.getBaseURL();
	}
}
