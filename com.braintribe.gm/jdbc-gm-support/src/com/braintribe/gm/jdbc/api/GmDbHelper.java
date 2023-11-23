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

import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn;
import com.braintribe.gm.jdbc.impl.column.DateColumn;
import com.braintribe.gm.jdbc.impl.column.EntityAsStringColumn;
import com.braintribe.gm.jdbc.impl.column.EnumColumn;
import com.braintribe.gm.jdbc.impl.column.GmColumnBuilderImpl;
import com.braintribe.gm.jdbc.impl.column.NvarcharClobColumn;
import com.braintribe.gm.jdbc.impl.column.ResourceColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.BigDecimalColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.BooleanColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.DoubleColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.FloatColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.IntegerColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.LongColumn;
import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.StringColumn;
import com.braintribe.gm.jdbc.impl.column.TimestampColumn;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.util.jdbc.dialect.DbVariant;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * @see GmDb
 * 
 * @author peter.gazdik
 */
/* package */ class GmDbHelper {

	public static <T> GmColumnBuilder<T> columnBuilder(AbstractGmColumn<T> column) {
		return new GmColumnBuilderImpl<T>(column);
	}

	public static BooleanColumn booleanColumn(String name, JdbcDialect dialect) {
		return new BooleanColumn(name, dialect.booleanType());
	}

	public static IntegerColumn intColumn(String name, JdbcDialect dialect) {
		return new IntegerColumn(name, dialect.intType());
	}

	public static LongColumn longColumn(String name, JdbcDialect dialect) {
		return new LongColumn(name, dialect.longType());
	}

	public static FloatColumn floatColumn(String name, JdbcDialect dialect) {
		return new FloatColumn(name, dialect.floatType());
	}

	public static DoubleColumn doubleColumn(String name, JdbcDialect dialect) {
		return new DoubleColumn(name, dialect.doubleType());
	}

	public static BigDecimalColumn bigDecimalColumn(String name, JdbcDialect dialect) {
		return new BigDecimalColumn(name, dialect.bigDecimalType());
	}

	public static DateColumn dateColumn(String name, JdbcDialect dialect) {
		return new DateColumn(name, dialect);
	}

	public static TimestampColumn timestampColumn(String name, JdbcDialect dialect) {
		return new TimestampColumn(name, dialect);
	}

	public static StringColumn shortString255Column(String name, JdbcDialect dialect) {
		return new StringColumn(name, dialect.nvarchar255());

	}

	/**
	 * Short string is a unicode encoded String. In some cases it is the standard varchar, for say MS SQL we go with nvarchar.
	 * 
	 * <h3>Why nvarchar for MS SQL?</h3>
	 * 
	 * Consider String IDs. The 'varchar(255)' does not support unicode, however, all values coming via JDBC do, and are thus of nvarchar type.
	 * <p>
	 * This means, that for example on "update where id=..." the index couldn't be used and instead a sequential scan of the entire index happens. Not
	 * only is it inefficient, but also requires allocating a lock on entire table or at least on some page, and can potentially lead to a deadlock.
	 * <p>
	 * This was actually happening when we simply tried to create a Resource and assign it a new ResourceSource in parallel. Lesson learned, avoid
	 * varchar on MS SQL if possible.
	 * 
	 * <h3>Length limitation</h3>
	 * 
	 * For other supported dialects you can take an arbitrary length as far as I know, but for Oracle there is a limit of 4000 bytes. Thus I would not
	 * recommend to use more than roughly 1300 characters here if Oracle should be supported too..
	 */
	public static StringColumn shortStringColumn(String name, int size, JdbcDialect dialect) {
		return new StringColumn(name, dialect.nvarchar(size));
	}

	public static AbstractGmColumn<String> stringColumn(String name, JdbcDialect dialect) {
		if (!dialect.clobType().equalsIgnoreCase("clob"))
			return new StringColumn(name, dialect.clobType());

		if (dialect.hibernateDialect().contains(".Oracle"))
			return new NvarcharClobColumn(name, dialect, 1000);

		// TODO examine when even longer limit can be taken
		return new NvarcharClobColumn(name, dialect, 4000);
	}

	public static <E extends Enum<E>> EnumColumn<E> enumColumn(String name, Class<E> enumClass, JdbcDialect dialect) {
		return new EnumColumn<E>(shortString255Column(name, dialect), enumClass);
	}

	public static <T extends GenericEntity> EntityAsStringColumn<T> entityAsStringColumn(String name, EntityType<T> et, GmCodec<Object, String> codec,
			JdbcDialect dialect) {
		return new EntityAsStringColumn<T>(stringColumn(name, dialect), et, codec);
	}

	public static ResourceColumn resourceColumn(String name, int maxStringLength, JdbcDialect dialect, StreamPipeFactory pipeFactory) {
		return new ResourceColumn(name, dialect, maxStringLimit(maxStringLength, dialect), pipeFactory);
	}

	private static int maxStringLimit(int limit, JdbcDialect dialect) {
		return Math.min(limit, maxStringLimit(dialect));
	}

	private static int maxStringLimit(JdbcDialect dialect) {
		if (dialect.hibernateDialect().contains(".Oracle"))
			return 1000;
		else if (dialect.hibernateDialect().toLowerCase().contains("derby"))
			// https://cdn.cdata.com/help/FHF/jdbc/pg_cachedatatypemapping.htm
			return 32672;
		else if (dialect.knownDbVariant() == DbVariant.mysql)
			// https://wiki.ispirer.com/sqlways/mysql/data-types/nvarchar_n
			// 21845 is max, but that does not work, just having nvarchar(255),nvarchar(21845), blob leads to:
			/* Row size too large. The maximum row size for the used table type, not counting BLOBs, is 65535. This includes storage overhead, check
			 * the manual. You have to change some columns to TEXT or BLOBs */
			return 21_000;
		else
			return Integer.MAX_VALUE;
	}
}
