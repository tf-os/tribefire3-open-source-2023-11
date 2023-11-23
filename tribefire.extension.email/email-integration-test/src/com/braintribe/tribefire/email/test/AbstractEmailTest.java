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
package com.braintribe.tribefire.email.test;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.email.data.Recipient;
import com.braintribe.model.processing.email.util.EmailConstants;
import com.braintribe.model.processing.email.util.QueryingUtil;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

/**
 *
 */
public abstract class AbstractEmailTest extends AbstractTribefireQaTest implements EmailConstants {

	// public static final String SERVICES_URL = "https://localhost:9443/tribefire-services/";
	public static final String SERVICES_URL = "http://localhost:9080/tribefire-services/";
	public static final String CORTEX_USER = "cortex";
	public static final String CORTEX_PASSWORD = "cortex"; // NOSONAR: it is a test
	public static final String CORTEX_ID = "cortex";
	public static final String EMAIL_ACCESS_ID = "email.access";

	public static final String DATE_FORMATTER_PATTERN = "yyyy-MM-dd_HH_mm_ss_SSS";

	public static final int EMAIL_STRESS_COUNT = 10000;

	// this can only be used to send email - a check if it works (except exceptions) does not exists...
	public static final String GREENMAIL_TEST_HOST = "localhost";
	public static final int GREENMAIL_TEST_PORT = 31010;
	public static final String GREENMAIL_TEST_USER = "test";
	public static final String GREENMAIL_TEST_USER_EMAIL = "test@localhost.com";
	public static final String GREENMAIL_TEST_PASSWORD = "asdfghjkl1!2"; // NOSONAR: it is a test

	public static final String GMAIL_TEST_HOST = "smtp.gmail.com";
	public static final String GMAIL_TEST_USER_EMAIL = "email.cartridge@gmail.com";
	public static final String GMAIL_TEST_PASSWORD = "asdfghjkl1!2"; // NOSONAR: it is a test

	public static final String YAHOO_TEST_HOST = "smtp.mail.yahoo.com";
	public static final String YAHOO_TEST_USER_EMAIL = "email.cartridge@yahoo.com";
	public static final String YAHOO_TEST_PASSWORD = "asdfghjkl1!2"; // NOSONAR: it is a test

	public static final String EMAIL_ADDRESS = "email message";

	public static final String SIMPLE_EMAIL_SUBJECT = "email subject";
	public static final String SIMPLE_EMAIL_MESSAGE = "email message";

	public static final String HTML_EMAIL_RESOURCE_NAME = "image1.png Name";
	public static final String HTML_EMAIL_RESOURCE_PATH = "res/image1.png";
	public static final String HTML_EMAIL_TEMPLATE_RESOURCE_NAME = "template1.vm Name";
	public static final String HTML_EMAIL_TEMPLATE_RESOURCE_PATH = "res/template1.vm";

	protected static final String TEST_EXTERNAL_ID_SMTP_CONNECTION_GMAIL_GMAIL = "test.email.gmail.smtp.connection.gmail";
	protected static final String TEST_EXTERNAL_ID_IMAP_CONNECTION_GMAIL_GMAIL = "test.email.gmail.imap.connection.gmail";
	protected static final String TEST_EXTERNAL_ID_POP3_CONNECTION_GMAIL_GMAIL = "test.email.gmail.pop3.connection.gmail";

	protected String currentDate;

	protected PersistenceGmSessionFactory sessionFactory;

	protected static PersistenceGmSession cortexSession;
	protected PersistenceGmSession testEmailSession;

	protected static ImpApi globalImp;

	// -----------------------------------------------------------------------
	// SETUP & TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		if (globalImp == null) {
			globalImp = ImpApiFactory.with().credentials(CORTEX_USER, CORTEX_PASSWORD).build();
			// globalImp = ImpApiFactory.with().credentials(CORTEX_USER, CORTEX_PASSWORD).url(SERVICES_URL).build();

			cortexSession = globalImp.switchToAccess(CORTEX_ID).session();
		}

	}

	@After
	public void after() throws Exception {
		// nothing
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected void redeployDeployable(Deployable deployable) {
		cortexSession.commit();

		Redeploy deploy = Redeploy.T.create();
		deploy.setExternalIds(asSet(deployable.getExternalId()));

		deploy.eval(cortexSession).get();
	}

	protected void deployDeployable(Deployable deployable) {
		cortexSession.commit();

		Deploy deploy = Deploy.T.create();
		deploy.setExternalIds(asSet(deployable.getExternalId()));

		deploy.eval(cortexSession).get();
	}

	protected void deleteAllDeployables() {
		deleteDeployable(TEST_EXTERNAL_ID_IMAP_CONNECTION_GMAIL_GMAIL);
		deleteDeployable(TEST_EXTERNAL_ID_SMTP_CONNECTION_GMAIL_GMAIL);
	}

	protected void deleteDeployable(String externalId) {
		Deployable deployable = QueryingUtil.queryEntityByProperty(Deployable.T, Deployable.externalId, externalId, cortexSession);

		if (deployable != null) {
			cortexSession.deleteEntity(deployable);
			cortexSession.commit();
		}
	}

	protected List<Recipient> createRecipientList(int length) {
		List<Recipient> l = new ArrayList<Recipient>();
		for (int i = 0; i < length; i++) {
			l.add(Recipient.create(GREENMAIL_TEST_USER_EMAIL));
		}

		return l;
	}

}
