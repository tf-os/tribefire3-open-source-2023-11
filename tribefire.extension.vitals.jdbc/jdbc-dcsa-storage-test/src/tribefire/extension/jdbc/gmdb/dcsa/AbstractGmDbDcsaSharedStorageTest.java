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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.common.db.DbVendor;
import com.braintribe.common.db.SimpleDbTestSession;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.jdbc.api.GmDb;
import com.braintribe.gm.jdbc.api.GmRow;
import com.braintribe.model.access.smood.collaboration.distributed.api.sharedstorage.AbstractSharedStorageTest;
import com.braintribe.model.processing.lock.db.impl.DbLockManager;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.stream.api.StreamPipes;

@RunWith(Parameterized.class)
public abstract class AbstractGmDbDcsaSharedStorageTest extends AbstractSharedStorageTest {

	// ###############################################
	// ## . . . . . . . . Static . . . . . . . . . .##
	// ###############################################

	private static SimpleDbTestSession dbTestSession;

	// We user timestamp suffix for table/index names so we can run tests multiple times without re-deploying docker containers
	protected static final String tmSfx = "_" + RandomTools.timeStamp();

	@BeforeClass
	public static void beforeClass() throws Exception {
		deleteResFolderWithDerbyData();

		dbTestSession = SimpleDbTestSession.startDbTest();
	}

	private static void deleteResFolderWithDerbyData() {
		File res = new File("res-gmdb");
		if (res.exists())
			FileTools.deleteDirectoryRecursivelyUnchecked(res);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		dbTestSession.shutdownDbTest();
	}

	// ###############################################
	// ## . . . . . . . . . Tests . . . . . . . . . ##
	// ###############################################

	@Parameters
	public static Object[][] params() {
		return new Object[][] { //
				{ DbVendor.derby }, //
				// { DbVendor.postgres }, //
				// { DbVendor.mysql }, //
				// { DbVendor.mssql }, //
				// { DbVendor.oracle }, //
		};
	}

	private static final GmCodec<Object, String> jsonCodec = new JsonStreamMarshaller();

	protected final DataSource dataSource;
	protected DbLockManager lockManager;
	protected final GmDb gmDb;


	public AbstractGmDbDcsaSharedStorageTest(DbVendor vendor) {
		this.dataSource = dbTestSession.contract.dataSource(vendor);
		this.lockManager = lockManager(dataSource);
		this.gmDb = GmDb.newDb(dataSource) //
				.withDefaultCodec(jsonCodec) //
				.withStreamPipeFactory(StreamPipes.simpleFactory()) //
				.done();
	}

	private DbLockManager lockManager(DataSource dataSource) {
		DbLockManager bean = new DbLockManager();
		bean.setDataSource(dataSource);
		bean.setAutoUpdateSchema(true);
		bean.postConstruct();
		return bean;
	}

	// ###############################################
	// ## . . . . . Query Result Asserts . . . . . .##
	// ###############################################

	protected List<GmRow> queryResult;

	protected void collectResult(Iterable<GmRow> rows) {
		queryResult = newList();

		for (GmRow gmRow : rows)
			queryResult.add(gmRow);
	}

	protected void assertResultSize(int expected) {
		assertThat(queryResult).hasSize(expected);
	}

	protected void assertSameResource(Resource actual, Resource expected) {
		assertThat(actual).isNotNull();

		byte[] actualBytes = toBytes(actual);
		byte[] expectedBytes = toBytes(expected);

		assertThat(actualBytes).containsExactly(expectedBytes);
	}

	protected byte[] toBytes(Resource resource) {
		try (InputStream inputStream = resource.openStream()) {
			return IOTools.slurpBytes(inputStream);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

}
