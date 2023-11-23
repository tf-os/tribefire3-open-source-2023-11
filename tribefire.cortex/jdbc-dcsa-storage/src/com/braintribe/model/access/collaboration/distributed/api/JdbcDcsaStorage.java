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
package com.braintribe.model.access.collaboration.distributed.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.sql.DataSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.jdbc.DatabaseTypes;
import com.braintribe.util.jdbc.JdbcTypeSupport;
import com.braintribe.utils.DigestGenerator;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.lcd.StopWatch;

public class JdbcDcsaStorage implements DcsaSharedStorage, LifecycleAware {

	private static final Logger logger = Logger.getLogger(JdbcDcsaStorage.class);

	private static final String tablename = "TF_DCSA";

	private DataSource dataSource;
	private LockManager lockManager;
	private List<String> createTableStatements;
	private String projectId;
	private boolean autoUpdateSchema = true;
	protected String blobType = "BLOB";
	protected String clobType = "CLOB";
	protected String timestampType = "TIMESTAMP";
	protected HasStringCodec marshaller;
	private String mimeType = "application/json";
	private String fileExtension = "json";

	private boolean initialized = false;

	protected Long lockTtlInMs = null;

	private ExecutorService executor;
	private boolean createdExecutor = false;
	private int parallelFetchThreads = 5;

	@Override
	public Lock getLock(String accessId) {
		return lockManager.forIdentifier(accessId).lockTtl(getLockTtlInMs(), TimeUnit.MILLISECONDS).exclusive();
	}

