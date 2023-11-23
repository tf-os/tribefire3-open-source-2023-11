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
package com.braintribe.web.servlet.about.expert;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platformreflection.hotthreads.ThreadDump;
import com.braintribe.model.platformreflection.request.GetThreadDump;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.ZippingInputStreamProvider;

public class Threaddump {

	private static Logger logger = Logger.getLogger(Threaddump.class);

	public void processThreaddumpRequest(Evaluator<ServiceRequest> requestEvaluator, Collection<InstanceId> selectedServiceInstances,
			HttpServletResponse resp, String userSessionId, ExecutorService executor) throws Exception {

		logger.debug(() -> "Sending a request to create a threaddump to " + selectedServiceInstances + " with session " + userSessionId);

		final File tempDir = FileTools.createNewTempDir(UUID.randomUUID().toString());
		try {

			String now = DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT_2);
			List<File> storedFiles = Collections.synchronizedList(new ArrayList<>(selectedServiceInstances.size()));

			AbstractMulticastingExpert.execute(selectedServiceInstances, executor, "Threaddump", i -> {

				GetThreadDump gtd = GetThreadDump.T.create();

				MulticastRequest mcR = MulticastRequest.T.create();
				mcR.setAsynchronous(false);
				mcR.setServiceRequest(gtd);
				mcR.setAddressee(i);
				mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE * 5);
				mcR.setSessionId(userSessionId);
				EvalContext<? extends MulticastResponse> eval = mcR.eval(requestEvaluator);
				MulticastResponse multicastResponse = eval.get();

				for (Map.Entry<InstanceId, ServiceResult> entry : multicastResponse.getResponses().entrySet()) {

					InstanceId instanceId = entry.getKey();

					logger.debug(() -> "Received a response from instance: " + instanceId);

					String normalizedInstanceId = FileTools.normalizeFilename(instanceId.toString(), '_');

					ServiceResult result = entry.getValue();
					if (result instanceof Failure) {
						Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
						logger.error("Received failure from " + instanceId, throwable);
					} else if (result instanceof ResponseEnvelope) {

						ResponseEnvelope envelope = (ResponseEnvelope) result;
						ThreadDump threadDump = (ThreadDump) envelope.getResult();
						String td = threadDump.getThreadDump();

						File outFile = new File(tempDir, "threaddump-" + normalizedInstanceId + "-" + now + ".txt");
						FileTools.writeStringToFile(outFile, td, "UTF-8");

						storedFiles.add(outFile);

					} else {
						logger.error("Unsupported response type: " + result);
					}

				}

			});

			String packagedName = "threaddumps-" + now + ".zip";

			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", packagedName));

			ZippingInputStreamProvider inputStreamProvider = new ZippingInputStreamProvider(packagedName, storedFiles, true);
			inputStreamProvider.setFolderName("threaddumps-" + now);
			try (InputStream in = inputStreamProvider.openInputStream()) {
				IOTools.pump(in, resp.getOutputStream(), 0xffff);
			}

		} catch (Exception e) {
			throw new Exception("Error while trying to produce a thread dump.", e);
		} finally {
			FileTools.deleteDirectoryRecursively(tempDir);
		}

		logger.debug(() -> "Done with processing a request to create a threaddump.");
	}
}
