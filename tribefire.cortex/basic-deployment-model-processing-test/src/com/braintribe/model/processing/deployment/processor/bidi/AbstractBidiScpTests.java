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
package com.braintribe.model.processing.deployment.processor.bidi;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.processor.AbstractScpTests;
import com.braintribe.model.processing.deployment.processor.BidiPropertyStateChangeProcessor;
import com.braintribe.model.processing.deployment.processor.bidi.data.BidiPropertyTestModel;
import com.braintribe.model.processing.deployment.processor.bidi.data.Company;
import com.braintribe.model.processing.deployment.processor.bidi.data.Folder;
import com.braintribe.model.processing.deployment.processor.bidi.data.Person;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.query.EntityQuery;

/**
 * 
 */
public abstract class AbstractBidiScpTests extends AbstractScpTests {

	private static BidiPropertyStateChangeProcessor processor = new BidiPropertyStateChangeProcessor();

	@Override
	protected StateChangeProcessorRule stateChangeProcessorRule() {
		return processor;
	}

	@Override
	protected GmMetaModel newMetaModel() {
		return BidiPropertyTestModel.enriched();
	}

	// ###################################
	// ## . . . . . queries . . . . . . ##
	// ###################################

	protected Person personByName(String name) {
		return byName(Person.class, name);
	}

	protected Company companyByName(String name) {
		return byName(Company.class, name);
	}

	protected Folder folderByName(String name) {
		return byName(Folder.class, name);
	}

	protected Person personByName(String name, PersistenceGmSession session) {
		return byName(Person.class, name, session);
	}

	protected Company companyByName(String name, PersistenceGmSession session) {
		return byName(Company.class, name, session);
	}
	
	protected Folder folderByName(String name, PersistenceGmSession session) {
		return byName(Folder.class, name, session);
	}

	
	private <T extends GenericEntity> T byName(Class<T> clazz, String name, PersistenceGmSession session) {
		try {
			EntityQuery eq = EntityQueryBuilder.from(clazz).where().property("name").eq(name).done();
			return clazz.cast(session.query().entities(eq).unique());

		} catch (Exception e) {
			throw new RuntimeException("Query failed!", e);
		}
	}

	// ###################################
	// ## . . . instantiations . . . . .##
	// ###################################

	protected Person newPerson(PersistenceGmSession session) {
		return session.create(Person.T);
	}

	protected Person newPerson(PersistenceGmSession session, String name) {
		Person person = session.create(Person.T);
		person.setName(name);

		return person;
	}

	protected Company newCompany(PersistenceGmSession session) {
		return newCompany(session, null);
	}

	protected Company newCompany(PersistenceGmSession session, String name) {
		Company company = session.create(Company.T);
		company.setName(name);

		return company;
	}

	
	protected Folder newFolder(PersistenceGmSession session) {
		return newFolder(session, null);
	}

	protected Folder newFolder(PersistenceGmSession session, String name) {
		Folder folder = session.create(Folder.T);
		folder.setName(name);
		return folder;
	}

}
