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
package com.braintribe.gm.jdbc.api;

import static com.braintribe.gm.jdbc.api.GmDbHelper.bigDecimalColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.booleanColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.columnBuilder;
import static com.braintribe.gm.jdbc.api.GmDbHelper.dateColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.doubleColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.entityAsStringColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.enumColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.floatColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.intColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.longColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.resourceColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.shortString255Column;
import static com.braintribe.gm.jdbc.api.GmDbHelper.shortStringColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.stringColumn;
import static com.braintribe.gm.jdbc.api.GmDbHelper.timestampColumn;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.cfg.DestructionAware;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.common.lcd.function.XConsumer;
import com.braintribe.gm.jdbc.impl.GmIndexImpl;
import com.braintribe.gm.jdbc.impl.GmTableImpl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * Simple JDBC framework for creating tables consisting of GM-value columns.
 * 
 * <h2>Supported GM Types</h2>
 * <ul>
 * <li>Simple types
 * <li>Enums
 * <li>Entities (encoded as Strings, stored as Strings or clobs)
 * <li>Resources (stored as Strings when mimeType is plain/text and length is small enough, otherwise blob)
 * </ul>
 * 
 * <h2>Supported DBs</h2>
 * <ul>
 * <li>PostgreSQL
 * <li>MySQL
 * <li>MS SQL Server
 * <li>Oracle
 * <li>Derby
 * </ul>
 * 
 * <h2>Managing tables</h2>
 * <ul>
 * <li>Table is created and updated automatically, update is capable of adding missing columns and indices.
 * <li>Automatically picks the right column or even columns for given constrains. Longer strings are stored as two columns for some DBs, one for short
 * enough to fit as text, another for longer stored as CLOBs.
 * <li>Entities are stored encoded as Strings. See {@link GmDbBuilder#withDefaultCodec(GmCodec)} and
 * {@link GmDb#entityAsString(String, EntityType, GmCodec)}.
 * </ul>
 * 
 * <h2>Managing data</h2>
 * 
 * <ul>
 * <li>Insert/Update are performed by simply providing a {@code Map<GmColum, Value>}.
 * <li>Support for conditions, ordering and pagination
 * <li>LOBs can be fetched in parallel - see {@link GmDbBuilder#withExecutor(ExecutorService)} and {@link GmDbBuilder#withExecutorPoolSize(int)}
 * <li>Binary data of {@link Resource}s is stored in {@link StreamPipe}s so the connection can be closed yet data is not necessarily stored in memory
 * - see {@link GmDbBuilder#withStreamPipeFactory(StreamPipeFactory)}
 * </ul>
 * 
 * <h2>Misc</h2>
 * <ul>
 * <li>Every column is implemented as nullable. Some JDBC drivers (Mysql, Oracle) would never return <tt>null</tt> for a Boolean, for example, but
 * they return false. The implementation uses {@link ResultSet#wasNull()} to ensure <tt>null</tt> is returned when the value is <tt>null</tt>.
 * </ul>
 * 
 * @author peter.gazdik
 */
public class GmDb implements DestructionAware {

	public static GmDbBuilder newDb(DataSource dataSource) {
		return new GmDbBuilder(dataSource);
	}

	public static class GmDbBuilder {
		/* package */ final DataSource dataSource;
		/* package */ GmCodec<Object, String> defaultCodec;
		/* package */ StreamPipeFactory pipeFactory;

		/* package */ ExecutorService executor;
		/* package */ int executorPoolSize = 5;

		public GmDbBuilder(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		/**
		 * This is only needed for columns which contain entities encoded as String and don't specify their own codec, i.e. with
		 * {@link GmDb#entityAsString(String, EntityType)} method for creating a column.
		 */
		public GmDbBuilder withDefaultCodec(GmCodec<Object, String> defaultCodec) {
			this.defaultCodec = defaultCodec;
			return this;
		}

		/** {@link StreamPipeFactory} is only needed if {@link Resource}s are being persisted. */
		public GmDbBuilder withStreamPipeFactory(StreamPipeFactory pipeFactory) {
			this.pipeFactory = pipeFactory;
			return this;
		}

		/**
		 * Sets executor to use when fetching rows with {@link GmSelectBuilder#rowsInBatchesOf(int)}. If no is given, a default executor is created
		 * internally with 5 threads.
		 */
		public GmDbBuilder withExecutor(ExecutorService executor) {
			this.executor = executor;
			return this;
		}

		/**
		 * Pool size of the pooled {@link ExecutorService} created internally if no executor is configured explicitly via
		 * {@link #withExecutor(ExecutorService)}.
		 * 
		 * IMPORTANT: This is here for fast prototyping, but in production code you should configure the executor directly, providing use-case related
		 */
		public GmDbBuilder withExecutorPoolSize(int executorPoolSize) {
			this.executorPoolSize = executorPoolSize;
			return this;
		}

		public GmDb done() {
			return new GmDb(this);
		}
	}

	// #################################################
	// ## . . . . . . . . Actual GmDb . . . . . . . . ##
	// #################################################

	public final DataSource dataSource;
	public final JdbcDialect dialect;
	public final GmCodec<Object, String> defaultCodec;
	public final StreamPipeFactory pipeFactory;

	private ExecutorService executor;
	private final boolean ownExecutor;
	private final int executorPoolSize;

	private GmDb(GmDbBuilder builder) {
		this.dataSource = builder.dataSource;
		this.dialect = JdbcDialect.detectDialect(dataSource);
		this.defaultCodec = builder.defaultCodec;
		this.pipeFactory = builder.pipeFactory;

		this.executor = builder.executor;
		this.ownExecutor = builder.executor == null;
		this.executorPoolSize = builder.executorPoolSize;
	}

	public GmTableBuilder newTable(String name) {
		return new GmTableImpl(name, this);
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Boolean> booleanCol(String name) {
		return columnBuilder(booleanColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Integer> intCol(String name) {
		return columnBuilder(intColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Long> longCol(String name) {
		return columnBuilder(longColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Float> floatCol(String name) {
		return columnBuilder(floatColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Double> doubleCol(String name) {
		return columnBuilder(doubleColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<BigDecimal> bigDecimal(String name) {
		return columnBuilder(bigDecimalColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Date> date(String name) {
		return columnBuilder(dateColumn(name, dialect));
	}

	/** NOTE: Always backed by a single column. */
	public GmColumnBuilder<Timestamp> timestamp(String name) {
		return columnBuilder(timestampColumn(name, dialect));
	}

	/** Uses the {@link JdbcDialect#nvarchar255()} type. This is safe as a primary key column type. */
	public GmColumnBuilder<String> shortString255(String name) {
		return columnBuilder(shortString255Column(name, dialect));
	}

	/**
	 * Uses the {@link JdbcDialect#nvarchar(int)} type. This NOT SAFE AS A PRIMARY KEY column type, and is in general only safe for up to 4000 bytes
	 * (Oracle) - so just to be safe let's say around 1300 characters.
	 */
	public GmColumnBuilder<String> shortString(String name, int size) {
		return columnBuilder(shortStringColumn(name, size, dialect));
	}

	/**
	 * Depending on underlying DB, this might be a single column with the string value stored directly, or two columns with where one is used for
	 * shorter strings and another is a CLOB, for really long strings.
	 * <p>
	 * NOTE: Might be backed by multiple columns.
	 */
	public GmColumnBuilder<String> string(String name) {
		return columnBuilder(stringColumn(name, dialect));
	}

	/** Backed by {@link #shortString255(String)} column, so this only supports enums with constans of up to 255 chars. */
	public <E extends Enum<E>> GmColumnBuilder<E> enumCol(String name, Class<E> enumClass) {
		return columnBuilder(enumColumn(name, enumClass, dialect));
	}

	/** NOTE: Might be backed by multiple columns. */
	public <T extends GenericEntity> GmColumnBuilder<T> entityAsString(String name, EntityType<T> et) {
		return entityAsString(name, et, defaultCodec("Entity"));
	}

	/** NOTE: Might be backed by multiple columns. */
	public <T extends GenericEntity> GmColumnBuilder<T> entityAsString(String name, EntityType<T> et, GmCodec<Object, String> codec) {
		return columnBuilder(entityAsStringColumn(name, et, codec, dialect));
	}

	/** NOTE: Might be backed by multiple columns. */
	public GmColumnBuilder<Resource> resource(String name) {
		return resource(name, IOTools.SIZE_64K);
	}

	/** NOTE: Might be backed by multiple columns. */
	public GmColumnBuilder<Resource> resource(String name, int maxStringLength) {
		return columnBuilder(resourceColumn(name, maxStringLength, dialect, pripeFactory("Resource")));
	}

	public GmIndex index(String name, GmColumn<?> column) {
		return new GmIndexImpl(name, column);
	}

	private GmCodec<Object, String> defaultCodec(String type) {
		return requireNonNull(defaultCodec,
				() -> "Cannot create column for '" + type + "' without specifying a GmCodec as no default codec was configured.");
	}

	private StreamPipeFactory pripeFactory(String type) {
		return requireNonNull(pipeFactory, () -> "Cannot create column for '" + type + "' as no StreamPipeFactorys was configured.");
	}

	public ExecutorService getExecutor() {
		if (executor == null)
			executor = createExecutor();
		return executor;
	}

	private ExecutorService createExecutor() {
		ThreadPoolExecutor result = new ThreadPoolExecutor( //
				executorPoolSize, // corePoolSize
				executorPoolSize, // maxPoolSize
				5, // keepAliveTime
				TimeUnit.MINUTES, // keepAliveTimeUnit
				new LinkedBlockingQueue<>() //
		);
		result.allowCoreThreadTimeOut(true);

		return result;
	}

	/** Convenience method to perform a task with a {@link Connection} created for this GmDb's {@link #dataSource}. */
	public void withManualCommitConnection(Supplier<String> details, XConsumer<Connection> task) {
		JdbcTools.withManualCommitConnection(dataSource, details, task);
	}

	@Override
	public void preDestroy() {
		if (executor != null && ownExecutor)
			executor.shutdown();
	}

}
