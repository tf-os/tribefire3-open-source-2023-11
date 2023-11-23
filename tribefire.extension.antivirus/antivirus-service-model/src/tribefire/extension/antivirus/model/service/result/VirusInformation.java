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

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 *
 */
public interface VirusInformation extends AntivirusResult {

	EntityType<? extends VirusInformation> T = EntityTypes.T(VirusInformation.class);

	String details = "details";
	String numberInfectedResources = "numberInfectedResources";
	String durationInMs = "durationInMs";

	@Name("Details")
	@Description("Detailed information about scan runs")
	@Unmodifiable
	List<AbstractAntivirusResult> getDetails();
	void setDetails(List<AbstractAntivirusResult> details);

	@Name("Number Of Infected Resources")
	@Unmodifiable
	long getNumberInfectedResources();
	void setNumberInfectedResources(long numberInfectedResources);

	@Name("Duration")
	@Description("Total duration in ms")
	@Unmodifiable
	long getDurationInMs();
	void setDurationInMs(long durationInMs);
}
