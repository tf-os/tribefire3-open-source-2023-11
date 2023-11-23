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
package tribefire.extension.antivirus.model.deployment.repository.configuration;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ClamAVSpecification extends ProviderSpecification {

	EntityType<ClamAVSpecification> T = EntityTypes.T(ClamAVSpecification.class);

	@Mandatory
	@Name("URL")
	@Description("URL endpoint")
	String getURL();
	void setURL(String url);

	@Mandatory
	@Name("Port")
	@Description("Port (default: 3310)")
	@Initializer("3310")
	int getPort();
	void setPort(int port);

	@Mandatory
	@Name("Port")
	@Description("Timeout in milliseconds (default: 20 s)")
	@Initializer("20000")
	int getTimeout();
	void setTimeout(int timeout);
}
