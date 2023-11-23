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
package tribefire.extension.elasticsearch.model.api.request.doc;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Condition;

@Abstract
public interface Comparison extends Condition {

	EntityType<Comparison> T = EntityTypes.T(Comparison.class);

	String filter = "filter";
	String matchPhrase = "matchPhrase";
	String priority = "priority";

	@Priority(0.79d)
	@Name("Filter")
	@Description("Filter.")
	boolean getFilter();
	void setFilter(boolean filter);

	@Priority(0.78d)
	@Name("Match Phrase")
	@Description("Match Phrase.")
	boolean getMatchPhrase();
	void setMatchPhrase(boolean matchPhrase);

	@Priority(0.7d)
	@Name("Priority")
	@Description("Priority.")
	String getPriority();
	void setPriority(String priority);

}
