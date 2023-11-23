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
package tribefire.extension.hibernate.lab.wire.space;

import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.meta.DbUpdateStatement;
import com.braintribe.model.accessdeployment.hibernate.selector.HibernateDbVendor;
import com.braintribe.model.accessdeployment.hibernate.selector.HibernateDbVendorSelector;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.hibernate.lab.model.data.Movie;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * Lab module for testing hibernate-module.
 */
@Managed
public class HibernateLabModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind("cortex", this::initLabAccess);
	}

	private void initLabAccess(PersistenceInitializationContext ctx) {
		ManagedGmSession session = ctx.getSession();

		String dbName = "hib-lab-module-movies";

		GenericDatabaseConnectionDescriptor cd = session.create(GenericDatabaseConnectionDescriptor.T, "connection.descriptor.hibernate.lab.access");
		cd.setUrl("jdbc:postgresql://localhost:5432/" + dbName);
		cd.setDriver("org.postgresql.Driver");
		cd.setUser("postgres");
		cd.setPassword("root");

		HikariCpConnectionPool connector = session.create(HikariCpConnectionPool.T, "connector.hibernate.lab.access");
		connector.setExternalId("connector.hibernate.lab.access");
		connector.setConnectionDescriptor(cd);

		HibernateAccess access = session.create(HibernateAccess.T, "hibernate.lab.access");
		access.setExternalId("hibernate.lab.access");
		access.setName("Hibernate Lab Access");
		access.setMetaModel(configurationModel(session));
		access.setConnector(connector);
	}

	private GmMetaModel configurationModel(ManagedGmSession session) {
		String modelName = "tribefire.extensions.hibernate:configured-hibernate-lab-access-model";
		GmMetaModel configuredModule = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
		configuredModule.setName(modelName);
		configuredModule.setVersion("1.0");
		configuredModule.getDependencies().add(session.getEntityByGlobalId(Movie.T.getModel().globalId()));

		HibernateDbVendorSelector postgreSelector=session.create(HibernateDbVendorSelector.T, "meta:HibernateDbVendorSelector:lab-access-model:postgres");
		postgreSelector.setVendor(HibernateDbVendor.PostgreSQL);

		DbUpdateStatement titleIndex = session.create(DbUpdateStatement.T, "meta:DbUpdateStatement:lab-access-model:Movie:title");
		titleIndex.setExpression("create index my_index on ${TABLE} (${TABLE/title}) ");
		titleIndex.setSelector(postgreSelector);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(configuredModule).withSession(session).done();
		mdEditor.onEntityType(Movie.T).addMetaData(titleIndex);

		return configuredModule;
	}

}
