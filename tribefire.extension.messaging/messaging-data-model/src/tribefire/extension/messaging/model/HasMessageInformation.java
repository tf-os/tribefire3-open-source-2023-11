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
package tribefire.extension.messaging.model;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface HasMessageInformation extends GenericEntity {

	EntityType<HasMessageInformation> T = EntityTypes.T(HasMessageInformation.class);

	String timestamp = "timestamp";
	String nanoTimestamp = "nanoTimestamp";
	String context = "context";

	@Name("Timestamp")
	@Description("Timestamp as a Date")
	Date getTimestamp();
	void setTimestamp(Date timestamp);

	@Name("Nano Timestamp")
	@Description("Timestamp as a long in ns")
	Long getNanoTimestamp();
	void setNanoTimestamp(Long nanoTimestamp);

	@Name("Context")
	@Description("Context which links multiple messages together")
	String getContext();
	void setContext(String context);
}
