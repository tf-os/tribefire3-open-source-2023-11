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
package tribefire.extension.jdbc.gmdb.dcsa;

import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.NO_LOB;
import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.ONLY_LOB;
import static com.braintribe.model.access.collaboration.distributed.api.model.CsaOperationType.APPEND_DATA_MANIPULATION;
import static com.braintribe.model.access.collaboration.distributed.api.model.CsaOperationType.APPEND_MODEL_MANIPULATION;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.last;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.sort;
import static com.braintribe.utils.lcd.NullSafe.nonNull;
import static java.util.Collections.emptyMap;

import java.sql.Connection;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.execution.CustomThreadFactory;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmDb;
import com.braintribe.gm.jdbc.api.GmIndex;
import com.braintribe.gm.jdbc.api.GmLobLoadingMode;
import com.braintribe.gm.jdbc.api.GmRow;
import com.braintribe.gm.jdbc.api.GmSelectBuilder;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.gm.jdbc.impl.column.ResourceColumn;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.DcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperationType;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.jdbc.dialect.DbVariant;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.lcd.NullSafe;

/**
 * When storing CsaOperation:
 * <ul>
 * <li>id (if present) is used for id column and is purged from CsaOperation
 * <li>payload is purged from GMML operations
 * </ul>
 * 
 * When loading CsaOperation:
 * <ul>
 * <li>id is set from the id column
 * <li>payload is set from the resource column for GMML operations
 * <li>payload for non-GMML resource-based operations is absent. Binary data can be retrieved with {@link #readResource(String, Collection)} method
 * </ul>
 */
public class GmDbDcsaSharedStorage implements DcsaSharedStorage, LifecycleAware {

	public static final String DEFAULT_OPS_TABLE_NAME = "TF_DCSA_OPS"; // temporarily public
	public static final String DEFAULT_RES_TABLE_NAME = "TF_DCSA_RES"; // temporarily public
	private static final long DEFAULT_LOCK_TTL = Numbers.MILLISECONDS_PER_MINUTE;
	private static final boolean DEFAULT_AUTO_UPDATE_SCHEMA = true;
	private static final int DEFAULT_EXECUTOR_THREADS = 4;
	private static final int DEFAULT_BATCH_SIZE = 10;

	private static final Logger log = Logger.getLogger(GmDbDcsaSharedStorage.class);

	private LockManager lockManager;
	private GmDb gmDb;
	private String projectId;

	private final String opsTableName = DEFAULT_OPS_TABLE_NAME; // temporarily not configurable
	private final String resTableName = DEFAULT_RES_TABLE_NAME; // temporarily not configurable
	private long lockTtlInMs = DEFAULT_LOCK_TTL;
	private boolean autoUpdateSchema = DEFAULT_AUTO_UPDATE_SCHEMA;

	private ThreadPoolExecutor executor;
	private boolean createdExecutor = false;
	private int executorThreads = DEFAULT_EXECUTOR_THREADS;
	private int batchSize = DEFAULT_BATCH_SIZE;

	private final LazyInitialized<TableDriver> tableDriver = new LazyInitialized<>(TableDriver::new);

	// @formatter:off
	@Required public void setLockManager(LockManager lockManager) { this.lockManager = lockManager; }
	/** This GmDb must have a {@link GmDb#defaultCodec default codec} configured. */
	@Required public void setGmDb(GmDb gmDb) { this.gmDb = gmDb; }
	@Required public void setProjectId(	String projectId) { this.projectId = projectId; }	

	// Optional:
	
	/** Default value is {@value #DEFAULT_LOCK_TTL} */
	@Configurable public void setLockTtlInMs(long lockTtlInMs) { this.lockTtlInMs = lockTtlInMs; }
	/** Default name is {@value #DEFAULT_TABLE_NAME} */
	//@Configurable public void setTableName(String tableName) { this.tableName = tableName;}
	/** Default is {@value #DEFAULT_AUTO_UPDATE_SCHEMA} */
	@Configurable public void setAutoUpdateSchema(boolean autoUpdateSchema) { this.autoUpdateSchema = autoUpdateSchema; }	

