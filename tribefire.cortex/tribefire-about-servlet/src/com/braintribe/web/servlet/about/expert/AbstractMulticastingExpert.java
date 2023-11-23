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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.braintribe.execution.generic.ContextualizedFuture;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.InstanceId;

public abstract class AbstractMulticastingExpert {

	private static Logger logger = Logger.getLogger(AbstractMulticastingExpert.class);
	
	protected static void execute(Collection<InstanceId> selectedServiceInstances, ExecutorService executor, String useCase, final Consumer<InstanceId> runOnInstanceId) {
		
		if (selectedServiceInstances == null || selectedServiceInstances.isEmpty()) {
			logger.debug(() -> "Not executing "+useCase+" as no service instances were selected.");
			return;
		}
		
		long executionStart = System.currentTimeMillis();
		AtomicLong durationSum = new AtomicLong(0);
		
		List<ContextualizedFuture<Void,InstanceId>> futures = new ArrayList<>();
		
		selectedServiceInstances.forEach(instanceId -> {
			Future<Void> future = (Future<Void>) executor.submit(() -> {
				long singleStart = System.currentTimeMillis();
				runOnInstanceId.accept(instanceId);	
				long singleDuration = System.currentTimeMillis() - singleStart;
				durationSum.addAndGet(singleDuration);
			});
			futures.add(new ContextualizedFuture<Void,InstanceId>(future, instanceId));
		});
		
		for (ContextualizedFuture<Void,InstanceId> cf : futures) {
			try {
				cf.get();
			} catch (InterruptedException e) {
				final String message = "Got interrupted while waiting for execution result from "+cf.getContext()+" (use case: "+useCase+")";
				logger.debug(() -> message);
				throw new RuntimeException(message, e);
				
			} catch (ExecutionException e) {
				
				throw new RuntimeException("Error while waiting for execution of "+useCase+" at "+cf.getContext(), e);
			}
		}
		
		long executionDuration = System.currentTimeMillis() - executionStart;
		long diff = executionDuration - durationSum.longValue();
		if (diff > 500) {
			logger.debug(() -> "Lost "+diff+" ms when executing "+selectedServiceInstances.size()+" "+useCase+" requests in parallel.");
		}
	}
	
}
