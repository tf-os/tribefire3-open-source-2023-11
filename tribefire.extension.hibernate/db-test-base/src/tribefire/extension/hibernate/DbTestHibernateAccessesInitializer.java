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
package tribefire.extension.hibernate;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Set;

import com.braintribe.common.db.DbTestConstants;
import com.braintribe.common.db.DbVendor;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.cortex.initializer.tools.ModelInitializingTools;

/**
 * Convenience initializer that wires a {@link HibernateAccess} (or even more if multiple {@link DbVendor vendors} are provided).
 * <p>
 * These accesses are configured for our standard docker databases, from the "docker-databases" rep.
 * <p>
 * The actual DB URL and credentials are taken from
 * 
 * Just bind it from your module, e.g.:
 * 
 * <pre>
 * &#64;Override
 * public void bindInitializers(InitializerBindingBuilder bindings) {
 * 	bindings.bind(new DbTestHibernateAccessesInitializer("resource", EnumSet.of(DbVendor.mssql), true, SqlSource.T.getModel()));
 * }
 * </pre>
 *
 * @author peter.gazdik
 */
public class DbTestHibernateAccessesInitializer implements DataInitializer {

	private final String identifier;
	private final Set<DbVendor> vendors;
	private final boolean autoDeploy;
	private final Set<Model> models;

	/**
	 * @param identifier
	 *            technical and non-technical identifier of our access. This value is used in the created model names, deployables' names and
	 *            externalIds and is also used as prefix for the generated tables.
	 * 
	 * @param vendors
	 *            set of {@link DbVendor}s for which a new access should be created. In other words, there will be a new access per given vendor.
	 */
	public DbTestHibernateAccessesInitializer(String identifier, Set<DbVendor> vendors, boolean autoDeploy, Model... models) {
		this.identifier = identifier;
		this.vendors = vendors;
		this.autoDeploy = autoDeploy;
		this.models = CollectionTools2.requireNonEmpty(asSet(models), "models cannot be empty");
	}

	@Override
	public void initialize(PersistenceInitializationContext context) {
		new DbTestInit(context).run();
	}

	private class DbTestInit implements DbTestConstants {

		private final ManagedGmSession session;

		private final GmMetaModel gmModel;

		public DbTestInit(PersistenceInitializationContext context) {
			String modelName = "db-test:" + identifier + "-model";

			session = context.getSession();
			gmModel = newModel(modelName);
		}

		private GmMetaModel newModel(String modelName) {
			GmMetaModel result = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
			result.setName(modelName);
			result.setVersion("1.0-synth");

			ModelInitializingTools.extendModelToCoverModels(session, result, models);

			return result;
		}

		public void run() {
			// Dialects are just guessed, if something doesn't work properly, improve!

			// createAccess("derby", derbyUrl, derbyDriver, HibernateDialect.DerbyDialect);

			if (vendors.contains(DbVendor.mssql))
				createAccess("mssql", mssqlUrl, mssqlDriver, HibernateDialect.SQLServer2012Dialect);

			if (vendors.contains(DbVendor.mysql))
				createAccess("mysql", mysqlUrl, mysqlDriver, HibernateDialect.MySQL57Dialect);

			if (vendors.contains(DbVendor.oracle))
				createAccess("oracle", oracleUrl, oracleDriver, HibernateDialect.Oracle9iDialect);

			if (vendors.contains(DbVendor.postgres))
				createAccess("postgres", postgresUrl, postgresDriver, HibernateDialect.PostgreSQL95Dialect);
		}

		private void createAccess(String vendor, String url, String driver, HibernateDialect dialect) {
			String externalId = "hibernate." + identifier + "." + vendor;

			HibernateAccess access = session.createRaw(HibernateAccess.T, "access:" + externalId);
			access.setExternalId(externalId);
			access.setName("Hibernate " + identifier + " access [" + vendor + "]");
			access.setTableNamePrefix(identifier);

			access.setMetaModel(gmModel);
			access.setDialect(dialect);
			access.setConnector(connector(vendor, url, driver));
			access.setAutoDeploy(autoDeploy);
		}

		private DatabaseConnectionPool connector(String vendor, String url, String driver) {
			GenericDatabaseConnectionDescriptor cd = session.create( //
					GenericDatabaseConnectionDescriptor.T, "connection-descriptor." + identifier + "." + vendor);
			cd.setUser(dbTestUsername);
			cd.setPassword(dbTestPassword);
			cd.setUrl(url);
			cd.setDriver(driver);

			String externalId = "hikari." + identifier + "." + vendor;
			HikariCpConnectionPool cp = session.createRaw(HikariCpConnectionPool.T, "connection:" + externalId);
			cp.setExternalId(externalId);
			cp.setName("Hikari " + identifier + " connection pool [" + vendor + "]");
			cp.setConnectionDescriptor(cd);
			cp.setAutoDeploy(autoDeploy);

			return cp;
		}

	}

}
