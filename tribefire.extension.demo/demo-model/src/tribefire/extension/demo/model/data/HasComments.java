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
package tribefire.extension.demo.model.data;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Abstract type that can be sub-classed by entities having comments assigned.
 * <br />
 * In real-life a Comment probably would be a complex entity with additional
 * information like Date, Author, ...<br />
 * To demonstrate a UseCase of a simple type (String) collection we reduced the
 * complexity.
 */
@Abstract
public interface HasComments extends GenericEntity {

	
	final EntityType<HasComments> T = EntityTypes.T(HasComments.class);
	
	/*
	 * Constants for each property name.
	 */
	public static final String comments = "comments";

	/**
	 * The natural ordered list of (simplified) comments.
	 */
	void setComments(List<String> comments);

	List<String> getComments();

}
