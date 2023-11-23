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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlusage.access;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.A;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.B;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.C;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.E;
import com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.Y;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;

public class InheritanceOverlappingMergeableTest extends HibernateAccessBasedTest {
	
	@Override
	public Class<?>[] getTestEntityClasses() {
		return new Class<?>[] {
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.A.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.B.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.C.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.D.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.E.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.F.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.G.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.H.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.X.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.Y.class
		};
	}
	
	public static void main(String[] args) throws Exception {
		InheritanceOverlappingMergeableTest test = new InheritanceOverlappingMergeableTest();
		test.testPersistAndQuery();
		test.testPolymorphicQueries();
	}

	@BeforeClass
	public static void start() throws Exception {
		initialize();
	}

	@AfterClass
	public static void stop() throws Exception {
		destroy();
	}

	@Override
	@Test
	public void testPersistAndQuery() throws Exception {
		super.testPersistAndQuery();
	}

	@Test
	public void testPolymorphicQueries() throws Exception {
		testPolymorphicQueries(false);
	}

	/**
	 * This method demonstrates the possible WrongClassException issues resulting from multiple inheritance simulation with hibernate's single inheritance, 
	 * which is a necessary workaround for merging overlapping hierarchies.
	 * 
	 * Given the following model:
	 * 
	 * 		   A          
	 * 	      / \         
	 *  X<>--B   C--<>Y   
	 * 	    / \ / \       
	 * 	   D   E   F      
	 * 
	 * E extends B and C, directly. For Hibernate, E extends B only.
	 * 
	 * Y has a property named 'y' of type C, which can be an instance of E. But retrieving it as "select o.y from Y o" will result in:
	 * 
	 * org.hibernate.WrongClassException: Object with id: 1 was not of the specified subclass: [package].C (Discriminator: [package].E)
	 * 
	 * Note that the property 'y' can still be retrieved if the whole "Y" is fetched and getY() is called.
	 * 
	 */
	private void testPolymorphicQueries(boolean allowError) throws Exception {
		
		PersistenceGmSession gmSession = createPersistenceGmSession();
		
		//In Java and GM, "E" extends B and C, directly. For Hibernate, "E" extends "B" only.

		E e = gmSession.create(E.T);
		e.setA("test A value");
		e.setB("test B value");
		e.setC("test C value");
		e.setE("test E value");
		
		//Assigns an instance of "E" to a property expecting "C", which is a super type of "E" for Java and GM but NOT for Hibernate.
		
		Y y = gmSession.create(Y.T);
		y.setY(e);
		
		//Saving works without problems:
		
		gmSession.commit();

		//Selecting the saved entities as a whole (without specific properties in "SELECT" clause) works without problems: 
		
		EntityQuery fromA = EntityQueryBuilder.from(A.class).done(); //"FROM A o": E will be returned as expected.
		EntityQuery fromB = EntityQueryBuilder.from(B.class).done(); //"FROM B o": E will be returned as expected.
		EntityQuery fromC = EntityQueryBuilder.from(C.class).done(); //"FROM C o": E will be returned as expected. even though C is not a supertype of E for Hibernate.
		EntityQuery fromE = EntityQueryBuilder.from(E.class).done(); //"FROM E o": E will be returned as expected.
		EntityQuery fromY = EntityQueryBuilder.from(Y.class).done(); //"FROM Y o": E will be returned via y.getY(), even though C is not a supertype of E for Hibernate.
		
		List<EntityQuery> queries = Arrays.asList(fromA, fromB, fromC, fromE, fromY);
		
		executeQueries(gmSession, queries);
		
		//Trying to access the instance of "E" throug y.getY() works without problems: 

		Y fetchedY = gmSession.query().entity(y).require();
		
		System.out.println("Fetched ("+y+"). Results: "+GMCoreTools.getDescription(fetchedY));

		C fetchedCThroughY = fetchedY.getY();
		System.out.println("Fetched ("+fetchedCThroughY+"). Results: "+GMCoreTools.getDescription(fetchedCThroughY));
		
		if (allowError) {

			
			/* TO DEMONSTRATE ATTRIBUTE QUERY INCOMPATIBILITY IN MERGED HIERARCHY MODELS
			 * 
			 * Selecting the "y" attribute of Y  ("SELECT alias.y from Y alias") fails. The discriminator of the retrieved instance "E" is not a known subtype of "C" for Hiberante:
			 * org.hibernate.WrongClassException: 
			 *	 Object with id: 1 was not of the specified subclass: 
			 *		 com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.C 
			 *			(Discriminator: com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.E)
			 * 
			 */

			SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder();
			SelectQuery selectCfromY = selectQueryBuilder.from(Y.class, "alias").select("alias", "y").done();
			
			List<C> selectedC = gmSession.query().select(selectCfromY).list();
			System.out.println("Selected ("+selectedC+"). Results: "+GMCoreTools.getDescription(selectedC));
		}
		
		
		
	}
	
}
