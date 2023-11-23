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
package tribefire.cortex.testing.user;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

/**
 * This class gives access to global static fields that provide session factories for testing purposes. They are set by
 * the <tt>tribefire.cortex.testing:test-runner-module</tt> to be used by integration tests. When you execute the tests from your
 * IDE, these fields most likely won't be set and a remote session should be used instead. <code>Imp</code> for example uses
 * {@link #userSessionFactory} as default when it is set or otherwise a remote session.
 * 
 * @author Neidhart.Orlich
 * @author Peter.Gazdik
 */
public class UserRelatedTestApi {
	public static PersistenceGmSessionFactory userSessionFactory;
	public static PersistenceGmSessionFactory systemSessionFactory;
}
