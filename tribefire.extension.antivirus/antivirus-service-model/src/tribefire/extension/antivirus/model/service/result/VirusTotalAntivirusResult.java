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
package tribefire.extension.antivirus.model.service.result;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 * VirusTotal Provider result
 * 
 *
 */
public interface VirusTotalAntivirusResult extends AbstractAntivirusResult {

	EntityType<? extends VirusTotalAntivirusResult> T = EntityTypes.T(VirusTotalAntivirusResult.class);

	@Name("Permalink")
	@Description("Permalink")
	@Unmodifiable
	String getPermalink();
	void setPermalink(String permalink);
	
	@Name("ScanDate")
	@Description("Scan date")
	@Unmodifiable
	String getScanDate();
	void setScanDate(String scanDate);
	
	@Name("ResourceID")
	@Description("The id of the scanned resource")
	@Unmodifiable
	String getResourceID();
	void setResourceID(String resourceID);
	
	@Override
	default String getDetails() {
		return String.format("Permalink: %s, Scan date: %s, Resource id: %s", getPermalink(), getScanDate(), getResourceID());						
	}
}
