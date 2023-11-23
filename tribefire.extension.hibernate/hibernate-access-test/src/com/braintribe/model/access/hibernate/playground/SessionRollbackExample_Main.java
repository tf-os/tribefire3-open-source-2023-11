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
package com.braintribe.model.access.hibernate.playground;

import static com.braintribe.utils.SysPrint.spOut;

import java.util.List;
import java.util.function.Supplier;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.access.hibernate.base.tools.HibernateAccessSetupHelper;
import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public class SessionRollbackExample_Main {

	private static final String dbName = "hibernate-test-all";
	private static final Supplier<GmMetaModel> modelSupplier = HibernateAccessRecyclingTestBase.hibernateModels::basic_NoPartition;

	public static void main(String[] args) throws Exception {
		try {
			new SessionRollbackExample_Main().run();

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			HibernateAccessSetupHelper.close();
		}
	}

	private void run() throws Exception {
		SessionFactory sessionFactory = HibernateAccessSetupHelper.hibernateSessionFactory(modelSupplier,
				HibernateAccessSetupHelper.dataSource_H2(dbName));

		Session session = sessionFactory.openSession();
		Transaction t = session.beginTransaction();

		BasicScalarEntity bse = BasicScalarEntity.T.create();
		bse.setName("BSE 1");

		session.save(bse);
		spOut("ENTITY:" + bse);

		String QUERY = "select e from com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity e";

		List<?> results = session.createQuery(QUERY).getResultList();
		spOut("BEFORE ROLLBACK:" + results);

		t.rollback();
		session.beginTransaction();

		results = session.createQuery(QUERY).getResultList();
		spOut("AFTER ROLLBACK:" + results);

		session.close();
	}

}
