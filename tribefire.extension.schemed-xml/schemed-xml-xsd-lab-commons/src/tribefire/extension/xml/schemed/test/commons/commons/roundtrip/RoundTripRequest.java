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
package tribefire.extension.xml.schemed.test.commons.commons.roundtrip;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RoundTripRequest extends GenericEntity {
	
	final EntityType<RoundTripRequest> T = EntityTypes.T(RoundTripRequest.class);

	void setWorkingDirectoryname(String workingDirectoryname);
	String getWorkingDirectoryname();
	
	void setXmlFilenames( List<String> xmlFilenames);
	List<String> getXmlFilenames();
	
	void setXsdFilename( String xsdFilename);
	String getXsdFilename();
	
	void setModelFilename( String modelFilename);
	String getModelFilename();
	
	void setRepeats(int repeats);
	@Initializer("1")
	int getRepeats();
	
	void setMeasuringThreshold( int threshold);
	@Initializer("0")
	int getMeasuringThreshold();
	
}
