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
package com.braintribe.model.wopi;

import java.util.Date;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Document lock information
 * 
 * @see <a href= "https://wopi.readthedocs.io/projects/wopirest/en/latest/files/Lock.html">WOPI Locking mechanism</a>
 * 
 *
 */
@SelectiveInformation("${lock}/${creationDate}")
public interface WopiLock extends StandardIdentifiable {

	EntityType<WopiLock> T = EntityTypes.T(WopiLock.class);

	String lock = "lock";
	String creationDate = "creationDate";

	@Name("Lock")
	@Description("WOPI Lock information.")
	@Mandatory
	@Unmodifiable
	String getLock();
	void setLock(String lock);

	@Name("Creation Date")
	@Description("Creation Date of the WOPI lock.")
	@Mandatory
	@Unmodifiable
	Date getCreationDate();
	void setCreationDate(Date creationDate);

}