	/** If no executor is configured, a new is created internally with the number of threads according to {@link #setExecutorThreads(int)} */
	@Configurable public void setExecutor(ThreadPoolExecutor executor) { this.executor = executor; }
	/** Default value is {@value #DEFAULT_EXECUTOR_THREADS} */
	@Configurable public void setExecutorThreads(int executorThreads) { this.executorThreads = executorThreads; }
	/**
	 * The size of a batch processed by a single executor thread at a time. Default value is {@value #DEFAULT_BATCH_SIZE}.
	 * <p>
	 * This is related to loading LOBs, see {@link GmSelectBuilder#rowsInBatchesOf(int)}.
	 */
	@Configurable public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
	// @formatter:on

	@Override
	public void postConstruct() {
		validateRequiredDependenciesConfigured();

		tableDriver();
	}

	private void validateRequiredDependenciesConfigured() {
		validateNotNull(gmDb, "gmDb");
		validateNotNull(gmDb.defaultCodec, "gmDb.defaultCodec");
		validateNotNull(lockManager, "lockManager");
		validateNotEmpty(projectId, "projectId");
		validateNotEmpty(opsTableName, "tableName");
		validateNotEmpty(resTableName, "tableName");
	}

	private void validateNotNull(Object o, String name) {
		if (o == null)
			throw new IllegalStateException("Required dependency not configured: " + name);
	}

	private void validateNotEmpty(String value, String name) {
		if (StringTools.isEmpty(value))
			throw new IllegalStateException("Required property not configured: " + name);
	}

	@Override
	public void preDestroy() {
		if (executor != null && createdExecutor)
			executor.shutdown();
	}

	// ###############################################################
	// ## . . . . . . . . DcsaSharedStorage methods . . . . . . . . ##
	// ###############################################################

	/** {@inheritDoc} */
	@Override
	public Lock getLock(String accessId) {
		return lockManager.forIdentifier(accessId).lockTtl(lockTtlInMs, TimeUnit.MILLISECONDS).exclusive();
	}

	/** {@inheritDoc} */
	@Override
	public String storeOperation(String accessId, CsaOperation csaOperation) {
		nonNull(accessId, "accessId");
		nonNull(csaOperation, "csaOperation");
		return tableDriver().insert(accessId, csaOperation);
	}

	public void storeOperations(String accessId, List<CsaOperation> csaOperations) {
		nonNull(accessId, "accessId");
		nonNull(csaOperations, "csaOperations");
		tableDriver().insertMany(accessId, csaOperations);
	}

