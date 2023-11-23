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
package com.braintribe.model.access.smood.basic;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.smood.api.ManipulationStorage;
import com.braintribe.model.access.smood.api.ManipulationStorageException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.StandardManipulationCollector;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.utils.lcd.StopWatch;

public class SmoodAccess extends AbstractSmoodAccess {

	protected static final Logger logger = Logger.getLogger(SmoodAccess.class);

	protected Smood database;
	protected NonIncrementalAccess dataDelegate;

	protected boolean refreshDatabase;

	private ManipulationStorage manipulationBuffer;
	private boolean initialBufferFlush = true;
	private Long bufferFlushThresholdInBytes = 10 * 1024 * 1024L;

	private Predicate<Manipulation> manipulationBufferFilter;
	private String selfModelName = null;

	private boolean storageAssemblyIsLinearPopulation;

	/**
	 * Configures the delegate {@link NonIncrementalAccess} that can load and store data. From this delegate this memory
	 * based access will load its data in one rush. It will also store its data after any call to
	 * {@link #applyManipulation} via the data delegate.
	 */
	@Required
	public void setDataDelegate(NonIncrementalAccess dataDelegate) {
		this.dataDelegate = dataDelegate;
	}

	@Configurable
	public void setManipulationBuffer(ManipulationStorage manipulationBuffer) {
		this.manipulationBuffer = manipulationBuffer;
	}

	@Configurable
	public void setInitialBufferFlush(boolean initialBufferFlush) {
		this.initialBufferFlush = initialBufferFlush;
	}

	@Configurable
	public void setBufferFlushThresholdInBytes(Long bufferFlushThresholdInBytes) {
		this.bufferFlushThresholdInBytes = bufferFlushThresholdInBytes;
	}

	@Configurable
	public void setManipulationBufferFilter(Predicate<Manipulation> manipulationBufferFilter) {
		this.manipulationBufferFilter = manipulationBufferFilter;
	}

	@Configurable
	public void setSelfModelName(String selfModelName) {
		this.selfModelName = selfModelName;
	}

	@Override
	public Smood getDatabase() throws ModelAccessException {

		if (refreshDatabase) {
			database = null;
			refreshDatabase = false;
		}

		if (database == null) {

			logger.debug(() -> "Loading initial data from " + dataDelegate + ", with ManipulationBuffer: " + manipulationBuffer);
			Object genericModelValue = dataDelegate.loadModel();
			Smood smood = new Smood(readWriteLock);
			smood.setLocaleProvider(localeProvider);
			if (getPartitions().size() <= 1)
				smood.setDefaultPartition(defaultPartition);

			try {
				/* NOTE that we simply must do the following three steps in this order, because there is a situation
				 * when a smood contains it's own meta-model, which is retrieved per query on that very smood. This can
				 * only work if we first initialize the smood, then set it as the database of this access and only then
				 * try to get the meta-model. (Setting the meta-model first makes sense in general, because the
				 * meta-model contains index-related meta-data.) */
				if (storageAssemblyIsLinearPopulation && genericModelValue instanceof Collection) {
					logger.debug(() -> "" + getAccessId() + ": Treating initial storage as a linear set of GenericEntities");
					smood.initializePopulation((Collection<GenericEntity>) genericModelValue, true);

				} else {
					if (genericModelValue != null) {
						logger.debug(() -> "" + getAccessId() + ": Initial storage is of type " + genericModelValue.getClass().getName());
						smood.initialize(genericModelValue);

					} else {
						logger.debug(() -> "" + getAccessId() + ": No initial storage available.");
					}
				}
				database = smood;

				/* To resolve just another Muenchhausen we need to introduce a way to let the Smood determine it's
				 * MetaModel from it's own database. This selfModelName is most likely only configured for the cortex
				 * Smood. */
				if (selfModelName != null) {
					smood.setSelfMetaModel(selfModelName);
				} else {
					smood.setMetaModel(getMetaModel());
				}

				updateSmoodFromManipulationBuffer(smood);

				if (selfModelName != null) {
					smood.setMetaModel(getMetaModel());
				}

			} catch (Exception e) {
				database = null;
				throw new ModelAccessException("Initialization failed. Delegate: " + dataDelegate, e);
			}

		}

		return database;
	}

