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
package com.braintribe.model.email.deployment.connection;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 *
 */
@SelectiveInformation("Yahoo SMTP Connector")
public interface YahooSmtpConnector extends SmtpConnector {

	final EntityType<YahooSmtpConnector> T = EntityTypes.T(YahooSmtpConnector.class);

	@Mandatory
	@Initializer("'smtp.mail.yahoo.com'")
	@Unmodifiable
	@Override
	String getSmtpHostName();
	@Override
	void setSmtpHostName(String smtpHostName);

	@Mandatory
	@Initializer("587")
	@Unmodifiable
	@Override
	int getSmtpPort();
	@Override
	void setSmtpPort(int port);

	@Mandatory
	@Initializer("enum(com.braintribe.model.email.deployment.connection.TransportStrategy,SMTP_TLS)")
	@Unmodifiable
	@Override
	TransportStrategy getTransportStrategy();
	@Override
	void setTransportStrategy(TransportStrategy protocol);

}