	@Override
	public String storeOperation(String accessId, CsaOperation csaOperation) {

		postConstruct();

		accessId = truncateId(accessId);

		Connection connection = null;
		PreparedStatement insertSt = null;
		Lock lock = lockManager.forIdentifier(accessId.concat("-write-lock")).lockTtl(getLockTtlInMs(), TimeUnit.MILLISECONDS).exclusive();
		lock.lock();
		try {

			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			String sql = null;
			if (csaOperation instanceof CsaResourceBasedOperation) {
				sql = "insert into " + tablename
						+ " (id, projectId, accessId, creationDate, encoded, content, mimeType, resourceName, resourceRelativePath) values (?,?,?,?,?,?,?,?,?)";
			} else {
				sql = "insert into " + tablename + " (id, projectId, accessId, creationDate, encoded) values (?,?,?,?,?)";
			}
			insertSt = connection.prepareStatement(sql);

			String id = RandomTools.getRandom32CharactersHexString(true);
			csaOperation.setId(id);
			String encoded = marshaller.getStringCodec().encode(csaOperation);

			insertSt.setString(1, id);
			insertSt.setString(2, projectId);
			insertSt.setString(3, accessId);
			insertSt.setTimestamp(4, Timestamp.from(NanoClock.INSTANCE.instant()));

			if (clobType.equals("CLOB")) {
				StringReader reader = new StringReader(encoded);
				insertSt.setClob(5, reader, encoded.length());
			} else {
				insertSt.setString(5, encoded);
			}

			if (csaOperation instanceof CsaResourceBasedOperation) {
				CsaResourceBasedOperation crbo = (CsaResourceBasedOperation) csaOperation;
				setBlobFromResource(crbo, insertSt, 6);
				Resource payload = crbo.getPayload();
				if (payload != null) {
					insertSt.setString(7, payload.getMimeType());
					insertSt.setString(8, payload.getName());
					if (crbo instanceof CsaStoreResource) {
						CsaStoreResource csr = (CsaStoreResource) crbo;
						insertSt.setString(9, csr.getResourceRelativePath());
					} else {
						insertSt.setString(9, null);
					}
				} else {
					insertSt.setString(7, null);
					insertSt.setString(8, null);
					insertSt.setString(9, null);
				}
			}

			int updated = insertSt.executeUpdate();
			if (updated != 1) {
				throw new Exception("The execution of '" + sql + "' did not change anything in the DB (result is " + updated + ", expected 1)");
			}

			connection.commit();

			return id;

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e1) {
					logger.error("Could not rollback changes", e);
				}
			}
			throw Exceptions.unchecked(e, "Could not store " + csaOperation + "in access " + accessId);
		} finally {
			lock.unlock();
			IOTools.closeCloseable(insertSt, logger);
			IOTools.closeCloseable(connection, logger);
		}
	}

	protected void setBlobFromResource(CsaResourceBasedOperation crbo, PreparedStatement ps, int index) throws Exception {
		Resource resource = crbo.getPayload();
		if (resource == null) {
			ps.setBlob(index, new ByteArrayInputStream(new byte[0]));
			return;
		}
		byte[] resourceContent = null;
		try (InputStream inputStream = resource.openStream()) {
			resourceContent = IOTools.slurpBytes(inputStream);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not load resource of " + crbo);
		}

		ps.setBlob(index, new ByteArrayInputStream(resourceContent));
	}

	@Override
	public Map<String, Resource> readResource(String accessId, Collection<String> resourceRelativePaths) {

		if (resourceRelativePaths == null || resourceRelativePaths.isEmpty()) {
			return Collections.EMPTY_MAP;
		}

		StopWatch stopWatch = new StopWatch();

		postConstruct();

		accessId = truncateId(accessId);

		stopWatch.intermediate("postConstruct");

		List<String> resourceRelativePathList = new ArrayList<>(resourceRelativePaths);

		int resourceRelativePathListSize = resourceRelativePathList.size();

		LinkedHashMap<String, Resource> resultMap = new LinkedHashMap<>(resourceRelativePathListSize);
		resourceRelativePathList.forEach(id -> resultMap.put(id, null));

		stopWatch.intermediate("splitPaths");

		logger.debug(() -> "Retrieving " + resourceRelativePathListSize + " Resources from the DB");

		int splitSize = 15;
		List<ReadResourceContext> splitList = splitResourceIds(resourceRelativePathList, splitSize);

		int subLists = splitList.size();

		ExecutorService pool = this.getExecutor();
		List<Future<Map<String, Resource>>> futures = new ArrayList<>(subLists);
		for (int i = 0; i < subLists; ++i) {
			final int sublistId = i;
			futures.add(pool.submit(() -> fetchResources(splitList.get(sublistId))));
		}

		stopWatch.intermediate("submitSublists");

		for (Future<Map<String, Resource>> f : futures) {
			try {
				Map<String, Resource> subMap = f.get();
				resultMap.putAll(subMap);
			} catch (InterruptedException ie) {
				String message = "Got interrupted while waiting for read Resources operation to finish.";
				logger.debug(() -> message);
				throw new RuntimeException(message, ie);
			} catch (ExecutionException ee) {
				String message = "Error while reading Resources from the DB for project " + projectId + " and access " + accessId;
				logger.debug(() -> message);
				throw new RuntimeException(message, ee);
			}
		}

		stopWatch.intermediate("collectSublists");

		String stats = getStatistics(splitList);

		logger.debug(() -> "Reading " + resultMap.size() + " Resources took: " + stopWatch.toString() + " (" + stats + ")");

		return resultMap;
	}

	private static String getStatistics(List<ReadResourceContext> list) {
		double waitTotal = 0d;
		double execTotal = 0d;
		for (ReadResourceContext c : list) {
			waitTotal += (c.start - c.creation);
			execTotal += (c.end - c.start);
		}
		double avgWaitNs = waitTotal / list.size();
		double avgExecNs = execTotal / list.size();
		long avgWaitMs = (long) (avgWaitNs / Numbers.NANOSECONDS_PER_MILLISECOND);
		long avgExecMs = (long) (avgExecNs / Numbers.NANOSECONDS_PER_MILLISECOND);
		String stats = "Avg. wait: " + StringTools.prettyPrintDuration(avgWaitMs, true, ChronoUnit.MILLIS) + ", Avg. exec: "
				+ StringTools.prettyPrintDuration(avgExecMs, true, ChronoUnit.MILLIS);
		return stats;
	}

	private static List<ReadResourceContext> splitResourceIds(List<String> resourceRelativePathList, int splitSize) {
		List<List<String>> splitList = CollectionTools.splitList(resourceRelativePathList, splitSize);
		List<ReadResourceContext> result = new ArrayList<>(splitList.size());
		splitList.forEach(l -> {
			result.add(new ReadResourceContext(l));
		});
		return result;
	}

	private static class ReadResourceContext {
		long creation = System.nanoTime();
		List<String> idList;
		long start;
		long end;

		public ReadResourceContext(List<String> idList) {
			this.idList = idList;
		}
	}

	private Map<String, Resource> fetchResources(ReadResourceContext context) {
		context.start = System.nanoTime();

		Connection connection = null;
		PreparedStatement selectSt = null;
		ResultSet rs = null;

		List<String> resourceRelativePaths = context.idList;
		Map<String, Resource> resultMap = new HashMap<>();
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			int size = resourceRelativePaths.size();

			StringBuilder sb = new StringBuilder("select id, content, mimeType, resourceName, resourceRelativePath from ");
			sb.append(tablename);
			sb.append(" where resourceRelativePath in (");
			for (int i = 0; i < size; ++i) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");

			selectSt = connection.prepareStatement(sb.toString());

			for (int i = 0; i < size; ++i) {
				selectSt.setString(i + 1, resourceRelativePaths.get(i));
			}

			rs = selectSt.executeQuery();

			while (rs.next()) {

				String id = rs.getString(1);
				String resourceRelativePath = rs.getString(5);

				final byte[] content;
				Blob blob = rs.getBlob(2);
				if (blob != null) {
					try (InputStream in = blob.getBinaryStream()) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOTools.pump(in, baos);
						content = baos.toByteArray();
					}
				} else {
					content = null;
				}
				if (content != null) {
					String dbMimeType = rs.getString(3);
					String dbResourceName = rs.getString(4);

					Resource resource = Resource.createTransient(() -> {
						return new ByteArrayInputStream(content);
					});

					if (StringTools.isBlank(dbMimeType)) {
						dbMimeType = mimeType;
					}
					if (StringTools.isBlank(dbResourceName)) {
						dbResourceName = id + "." + fileExtension;
					}

					resource.setMimeType(dbMimeType);
					resource.setName(dbResourceName);
					resource.setFileSize((long) content.length);

					resultMap.put(resourceRelativePath, resource);
				}

				connection.commit();

			}

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not rollback changes.", e2);
				}
			}
			throw Exceptions.unchecked(e, "Error while trying to load the Resource paths " + resourceRelativePaths);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(selectSt, logger);
			IOTools.closeCloseable(connection, logger);
		}

		context.end = System.nanoTime();

		return resultMap;
	}

	@Override
	public JdbcDcsaIterable readOperations(String accessId, String lastReadMarker) {

		StopWatch stopWatch = new StopWatch();

		postConstruct();

		accessId = truncateId(accessId);

		stopWatch.intermediate("postConstruct");

		List<String> idList = new ArrayList<>();

		fetchIds(accessId, lastReadMarker, stopWatch, idList);
		int idListSize = idList.size();

		stopWatch.intermediate("getIdResults");

		LinkedHashMap<String, CsaOperation> resultMap = new LinkedHashMap<>(idListSize);

		if (idListSize > 0) {

			idList.forEach(id -> resultMap.put(id, null));

			if (logger.isDebugEnabled()) {
				logger.debug("Retrieved " + idListSize + " IDs from the DB for Access " + accessId + ". Loading entries with " + parallelFetchThreads
						+ " threads now.");
			}

			int splitSize = 100;

			List<List<String>> splitList = CollectionTools.splitList(idList, splitSize);
			int subLists = splitList.size();

			ExecutorService pool = this.getExecutor();
			List<Future<FetchContext>> futures = new ArrayList<>(subLists);
			for (int i = 0; i < subLists; ++i) {
				final int sublistId = i;
				FetchContext context = new FetchContext(accessId, i, subLists, splitList.get(sublistId));
				futures.add(pool.submit(() -> fetchContent(context)));
			}

			stopWatch.intermediate("submitSublists");

			for (Future<FetchContext> f : futures) {
				try {
					FetchContext context = f.get();
					logger.debug(() -> "Read " + context);
					Map<String, CsaOperation> subMap = context.result;
					resultMap.putAll(subMap);
				} catch (InterruptedException ie) {
					String message = "Got interrupted while waiting for read operation to finish.";
					logger.debug(() -> message);
					throw new RuntimeException(message, ie);
				} catch (ExecutionException ee) {
					String message = "Error while reading data from the DB for project " + projectId + " and access " + accessId;
					logger.debug(() -> message);
					throw new RuntimeException(message, ee);
				}
			}

			stopWatch.intermediate("collectSublists");
		}

		String newLastReadMarker = (idListSize > 0) ? idList.get(idListSize - 1) : null;

		List<CsaOperation> resultList = new ArrayList<>(resultMap.values());

		int size = resultList.size();
		if (size > 0 && logger.isDebugEnabled()) {
			logger.debug("Reading " + size + " from Access " + accessId + " entries took: " + stopWatch.toString());
		}
		return new JdbcDcsaIterable(newLastReadMarker, resultList);

	}

	private class FetchContext {
		int packetNumber;
		List<String> ids;
		String accessId;
		long duration = -1L;
		long totalContentSize = 0L;
		long totalResourceSize = 0L;
		Map<String, CsaOperation> result = new HashMap<>();
		private final int noOfPackets;

		public FetchContext(String accessId, int packetNumber, int noOfPackets, List<String> ids) {
			this.accessId = accessId;
			this.packetNumber = packetNumber;
			this.noOfPackets = noOfPackets;
			this.ids = ids;
		}
		@Override
		public String toString() {
			return "Access: " + accessId + ", Packet " + packetNumber + "/" + noOfPackets + ", Ids: " + ids.size() + ", Total Content Size: "
					+ totalContentSize + ", Total Resource Size: " + totalResourceSize + ", Duration: " + duration + " ms";
		}

	}

	private FetchContext fetchContent(FetchContext context) {
		Connection connection = null;
		PreparedStatement selectSt = null;
		ResultSet rs = null;

		long start = System.currentTimeMillis();

		boolean includeResourceContent = !context.accessId.equals("cortex");

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			int size = context.ids.size();

			StringBuilder sb = new StringBuilder();
			sb.append("select id, encoded, resourceRelativePath, content, mimeType, resourceName from ");
			sb.append(tablename);
			sb.append(" where id in (");
			for (int i = 0; i < size; ++i) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");

			selectSt = connection.prepareStatement(sb.toString());

			for (int i = 0; i < size; ++i) {
				selectSt.setString(i + 1, context.ids.get(i));
			}

			rs = selectSt.executeQuery();

			while (rs.next()) {

				String encoded = null;
				String id = rs.getString(1);
				if (clobType.equals("CLOB")) {
					Clob clob = rs.getClob(2);
					Reader reader = clob.getCharacterStream();
					try {
						StringWriter sw = new StringWriter();
						IOTools.pump(reader, sw);
						encoded = sw.toString();
					} finally {
						reader.close();
					}
				} else {
					encoded = rs.getString(2);
				}

				if (encoded != null) {
					context.totalContentSize += encoded.length();

					CsaOperation ge = (CsaOperation) marshaller.getStringCodec().decode(encoded);
					ge.setId(id);

					if (ge instanceof CsaResourceBasedOperation) {

						String dbResourceRelativePath = rs.getString(3);

						if (includeResourceContent || !(ge instanceof CsaStoreResource)) {

							final byte[] content;
							Blob blob = rs.getBlob(4);
							if (blob != null) {
								try (InputStream in = blob.getBinaryStream()) {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									IOTools.pump(in, baos);
									content = baos.toByteArray();
								}
							} else {
								content = null;
							}
							if (content != null) {
								context.totalResourceSize += content.length;

								String dbMimeType = rs.getString(5);
								String dbResourceName = rs.getString(6);

								Resource resource = Resource.createTransient(() -> {
									return new ByteArrayInputStream(content);
								});

								if (StringTools.isBlank(dbMimeType)) {
									dbMimeType = mimeType;
								}
								if (StringTools.isBlank(dbResourceName)) {
									dbResourceName = id + "." + fileExtension;
								}

								resource.setMimeType(dbMimeType);
								resource.setName(dbResourceName);
								resource.setFileSize((long) content.length);

								((CsaResourceBasedOperation) ge).setPayload(resource);
							}

						} else {
							// Cortex Access; don't eagerly load Resources
							CsaResourceBasedOperation cro = (CsaResourceBasedOperation) ge;

							Property payloadProperty = cro.entityType().getProperty(CsaResourceBasedOperation.payload);
							payloadProperty.setAbsenceInformation(cro, GMF.absenceInformation());
						}

						if (dbResourceRelativePath == null && ge instanceof CsaStoreResource) {
							CsaStoreResource csr = (CsaStoreResource) ge;
							String resourceRelativePath = csr.getResourceRelativePath();
							updateResourceRelativePath(id, resourceRelativePath, connection);
						}
					}

					context.result.put(id, ge);
				}

				connection.commit();

			}

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not rollback changes.", e2);
				}
			}
			throw Exceptions.unchecked(e, "Error while trying to load the IDs " + context.ids + ", Context: " + context);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(selectSt, logger);
			IOTools.closeCloseable(connection, logger);
		}

		context.duration = (System.currentTimeMillis() - start);

		return context;
	}

	/* This method adds the resourceRelativePath to where it is missing. This is because this property was introduced
	 * later. */
	private void updateResourceRelativePath(String id, String resourceRelativePath, Connection connection) throws Exception {
		if (resourceRelativePath == null) {
			return;
		}

		logger.debug(() -> "Adding missing resourceRelativePath " + resourceRelativePath + " to row with id " + id);

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("update " + tablename + " set resourceRelativePath = ? where id = ?");
			statement.setString(1, resourceRelativePath);
			statement.setString(2, id);
			statement.executeUpdate();
		} catch (Exception e) {
			throw Exceptions.contextualize(e, "Could not update resourceRelativePath to " + resourceRelativePath + " for id " + id);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private void fetchIds(String accessId, String lastReadMarker, StopWatch stopWatch, List<String> idList) {

		accessId = truncateId(accessId);

		Connection connection = null;
		PreparedStatement selectSt = null;
		ResultSet rs = null;
		try {

			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			if (lastReadMarker != null) {
				if (projectId == null) {
					selectSt = connection
							.prepareStatement("select id from " + tablename + " where id > ? and accessId = ? and projectId is null order by id asc");
					selectSt.setString(1, lastReadMarker);
					selectSt.setString(2, accessId);
				} else {
					selectSt = connection
							.prepareStatement("select id from " + tablename + " where id > ? and accessId = ? and projectId = ? order by id asc");
					selectSt.setString(1, lastReadMarker);
					selectSt.setString(2, accessId);
					selectSt.setString(3, projectId);
				}
			} else {
				if (projectId == null) {
					selectSt = connection
							.prepareStatement("select id from " + tablename + " where accessId = ? and projectId is null order by id asc");
					selectSt.setString(1, accessId);
				} else {
					selectSt = connection.prepareStatement("select id from " + tablename + " where accessId = ? and projectId = ? order by id asc");
					selectSt.setString(1, accessId);
					selectSt.setString(2, projectId);
				}
			}
			rs = selectSt.executeQuery();

			stopWatch.intermediate("idQueryExecution");

			while (rs.next()) {
				String id = rs.getString(1);
				idList.add(id);
			}

		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception e2) {
					logger.error("Could not rollback changes.", e2);
				}
			}
			throw Exceptions.unchecked(e, "Error while trying to load the population for project " + projectId + " and access " + accessId);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(selectSt, logger);
			IOTools.closeCloseable(connection, logger);
		}
	}

	public static class JdbcDcsaIterable implements DcsaIterable {

		public final String lastReadMarker;
		public final List<CsaOperation> operations;

		public JdbcDcsaIterable(String lastReadMarker, List<CsaOperation> operations) {
			this.lastReadMarker = lastReadMarker;
			this.operations = operations;
		}

		@Override
		public Iterator<CsaOperation> iterator() {
			return operations.iterator();
		}

		@Override
		public String getLastReadMarker() {
			return lastReadMarker;
		}

	}

	@Configurable
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	@Configurable
	@Required
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	@Configurable
	@Required
	public void setMarshaller(HasStringCodec marshaller) {
		this.marshaller = marshaller;
	}
	@Configurable
	public void setMimeType(String mimeType) {
		if (!StringTools.isBlank(mimeType)) {
			this.mimeType = mimeType;
		}
	}
	@Configurable
	public void setFileExtension(String fileExtension) {
		if (!StringTools.isBlank(fileExtension)) {
			this.fileExtension = fileExtension;
		}
	}
	@Configurable
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	@Configurable
	public void setAutoUpdateSchema(boolean autoUpdateSchema) {
		this.autoUpdateSchema = autoUpdateSchema;
	}

	@Override
	public void postConstruct() {
		if (initialized) {
			return;
		}
		initialized = true;

		DatabaseTypes dbTypes = JdbcTypeSupport.getDatabaseTypes(dataSource);
		clobType = dbTypes.getClobType();
		blobType = dbTypes.getBlobType();
		timestampType = dbTypes.getTimestampType();

		logger.debug(() -> "Identified CLOB type: " + clobType);
		logger.debug(() -> "Identified BLOB type: " + blobType);
		logger.debug(() -> "Identified TIMESTAMP type: " + timestampType);

		if (!autoUpdateSchema) {
			return;
		}
		if (createTableStatements == null || createTableStatements.isEmpty()) {
			createTableStatements = new ArrayList<>();

			createTableStatements.add("create table " + tablename
					+ " (id varchar(255) primary key not null, projectId varchar(255), accessId varchar(255), creationDate " + timestampType
					+ ", encoded " + clobType + ", content " + blobType
					+ ", mimeType varchar(255), resourceName varchar(255), resourceRelativePath varchar(255))");
		}

		try {
			updateSchema();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not connect to database and/or create the necessary table.");
		}
	}

	protected void updateSchema() throws Exception {
		Connection connection = dataSource.getConnection();

		try {
			String existingTableName = tableExists(connection);
			if (existingTableName == null) {
				logger.debug("Table " + tablename + " does not exist.");
				Statement statement = connection.createStatement();
				try {
					for (String st : createTableStatements) {
						logger.debug("Creating table with statement: " + st);
						statement.executeUpdate(st);
						logger.debug("Successfully created table " + tablename + ".");
					}
				} catch (Exception e) {
					if (tableExists(connection) != null)
						return;
					else
						throw e;
				} finally {
					statement.close();
				}
			} else {
				Map<String, Boolean> existingColumns = columnsExist(connection, existingTableName, "mimeType", "resourceName",
						"resourceRelativePath");
				if (!existingColumns.get("mimeType") && !existingColumns.get("resourceName")) {
					updateResourceColumns(connection, existingTableName);
				}
				if (!existingColumns.get("resourceRelativePath")) {
					updateResourceRelativePathColumns(connection, existingTableName);
				}

				Map<String, Boolean> existingColumnsRecheck = columnsExist(connection, existingTableName, "mimeType", "resourceName",
						"resourceRelativePath");
				List<String> missing = new ArrayList<>();
				for (Map.Entry<String, Boolean> entry : existingColumnsRecheck.entrySet()) {
					if (!entry.getValue()) {
						missing.add(entry.getKey());
					}
				}
				if (!missing.isEmpty()) {
					throw new Exception(
							"Could not update existing table " + existingTableName + ". The following columns could not be added: " + missing);
				}

			}
		} finally {
			connection.close();
		}
	}
	private void updateResourceColumns(Connection connection, String existingTableName) throws SQLException {
		List<String> updates = CollectionTools2.asList( //
				"alter table " + existingTableName + " add mimeType varchar(255)", //
				"alter table " + existingTableName + " add resourceName varchar(255)");

		Statement statement = connection.createStatement();
		try {
			for (String st : updates) {
				logger.debug("Executing update statement: " + st);
				statement.executeUpdate(st);
				logger.debug("Successfully updated " + tablename + ".");
			}

		} finally {
			statement.close();
		}
	}

	private void updateResourceRelativePathColumns(Connection connection, String existingTableName) throws SQLException {
		List<String> updates = CollectionTools2.asList("alter table " + existingTableName + " add resourceRelativePath varchar(255)");

		Statement statement = connection.createStatement();
		try {
			for (String st : updates) {
				logger.debug("Executing update statement: " + st);
				statement.executeUpdate(st);
				logger.debug("Successfully updated " + tablename + ".");
			}

		} finally {
			statement.close();
		}

		List<String> indices = CollectionTools2.asList("create index TFDCSARRP on " + existingTableName + " (resourceRelativePath)");

		Statement indexStatement = connection.createStatement();
		try {
			for (String st : indices) {
				logger.debug("Executing index statement: " + st);
				indexStatement.executeUpdate(st);
				logger.debug("Successfully added index:" + st);
			}

		} finally {
			statement.close();
		}
	}

	private Map<String, Boolean> columnsExist(Connection connection, String existingTableName, String... requestedColumnNames) throws SQLException {
		ResultSet rs = connection.getMetaData().getColumns(null, null, existingTableName, null);

		Map<String, Boolean> result = new HashMap<>();
		for (String requestedColumnName : requestedColumnNames) {
			result.put(requestedColumnName, Boolean.FALSE);
		}

		try {
			while (rs.next()) {

				String columnName = rs.getString("COLUMN_NAME");

				for (String requestedColumnName : requestedColumnNames) {
					if (columnName.equalsIgnoreCase(requestedColumnName)) {
						result.put(requestedColumnName, Boolean.TRUE);
					}

				}
			}

		} finally {
			rs.close();
		}

		return result;
	}

	/* IMPLEMENTATION NOTE: Originally, the third parameter for the MetaData.getTables(...) invocation (i.e.
	 * tableNamePattern) was not null, but the actual name of the table (i.e. "TF_DSTLCK"). This, however, caused problems
	 * for the PostreSQL DB, because calling "create table TF_DSTLCK" there creates a table called "tf_dstlck" (i.e. all
	 * chars uncapitalized). To avoid this problem, and possible future problems with different conventions, we simply
	 * retrieve all the tables and then perform a case-insensitive check for each table name. */
	protected String tableExists(Connection connection) throws SQLException {

		Instant start = NanoClock.INSTANCE.instant();

		ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[] { "TABLE" });

		try {
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");

				if (tableName.equalsIgnoreCase(tablename)) {
					return tableName;
				}
			}

			return null;

		} finally {
			rs.close();

			Duration duration = Duration.between(start, NanoClock.INSTANCE.instant());
			if (duration.toMillis() > (Numbers.MILLISECONDS_PER_SECOND * 5)) {
				logger.info(() -> "The check for the existence of " + tablename + " took "
						+ StringTools.prettyPrintDuration(duration, true, ChronoUnit.MILLIS));
			} else {
				logger.debug(() -> "The check for the existence of " + tablename + " took "
						+ StringTools.prettyPrintDuration(duration, true, ChronoUnit.MILLIS));
			}
		}
	}

	protected long getLockTtlInMs() {
		if (lockTtlInMs != null) {
			return lockTtlInMs;
		}
		String ttlString = TribefireRuntime.getProperty("TRIBEFIRE_JDBC_DCSA_LOCK_TTL");
		if (!StringTools.isBlank(ttlString)) {
			try {
				lockTtlInMs = Long.parseLong(ttlString);
			} catch (NumberFormatException nfe) {
				logger.warn(() -> "Could not parse value of TRIBEFIRE_JDBC_DCSA_LOCK_TTL: " + ttlString, nfe);
			}
		}
		if (lockTtlInMs == null) {
			lockTtlInMs = (long) Numbers.MILLISECONDS_PER_MINUTE;
		}
		return lockTtlInMs;
	}

	@Override
	public void preDestroy() {
		if (executor != null && createdExecutor) {
			executor.shutdown();
		}
	}

	public ExecutorService getExecutor() {
		if (executor == null) {
			createdExecutor = true;

			executor = VirtualThreadExecutorBuilder.newPool().concurrency(parallelFetchThreads).threadNamePrefix("tribefire.jdbc.dcsa-")
					.description("JDBC DCSA").build();
		}
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	protected String truncateId(String id) {
		if (id == null)
			throw new IllegalArgumentException("The identifier of the lock must not be null.");

		if (id.length() > 240) {
			String md5;
			try {
				md5 = DigestGenerator.stringDigestAsString(id, "MD5");
			} catch (Exception e) {
				logger.error("Could not generate an MD5 sum of ID " + id, e);
				md5 = "";
			}
			String cutId = id.substring(0, 200);
			String newId = cutId.concat("#").concat(md5);
			return newId;
		}
		return id;
	}

	@Configurable
	public void setParallelFetchThreads(int parallelFetchThreads) {
		this.parallelFetchThreads = parallelFetchThreads;
	}

}
