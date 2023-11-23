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
package tribefire.extension.hibernate.lab.model.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface Movie extends GenericEntity {

	EntityType<Movie> T = EntityTypes.T(Movie.class);

	String getTitle();
	void setTitle(String title);

	Director getDirector();
	void setDirector(Director director);

	Set<Actor> getActorSet();
	void setActorSet(Set<Actor> actorSet);

	List<Actor> getActorList();
	void setActorList(List<Actor> actorList);

	Map<String, Actor> getActorMap();
	void setActorMap(Map<String, Actor> actorMap);

}
