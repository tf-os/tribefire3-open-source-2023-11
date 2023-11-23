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
package com.braintribe.model.processing.email.connection;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.email.cache.MailerCache;

public class SmtpConnectorImpl implements com.braintribe.model.processing.email.connection.SmtpConnector, DestructionAware {

	private static final Logger logger = Logger.getLogger(SmtpConnectorImpl.class);

	private com.braintribe.model.email.deployment.connection.SmtpConnector connector;
	private MailerCache mailerCache;

	@Configurable
	@Required
	public void setConnector(com.braintribe.model.email.deployment.connection.SmtpConnector connector) {
		this.connector = connector;
	}

	@Override
	public void preDestroy() {
		if (mailerCache != null) {
			logger.debug(() -> "Closing mailer for connection " + connector.getExternalId());
			mailerCache.removeMailer(connector.getExternalId());
		}
	}

	@Configurable
	@Required
	public void setMailerCache(MailerCache mailerCache) {
		this.mailerCache = mailerCache;
	}

}
