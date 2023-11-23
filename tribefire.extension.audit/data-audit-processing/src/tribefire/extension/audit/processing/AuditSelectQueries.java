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
package tribefire.extension.audit.processing;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.query.From;
import com.braintribe.model.query.SelectQuery;

class AuditSelectQueries extends SelectQueries {
	static SelectQuery queryEntities(String typeSignature, Set<PersistentEntityReference> references, List<String> preserveProperties) {
		
		From entity = source(typeSignature, "e");
		
		SelectQuery query = from(entity) //
			.select( //
				property(entity, GenericEntity.id), // 
				property(entity, GenericEntity.partition) //
			) //
			.where( //
				and( //
					eq(entitySignature(entity), typeSignature), //
					in(entity, references) //
				) //
			);
		
		preserveProperties.stream() //
			.map(p -> property(entity, p)) //
			.forEach(query::select);
		
		return query;
	}
}