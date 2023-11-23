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
package com.braintribe.model.processing.deployment.hibernate.mapping.hints;

public class PropertyHint {
	
	public Boolean mapToDb = true; 
	
	public String type;
	public String column;
	
	public Boolean unique;
	public Boolean notNull;
	
	public String uniqueKey;
	public String index;
	public String foreignKey;
	
	public String idGeneration;
	
	public Long length;
	public Long precision;
	public Long scale;
	
	public String lazy;
	public String fetch;
	
	// for collection properties:
	public Boolean oneToMany;
	public String table;
	public String keyType;
	public Long keyLength;
	public String keyColumn;
	public String keyPropertyRef;
	public String indexColumn;
	public String mapKeyColumn;
	public String mapKeyForeignKey;
	public String elementColumn;
	public String elementForeignKey;
	public String manyToManyFetch; //special case for <many-to-many> 'fetch' attribute
	
}
