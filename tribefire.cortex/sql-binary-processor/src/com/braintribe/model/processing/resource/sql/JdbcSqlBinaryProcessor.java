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
package com.braintribe.model.processing.resource.sql;

import static com.braintribe.util.jdbc.JdbcTools.thoroughlyCloseJdbcConnection;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.common.lcd.function.CheckedBiFunction;
import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedTriFunction;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.resource.streaming.AbstractBinaryProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.SqlSource;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.request.FixSqlSources;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.LazyInitialization;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.stream.RangeInputStream;

/**
 * <p>
 * A binary processor for {@link SqlSource}(s).
 */
public class JdbcSqlBinaryProcessor extends AbstractBinaryProcessor implements InitializationAware {

	private static final Logger log = Logger.getLogger(JdbcSqlBinaryProcessor.class);

	// configurable
	private String externalId;
	private DataSource dataSource;
	private Locking locking;

	private Function<Resource, String> idGenerator = r -> UUID.randomUUID().toString();
	private Evaluator<ServiceRequest> evaluator;

	private String tableName = "gm_resources";
	private String idColumnName = "id";
	private String blobColumnName = "data";

	private String insertSql;
	private String getSql;
	private String deleteSql;

	// Marks that getBinaryStream(position, length) isn't supported by JDBC driver; only set if
	// SQLFeatureNotSupportedException is thrown
	private Boolean rangedBinaryStreamNotSupported;

