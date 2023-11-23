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
package com.braintribe.model.accessdeployment.smart.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.QualifiedProperty;

/**
 * This is a mapping for an entity property (or a set of entities), which in the delegate are represented by a simple
 * value, which is actually a key for that given entity (i.e. is unique and mandatory and unambiguously identifies the
 * entity).
 * 
 * <p>
 * Example 1: {@code
 * 
 *  Folder 
 *  - Long id
 *  - Long parentId
 * 
 *  SmartFolder
 *  - Long id
 *  - SmartFolder parent
 *  
 *  MD for SmartFolder.parent:
 *  	qualfiedProperty -> Folder.parentId
 *  	keyProperty -> Folder.id
 *  
 *  }
 * 
 * <p>
 * 
 * Example 2: {@code
 * 
 *  Book
 *  - Long id
 *  - String isbn
 *  
 *  Person
 *  - Long id
 *  - String favouriteBookIsbn
 *  
 *  --------------------------------------------------------------
 *  
 *  SmartBook
 *  - Long id
 *  - String isbn
 *  
 *  SmartPerson
 *  - Long id
 *  - SmartBook favouriteBook
 *  
 *  --------------------------------------------------------------

 *  SmartPerson.favouriteBook mapped with KeyPropertyAssignment:
 *  	property -> Person.favouriteBookIsbn
 *  	keyProperty -> Book.isbn
 *  
 *  }
 * 
 * @see InverseKeyPropertyAssignment
 */
public interface KeyPropertyAssignment extends PropertyAssignment {

	EntityType<KeyPropertyAssignment> T = EntityTypes.T(KeyPropertyAssignment.class);

	ConvertibleQualifiedProperty getProperty();
	void setProperty(ConvertibleQualifiedProperty property);

	/**
	 * This addresses the property of either the property type of the meta data owner property if this is an entity type
	 * already or the element type of the collection property type of the meta data owner property. The property
	 * {@link #setProperty(ConvertibleQualifiedProperty)} from DistinctPropertyAssignment addresses the simple or simple
	 * collection type property that is used as correlation.
	 */
	QualifiedProperty getKeyProperty();
	void setKeyProperty(QualifiedProperty keyProperty);

	/**
	 * {@linkplain KeyPropertyAssignment} can also be configured to join two entities from the same access. In such a
	 * case the SmartAccess delegates the joining given by this assignment. This might, however, be unwanted, e.g. in
	 * case the delegate access does not support joins. In such case this flag should be set to true to prevent
	 * SmartAccess from delegating the join and doing it itself.
	 */
	boolean getForceExternalJoin();
	void setForceExternalJoin(boolean forceExternalJoin);

}