	/** {@inheritDoc} */
	@Override
	public DcsaIterable readOperations(String accessId, String lastReadMarker) {
		nonNull(accessId, "accessId");
		return tableDriver().read(accessId, lastReadMarker);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Resource> readResource(String accessId, Collection<String> storedResourcesPaths) {
		nonNull(accessId, "accessId");
		return tableDriver().readResources(accessId, storedResourcesPaths);
	}

	public void ensureTable() {
		tableDriver().ensureTables();
	}

	// ###############################################################
	// ## . . . . . . . . . . . . . CRUD . . . . . . . . . . . . . .##
	// ###############################################################

	private class TableDriver {

		// CSA OPS
		private final GmColumn<String> colOpIdStr = gmDb.shortString255("id").primaryKey().notNull().done();
		private final GmColumn<String> colOpProjectId = gmDb.shortString255("projectId").notNull().done();
		private final GmColumn<String> colOpAccessId = gmDb.shortString255("accessId").notNull().done();
		private final GmColumn<Date> colOpCreated = gmDb.date("created").notNull().done();
		private final GmColumn<CsaOperation> colOpEntity = gmDb.<CsaOperation> entityAsString("entity", CsaOperation.T).notNull().done();
		private final GmColumn<Resource> colOpPayload = gmDb.resource("payload", IOTools.SIZE_64K).done();
		private final GmColumn<Boolean> colOpIsGc = gmDb.booleanCol("cleanup").notNull().done();

		// CSA RESOURCES
		private final GmColumn<String> colResIdStr = gmDb.shortString255("id").primaryKey().notNull().done();
		private final GmColumn<String> colResProjectId = gmDb.shortString255("projectId").notNull().done();
		private final GmColumn<String> colResAccessId = gmDb.shortString255("accessId").notNull().done();
		private final GmColumn<Date> colResCreated = gmDb.date("created").notNull().done();
		private final GmColumn<String> colResPath = gmDb.shortString255("path").done();
		private final GmColumn<String> colResName = gmDb.shortString255("name").done();
		private final GmColumn<String> colResMimeType = gmDb.shortString255("mimeType").done();
		private final GmColumn<Resource> colResEntity = gmDb.<Resource> entityAsString("entity", Resource.T).notNull().done();
		private final GmColumn<Resource> colResPayload = gmDb.resource("payload", resPayloadSize()).done();

		private int resPayloadSize() {
			return gmDb.dialect.knownDbVariant() == DbVariant.mysql ? 15_000 : IOTools.SIZE_64K;
		}

		private final GmIndex idxResourcePath = gmDb.index("idx_res_path", colResPath);

		public final GmTable opsTable = //
				gmDb.newTable(opsTableName) //
						.withColumns( //
								colOpIdStr, //
								colOpProjectId, //
								colOpAccessId, //
								colOpCreated, //
								colOpEntity, //
								colOpPayload, //
								colOpIsGc //
						).done();

		public final GmTable resTable = //
				gmDb.newTable(resTableName) //
						.withColumns( //
								colResIdStr, //
								colResProjectId, //
								colResAccessId, //
								colResCreated, //
								colResPath, colResName, colResMimeType, //
								colResEntity, // not used yet (if ever)
								colResPayload //
						).withIndices(idxResourcePath) //
						.done();

		public TableDriver() {
			if (autoUpdateSchema)
				ensureTables();
		}

		public void ensureTables() {
			opsTable.ensure();
			resTable.ensure();
		}

		// ###############################################
		// ## . . . . . . . . Writing . . . . . . . . . ##
		// ###############################################

		public String insert(String accessId, CsaOperation csaOp) {
			return insert(accessId, csaOp, null);
		}

		public void insertMany(String accessId, List<CsaOperation> csaOperations) {
			gmDb.withManualCommitConnection(() -> "Storing " + csaOperations.size() + " CsaOperations in GmDbStorage", c -> {
				for (CsaOperation csaOp : csaOperations)
					insert(accessId, csaOp, c);
			});
		}

		private String insert(String accessId, CsaOperation csaOp, Connection c) {
			String id = NullSafe.provide(csaOp.getId(), () -> RandomTools.getRandom32CharactersHexString(true));

			csaOp = GmReflectionTools.makeShallowCopy(csaOp);
			csaOp.setId(null);

			Resource resource = null;

			if (csaOp instanceof CsaResourceBasedOperation) {
				CsaResourceBasedOperation op = (CsaResourceBasedOperation) csaOp;
				resource = op.getPayload();
				op.setPayload(null);

				if (isGmmlOpreation(op)) {
					resource = ensurePlainTextMimeType(resource);

				} else if (csaOp instanceof CsaStoreResource) {
					String resourceId = storeResource(id, accessId, resource, ((CsaStoreResource) csaOp).getResourceRelativePath());
					byte[] bytes = resourceId.getBytes();
					resource = Resource.createTransient(InputStreamProvider.fromBytes(bytes));
					resource.setMimeType(ResourceColumn.TEXT_PLAIN_MIME_TYPE);
					resource.setFileSize((long) bytes.length);
				}
			}

			opsTable.insert(c, //
					colOpIdStr, id, //
					colOpProjectId, projectId, //
					colOpAccessId, accessId, //
					colOpCreated, now(), //
					colOpEntity, csaOp, //
					colOpPayload, resource, //
					colOpIsGc, false //
			);

			return id;
		}

		private String storeResource(String id, String accessId, Resource resource, String resourcePath) {
			logProblemIfPathAlreadyUsed(accessId, resource, resourcePath);

			String resId = id + "r";
			String resourceName = resource.getName();
			String resourceMimeType = resource.getMimeType();

			resTable.insert( //
					colResIdStr, resId, //
					colResProjectId, projectId, //
					colResAccessId, accessId, //
					colResCreated, now(), //
					colResPath, resourcePath, //
					colResName, resourceName, //
					colResMimeType, resourceMimeType, //
					colResEntity, resource, //
					colResPayload, resource);

			return resId;
		}

		private void logProblemIfPathAlreadyUsed(String accessId, Resource resource, String resourcePath) {
			List<GmRow> rows = resTable.select(colResCreated) //
					.whereColumn(colResProjectId, projectId) //
					.whereColumn(colResAccessId, accessId) //
					.whereColumn(colResPath, resourcePath)//
					.rows();

			if (rows.isEmpty())
				return;

			log.warn("[DCSA-RESOURCE-PATH-OVERWRITE] If you see this please send Peter G the stacktrace.", new IllegalArgumentException(
					"Access [" + accessId + "] Resource [" + resource + "] is overwriting another resource for path: " + resPayloadSize()));
		}

		private Resource ensurePlainTextMimeType(Resource resource) {
			if (!ResourceColumn.TEXT_PLAIN_MIME_TYPE.equals(resource.getMimeType())) {
				resource = GmReflectionTools.makeShallowCopy(resource);
				resource.setMimeType(ResourceColumn.TEXT_PLAIN_MIME_TYPE);
			}

			return resource;
		}

		private Date now() {
			return new Date();
		}

		// ###############################################
		// ## . . . . . . . . Reading . . . . . . . . . ##
		// ###############################################

		public DcsaIterable read(String accessId, String lastReadMarker) {
			return new DataLoader(accessId, lastReadMarker).load();
		}

		/**
		 * Loads CSA OPs for given accessId and lastReadMarker - handles single {@link DcsaSharedStorage#readOperations(String, String)} invocation.
		 */
		private class DataLoader {

			private final String accessId;
			private final String lastReadMarker;

			public DataLoader(String accessId, String lastReadMarker) {
				this.accessId = accessId;
				this.lastReadMarker = lastReadMarker;
			}

			public DcsaIterable load() {
				List<GmRow> rows = restrict(opsTable.select(), NO_LOB).orderBy(colOpIdStr.getSingleSqlColumn() + " asc").rows();

				Map<String, Resource> lobResources = loadGmmlLobs(rows);

				List<CsaOperation> ops = rows.stream() //
						.map(row -> extractCsaOperation(row, lobResources)) //
						.collect(Collectors.toList());

				return new DcsaIterable() {

					@Override
					public Iterator<CsaOperation> iterator() {
						return ops.iterator();
					}

					@Override
					public String getLastReadMarker() {
						return rows.isEmpty() ? null : last(rows).getValue(colOpIdStr);
					}
				};
			}

			private Map<String, Resource> loadGmmlLobs(List<GmRow> rows) {
				if (rows.isEmpty())
					return emptyMap();

				List<Object> ids = findIdsForGmmlLobs(rows);

				// TODO why the restrict? Conditions are ignored when ids are given to rowsInBatchesOf.
				Map<Object, GmRow> idToLobRow = restrict(opsTable.select(colOpIdStr, colOpPayload), ONLY_LOB).rowsInBatchesOf(ids, batchSize);

				return idToLobRow.entrySet().stream() //
						.collect(Collectors.toMap( //
								e -> (String) e.getKey(), //
								e -> e.getValue().getValue(colOpPayload) //
						));
			}

			private GmSelectBuilder restrict(GmSelectBuilder sb, GmLobLoadingMode mode) {
				return sb.lobLoading(colOpPayload, mode) //
						.whereColumn(colOpProjectId, projectId) //
						.whereColumn(colOpAccessId, accessId) //
						.when(lastReadMarker != null).where(colOpIdStr.getSingleSqlColumn() + " > ?", lastReadMarker);
			}

			private List<Object> findIdsForGmmlLobs(List<GmRow> rows) {
				return rows.stream() //
						.filter(this::isGmmlLob) //
						.map(row -> row.getValue(colOpIdStr)) //
						.collect(Collectors.toList());
			}

			private boolean isGmmlLob(GmRow row) {
				CsaOperation op = row.getValue(colOpEntity);
				return isGmmlOpreation(op) && row.getValue(colOpPayload) == null;
			}

			private CsaOperation extractCsaOperation(GmRow row, Map<String, Resource> lobResources) {
				CsaOperation result = row.getValue(colOpEntity);
				result.setId(row.getValue(colOpIdStr));

				if (result instanceof CsaResourceBasedOperation) {
					CsaResourceBasedOperation rbOp = (CsaResourceBasedOperation) result;
					if (isGmmlOpreation(result))
						appendIspForTransientSource(rbOp, row, lobResources);
					else
						rbOp.entityType().getProperty(CsaResourceBasedOperation.payload).setAbsenceInformation(rbOp, GMF.absenceInformation());
				}

				return result;
			}

			private void appendIspForTransientSource(CsaResourceBasedOperation result, GmRow row, Map<String, Resource> lobResources) {
				result.setPayload(resolveGmmlResource(row, lobResources));
			}

			private Resource resolveGmmlResource(GmRow row, Map<String, Resource> lobResources) {
				Resource r = row.getValue(colOpPayload);
				if (r != null)
					return r;

				String id = row.getValue(colOpIdStr);
				r = lobResources.get(id);

				if (r == null)
					throw new IllegalStateException(
							"This should not happen. No resource found for entry with id = " + id + ", operation: " + row.getValue(colOpEntity));

				return r;
			}

		} // Data Loader

		// ###############################################
		// ## . . . . . . Reading resources . . . . . . ##
		// ###############################################

		public Map<String, Resource> readResources(String accessId, Collection<String> storedResourcesPaths) {
			return new ResourceLoader(accessId, storedResourcesPaths).load();
		}

		private class ResourceLoader {

			private final String accessId;
			private final Collection<String> storedResourcesPaths;

			public ResourceLoader(String accessId, Collection<String> storedResourcesPaths) {
				this.accessId = accessId;
				this.storedResourcesPaths = storedResourcesPaths;
			}

			public Map<String, Resource> load() {
				if (isEmpty(storedResourcesPaths))
					return emptyMap();

				Map<Object, GmRow> idToResourceRow = resTable.select(colResPayload, colResPath, colResCreated) //
						.whereColumn(colResProjectId, projectId) //
						.whereColumn(colResAccessId, accessId) //
						.whereColumnInValues(colResPath, newList(storedResourcesPaths))//
						.rowsInBatchesOf(batchSize);

				// this is a temporary implementation to handle the mysterious situation with paths not being unique

				// sort rows by date created
				// we do not expect any value of idToResourceView to be null as this can only happen if something was deleted in the DB.
				List<GmRow> rows = sort(idToResourceRow.values(), Comparator.comparing(colResCreated::getRowValue));

				Map<String, Resource> result = newMap();
				for (GmRow row : rows)
					// entries with later date are put in the map later
					result.put(row.getValue(colResPath), row.getValue(colResPayload));

				return result;

				// This doesn't work when resPath is not unique. If it is fixed, we can also get rid of colResCreated being queried.

				// return idToResourceRow.values().stream() //
				// .collect(Collectors.toMap( //
				// colResPath::getRowValue, //
				// colResPayload::getRowValue //
				// ));
			}
		}

	} // TableDriver

	// ###############################################################
	// ## . . . . . . . . . . . . Helpers . . . . . . . . . . . . . ##
	// ###############################################################

	private TableDriver tableDriver() {
		return tableDriver.get();
	}

	public ExecutorService getExecutor() {
		if (executor == null) {
			createdExecutor = true;
			executor = newExecutor();
		}

		return executor;
	}
	private ExtendedThreadPoolExecutor newExecutor() {
		ExtendedThreadPoolExecutor result = new ExtendedThreadPoolExecutor(//
				executorThreads, // corePoolSize
				executorThreads, // maxPoolSize
				5, // keepAliveTime
				TimeUnit.MINUTES, // keepAliveTimeUnit
				new LinkedBlockingQueue<>(), //
				CustomThreadFactory.create().namePrefix("gmdb-dcsa-") //
		);
		result.allowCoreThreadTimeOut(true);
		result.setDescription("GMDB DCSA");

		return result;
	}

	private static boolean isGmmlOpreation(CsaOperation op) {
		CsaOperationType ot = op.operationType();
		return ot == APPEND_DATA_MANIPULATION || //
				ot == APPEND_MODEL_MANIPULATION;
	}
}