	protected void updateSmoodFromManipulationBuffer(Smood smood) throws ModelAccessException {
		if (manipulationBuffer == null)
			return;

		try {
			Manipulation manipulation = manipulationBuffer.getAccumulatedManipulation();
			if (manipulation == null)
				return;

			StopWatch stopWatch = new StopWatch();
			manipulation = Normalizer.normalize(manipulation);
			logger.debug(() -> "Normalizing the manipulations took " + stopWatch.getElapsedTime() + " ms.");

			if (manipulationBufferFilter != null) {
				List<Manipulation> filteredManipulations = manipulation.stream() //
						.filter(m -> !manipulationBufferFilter.test(m)) //
						.collect(Collectors.toList());
				manipulation = compound(filteredManipulations);
			}

			logger.debug(() -> "Filtered manipulations");

			ManipulationRequest manipulationRequest = ManipulationRequest.T.create();
			manipulationRequest.setManipulation(manipulation);

			stopWatch.reset();
			smood.applyManipulation(manipulationRequest);
			logger.debug(() -> "Applying the manipulations took " + stopWatch.getElapsedTime() + " ms.");

			if (initialBufferFlush)
				flushManipulationBuffer(smood);
			else
				flushManipulationBufferOnThreshold(smood);

		} catch (Exception e) {
			throw new ModelAccessException("Error while updating smood from manipulation buffer. DataDelegate: " + dataDelegate
					+ ", ManipulationBuffer: " + manipulationBuffer, e);
		}
	}

	public void flushManipulationBufferOnThreshold() throws ModelAccessException, ManipulationStorageException {
		flushManipulationBufferOnThreshold(getDatabase());
	}

	public void flushManipulationBuffer() throws ModelAccessException, ManipulationStorageException {
		flushManipulationBuffer(getDatabase());
	}

	protected void flushManipulationBufferOnThreshold(Smood smood) throws ModelAccessException, ManipulationStorageException {
		if (bufferFlushThresholdInBytes != null && manipulationBuffer.getSize() >= bufferFlushThresholdInBytes) {
			flushManipulationBuffer(smood);
		}
	}

	protected void flushManipulationBuffer(Smood smood) throws ModelAccessException, ManipulationStorageException {
		storeDatabase(smood);
		manipulationBuffer.reset();
	}

	protected void storeDatabase() throws ModelAccessException {
		storeDatabase(getDatabase());
	}

	protected void storeDatabase(Smood smood) throws ModelAccessException {
		try {
			Object genericModelValue = smood.getAllEntities();
			dataDelegate.storeModel(genericModelValue);
		} catch (RuntimeException e) {
			throw new ModelAccessException("Error while storing smood data in database. DataDelegate: " + this.dataDelegate, e);
		}
	}

	/**
	 * @see com.braintribe.model.access.IncrementalAccess#getMetaModel()
	 */
	@Override
	public GmMetaModel getMetaModel() {
		return dataDelegate.getMetaModel();
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		writeLock.lock();

		try {
			return w_applyManipulation(manipulationRequest);

		} finally {
			writeLock.unlock();
		}
	}
	
	protected ManipulationResponse w_applyManipulation(ManipulationRequest manipulationRequest) {
		StandardManipulationCollector manipulationCollector = new StandardManipulationCollector();
		manipulationCollector.setCollectCompoundManipulations(false);
		Smood smood = getDatabase();
		smood.getGmSession().listeners().add(manipulationCollector);
		try {
			ManipulationReport report = smood.apply() //
					.generateId(true) //
					.ignoreUnknownEntitiesManipulations(false) //
					.checkRefereesOnDelete(true) //
					.request2(manipulationRequest);

			storeChange(manipulationCollector.toManipulation());

			List<Manipulation> partitionAssignments = newList();
			for (GenericEntity instantiation : report.getInstantiations().values())
				if (instantiation.getPartition() == null)
					partitionAssignments.add(createPartitionAssignmentManipulation(instantiation));

			ManipulationResponse response = report.getManipulationResponse();
			if (!partitionAssignments.isEmpty()) {
				List<Manipulation> newInducedManipulations = newList();
				Manipulation currentInducedManipulation = response.getInducedManipulation();
				if (currentInducedManipulation != null)
					newInducedManipulations.add(currentInducedManipulation);

				newInducedManipulations.addAll(partitionAssignments);
				response.setInducedManipulation(compound(newInducedManipulations));
			}

			return response;

		} finally {
			smood.getGmSession().listeners().remove(manipulationCollector);
		}
	}

	protected void storeChange(Manipulation manipulation) throws ModelAccessException {
		if (manipulationBuffer != null) {
			try {
				Manipulation remotifiedManipulation = ManipulationRemotifier.remotify(manipulation, true);
				manipulationBuffer.appendManipulation(remotifiedManipulation);
			} catch (Exception e) {
				throw new ModelAccessException(
						"Error while storing changes in manipulation buffer. ManipulationBuffer: " + manipulationBuffer.toString(), e);
			}

			try {
				flushManipulationBufferOnThreshold();
			} catch (ManipulationStorageException e) {
				throw new ModelAccessException("error while flushing buffer. DataDelegate: " + dataDelegate, e);
			}
		} else {
			storeDatabase();
		}
	}

	@Configurable
	public void setStorageAssemblyIsLinearPopulation(boolean storageAssemblyIsLinearPopulation) {
		this.storageAssemblyIsLinearPopulation = storageAssemblyIsLinearPopulation;
	}

}
