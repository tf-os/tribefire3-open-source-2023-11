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
package com.braintribe.transport.messaging.etcd;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.etcd.ComposeEtcdMessaging;
import com.braintribe.transport.messaging.api.Messaging;
import com.braintribe.transport.messaging.api.MessagingContext;

/**
 * <p>
 * Etcd implementation of the GenericModel-based messaging system.
 * 
 * @see Messaging
 * @author roman.kurmanowytsch
 */
public class EtcdMessaging implements Messaging<com.braintribe.model.messaging.etcd.EtcdMessaging> {

	private static final Logger logger = Logger.getLogger(EtcdMessaging.class);

	@Override
	public EtcdConnectionProvider createConnectionProvider(com.braintribe.model.messaging.etcd.EtcdMessaging denotation, MessagingContext context) {

		EtcdConnectionProvider connectionProvider = new EtcdConnectionProvider();
		connectionProvider.setConnectionConfiguration(denotation);
		connectionProvider.setMessagingContext(context);

		if (logger.isDebugEnabled()) {
			if (denotation instanceof ComposeEtcdMessaging) {
				ComposeEtcdMessaging composeEtcdMessaging = (ComposeEtcdMessaging) denotation;
				logger.debug(
						() -> "Created '" + EtcdConnectionProvider.class.getSimpleName() + "' with endpointUrls: '" + denotation.getEndpointUrls()
								+ "' project: '" + denotation.getProject() + "' username: '" + denotation.getUsername() + "' authority: '"
								+ composeEtcdMessaging.getAuthority() + "' authorityPrefix: '" + composeEtcdMessaging.getAuthorityPrefix() + "'");
			} else {
				// EtcdMessaging
				logger.debug(() -> "Created '" + EtcdConnectionProvider.class.getSimpleName() + "' with endpointUrls: '"
						+ denotation.getEndpointUrls() + "' project: '" + denotation.getProject() + "' username: '" + denotation.getUsername() + "'");
			}
		}

		return connectionProvider;
	}

}