	// @formatter:off
	@Required public void setExternalId(String externalId) { this.externalId = externalId; }
	@Required public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }
	@Required public void setLocking(Locking locking) { this.locking = locking; }

	/** Specifies the id generator used in the {@link #store(ServiceRequestContext, StoreBinary)} method. By default a random UUID is used. */
	@Configurable public void setIdGenerator(Function<Resource, String> idGenerator) { this.idGenerator = NullSafe.get(idGenerator, this.idGenerator); }
	@Configurable public void setEvaluator(Evaluator<ServiceRequest> evaluator) { this.evaluator = evaluator; }

	@Configurable public void setTableName(String tableName) { this.tableName = NullSafe.get(tableName, this.tableName); }
	@Configurable public void setIdColumnName(String idColumnName) { this.idColumnName = NullSafe.get(idColumnName, this.idColumnName); }
	@Configurable public void setBlobColumnName(String blobColumnName) { this.blobColumnName = NullSafe.get(blobColumnName, this.blobColumnName); }
	// @formatter:on

	// ######################################################
	// ## . . . . . . . . Initialization . . . . . . . . . ##
	// ######################################################

	@Override
	public void postConstruct() {
		insertSql = "insert into " + tableName + " (" + idColumnName + ", " + blobColumnName + ") values (?,?)";
		getSql = "select " + blobColumnName + " from " + tableName + " where " + idColumnName + " = ?";
		deleteSql = "delete from  " + tableName + " where " + idColumnName + " = ?";
	}
	
	private final LazyInitialization ensureTable = new LazyInitialization(this::ensureTableExists);
	
	private void ensureTableExists() {
		JdbcDialect dialect = JdbcDialect.detectDialect(dataSource);
		log.debug(() -> logPrefix() + "Detected dialect: " + dialect.hibernateDialect());

		doWithConnection(c -> ensureTableExists(c, dialect), "ensuring binary persistence table");
	}

	private void doWithConnection(CheckedConsumer<Connection, Exception> code, String operation) {
		withInitializedConnection(this::doThis, code, operation);
	}

	private Object doThis(Connection c, CheckedConsumer<Connection, Exception> code) throws Exception {
		code.accept(c);
		return null;
	}

	private void ensureTableExists(Connection connection, JdbcDialect dialect) throws Exception {
		if (resourcesTableExists(connection))
			return;

		String st = "create table " + tableName + " (" + //
				idColumnName + " varchar(255) primary key not null, " + //
				blobColumnName + " " + dialect.blobType() + //
				")";

		log.debug(() -> logPrefix() + "creating binary data table with statement: " + st);

		String lockIdentifier = externalId.concat("-ddl-lock");
		Lock lock = locking.forIdentifier(lockIdentifier).writeLock();
		lock.lock();

		try {
			if (resourcesTableExists(connection))
				return;

			Statement statement = connection.createStatement();
			try {
				statement.executeUpdate(st);
				log.debug(() -> logPrefix() + "Successfully created table: " + tableName);

			} finally {
				statement.close();
			}

		} finally {
			lock.unlock();
		}
	}

	private boolean resourcesTableExists(Connection connection) {
		return JdbcTools.tableExists(connection, tableName) != null;
	}

	// ######################################################
	// ## . . . . . . . Binary Persistence . . . . . . . . ##
	// ######################################################

	@Override
	public StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {
		return withConnection(this::store, context, request, "store resource");
	}

	private StoreBinaryResponse store(Connection connection, @SuppressWarnings("unused") ServiceRequestContext context, StoreBinary request)
			throws Exception {
		Resource resource = requireNonNull(request.getCreateFrom(), "request resource cannot be null");

		String id = store(connection, resource);

		SqlSource resourceSource = SqlSource.T.create();
		resourceSource.setBlobId(id);

		Resource managedResource = createResource(null, resource, resourceSource);

		StoreBinaryResponse response = StoreBinaryResponse.T.create();
		response.setResource(managedResource);

		return response;
	}

	private String store(Connection connection, Resource resource) throws Exception {
		try (InputStream inputStream = resource.openStream()) {
			String id = idGenerator.apply(resource);

			insert(connection, id, inputStream);

			return id;
		}
	}

	private void insert(Connection connection, String id, InputStream resourceInputStream) throws SQLException, Exception {
		try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
			ps.setString(1, id);
			ps.setBlob(2, resourceInputStream);

			int updated = ps.executeUpdate();
			if (updated != 1)
				throw new Exception("The execution of '" + insertSql + "' did not change anything in the DB (result is " + updated + ", expected 1)");
		}
	}

	@Override
	public DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary request) {
		return withConnection(this::delete, request, "delete");
	}

	private DeleteBinaryResponse delete(Connection connection, DeleteBinary request) throws SQLException {
		Resource resource = requireNonNull(request.getResource(), "request resource cannot be null");
		SqlSource source = retrieveSqlSource(resource);

		try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
			String id = getBlobId(source, request.getDomainId());
			ps.setString(1, id);

			int deleted = ps.executeUpdate();
			connection.commit();

			if (deleted == 1)
				log.trace(() -> logPrefix() + "Deleted resource: " + id);
			else
				log.debug(() -> logPrefix() + "Attempt to delete resource '" + id + "' did not delete one entry, but: " + deleted);
		}

		return DeleteBinaryResponse.T.create();
	}

	// ######################################################
	// ## . . . . . . . . Binary Retrieval . . . . . . . . ##
	// ######################################################

	@Override
	protected StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request, StreamBinaryResponse response) {
		return withConnection(connection -> {
			Resource resource = requireNonNull(request.getResource(), "request resource cannot be null");
			SqlSource source = retrieveSqlSource(resource);

			doStream(context, connection, request, source, response);

			return response;
		}, "stream");
	}

	private void doStream(ServiceRequestContext context, Connection connection, StreamBinary request, SqlSource source,
			StreamBinaryResponse response) {

		String key = getBlobId(source, request.getDomainId());
		Blob blob = null;
		StreamRange range = null;
		try {

			blob = queryBlob(connection, key);
			range = resolveRange(request, blob);

			addRangeDataToResponseIfNeeded(range, response, blob);

			context.notifyResponse(response);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve " + SqlSource.class.getSimpleName() + " with id " + key);
		}
		try (InputStream is = new BufferedInputStream(resolveBlobStream(range, blob)); OutputStream os = request.getCapture().openStream()) {
			IOTools.pump(is, os);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to stream " + SqlSource.class.getSimpleName() + " with id " + key);
		}
	}

	@Override
	protected GetBinaryResponse get(ServiceRequestContext context, GetBinary request, GetBinaryResponse response) {
		return withConnection(connection -> {
			Resource resource = requireNonNull(request.getResource(), "request resource cannot be null");
			SqlSource source = retrieveSqlSource(resource);

			String id = getBlobId(source, request.getDomainId());
			Blob blob = queryBlob(connection, id);

			StreamRange range = resolveRange(request, blob);

			Supplier<InputStream> newConnectionCreatingIss = () -> openDbConnectionAndGetInputStream(id, range);

			// set response and cut stream
			addRangeDataToResponseIfNeeded(range, response, blob);

			Resource callResource = Resource.createTransient(newConnectionCreatingIss::get);
			callResource.setName(resource.getName());
			callResource.setMimeType(resource.getMimeType());
			callResource.setFileSize(streamLength(resource, range));
			callResource.setMimeType(resource.getMimeType());

			response.setResource(callResource);

			return response;
		}, "get resource");
	}

	private Long streamLength(Resource resource, StreamRange range) {
		// Note the cast to Long is important!
		return range != null ? (Long) (range.getEnd() - range.getStart() + 1) : resource.getFileSize();
	}

	private StreamRange resolveRange(BinaryRetrievalRequest request, Blob blob) {
		StreamRange range = request.getRange();
		if (range == null)
			return null;

		Long start = range.getStart();
		if (start == null)
			return null;

		Long end = range.getEnd();
		try {
			if (end == null || end < start || end >= blob.length())
				end = blob.length() - 1;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not get the length of BLOB " + blob);
		}

		return StreamRange.create(start, end);
	}

	private InputStream openDbConnectionAndGetInputStream(String id, StreamRange range) {
		Connection c = null;

		try {
			Connection connection = c = dataSource.getConnection();
			connection.setAutoCommit(false);

			try {
				Blob blob = queryBlob(connection, id);

				try {
					return new BufferedInputStream(resolveBlobStream(range, blob)) {
						@Override
						public void close() throws IOException {
							try {
								super.close();
							} finally {
								closeConnection(connection, id, true); // happy path, we commit
								log.trace(() -> logPrefix() + "Closed connection on InputStream.close(), resource: " + id);
							}
						}
					};

				} catch (Exception e) {
					closeConnection(connection, id, false); // error happened, no commit
					throw Exceptions.unchecked(e, logPrefix() + "Failed to open blob data input stream of resource: " + id);
				}

			} catch (Exception e) {
				connection.rollback();
				throw e;
			}

		} catch (SQLException e) {
			closeConnection(c, id, false);
			throw Exceptions.unchecked(e, logPrefix() + "Error while opening input stream for resource: " + id);
		}
	}

	private void closeConnection(Connection connection, String id, boolean commit) {
		thoroughlyCloseJdbcConnection(connection, commit, () -> logPrefix() + "Associated with resource: " + id);
	}

	private void addRangeDataToResponseIfNeeded(StreamRange range, BinaryRetrievalResponse response, Blob blob) {
		if (range != null) {
			try {
				response.setRanged(true);
				response.setRangeStart(range.getStart());
				response.setRangeEnd(range.getEnd());
				response.setSize(blob.length());
			} catch (Exception e) {
				throw new RuntimeException(logPrefix() + "Could not rangify stream according to " + range.getStart() + "-" + range.getEnd(), e);
			}
		}
	}

	private InputStream resolveBlobStream(StreamRange range, Blob blob) throws SQLException {
		if (range == null || isFullRange(range, blob))
			return blob.getBinaryStream();

		if (rangedBinaryStreamNotSupported == Boolean.TRUE)
			return resolveRangeBlobStream(blob, range);

		try {
			long len = range.getEnd() - range.getStart() + 1;
			return blob.getBinaryStream(range.getStart() + 1, len);

		} catch (SQLFeatureNotSupportedException e) {
			rangedBinaryStreamNotSupported = Boolean.TRUE;
			return resolveRangeBlobStream(blob, range);
		}
	}

	private boolean isFullRange(StreamRange range, Blob blob) throws SQLException {
		return range.getStart() == 0 && range.getEnd() == blob.length() - 1;
	}

	private InputStream resolveRangeBlobStream(Blob blob, StreamRange range) throws SQLException {
		try {
			return new RangeInputStream(blob.getBinaryStream(), range.getStart(), range.getEnd() + 1);
		} catch (IOException e) {
			throw new UncheckedIOException("This JDBC driver does not support getBinaryStream(pos, lenght) and an workaround failed too.", e);
		}
	}

	// ######################################################
	// ## . . . . . . . . . . Helpers . . . . . . . . . . .##
	// ######################################################

	// must not return null
	private Blob queryBlob(Connection connection, String id) {
		try (PreparedStatement ps = connection.prepareStatement(getSql)) {
			ps.setString(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new NotFoundException(logPrefix() + "No data found for id" + id + ". Table: " + tableName + ", idColumn: " + idColumnName);

				Blob blob = rs.getBlob(1);

				if (rs.next())
					log.warn("");

				return blob;
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not query for BLOB with id " + id);
		}
	}

	private static SqlSource retrieveSqlSource(Resource resource) {
		ResourceSource source = requireNonNull(resource.getResourceSource(), "resource source cannot be null");

		if (source instanceof SqlSource)
			return (SqlSource) source;
		else
			throw new IllegalStateException(JdbcSqlBinaryProcessor.class.getName() + " instances can only handle SqlSoruces, not: " + source);
	}

	private String getBlobId(SqlSource source, String domainId) {
		String blobId = source.getBlobId();

		if (blobId == null) {
			blobId = source.getId();

			if (evaluator != null) {
				FixSqlSources request = FixSqlSources.T.create();
				request.setDomainId(domainId);

				request.eval(evaluator).get(AsyncCallback.of(log::error));
			}
		}

		return blobId;
	}

	private <R, ARG1, ARG2> R withConnection(CheckedTriFunction<Connection, ARG1, ARG2, R, Exception> code, ARG1 arg1, ARG2 arg2, String operation) {
		return withConnection( //
				(c, argPair) -> code.apply(c, argPair.first, argPair.second), //
				Pair.of(arg1, arg2), //
				operation);
	}

	private <R, ARG> R withConnection(CheckedBiFunction<Connection, ARG, R, Exception> code, ARG arg, String operation) {
		ensureTable.run();
		return withInitializedConnection(code, arg, operation);
	}

	/** Unlike regular withConnections, this one does not call ensureTables to avoid recursion and thus stack overflow. */
	private <R, ARG> R withInitializedConnection(CheckedBiFunction<Connection, ARG, R, Exception> code, ARG arg, String operation) {
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);

			try {
				R result = code.apply(connection, arg);
				connection.commit();

				return result;

			} catch (SQLException e) {
				connection.rollback();
				throw e;
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, logPrefix() + "Error while performing " + operation);
		}
	}

	private <R> R withConnection(Function<Connection, R> code, String operation) {
		ensureTable.run();

		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);

			try {
				R result = code.apply(connection);
				connection.commit();

				return result;

			} catch (SQLException e) {
				connection.rollback();
				throw e;
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, logPrefix() + "Error while performing " + operation);
		}
	}

	private String logPrefix() {
		return getClass().getSimpleName() + "[" + externalId + "] ";
	}
}
