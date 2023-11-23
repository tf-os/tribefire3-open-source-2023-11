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
package tribefire.extension.antivirus.service.connector.clamav;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.resource.Resource;

import tribefire.extension.antivirus.connector.api.AbstractAntivirusConnector;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ClamAVSpecification;
import tribefire.extension.antivirus.model.service.result.ClamAVAntivirusResult;

public class ClamAVExpert extends AbstractAntivirusConnector<ClamAVAntivirusResult> {

	private static final Logger logger = Logger.getLogger(ClamAVExpert.class);

	private ClamScan clamScan;
	private ClamAVSpecification providerSpecification;

	@Override
	public ClamAVAntivirusResult scanViaExpert() {
		ScanResult scanResult = clamScan.scan(resource);

		boolean infected = scanResult.getStatus().equals(ScanResult.Status.FAILED);
		String message = scanResult.getStatus().name();

		ClamAVAntivirusResult result = createResult(ClamAVAntivirusResult.T, infected, message);

		// ClamAV specific results
		result.setStatus(scanResult.getStatus().name());
		result.setSignature(scanResult.getSignature());

		if (scanResult.getException() != null) {
			result.setException(scanResult.getException().toString());
		}

		return result;
	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static ClamAVExpert forScanForVirus(ClamAVSpecification context, Resource resource) {
		return createExpert(ClamAVExpert::new, (expert) -> {
			ClamScan clamScan = new ClamScan(context.getURL(), context.getPort(), context.getTimeout());
			expert.setClamScan(clamScan);
			expert.setResource(resource);
			expert.setProviderType(context.entityType().getShortName());
		});
	}

	public static ClamAVExpert forHealthCheck(ClamAVSpecification providerSpecification, Resource resource) {
		return createExpert(ClamAVExpert::new, (expert) -> {
			expert.setProviderSpecification(providerSpecification);
			expert.setResource(resource);
		});
	}

	@Override
	public CheckResultEntry healthCheck() {
		CheckResultEntry checkResultEntry = CheckResultEntry.T.create();
		checkResultEntry.setName("ClamAV check");

		String host = providerSpecification.getURL();
		Integer port = providerSpecification.getPort();

		try (Socket socket = new Socket()) {
			socket.setSoTimeout(5);
			socket.connect(new InetSocketAddress(host, port));

			if (socket.isConnected()) {
				checkResultEntry.setCheckStatus(CheckStatus.ok);
				checkResultEntry.setMessage(String.format("Connect was successful! Host: %s, Port: %s", host, port));
			} else {
				String errorMessage = String.format("Could connect to socket but connection is not established! Host: %s, Port: %s", host, port);
				checkResultEntry.setCheckStatus(CheckStatus.fail);
				checkResultEntry.setMessage(errorMessage);
				logger.debug(() -> errorMessage);
			}
		} catch (IOException e) {
			String errorMessage = String.format("Could not connect! Host: %s, Port: %s, Reason: %s", host, port, e.getMessage());
			checkResultEntry.setCheckStatus(CheckStatus.fail);
			checkResultEntry.setMessage(errorMessage);
			logger.debug(() -> errorMessage, e);
		}

		return checkResultEntry;
	}

	// Getters, Setters

	public void setClamScan(ClamScan clamScan) {
		this.clamScan = clamScan;
	}

	public void setProviderSpecification(ClamAVSpecification providerSpecification) {
		this.providerSpecification = providerSpecification;
	}
}
