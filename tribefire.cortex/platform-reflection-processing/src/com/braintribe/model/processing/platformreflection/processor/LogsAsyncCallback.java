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
package com.braintribe.model.processing.platformreflection.processor;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import com.braintribe.logging.Logger;
import com.braintribe.model.logs.request.Logs;
import com.braintribe.model.resource.Resource;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.IOTools;

public class LogsAsyncCallback implements AsyncCallback<Logs> {

	private static Logger logger = Logger.getLogger(LogsAsyncCallback.class);

	protected CountDownLatch countdown;
	protected DiagnosticPackageContext diagnosticPackageContext;

	public LogsAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
		this.diagnosticPackageContext = diagnosticPackageContext;
		this.countdown = countdown;
	}

	@Override
	public void onFailure(Throwable t) {
		countdown.countDown();
		logger.error("Error while waiting for logs", t);
	}

	@Override
	public void onSuccess(Logs future) {
		try {
			logger.debug(() -> "Received an asynchronous Logs response.");
			
			Resource resource = future.getLog();
			File tempFile = File.createTempFile(resource.getName(), ".tmp");
			tempFile.delete();
			try (InputStream in = resource.openStream()) {
				IOTools.inputToFile(in, tempFile);
			} 
			diagnosticPackageContext.logs = tempFile;
			diagnosticPackageContext.logsFilename = resource.getName();
		} catch(Exception e) {
			logger.error("Error while trying to include logs in the diagnostic package.", e);
		} finally {
			countdown.countDown();
		}
	}


}
