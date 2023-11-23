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
package com.braintribe.persistence.hibernate.sql;

import org.hibernate.SessionFactory;

import com.braintribe.cfg.Required;
import com.braintribe.util.jdbc.SchemaEnsuringDataSource;

/**
 * A {@link SchemaEnsuringDataSource} which ensures the DB schema using configured Hibernate {@link SessionFactory}
 * 
 * @author peter.gazdik
 */
public class HibernateEnhancedDataSource extends SchemaEnsuringDataSource {

	private SessionFactory sessionFactory;

	// @formatter:off
	@Required public void setSessionFactory(SessionFactory sessionFactory) { this.sessionFactory = sessionFactory; }
	// @formatter:on

	@Override
	protected void updateSchema() {
		sessionFactory.openStatelessSession().close();
	}

}
