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
package tribefire.extension.antivirus.initializer.wire.contract;

import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.antivirus.model.deployment.repository.configuration.ClamAVSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.CloudmersiveSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.VirusTotalSpecification;
import tribefire.extension.antivirus.model.deployment.service.HealthCheckProcessor;

public interface AntivirusInitializerContract extends WireSpace {

	void setupDefaultConfiguration(DefaultAntivirusProvider defaultProvider);

	CheckBundle functionalCheckBundle();

	HealthCheckProcessor healthCheckProcessor();

	ClamAVSpecification clamAVSpecification();

	CloudmersiveSpecification cloumersiveSpecification();

	VirusTotalSpecification virusTotalSpecification();

}
