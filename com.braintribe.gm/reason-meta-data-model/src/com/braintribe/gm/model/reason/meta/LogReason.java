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
package com.braintribe.gm.model.reason.meta;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * This metadata can be placed on subtypes of com.braintribe.gm.model.reason.Reason
 * to activate logging of Reasons that occur in an endpoint.
 * The Reason will be logged with the given {@link #getLevel() level}.
 * 
 * @author Dirk Scheffler
 */
public interface LogReason extends EntityTypeMetaData {
	EntityType<LogReason> T = EntityTypes.T(LogReason.class);

	String level = "level";
	String recursive = "recursive";
	
	LogLevel getLevel();
	void setLevel(LogLevel level);
	
	@Initializer("true")
	boolean getRecursive();
	void setRecursive(boolean recursive);
}
