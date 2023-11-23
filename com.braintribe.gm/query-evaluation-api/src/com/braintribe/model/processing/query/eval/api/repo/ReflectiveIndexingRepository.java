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
package com.braintribe.model.processing.query.eval.api.repo;

/**
 * Originally, method added in this interface was part of {@link IndexingRepository}, however, it was never actually
 * used by anyone. But since it is already supported by the smood 2.0, I did not want to completely remove it, so the
 * original interface was split.
 * 
 * @author peter.gazdik
 */
public interface ReflectiveIndexingRepository extends IndexingRepository {

	RepositoryInfo provideRepositoryInfo();

}
