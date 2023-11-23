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
package com.braintribe.devrock.zarathud.model.reasons;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a reason to reflect inner scan errors of zed
 * @author pit
 *
 */
@SelectiveInformation("no matching URL found while scanning for ${scanExpression} during analysis of ${scannedType} within ${scannedResource}")
public interface UrlNotFound extends Reason {
		
	EntityType<UrlNotFound> T = EntityTypes.T(UrlNotFound.class);
	
	String scanExpression = "scanExpression";
	String scannedResource = "scannedResource";
	String scannedType = "scannedType";
	String combinedUrl = "combinedUrl";

	/**
	 * @return - the expression a resource was searched for
	 */
	String getScanExpression();
	void setScanExpression(String value);
	
	/**
	 * @return - the resource that contained the scan
	 */
	String getScannedResource();
	void setScannedResource(String value);
	
	/**
	 * @return - the type of the resource that was scanned
	 */
	String getScannedType();
	void setScannedType(String value);
	
	/**
	 * @return - the raw resource URL (contains both resource & type)
	 */
	String getCombinedUrl();
	void setCombinedUrl(String value);


}
