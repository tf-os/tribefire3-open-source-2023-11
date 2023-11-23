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
package com.braintribe.model.exchangeapi;

import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface WriteToResourceRequest extends ExchangeRequest {
	
	EntityType<WriteToResourceRequest> T = EntityTypes.T(WriteToResourceRequest.class);
	
	@Initializer("true")
	boolean getPrettyOutput();
	void setPrettyOutput(boolean prettyOutput);
	
	@Initializer("true")
	boolean getStabilizeOrder();
	void setStabilizeOrder(boolean stabilizeOrder);

	boolean getWriteEmptyProperties();
	void setWriteEmptyProperties(boolean writeEmptyProperties);
	
	/**
	 * If set to true, this option will add the binaries for any resource of the exported assembly to the exchange-package file.
	 * This option only applies in case the exported assembly is an {@link ExchangePackage}.
	 */
	@Initializer("true")
	boolean getAddResourceBinaries();
	void setAddResourceBinaries(boolean addResourceBinaries);


}
