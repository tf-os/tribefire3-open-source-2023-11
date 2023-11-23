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
package com.braintribe.gwt.gmsession.client;

import java.util.function.Function;

import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.util.HistorySuspension;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.impl.managed.IdentityCompetence;
import com.braintribe.model.processing.session.impl.managed.merging.ContinuableMerger;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public class GwtContinuableMerger<M> extends ContinuableMerger<M> {

	private static Logger logger = Logger.getLogger(GwtContinuableMerger.class);

	private final long workerSliceThresholdInMs = 100;
	
	public GwtContinuableMerger(IdentityCompetence identityCompetence, boolean adopt) {
		super(identityCompetence, adopt);
	}
	
	public GwtContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt){
		super(identityCompetence,historySuspension,adopt);
	}
	
	public GwtContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt, Function<GenericEntity, GenericEntity> envelopeFactory){
		super(identityCompetence,historySuspension,adopt, envelopeFactory);
	}

	public GwtContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt,
			Function<GenericEntity, GenericEntity> envelopeFactory, boolean transferTransientProperties) {

		super(identityCompetence,historySuspension,adopt, envelopeFactory, transferTransientProperties);
	}
	
	@Override
	public void merge(M data, final AsyncCallback<M> asyncCallback) {
		initialize(data);

		final ProfilingHandle profilingHandle = Profiling.start(GwtContinuableMerger.class, "Entity merge", true);

		// start worker "thread"
		Scheduler.get().scheduleIncremental(new RepeatingCommand() {

			boolean trace = logger.isTraceEnabled();
			protected int workSliceCount = 0;
			protected int stepCount = 0;

			@Override
			public boolean execute() {
				try {					
					workSliceCount++;

					suspendHistory();
					try{
						long s = System.currentTimeMillis();
						while (currentStep != null) {

							stepCount++;
							long start = System.currentTimeMillis();

							currentStep.doStep();

							if (trace) {
								long diff = System.currentTimeMillis() - start;
								if (diff > workerSliceThresholdInMs) {
									logger.trace("The step #"+stepCount+" of type "+currentStep.getClass()+" took "+diff+" ms");
								}
							}

							currentStep = currentStep.next;

							long delta = System.currentTimeMillis() - s;
							if (delta > workerSliceThresholdInMs) {
								if (trace)
									logger.trace("Interrupting step "+workSliceCount+" after "+delta+" ms");
								return true;
							}
						}

						if (trace)
							logger.trace("Done with processing after "+(System.currentTimeMillis()-s)+" ms (steps: "+stepCount+")");

					} finally {
						resumeHistory();
					}

					if (trace)
						logger.trace("Entity merging required "+workSliceCount+" slices.");
					profilingHandle.stop();

					try {
						asyncCallback.onSuccess(mergedData);
					} catch (Exception e) {
						logger.error("error during callback processing", e);
					}

					if (trace)
						logger.trace("Done with async callback after entity merging.");
					
					return false;
				} catch (Exception e) {
					profilingHandle.stop();
					asyncCallback.onFailure(new GmSessionException("error while merging data",e));
					return false;
				}
			}
		});
	}
}
