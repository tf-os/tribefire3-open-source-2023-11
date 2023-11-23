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
package com.braintribe.model.access.smood.distributed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.ClobProxy;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.crypto.hash.md5.MD5HashGenerator;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.AccessServiceException;
import com.braintribe.model.access.ClassDataStorage;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.smood.distributed.codec.DefaultJavaClassCodec;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.tracking.StandardManipulationCollector;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.itw.synthesis.java.clazz.FolderClassLoader;
import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.smoodstorage.BufferedManipulation;
import com.braintribe.model.smoodstorage.JavaClass;
import com.braintribe.model.smoodstorage.SmoodStorage;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class DistributedSmoodAccess extends AbstractAccess implements ClassDataStorage, InitializationAware {

	protected static Logger logger = Logger.getLogger(DistributedSmoodAccess.class);

	protected SessionFactory hibernateSessionFactory = null;
	protected DataSource dataSource = null;
	protected ReadWriteLock readWriteLock;

	protected NonIncrementalAccess initialStorage = null;
	protected IncrementalAccess storage2 = null;
	protected Smood database = null;
	protected Supplier<String> localeProvider;

	protected CharacterMarshaller xmlMarshaller;
	protected GmDeserializationOptions deserializationOptions = GmDeserializationOptions.deriveDefaults()
			.setDecodingLenience(new DecodingLenience(false)).build();
	protected GmSerializationOptions serializationOptions = GmSerializationOptions.deriveDefaults().build();

	protected volatile int storageSequenceNumber = -1;
	protected volatile int manipulationBufferSequenceNumber = -1;
	protected volatile int lastKnownBufferSize = -1;
	protected ReentrantLock localStorageLock = new ReentrantLock();
	protected LockManager dbLockManager = null;

	protected int maxManipulationBufferSize = 5000000;
	protected int maxManipulationBuffers = 1000;
	protected int keepNOldDumps = 1;

	protected GenericEntity lockObject = null;

	protected Supplier<GmMetaModel> modelProvider = null;
	protected GmMetaModel metaModel = null;
	protected String metaModelSync = "metaModelSync";

	protected int classDependenciesSequenceNumber = -1;
	protected Codec<InputStream, String> javaClassCodec = null;
	protected Map<String, JavaClassBuffer> localJavaClassMap = new HashMap<String, JavaClassBuffer>();

	protected String selfModelName = null;

	protected DSmoodTiming timing = new DSmoodTiming();
	protected long logStatisticsInterval = 3600000L;
	protected long nextStatisticLog = -1;

	protected String identifierPrefix = "";

	private boolean storageAssemblyIsLinearPopulation = true;

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();

		if (debug) {
			this.logStatistics();
		}

		this.timing.totalSelectQueryCount.incrementAndGet();
		long start = System.currentTimeMillis();
		try {
			if (debug)
				logger.debug("Executing select query.");

			TraversingCriterion traversingCriterion = getTraversingCriterion(query);
			Smood db = null;
			long startDb = System.currentTimeMillis();
			try {
				db = this.getDatabase();
			} finally {
				this.timing.totalSelectQueryDbOverheadTime.addAndGet(System.currentTimeMillis() - startDb);
			}

			if (debug)
				logger.debug("Got updated db.");

			SelectQueryResult result = db.query(query);

			@SuppressWarnings("unchecked")
			List<Object> cuttedValue = (List<Object>) BaseType.INSTANCE.clone(result.getResults(), createMatcher(traversingCriterion),
					StrategyOnCriterionMatch.partialize);

			result.setResults(cuttedValue);

			if (debug)
				logger.debug("Done: Executing select query.");

			return result;

		} catch (Exception e) {
			throw new ModelAccessException("Error while performing SelectQuery.", e);
		} finally {
			this.timing.totalSelectQueryTime.addAndGet(System.currentTimeMillis() - start);
		}
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();

		if (debug) {
			this.logStatistics();
		}

		this.timing.totalQueryCount.incrementAndGet();
		long start = System.currentTimeMillis();
		try {
			if (debug)
				logger.debug("Executing entity query.");

			TraversingCriterion traversingCriterion = getTraversingCriterion(request);
			Smood db = null;
			long startDb = System.currentTimeMillis();
			try {
				db = this.getDatabase();
			} finally {
				this.timing.totalQueryDbOverheadTime.addAndGet(System.currentTimeMillis() - startDb);
			}

			if (debug)
				logger.debug("Got updated db.");

			EntityQueryResult result = db.queryEntities(request);

			List<GenericEntity> clonedEntries = cloneEntityQueryResult(result.getEntities(), traversingCriterion);

			result.setEntities(clonedEntries);

			if (debug)
				logger.debug("Done: Executing entity query.");

			return result;

		} catch (Exception e) {
			throw new ModelAccessException("error while querying entities", e);
		} finally {
			this.timing.totalQueryTime.addAndGet(System.currentTimeMillis() - start);
		}
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();
		boolean trace = logger.isTraceEnabled();

		if (debug) {
			this.logStatistics();
		}

		this.timing.totalPropertyQueryCount.incrementAndGet();
		long start = System.currentTimeMillis();
		try {
			if (debug) {
				logger.debug("Executing property query.");
			} else if (trace) {
				logger.trace(
						"Executing property query on " + request.getEntityReference().getTypeSignature() + "." + request.getPropertyName() + ".");
			}

			Smood localDatabase = null;
			long startDb = System.currentTimeMillis();
			try {
				localDatabase = this.getDatabase();
			} finally {
				this.timing.totalPropertyQueryDbOverheadTime.addAndGet(System.currentTimeMillis() - startDb);
			}

			if (debug)
				logger.debug("Got updated db.");

			PropertyQueryResult result = localDatabase.queryProperty(request);
			if (result != null) {
				Object resultValue = result.getPropertyValue();
				if (resultValue != null) {
					Object cuttedValue = super.clonePropertyQueryResult(resultValue, request);

					result.setPropertyValue(cuttedValue);
				}
			}

			if (debug)
				logger.debug("Done: Executing property query.");

			return result;

		} catch (Exception e) {
			throw new ModelAccessException(
					"error while querying property " + request.getPropertyName() + " of entity " + request.getEntityReference().getTypeSignature(),
					e);
		} finally {
			this.timing.totalPropertyQueryTime.addAndGet(System.currentTimeMillis() - start);
		}
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {

		Lock exclusiveLock = null;
		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug) {
			this.logStatistics();
		}

		this.timing.totalApplyManipulationCount.incrementAndGet();

		long start = System.currentTimeMillis();
		try {

			if (debug)
				logger.debug("Trying to obtain lock before applying manipulation.");

			if (trace)
				logger.trace("Trying to get lock on local storage.");
			this.localStorageLock.lock();
			if (trace)
				logger.trace("Lock on local storage obtained.");

			long startLock = System.currentTimeMillis();
			try {
				if (trace)
					logger.trace("Trying to get lock on DB storage.");
				LockBuilder lockBuilder = null;
				if (this.lockObject != null) {
					lockBuilder = dbLockManager.forEntity(this.lockObject);
				} else {
					lockBuilder = dbLockManager.forIdentifier(this.getAccessIdentifier());
				}
				exclusiveLock = lockBuilder.exclusive();
				exclusiveLock.lock();
				if (trace)
					logger.trace("Lock on DB storage obtained.");

			} catch (Exception e) {
				throw new ModelAccessException("Could not get an exclusive lock on " + this.lockObject, e);
			} finally {
				this.timing.totalApplyManipulationDbLockOverheadTime.addAndGet(System.currentTimeMillis() - startLock);
			}
			try {
				if (debug)
					logger.debug("Lock on local and remote storage obtained.");

				Normalizer.normalize(manipulationRequest);

				StandardManipulationCollector manipulationCollector = new StandardManipulationCollector();
				manipulationCollector.setCollectCompoundManipulations(false);
				long startDbRead = System.currentTimeMillis();
				Smood smood = null;
				try {
					smood = getDatabase();
				} finally {
					this.timing.totalApplyManipulationDbGetOverheadTime.addAndGet(System.currentTimeMillis() - startDbRead);
				}
				smood.getGmSession().listeners().add(manipulationCollector);
				try {
					ManipulationResponse referenceTranslation = smood.applyManipulation(manipulationRequest);

					long startDbWrite = System.currentTimeMillis();
					try {
						Manipulation collectedManipulation = manipulationCollector.toManipulation();
						this.storeChange(collectedManipulation);
					} finally {
						this.timing.totalApplyManipulationDbWriteOverheadTime.addAndGet(System.currentTimeMillis() - startDbWrite);
					}

					return referenceTranslation;
				} finally {
					smood.getGmSession().listeners().remove(manipulationCollector);
				}
			} catch (Exception e) {
				if (debug)
					logger.debug("An error occurred while applying manipulations. Refreshing database.");
				throw new ModelAccessException("error while applying manipulation", e);
			} finally {
				long unlockStart = System.currentTimeMillis();
				try {
					exclusiveLock.unlock();
					if (trace)
						logger.trace("Lock on DB storage released.");
				} catch (Exception e) {
					logger.error("Could not unlock ExclusiveLock " + exclusiveLock, e);
				} finally {
					this.timing.totalApplyManipulationDbUnlockOverheadTime.addAndGet(System.currentTimeMillis() - unlockStart);
				}

				this.localStorageLock.unlock();
				if (trace)
					logger.trace("Lock on local storage released.");
				if (debug)
					logger.debug("Locks released after applying manipulation.");
			}
		} finally {
			this.timing.totalApplyManipulationTime.addAndGet(System.currentTimeMillis() - start);
		}
	}

	@Override
	public void postConstruct() {
		if ((this.maxManipulationBuffers < 0) && (this.maxManipulationBufferSize < 0)) {
			throw new RuntimeException(
					"Neither maxManipulationBuffers nor maxManipulationBufferSize is set to a positive value. This is not allowed.");
		} else {
			logger.debug("Accepting settings: maxManipulationBuffers: " + maxManipulationBuffers + ", maxManipulationBufferSize: "
					+ maxManipulationBufferSize);
		}

		this.timing.accessId = this.getAccessIdentifier();

		Session session = this.hibernateSessionFactory.openSession();
		session.close();

		if (this.logStatisticsInterval > 0) {
			this.nextStatisticLog = System.currentTimeMillis() + this.logStatisticsInterval;
		}
	}

	protected void logStatistics() {
		if ((this.nextStatisticLog <= 0) || (this.logStatisticsInterval <= 0)) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now > this.nextStatisticLog) {
			this.nextStatisticLog = now + this.logStatisticsInterval;
			try {
				String stats = this.getStatistics();
				logger.debug(stats);
			} catch (Exception e) {
				logger.debug("Could not log statistics.", e);
			}
		}
	}

	protected boolean manipulationBufferSizeThresholdReached(Connection connection, SmoodStorage latestStorageInDb) {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		try {
			int currentSize = latestStorageInDb.getBufferedManipulationsSize();
			if ((this.maxManipulationBufferSize >= 0) && (currentSize > this.maxManipulationBufferSize)) {
				if (debug)
					logger.debug("Latest dump has exceeded max buffer size of " + this.maxManipulationBufferSize + " (" + currentSize + ")");
				return true;
			} else {
				if (this.maxManipulationBufferSize >= 0) {
					if (trace)
						logger.trace("The current size (" + currentSize + ") of the combined manipulation buffers does not yet exceed the limit of "
								+ this.maxManipulationBufferSize);
				}
			}
		} catch (Exception e) {
			logger.error("Could not check whether combined manipulation buffer size is exceeding the limit.", e);
		}

		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(
					"select count(bm.id) from TF_BUFFEREDMANIPULATION bm join TF_SMOODSTORAGEBUFFEREDMANIP ss on ss.BufferedManipulationId = bm.id where ss.SmoodStorageId = ?");
			ps.setString(1, latestStorageInDb.getId());
			rs = ps.executeQuery();
			rs.next();
			int size = rs.getInt(1);

			if ((this.maxManipulationBuffers >= 0) && (size >= this.maxManipulationBuffers)) {
				if (debug)
					logger.debug("Latest dump has exceeded max buffer count of " + this.maxManipulationBuffers + " (" + size + ")");
				return true;
			} else {
				if ((this.maxManipulationBuffers >= 0) && (trace)) {
					if (size > -1) {
						if (trace)
							logger.trace("The number of manipulation buffers  (" + size + ") does not yet exceed the limit of "
									+ this.maxManipulationBuffers);
					} else {
						if (trace)
							logger.trace(
									"Since there are no manipulation buffers, the limit of " + this.maxManipulationBuffers + " is not exceeded.");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not check whether combined manipulation buffer size is exceeding the limit.", e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}

		if (trace)
			logger.trace("The manipulation buffer size/count threshold is not exceeded.");
		return false;
	}

	protected void storeChange(Manipulation collectedManipulation) throws Exception {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		int nextManipulationBufferSequenceNumber = 0;
		Connection connection = null;

		try {

			if (debug) {
				String accessId = this.getAccessIdentifier();
				logger.debug("Storing a change for accessId " + accessId);
			}

			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			SmoodStorage latestStorageInDb = this.getLatestSmoodStorageHull(connection);

			boolean dumpFullSmood = false;

			if (latestStorageInDb == null) {
				dumpFullSmood = true;
			} else {
				dumpFullSmood = this.manipulationBufferSizeThresholdReached(connection, latestStorageInDb);
			}

			if (dumpFullSmood) {

				if (debug)
					logger.debug("A full dump will be created.");

				Object genericModelValue = database.getAllEntities();
				String fullDumpEncoded = this.encode(genericModelValue);
				int dumpLength = fullDumpEncoded.length();
				if (trace)
					logger.trace("The full dump has a length of " + dumpLength + " characters.");

				int sequenceNumber = 0;
				int localClassDependenciesSequenceNumber = 0;

				if (latestStorageInDb != null) {
					sequenceNumber = latestStorageInDb.getSequenceNumber() + 1;
					if (trace)
						logger.trace("The new sequence number for the dump will be " + sequenceNumber);
					localClassDependenciesSequenceNumber = latestStorageInDb.getClassDependenciesSequenceNumber();
				} else {
					if (trace)
						logger.trace("Since this seems to be first dump, the sequence number will be 0.");
				}

				this.storeSmoodBuffer(connection, latestStorageInDb, fullDumpEncoded, dumpLength, sequenceNumber,
						localClassDependenciesSequenceNumber);

				if (latestStorageInDb != null) {
					if (trace)
						logger.trace("An outdated dump exists. Checking whether it should be deleted.");
					this.deleteOlderSmoodDumps(connection, latestStorageInDb.getSequenceNumber());
				}

				nextManipulationBufferSequenceNumber = -1;

			} else {

				if (debug)
					logger.debug("Only a manipulation buffer will be created for storage " + latestStorageInDb.getId());

				Manipulation remotifiedManipulation = ManipulationRemotifier.remotify(collectedManipulation);
				String encodedManipulation = this.encode(remotifiedManipulation);
				int manipulationBufferSize = encodedManipulation.length();
				if (trace)
					logger.trace("The encoded manipulation has a length of " + manipulationBufferSize + " characters.");

				// int nextSequenceNumber = 0;
				int manipulationBufferCount = this.getManipulationBufferCount(connection, latestStorageInDb.getId());
				if (manipulationBufferCount > 0) {
					nextManipulationBufferSequenceNumber = this.manipulationBufferSequenceNumber + 1;
					if (trace)
						logger.trace("The new sequence number for the manipulation buffer is " + nextManipulationBufferSequenceNumber);
				}

				int oldCount = latestStorageInDb.getBufferedManipulationsSize();
				int newCount = oldCount + manipulationBufferSize;

				PreparedStatement ps = null;
				try {
					ps = connection.prepareStatement("update TF_SMOODSTORAGE set bufferedManipulationsSize = ? where id = ?");
					ps.setInt(1, newCount);
					ps.setString(2, latestStorageInDb.getId());
					ps.executeUpdate();
				} finally {
					IOTools.closeCloseable(ps, logger);
				}
				latestStorageInDb.setBufferedManipulationsSize(newCount);
				if (trace)
					logger.trace("The new size of the combined manipulation buffers is now " + newCount);

				PreparedStatement insertBm = null;
				PreparedStatement insertMapping = null;
				try {
					String bufferedManipulationId = UUID.randomUUID().toString();

					insertBm = connection.prepareStatement(
							"insert into TF_BUFFEREDMANIPULATION (id, encodedManipulation, sequenceNumber, size_) values (?, ?, ?, ?)");
					insertBm.setString(1, bufferedManipulationId);
					setClob(insertBm, 2, encodedManipulation);
					insertBm.setInt(3, nextManipulationBufferSequenceNumber);
					insertBm.setInt(4, manipulationBufferSize);
					insertBm.executeUpdate();

					insertMapping = connection
							.prepareStatement("insert into TF_SMOODSTORAGEBUFFEREDMANIP (SmoodStorageId, BufferedManipulationId) values (?, ?)");
					insertMapping.setString(1, latestStorageInDb.getId());
					insertMapping.setString(2, bufferedManipulationId);
					insertMapping.executeUpdate();

				} finally {
					IOTools.closeCloseable(insertBm, logger);
					IOTools.closeCloseable(insertMapping, logger);
				}

			}

			if (trace)
				logger.trace("Committing changes now.");

			connection.commit();

			this.manipulationBufferSequenceNumber = nextManipulationBufferSequenceNumber;

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not roll back.", e2);
				}
			}
			throw new ModelAccessException("Could not dump manipulation to storage Access.", e);
		} finally {
			IOTools.closeCloseable(connection, logger);
		}
		if (debug)
			logger.debug("Successfully stored the changes in the database.");
	}

	protected String encode(Object value) throws Exception {
		StringWriter sw = new StringWriter();
		CharacterMarshaller marshaller = this.getXmlMarshaller();
		marshaller.marshall(sw, value, this.serializationOptions);
		return sw.toString();
	}
	protected Object decode(String encodedValue) throws Exception {
		CharacterMarshaller marshaller = this.getXmlMarshaller();
		return marshaller.unmarshall(new StringReader(encodedValue), this.deserializationOptions);
	}

	protected int getManipulationBufferCount(Connection connection, String smoodStorageId) throws Exception {

		ResultSet rs = null;
		PreparedStatement ps = null;
		try {

			ps = connection.prepareStatement("select count(BufferedManipulationId) from TF_SMOODSTORAGEBUFFEREDMANIP where SmoodStorageId = ?");
			ps.setString(1, smoodStorageId);
			rs = ps.executeQuery();

			rs.next();
			int count = rs.getInt(1);
			return count;

		} catch (Exception e) {
			throw new Exception("Could not get number of manipulation buffers for smood storage " + smoodStorageId, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	protected void storeSmoodBuffer(Connection connection, SmoodStorage latestStorageInDb, String fullDumpEncoded, int dumpLength, int sequenceNumber,
			int localClassDependenciesSequenceNumber) throws Exception {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			String smoodStorageId = UUID.randomUUID().toString();

			ps = connection.prepareStatement(
					"insert into TF_SMOODSTORAGE (id, accessId, bufferedManipulationsSize, classDependSequenceNumber, encodedData, sequenceNumber, size_) values (?, ?, ?, ?, ?, ?, ?)");

			ps.setString(1, smoodStorageId);
			ps.setString(2, this.getAccessIdentifier());
			ps.setInt(3, 0);
			ps.setInt(4, localClassDependenciesSequenceNumber);
			setClob(ps, 5, fullDumpEncoded);
			ps.setInt(6, sequenceNumber);
			ps.setInt(7, dumpLength);

			ps.executeUpdate();

			List<String> assignedClassDependencies = this.getAssignedClassDependencies(connection, latestStorageInDb.getId());
			if (!assignedClassDependencies.isEmpty()) {
				ps2 = connection.prepareStatement("insert into TF_SMOODSTORAGECLASSDEPEND (SmoodStorageId, JavaClassId) values (?, ?)");
				for (String id : assignedClassDependencies) {
					ps2.setString(1, smoodStorageId);
					ps2.setString(2, id);
					ps2.executeUpdate();
				}
			}

			connection.commit();

		} catch (Exception e) {
			throw new Exception("Could not dump the full smood buffer", e);
		} finally {
			IOTools.closeCloseable(ps, logger);
			IOTools.closeCloseable(ps2, logger);
		}
	}

	protected List<String> getAssignedClassDependencies(Connection connection, String smoodStorageId) throws Exception {

		List<String> assignedIds = new LinkedList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement("select JavaClassId from TF_SMOODSTORAGECLASSDEPEND where SmoodStorageId = ?");
			ps.setString(1, smoodStorageId);
			rs = ps.executeQuery();
			while (rs.next()) {
				assignedIds.add(rs.getString(1));
			}
		} catch (Exception e) {
			throw new Exception("Could not get class dependencies for buffer " + smoodStorageId, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
		return assignedIds;
	}

	protected void deleteOlderSmoodDumps(Connection connection, int outdatedSequenceNumber) {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (this.keepNOldDumps < 0) {
			if (debug)
				logger.debug("keepNOldDumps is set to a negative value. Not deleting any old dumps.");
			return;
		}
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			if (trace)
				logger.trace("Searching for all dumps with a sequence number less or equal than " + outdatedSequenceNumber);

			ps = connection
					.prepareStatement("select id from TF_SMOODSTORAGE where accessId = ? and sequenceNumber <= ? order by sequenceNumber desc");
			ps.setString(1, this.getAccessIdentifier());
			ps.setInt(2, outdatedSequenceNumber);

			rs = ps.executeQuery();

			List<String> smoodStorageList = new LinkedList<String>();
			while (rs.next()) {
				smoodStorageList.add(rs.getString(1));
			}

			if (smoodStorageList.size() > 0) {

				if (trace)
					logger.trace("Found " + smoodStorageList.size() + " old dumps");

				// Keeping a configurable amount of old dumps, casting the elements to SmoodStorage
				List<String> entitiesToDelete = new ArrayList<String>(smoodStorageList.size());
				for (int i = this.keepNOldDumps; i < smoodStorageList.size(); ++i) {
					entitiesToDelete.add(smoodStorageList.get(i));
				}
				int delSize = entitiesToDelete.size();
				if (debug)
					logger.debug("Deleting " + delSize + " dumps. Keeping: " + this.keepNOldDumps);

				if (delSize > 0) {

					this.deleteManipulationBuffers(connection, entitiesToDelete);
					this.deleteClassReferences(connection, entitiesToDelete);
					this.deleteSmoodStorages(connection, entitiesToDelete);

					connection.commit();
				}
			} else {
				if (trace)
					logger.trace("No outdated dumps found.");
			}

		} catch (Exception e) {
			logger.error("Could not delete older dumps (last sequenceNumber: " + outdatedSequenceNumber + ")", e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}

		if (debug)
			logger.debug("Done looking for / deleting old storage dumps.");
	}

	protected void deleteSmoodStorages(Connection connection, List<String> entitiesToDelete) throws Exception {
		Statement st = null;
		try {
			st = connection.createStatement();
			StringBuilder sb = new StringBuilder();
			for (String id : entitiesToDelete) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append("'");
				sb.append(id);
				sb.append("'");
			}
			st.executeUpdate("delete from TF_SMOODSTORAGE where id in (" + sb.toString() + ")");
		} finally {
			IOTools.closeCloseable(st, logger);
		}
	}

	protected void deleteManipulationBuffers(Connection connection, List<String> smoodStorageIdList) throws Exception {
		Statement selectBms = null;
		Statement deleteMappings = null;
		Statement deleteBms = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder("select BufferedManipulationId from TF_SMOODSTORAGEBUFFEREDMANIP where SmoodStorageId in (");
			boolean first = true;
			for (String id : smoodStorageIdList) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append("'");
				sb.append(id);
				sb.append("'");
			}
			sb.append(")");

			selectBms = connection.createStatement();
			rs = selectBms.executeQuery(sb.toString());

			List<String> bmToDelete = new LinkedList<String>();
			while (rs.next()) {
				String bmId = rs.getString(1);
				bmToDelete.add(bmId);
			}

			if (!bmToDelete.isEmpty()) {

				sb = new StringBuilder("delete from TF_SMOODSTORAGEBUFFEREDMANIP where SmoodStorageId in (");
				first = true;
				for (String id : smoodStorageIdList) {
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append("'");
					sb.append(id);
					sb.append("'");
				}
				sb.append(")");

				deleteMappings = connection.createStatement();
				deleteMappings.executeUpdate(sb.toString());

				sb = new StringBuilder("delete from TF_BUFFEREDMANIPULATION where id in (");
				first = true;
				for (String id : bmToDelete) {
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append("'");
					sb.append(id);
					sb.append("'");
				}
				sb.append(")");

				deleteBms = connection.createStatement();
				deleteBms.executeUpdate(sb.toString());
			}

		} catch (Exception e) {
			throw new Exception("Could not delete manipulation buffers for " + smoodStorageIdList, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(selectBms, logger);
			IOTools.closeCloseable(deleteMappings, logger);
			IOTools.closeCloseable(deleteBms, logger);
		}
	}
	protected void deleteClassReferences(Connection connection, List<String> smoodStorageIdList) throws Exception {
		Statement deleteMappings = null;
		try {
			StringBuilder sb = new StringBuilder("delete from TF_SMOODSTORAGECLASSDEPEND where SmoodStorageId in (");
			boolean first = true;
			for (String id : smoodStorageIdList) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append("'");
				sb.append(id);
				sb.append("'");
			}
			sb.append(")");

			deleteMappings = connection.createStatement();
			deleteMappings.executeUpdate(sb.toString());

		} catch (Exception e) {
			throw new Exception("Could not delete class mappings for " + smoodStorageIdList, e);
		} finally {
			IOTools.closeCloseable(deleteMappings, logger);
		}
	}

	@Override
	public GmMetaModel getMetaModel() {
		synchronized (this.metaModelSync) {
			if (this.metaModel == null) {
				try {
					this.metaModel = this.modelProvider.get();
				} catch (final RuntimeException e) {
					throw new GenericModelException("error while providing metamodel", e);
				}
			}

			return this.metaModel;
		}
	}

	protected Smood getDatabase() throws ModelAccessException {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Getting up-to-date database.");

		if (trace)
			logger.trace("Trying to obtain lock on local storage.");
		this.localStorageLock.lock();
		if (trace)
			logger.trace("Obtained lock on local storage.");

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			// Get latest SmoodStorage from Access
			if (trace)
				logger.trace("Getting latest smood storage.");
			SmoodStorage latestStorageInDb = this.getLatestSmoodStorageHull(connection);

			// Check whether we have an outdated state; i.e., we have to reload

			if (trace)
				logger.trace("Determining whether we have to reload the smood from the storage.");
			List<BufferedManipulation> newBufferedManipulationsFromDb = null;

			boolean reload = false;
			if (latestStorageInDb != null) {
				if (trace)
					logger.trace("There is a smood dump in the database: " + latestStorageInDb.getId());

				if ((this.database == null) || (this.storageSequenceNumber < latestStorageInDb.getSequenceNumber())) {

					if (trace)
						logger.trace("Either this is the first access or the local sequence number " + this.storageSequenceNumber
								+ " is outdated. Reloading...");
					reload = true;

				} else {

					if (latestStorageInDb.getBufferedManipulationsSize() == this.lastKnownBufferSize) {

						if (trace)
							logger.trace("The local sequence number " + this.storageSequenceNumber + " is not outdated and the lastKnownBufferSize "
									+ lastKnownBufferSize + " has not changed. We have the current status.");

					} else {

						if (trace)
							logger.trace("The local sequence number " + this.storageSequenceNumber
									+ " is not outdated. Checking for new manipulation buffers (i.e., higher than "
									+ this.manipulationBufferSequenceNumber + ")");

						newBufferedManipulationsFromDb = this.loadManipulationBufferHulls(connection, latestStorageInDb,
								newBufferedManipulationsFromDb);

						if (!newBufferedManipulationsFromDb.isEmpty()) {
							if (trace)
								logger.trace("There are " + newBufferedManipulationsFromDb.size()
										+ " new manipulation buffers that have to be incorporated into the local storage.");
							reload = true;
						} else {
							if (trace)
								logger.trace("There are no new manipulation buffers. We have an up-to-date storage.");
						}
					}
				}
			} else {
				if (trace)
					logger.trace("There is no storage dump in the database.");

				if (this.database == null) {

					if (debug)
						logger.debug("This is the first initialization of the local storage. Creating a new Smood.");

					Smood smood = new Smood(readWriteLock);
					smood.setLocaleProvider(localeProvider);

					Object genericModelValue = null;
					try {
						/* NOTE that we simply must do the following three steps in this order, because there is a situation when a smood
						 * contains it's own meta-model, which is retrieved per query on that very smood. This can only work if we first
						 * initialize the smood, then set is as the database of this access and only then try to get the meta-model. (Setting
						 * the meta-model first makes sense in general, because the meta-model contains index-related meta-data.) */
						if (this.initialStorage != null) {
							if (debug)
								logger.debug("Loading initial storage");
							genericModelValue = this.initialStorage.loadModel();
							if (genericModelValue != null) {

								if ((storageAssemblyIsLinearPopulation) && (genericModelValue instanceof Set<?>)) {
									if (debug)
										logger.debug("" + super.getAccessId() + ": Treating initial storage as a linear set of GenericEntities");
									@SuppressWarnings("unchecked")
									Set<GenericEntity> linearSet = (Set<GenericEntity>) genericModelValue;
									smood.initializePopulation(linearSet, true);
								} else {
									if (debug)
										logger.debug("" + super.getAccessId() + ": Initial storage is of type " + genericModelValue.getClass());
									smood.initialize(genericModelValue);
								}

							}
						} else {
							if (trace)
								logger.trace("There is no initial storage configured.");
						}
						this.database = smood;

						if (selfModelName != null) {
							smood.setSelfMetaModel(selfModelName);
						} else {
							smood.setMetaModel(getMetaModel());
						}
						// smood.setMetaModel(getMetaModel());

					} catch (Exception e) {
						this.database = null;
						throw new ModelAccessException("Could not load data", e);
					}

					// Storing in DB

					if (trace)
						logger.trace("We have created/loaded an initial storage. Now dumping it to the database.");

					this.storeSmoodBuffer(connection, genericModelValue);

				} else {
					if (trace)
						logger.trace("Since we have already a local storage in memory, there is nothing more to do.");
				}
			}

			if (reload) {
				if (debug)
					logger.debug("We have established that we have to reload the storage (or parts of it) from the database.");
				try {
					this.updateJavaClasses(connection, latestStorageInDb);
					this.loadSmoodBuffer(connection, latestStorageInDb, newBufferedManipulationsFromDb);

					this.lastKnownBufferSize = latestStorageInDb.getBufferedManipulationsSize();
				} catch (Exception e) {
					throw new ModelAccessException("Could not load SmoodStorage " + latestStorageInDb, e);
				}
			} else {
				if (trace)
					logger.trace("There is no need to reload the storage from the database.");
			}

			connection.commit();

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not roll back.", e2);
				}
			}

			throw new ModelAccessException("Could not get the up-to-date database.", e);
		} finally {
			this.localStorageLock.unlock();
			if (trace)
				logger.trace("Released lock on local storage.");

			IOTools.closeCloseable(connection, logger);
		}
		if (debug)
			logger.debug("Returning up-to-date local storage.");
		return database;
	}

	protected void storeSmoodBuffer(Connection connection, Object genericModelValue) throws ModelAccessException {
		PreparedStatement ps = null;
		try {

			String fullDumpEncoded = null;
			int fullDumpSize = 0;
			if (genericModelValue != null) {
				fullDumpEncoded = this.encode(genericModelValue);
				fullDumpSize = fullDumpEncoded.length();
			}

			ps = connection.prepareStatement(
					"insert into TF_SMOODSTORAGE (id, accessId, bufferedManipulationsSize, classDependSequenceNumber, encodedData, sequenceNumber, size_) values (?, ?, ?, ?, ?, ?, ?)");

			String smoodBufferId = UUID.randomUUID().toString();

			ps.setString(1, smoodBufferId);
			ps.setString(2, this.getAccessIdentifier());
			ps.setInt(3, 0);
			ps.setInt(4, -1);
			setClob(ps, 5, fullDumpEncoded);
			ps.setInt(6, 0);
			ps.setInt(7, fullDumpSize);

			ps.executeUpdate();

		} catch (Exception e) {
			throw new ModelAccessException("Error while dumping initial storage in DB", e);
		} finally {
			IOTools.closeCloseable(ps, logger);
		}
	}

	protected List<BufferedManipulation> loadManipulationBufferHulls(Connection connection, SmoodStorage latestStorageInDb,
			List<BufferedManipulation> newBufferedManipulationsFromDb) throws ModelAccessException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(
					"select bm.id, bm.sequenceNumber, bm.size_ from TF_BUFFEREDMANIPULATION bm inner join TF_SMOODSTORAGEBUFFEREDMANIP ss on bm.id = ss.BufferedManipulationId where ss.SmoodStorageId = ? and bm.sequenceNumber > ? order by bm.sequenceNumber asc");

			ps.setString(1, latestStorageInDb.getId());
			ps.setInt(2, this.manipulationBufferSequenceNumber);

			rs = ps.executeQuery();

			newBufferedManipulationsFromDb = new LinkedList<BufferedManipulation>();
			while (rs.next()) {
				BufferedManipulation bm = BufferedManipulation.T.create();
				bm.setId(rs.getString(1));
				bm.setSequenceNumber(rs.getInt(2));
				bm.setSize(rs.getInt(3));
				newBufferedManipulationsFromDb.add(bm);
			}
		} catch (Exception e) {
			throw new ModelAccessException("Could not get the manipulation buffers for storage " + latestStorageInDb.getId(), e);

		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
		return newBufferedManipulationsFromDb;
	}

	protected SmoodStorage getLatestSmoodStorageHull(Connection connection) throws ModelAccessException {

		boolean trace = logger.isTraceEnabled();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String accessId = this.getAccessIdentifier();
			if (trace)
				logger.trace("Getting latest smood storage for accessId " + accessId + " that has greater or equal sequence number than "
						+ this.storageSequenceNumber);

			SmoodStorage latestStorageInDb = null;

			ps = connection.prepareStatement(
					"select id, bufferedManipulationsSize, classDependSequenceNumber, sequenceNumber, size_ from TF_SMOODSTORAGE where accessId = ? and sequenceNumber >= ? order by sequenceNumber desc");
			ps.setString(1, this.getAccessIdentifier());
			ps.setInt(2, this.storageSequenceNumber);

			rs = ps.executeQuery();

			if (rs.next()) {
				latestStorageInDb = SmoodStorage.T.create();
				latestStorageInDb.setId(rs.getString(1));
				latestStorageInDb.setBufferedManipulationsSize(rs.getInt(2));
				latestStorageInDb.setClassDependenciesSequenceNumber(rs.getInt(3));
				latestStorageInDb.setSequenceNumber(rs.getInt(4));
				latestStorageInDb.setSize(rs.getInt(5));
			}

			if (trace) {
				if (latestStorageInDb != null) {
					logger.trace("Found at least one dump with sequence number " + latestStorageInDb.getSequenceNumber());
				} else {
					logger.trace("Found not a single dump with a sequence number of " + this.storageSequenceNumber
							+ " or higher. This seems to be an error.");
				}
			}
			return latestStorageInDb;

		} catch (Exception e) {
			throw new ModelAccessException("Could not get latest SmoodStorage from Access.", e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	protected JavaClass getJavaClassHull(Connection connection, String qualifiedName, SmoodStorage smoodStorage) throws Exception {

		boolean trace = logger.isTraceEnabled();

		ResultSet rs = null;
		PreparedStatement ps = null;
		try {

			String accessId = this.getAccessIdentifier();
			if (trace)
				logger.trace("Getting Java class " + qualifiedName + " from smood storage " + accessId);

			ps = connection.prepareStatement(
					"select jc.id, jc.md5, jc.qualifiedName, jc.sequenceNumber from TF_JAVACLASS jc join TF_SMOODSTORAGECLASSDEPEND ss on ss.JavaClassId = jc.id where ss.SmoodStorageId = ? and jc.qualifiedName = ? order by jc.sequenceNumber desc");
			ps.setString(1, smoodStorage.getId());
			ps.setString(2, qualifiedName);
			rs = ps.executeQuery();
			if (rs.next()) {

				JavaClass jc = JavaClass.T.create();
				jc.setId(rs.getString(1));
				jc.setMd5(rs.getString(2));
				jc.setQualifiedName(rs.getString(3));
				jc.setSequenceNumber(rs.getInt(4));

				return jc;
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new Exception("Could not get classes for buffer " + smoodStorage.getId(), e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}

	}

	protected void loadSmoodBuffer(Connection connection, SmoodStorage latestStorageInDb, List<BufferedManipulation> newBufferedManipulationsFromDb)
			throws Exception {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (latestStorageInDb.getSequenceNumber() > this.storageSequenceNumber) {

			if (trace)
				logger.trace(
						"The sequence number in the database " + latestStorageInDb + " is higher than the local one: " + this.storageSequenceNumber);

			Object smoodData = null;
			String encodedData = this.getSmoodBufferEncodedData(connection, latestStorageInDb.getId());
			if (encodedData == null) {
				if (debug)
					logger.debug(
							"The encoded data of SmoodStorage " + latestStorageInDb + " is null. Registered length: " + latestStorageInDb.getSize());
			} else {
				if (trace)
					logger.trace("Loaded encoded dump of length " + encodedData.length());
				try {
					smoodData = this.decode(encodedData);
				} catch (CodecException e) {
					throw new Exception("Could not decode encoded Smood data.", e);
				}
			}
			try {
				Smood smood = new Smood(readWriteLock);
				smood.setLocaleProvider(this.localeProvider);

				/* NOTE that we simply must do the following three steps in this order, because there is a situation when a smood
				 * contains it's own meta-model, which is retrieved per query on that very smood. This can only work if we first
				 * initialize the smood, then set is as the database of this access and only then try to get the meta-model. (Setting
				 * the meta-model first makes sense in general, because the meta-model contains index-related meta-data.) */
				if (smoodData != null) {

					// We KNOW for sure that the smoodData is a linear Set of all entities as this code is the only place where this data
					// is actually written into the DB
					if (debug)
						logger.debug("Treating DB data as a linear set of GenericEntities");
					@SuppressWarnings("unchecked")
					Set<GenericEntity> linearSet = (Set<GenericEntity>) smoodData;
					smood.initializePopulation(linearSet, true);

				}
				database = smood;

				/* Updating storageSequenceNumber before getMetaModel() to avoid recursive loadSmoodBuffer() invocation if
				 * getMetaModel() triggers a query on this very access. */
				this.storageSequenceNumber = latestStorageInDb.getSequenceNumber();

				/* To resolve just another Muenchhausen we need to introduce a way to let the Smood determine it's MetaModel from it's
				 * own database. This selfModelName is most likely only configured for the cortex Smood. */
				if (selfModelName != null) {
					smood.setSelfMetaModel(selfModelName);
				} else {
					smood.setMetaModel(getMetaModel());
				}

				// smood.setMetaModel(getMetaModel());

				// Load all manipulation buffers
				if (trace)
					logger.trace("Loading all manipulation buffers of dump " + latestStorageInDb.getId());

				List<BufferedManipulation> bufferedManipulations = this.loadManipulationBuffers(connection, latestStorageInDb.getId());

				this.manipulationBufferSequenceNumber = -1;

				this.updateSmoodFromManipulationBuffer(connection, smood, bufferedManipulations);

				/* Muenchhausen ... see above */
				if (selfModelName != null) {
					smood.setSelfMetaModel(selfModelName);
					// smood.setMetaModel(getMetaModel());
				}

				if (trace)
					logger.trace("Updated to new storage sequence number " + this.storageSequenceNumber);

			} catch (Exception e) {
				database = null;
				throw new ModelAccessException("Could not load data for access " + this.getAccessIdentifier(), e);
			}

		} else {

			if (trace)
				logger.trace("The sequence number in the database is the same as local one: " + this.storageSequenceNumber
						+ ". Looking for new manipulation buffers.");

			this.updateSmoodFromManipulationBuffer(connection, this.database, newBufferedManipulationsFromDb);

		}
	}

	protected List<BufferedManipulation> loadManipulationBuffers(Connection connection, String smoodStorageId) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {

			ps = connection.prepareStatement(
					"select bm.id, bm.encodedManipulation, bm.sequenceNumber, bm.size_ from TF_BUFFEREDMANIPULATION bm join TF_SMOODSTORAGEBUFFEREDMANIP ss on ss.BufferedManipulationId = bm.id where ss.SmoodStorageId = ? order by bm.sequenceNumber asc");
			ps.setString(1, smoodStorageId);

			rs = ps.executeQuery();

			List<BufferedManipulation> bmList = new LinkedList<BufferedManipulation>();
			while (rs.next()) {
				BufferedManipulation bm = BufferedManipulation.T.create();
				bm.setId(rs.getString(1));
				bm.setEncodedManipulation(getClob(rs, 2));
				bm.setSequenceNumber(rs.getInt(3));
				bm.setSize(rs.getInt(4));
				bmList.add(bm);
			}

			return bmList;
		} catch (Exception e) {
			throw new Exception("Could not load manipulation buffer of smood storage " + smoodStorageId, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	protected String getSmoodBufferEncodedData(Connection connection, String smoodStorageId) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {

			ps = connection.prepareStatement("select encodedData from TF_SMOODSTORAGE where id = ?");
			ps.setString(1, smoodStorageId);
			rs = ps.executeQuery();

			if (rs.next()) {
				String encodedData = getClob(rs, 1);
				return encodedData;
			} else {
				return null;
			}

		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	protected void updateSmoodFromManipulationBuffer(Connection connection, Smood smood, List<BufferedManipulation> manipulations)
			throws ModelAccessException {
		boolean trace = logger.isTraceEnabled();

		if ((manipulations != null) && (manipulations.size() > 0)) {
			if (trace)
				logger.trace("Checking " + manipulations.size() + " buffered manipulations; local buffer sequcence number is "
						+ this.manipulationBufferSequenceNumber);

			for (BufferedManipulation bufferedManipulation : manipulations) {
				if (bufferedManipulation.getSequenceNumber() > this.manipulationBufferSequenceNumber) {
					// load manipulation
					if (trace)
						logger.trace("Loading buffered manipulation " + bufferedManipulation.getId() + " which has sequence number "
								+ bufferedManipulation.getSequenceNumber());
					try {
						String encodedManipulation = bufferedManipulation.getEncodedManipulation();
						if (encodedManipulation == null) {
							encodedManipulation = this.loadManipulationBufferEncodedData(connection, bufferedManipulation.getId());
							bufferedManipulation.setEncodedManipulation(encodedManipulation);
						}
						if ((encodedManipulation == null) || (encodedManipulation.length() == 0)) {
							throw new ModelAccessException("The encoded manipulation buffer of BufferedManipulation with id "
									+ bufferedManipulation.getId() + " is empty or null.");
						}
						Manipulation manipulation = (Manipulation) this.decode(encodedManipulation);

						ManipulationRequest manipulationRequest = ManipulationRequest.T.create();
						manipulationRequest.setManipulation(manipulation);
						smood.applyManipulation(manipulationRequest);
					} catch (Exception e) {
						throw new ModelAccessException("Error while updating smood from manipulation buffer: " + bufferedManipulation.getId(), e);
					}

					this.manipulationBufferSequenceNumber = bufferedManipulation.getSequenceNumber();
				}
			}

			if (trace)
				logger.trace("Updated local buffered manipulation sequence number to " + this.manipulationBufferSequenceNumber);
		}
	}

	protected String loadManipulationBufferEncodedData(Connection connection, String manipulationBufferId) throws Exception {

		ResultSet rs = null;
		PreparedStatement ps = null;

		try {

			ps = connection.prepareStatement("select encodedManipulation from TF_BUFFEREDMANIPULATION where id = ?");
			ps.setString(1, manipulationBufferId);
			rs = ps.executeQuery();

			if (rs.next()) {
				String data = getClob(rs, 1);
				return data;
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new Exception("Could not load the encoded data from manipulation buffer " + manipulationBufferId, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	@Configurable
	public void setLocaleProvider(Supplier<String> localeProvider) {
		this.localeProvider = localeProvider;
	}

	@Required
	@Configurable
	public void setDbLockManager(LockManager dbLockManager) {
		this.dbLockManager = dbLockManager;
	}

	@Configurable
	public void setMaxManipulationBuffers(int maxManipulationBuffers) {
		this.maxManipulationBuffers = maxManipulationBuffers;
	}

	@Configurable
	public void setKeepNOldDumps(int keepNOldDumps) {
		this.keepNOldDumps = keepNOldDumps;
	}

	@Configurable
	public void setLockObject(GenericEntity lockObject) {
		this.lockObject = lockObject;
	}

	@Configurable
	public void setInitialStorage(NonIncrementalAccess initialStorage) {
		this.initialStorage = initialStorage;
	}

	@Required
	@Configurable
	public void setModelProvider(final Supplier<GmMetaModel> metaModelProvider) {
		this.modelProvider = metaModelProvider;
	}

	@Configurable
	public void setMaxManipulationBufferSize(int maxManipulationBufferSize) {
		this.maxManipulationBufferSize = maxManipulationBufferSize;
	}

	@Override
	public void storeClass(String qualifiedName, InputStream inputStream, Set<String> dependencies) throws AccessServiceException {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Define class: Getting up-to-date database.");

		if (trace)
			logger.trace("Trying to obtain lock on local storage.");
		this.localStorageLock.lock();
		if (trace)
			logger.trace("Obtained lock on local storage.");

		Lock exclusiveLock = null;
		try {
			if (trace)
				logger.trace("Trying to get lock on DB storage.");
			LockBuilder lockBuilder = null;
			if (this.lockObject != null) {
				lockBuilder = dbLockManager.forEntity(this.lockObject);
			} else {
				lockBuilder = dbLockManager.forIdentifier(this.getAccessIdentifier());
			}
			exclusiveLock = lockBuilder.exclusive();
			exclusiveLock.lock();
			if (trace)
				logger.trace("Lock on DB storage obtained.");

		} catch (Exception e) {
			throw new AccessServiceException("Could not get an exclusive lock on " + this.lockObject, e);
		}

		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			connection.setAutoCommit(false);

			// Get latest SmoodStorage from Access
			if (trace)
				logger.trace("Getting latest smood storage.");
			SmoodStorage latestStorageInDb = null;
			try {
				latestStorageInDb = this.getLatestSmoodStorageHull(connection);
			} catch (ModelAccessException e) {
				throw new AccessServiceException("Could not get latest smood storage dump.", e);
			}
			if (latestStorageInDb == null) {
				try {
					// This makes sure that when the database is not yet initialized, it will load the initialStorage
					this.getDatabase();
					latestStorageInDb = this.getLatestSmoodStorageHull(connection);
				} catch (ModelAccessException e) {
					throw new AccessServiceException("Could not get database", e);
				}
			}

			this.updateJavaClasses(connection, latestStorageInDb);

			String encodedJavaClass = this.getJavaClassCodec().encode(inputStream);
			String md5OfEncodedJavaClass = MD5HashGenerator.MD5(encodedJavaClass);

			int javaClassSequenceNumber = 0;
			JavaClass javaClass = this.getJavaClassHull(connection, qualifiedName, latestStorageInDb);
			if (javaClass != null) {

				String storedJavaClassMd5 = javaClass.getMd5();
				if ((storedJavaClassMd5 != null) && (storedJavaClassMd5.equals(md5OfEncodedJavaClass))) {
					System.out.println("We have already this version of " + qualifiedName + " in storage; nothing to do");
					System.out.flush();
					if (debug)
						logger.debug("We have already this version of " + qualifiedName + " in storage; nothing to do");
					return;
				}

				if (trace)
					logger.trace("Replacing current class definition " + qualifiedName + " " + javaClass.getSequenceNumber());
				javaClassSequenceNumber = javaClass.getSequenceNumber() + 1;

				this.removeClassDependency(connection, latestStorageInDb, javaClass);
			}

			int localClassDependenciesSequenceNumber = latestStorageInDb.getClassDependenciesSequenceNumber();
			localClassDependenciesSequenceNumber++;

			PreparedStatement ps = null;
			try {
				ps = connection.prepareStatement("update TF_SMOODSTORAGE set classDependSequenceNumber = ? where id = ?");
				ps.setInt(1, localClassDependenciesSequenceNumber);
				ps.setString(2, latestStorageInDb.getId());
				ps.executeUpdate();
			} finally {
				IOTools.closeCloseable(ps, logger);
			}
			latestStorageInDb.setClassDependenciesSequenceNumber(localClassDependenciesSequenceNumber);

			if (trace)
				logger.trace("Increased classDependenciesSequenceNumber to " + localClassDependenciesSequenceNumber);

			PreparedStatement insertClass = null;
			PreparedStatement insertMapping = null;
			try {
				String javaClassId = UUID.randomUUID().toString();

				insertClass = connection
						.prepareStatement("insert into TF_JAVACLASS (id, classData, md5, qualifiedName, sequenceNumber) values (?, ?, ?, ?, ?)");
				insertClass.setString(1, javaClassId);
				setClob(insertClass, 2, encodedJavaClass);
				insertClass.setString(3, md5OfEncodedJavaClass);
				insertClass.setString(4, qualifiedName);
				insertClass.setInt(5, javaClassSequenceNumber);
				insertClass.executeUpdate();

				insertMapping = connection.prepareStatement("insert into TF_SMOODSTORAGECLASSDEPEND (SmoodStorageId, JavaClassId) values (?, ?)");
				insertMapping.setString(1, latestStorageInDb.getId());
				insertMapping.setString(2, javaClassId);
				insertMapping.executeUpdate();

			} finally {
				IOTools.closeCloseable(insertClass, logger);
				IOTools.closeCloseable(insertMapping, logger);
			}

			this.classDependenciesSequenceNumber = localClassDependenciesSequenceNumber;

			JavaClassBuffer localJavaClassBuffer = new JavaClassBuffer(javaClassSequenceNumber);
			this.localJavaClassMap.put(qualifiedName, localJavaClassBuffer);

			connection.commit();

			if (debug)
				logger.debug("Wrote new class definition: " + this.getAccessIdentifier() + " " + localClassDependenciesSequenceNumber);

		} catch (Throwable e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not roll back.", e2);
				}
			}

			throw new AccessServiceException("Could not store class definition for " + qualifiedName, e);
		} finally {
			if (exclusiveLock != null) {
				try {
					exclusiveLock.unlock();
					if (trace)
						logger.trace("Lock on DB storage released.");
				} catch (Exception e) {
					logger.error("Could not unlock ExclusiveLock " + exclusiveLock, e);
				}
			}

			this.localStorageLock.unlock();
			if (trace)
				logger.trace("Released lock on local storage.");

			IOTools.closeCloseable(connection, logger);
		}

	}

	protected void removeClassDependency(Connection connection, SmoodStorage latestStorageInDb, JavaClass javaClass) throws Exception {
		PreparedStatement ps = null;
		try {

			ps = connection.prepareStatement("delete from TF_SMOODSTORAGECLASSDEPEND where SmoodStorageId = ? and JavaClassId = ?");
			ps.setString(1, latestStorageInDb.getId());
			ps.setString(2, javaClass.getId());
			ps.executeUpdate();

		} catch (Exception e) {
			throw new Exception("Could not remove class dependency from " + latestStorageInDb.getId() + " to " + javaClass.getId(), e);
		} finally {
			IOTools.closeCloseable(ps, logger);
		}
	}

	@Override
	public Set<String> getQualifiedNamesOfStoredClasses() throws AccessServiceException {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Getting up-to-date database.");

		if (trace)
			logger.trace("Trying to obtain lock on local storage.");
		this.localStorageLock.lock();
		if (trace)
			logger.trace("Obtained lock on local storage.");

		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			connection.setAutoCommit(false);

			// Get latest SmoodStorage from Access
			if (trace)
				logger.trace("Getting latest smood storage.");
			SmoodStorage latestStorageInDb = null;
			try {
				latestStorageInDb = this.getLatestSmoodStorageHull(connection);
			} catch (ModelAccessException e) {
				throw new AccessServiceException("Could not get latest smood storage dump.", e);
			}
			if (latestStorageInDb == null) {
				try {
					// This makes sure that when the database is not yet initialized, it will load the initialStorage
					this.getDatabase();
					latestStorageInDb = this.getLatestSmoodStorageHull(connection);
				} catch (ModelAccessException e) {
					throw new AccessServiceException("Could not get database", e);
				}
			}

			if (latestStorageInDb != null) {
				this.updateJavaClasses(connection, latestStorageInDb);
			}

			Set<String> qualifiedNames = this.localJavaClassMap.keySet();
			return qualifiedNames;

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not roll back.", e2);
				}
			}
			throw new AccessServiceException("Could not get set of qualified names.", e);
		} finally {
			this.localStorageLock.unlock();
			if (trace)
				logger.trace("Released lock on local storage.");

			IOTools.closeCloseable(connection, logger);
		}
	}

	protected void updateJavaClasses(Connection connection, SmoodStorage latestStorageInDb) throws Exception {

		// System.out.println("Updating Java classes if necessary; Local classDependenciesSequenceNumber:
		// "+this.classDependenciesSequenceNumber+", remote: "+latestStorageInDb.getClassDependenciesSequenceNumber());
		// System.out.flush();

		if (latestStorageInDb.getClassDependenciesSequenceNumber() <= this.classDependenciesSequenceNumber) {
			logger.debug("We have the same sequence number. Nothing to do");
			return;
		}

		Set<JavaClass> javaClassDependencies = new HashSet<JavaClass>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = connection.prepareStatement(
					"select jc.id, jc.classData, jc.qualifiedName, jc.sequenceNumber from TF_JAVACLASS jc inner join TF_SMOODSTORAGECLASSDEPEND sc on sc.JavaClassId = jc.id where sc.SmoodStorageId = ?");

			ps.setString(1, latestStorageInDb.getId());

			rs = ps.executeQuery();
			while (rs.next()) {
				JavaClass jc = JavaClass.T.create();
				jc.setId(rs.getString(1));
				jc.setClassData(rs.getString(2));
				jc.setQualifiedName(rs.getString(3));
				jc.setSequenceNumber(rs.getInt(4));
				javaClassDependencies.add(jc);
			}

		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}

		// System.out.println("Java classes in DB: "+javaClassMap.keySet());
		// System.out.flush();

		if (javaClassDependencies.size() > 0) {

			File tempFolder = this.createTempFolder();
			boolean classWritten = false;

			try {

				for (JavaClass javaClass : javaClassDependencies) {

					String qualifiedName = javaClass.getQualifiedName();

					boolean isNewDefinition = false;

					JavaClassBuffer localJavaClassBuffer = this.localJavaClassMap.get(qualifiedName);
					if (localJavaClassBuffer == null) {
						isNewDefinition = true;
					} else {
						if (localJavaClassBuffer.getSequenceNumber() < javaClass.getSequenceNumber()) {
							isNewDefinition = true;
						}
					}

					if (isNewDefinition) {

						// System.out.println("Loading new class definition "+qualifiedName);
						// System.out.flush();

						File outputFile = new File(tempFolder, qualifiedName);
						String encodedClassData = javaClass.getClassData();
						InputStream inputStream = this.getJavaClassCodec().decode(encodedClassData);
						IOTools.inputToFile(inputStream, outputFile);
						inputStream.close();

						localJavaClassBuffer = new JavaClassBuffer(javaClass.getSequenceNumber());
						this.localJavaClassMap.put(qualifiedName, localJavaClassBuffer);

						classWritten = true;
					} else {
						// System.out.println("Already known class "+qualifiedName);
						// System.out.flush();

					}
				}

				if (classWritten) {
					try {
						FolderClassLoader folderClassLoader = new FolderClassLoader();
						folderClassLoader.setClassFolder(tempFolder);
						folderClassLoader.postConstruct();
					} catch (Exception e) {
						throw new Exception("Could not load class files from folder " + tempFolder.getAbsolutePath(), e);
					}
				}

			} finally {
				try {
					FileTools.deleteDirectoryRecursively(tempFolder);
				} catch (Exception e) {
					logger.error("Could not delete temporary files from " + tempFolder.getAbsolutePath(), e);
				}
			}
		}

		this.classDependenciesSequenceNumber = latestStorageInDb.getClassDependenciesSequenceNumber();
	}

	protected File createTempFolder() {
		StringBuilder folderName = new StringBuilder("tf-");
		folderName.append(DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT));
		folderName.append("-");
		folderName.append(UUID.randomUUID().toString());
		File tempFolder = FileTools.createNewTempDir(folderName.toString());
		return tempFolder;
	}

	public Codec<InputStream, String> getJavaClassCodec() {
		if (this.javaClassCodec == null) {
			this.javaClassCodec = new DefaultJavaClassCodec();
		}
		return javaClassCodec;
	}

	public String getStatistics() {
		return this.timing.toString();
	}

	@Configurable
	public void setJavaClassCodec(Codec<InputStream, String> javaClassCodec) {
		this.javaClassCodec = javaClassCodec;
	}

	@Configurable
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Configurable
	@Required
	public void setReadWriteLock(ReadWriteLock readWriteLock) {
		this.readWriteLock = readWriteLock;
	}

	@Configurable
	@Required
	public void setHibernateSessionFactory(SessionFactory sessionFactory) {
		this.hibernateSessionFactory = sessionFactory;
	}

	@Configurable
	public void setSelfModelName(String selfModelName) {
		this.selfModelName = selfModelName;
	}

	private static void setClob(PreparedStatement ps, int parameterIndex, String value) throws SQLException {

		if (value == null) {
			ps.setNull(parameterIndex, Types.CLOB);
		} else {
			try {
				ps.setClob(parameterIndex, new StringReader(value));
			} catch (SQLFeatureNotSupportedException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("PreparedStatement.setClob(int, Reader) is not supported, using setClob(int, Clob) instead"
							+ (e.getMessage() != null ? ": " + e.getMessage() : ""));
				}
				ps.setClob(parameterIndex, ClobProxy.generateProxy(value));
			}
		}

	}

	private static String getClob(ResultSet rs, int parameterIndex) throws SQLException, IOException {

		Clob encodedDataClob = rs.getClob(parameterIndex);

		if (encodedDataClob == null) {
			return null;
		}

		Reader clobReader = encodedDataClob.getCharacterStream();
		try {
			return IOTools.slurp(clobReader);
		} finally {
			IOTools.closeCloseable(clobReader, logger);
		}

	}

	protected String getAccessIdentifier() {
		return this.identifierPrefix + super.getAccessId();
	}

	@Configurable
	public void setLogStatisticsInterval(long logStatisticsInterval) {
		this.logStatisticsInterval = logStatisticsInterval;
	}
	@Configurable
	public void setIdentifierPrefix(String identifierPrefix) {
		this.identifierPrefix = identifierPrefix;
	}
	@Configurable
	public void setStorageAssemblyIsLinearPopulation(boolean storageAssemblyIsLinearPopulation) {
		this.storageAssemblyIsLinearPopulation = storageAssemblyIsLinearPopulation;
	}

	@Configurable
	public void setXmlMarshaller(CharacterMarshaller xmlMarshaller) {
		this.xmlMarshaller = xmlMarshaller;
	}
	public CharacterMarshaller getXmlMarshaller() {
		if (this.xmlMarshaller == null) {
			this.xmlMarshaller = StaxMarshaller.defaultInstance;
		}
		return this.xmlMarshaller;
	}

	@Configurable
	public void setDeserializationOptions(GmDeserializationOptions deserializationOptions) {
		this.deserializationOptions = deserializationOptions;
	}
	@Configurable
	public void setSerializationOptions(GmSerializationOptions serializationOptions) {
		this.serializationOptions = serializationOptions;
	}
}
