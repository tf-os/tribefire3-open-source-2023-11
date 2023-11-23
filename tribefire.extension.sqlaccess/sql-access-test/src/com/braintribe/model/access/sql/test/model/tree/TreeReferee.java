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
package com.braintribe.model.access.sql.test.model.tree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.access.sql.test.model.SqlAccessEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An entity that references a tree hierarchy, which demonstrates various use-cases for a polymorphic join.
 * 
 * @author peter.gazdik
 */
public interface TreeReferee extends SqlAccessEntity {

	EntityType<TreeReferee> T = EntityTypes.T(TreeReferee.class);

	TreeBase getTreeEntity();
	void setTreeEntity(TreeBase treeEntity);

	Set<TreeBase> getTreeEntitySet();
	void setTreeEntitySet(Set<TreeBase> treeEntitySet);

	List<TreeBase> getTreeEntityList();
	void setTreeEntityList(List<TreeBase> treeEntityList);

	Map<Integer, TreeBase> getIntgerTreeEntityMap();
	void setIntgerTreeEntityMap(Map<Integer, TreeBase> intgerTreeEntityMap);

	Map<TreeBase, Integer> getTreeEntityIntegerMap();
	void setTreeEntityIntegerMap(Map<TreeBase, Integer> treeEntityIntegerMap);

}
