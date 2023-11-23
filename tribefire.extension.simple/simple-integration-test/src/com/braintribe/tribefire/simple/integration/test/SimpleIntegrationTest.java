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
package com.braintribe.tribefire.simple.integration.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.testing.test.AbstractTest;

import tribefire.extension.simple.SimpleConstants;
import tribefire.extension.simple.model.data.Person;
import tribefire.extension.simple.model.service.SimpleEchoRequest;
import tribefire.extension.simple.model.service.SimpleEchoResponse;

/**
 * Provides a set of simple integration tests for some deployables of the simple-cartridge.<br>
 * The main purpose of this class is to demonstrate how to write integration tests for cartridges and tribefire in general.<br>
 * <p>
 * In contrast to a unit test, which first must set up the unit(s) to be tested (e.g. an access), this integration test expects a prepared
 * tribefire-services instance to be available and just {@link #newRemoteSession() opens a remote session} to it. After obtaining the session the
 * actual test code is simple and similar to a normal unit test.
 * <p>
 * The test requires a prepared environment, i.e. running tribefire-services with simple-cartridge and deployables set The tribefire tool stack
 * provides the tools <i>Jinni</i> and <i>tfcloud-cli</>, which can be used to create such an environment based on
 * <code>simple-cartridge-setup</code>, either locally or in a Kubernetes cluster. These tools can also easily be integrated into a CI pipeline. For
 * further information on those tools, please check the respective documentation.
 * <p>
 *
 * @author michael.lafite
 */
public class SimpleIntegrationTest extends AbstractTest {

	/**
	 * The default tribefire-services url, used by {@link #newRemoteSession()} unless {@value #TRIBEFIRE_SERVICES_URL_SYSTEM_PROPERTY} is set.
	 */
	private static final String DEFAULT_TRIBEFIRE_SERVICES_URL = "http://localhost:8080/tribefire-services";
	/** The system property used to specify the tribefire-services URL {@link #newRemoteSession()} connects to. */
	private static final String TRIBEFIRE_SERVICES_URL_SYSTEM_PROPERTY = "tribefire.services.url";

	/**
	 * This method first tries to get the tribefire-services url from system property {@value #TRIBEFIRE_SERVICES_URL_SYSTEM_PROPERTY} (which can e.g.
	 * be provided by CI pipeline). If the property is not set, the default url {@value #DEFAULT_TRIBEFIRE_SERVICES_URL} is used. Afterwards the
	 * method just delegates to {@link #newRemoteSession(String)}.
	 *
	 */
	private static PersistenceGmSession newRemoteSession() throws GmSessionException {
		return newRemoteSession(System.getProperty(TRIBEFIRE_SERVICES_URL_SYSTEM_PROPERTY, DEFAULT_TRIBEFIRE_SERVICES_URL));
	}

	/**
	 * Connects to tribefire services via the specified specified <code>servicesUrl</code> using the passed credentials and returns a session which
	 * can be used to access the <code>impleInMemoryAccess</code>, but also to send service requests.
	 */
	private static PersistenceGmSession newRemoteSession(String servicesUrl) throws GmSessionException {
		// default credentials
		String user = "cortex";
		String password = "cortex";

		try {
			PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote(servicesUrl).authentication(user, password).done();
			return sessionFactory.newSession(SimpleConstants.SIMPLE_ACCESS_EXTERNALID);
		} catch (GmSessionFactoryBuilderException e) {
			throw new GmSessionException("Could not create a session to " + servicesUrl + " with user " + user, e);
		}
	}

	/**
	 * A simple example test for the <code>SimpleInMemoryAccess</code>. It just runs a simple query and then checks the results.
	 */
	@Test
	public void testSimpleInMemoryAccess() throws Exception {
		// create a remote session
		PersistenceGmSession session = newRemoteSession();

		// query for person entities and check results via comparison of first names
		assertThat(session.query().entities(EntityQueryBuilder.from(Person.class).done()).list()).hasSize(4).extracting(Person.firstName)
				.containsExactlyInAnyOrder("Jack", "Jim", "Jane", "John");
	}

	/**
	 * A simple example test for the <code>SimpleEchoService</code>. It sends a {@link SimpleEchoRequest} and then checks the
	 * {@link SimpleEchoResponse}.
	 */
	@Test
	public void testSimpleEchoService() throws Exception {
		// create a remote session
		PersistenceGmSession session = newRemoteSession();

		// create service request
		SimpleEchoRequest request = SimpleEchoRequest.T.create();
		request.setMessage("This is a test message from " + SimpleIntegrationTest.class.getSimpleName() + ". It should be echoed.");

		logger.info("Sending echo request with message: " + request.getMessage());
		SimpleEchoResponse response = (SimpleEchoResponse) session.eval(request).get();

		logger.info("Received echo: " + response.getEcho());

		// validate the response
		assertThat(response.getEcho()).contains(request.getMessage());
	}
}
