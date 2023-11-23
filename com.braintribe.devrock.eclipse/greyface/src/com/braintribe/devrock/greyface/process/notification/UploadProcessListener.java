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
package com.braintribe.devrock.greyface.process.notification;

import java.util.Set;

import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

/**
 * listener for upload process 
 * @author pit
 *
 */
public interface UploadProcessListener {
	/**
	 * upload begins 
	 * @param setting - the current target 
	 * @param count - the number of parts to be loaded (overall, all solutions combined)
	 */
	void acknowledgeUploadBegin( RepositorySetting setting, int count);
	
	/**
	 * upload ended	
	 */
	void acknowledgeUploadEnd( RepositorySetting setting);
	
	/**
	 * root solution 
	 * @param setting - target 
	 * @param solution - the solution that represents a root of a scan  
	 */
	void acknowledgeRootSolutions( RepositorySetting setting, Set<Solution> solution);
	
	/**
	 * solution's upload begins 
	 */
	void acknowledgeUploadSolutionBegin( RepositorySetting setting, Solution solution);
	
	/**
	 * solution's upload ends without any failures 
	 */
	void acknowledgeUploadSolutionEnd( RepositorySetting setting, Solution solution);
	
	/**
	 * solution's upload ends with some failures 	
	 */
	void acknowledgeUploadSolutionFail( RepositorySetting setting, Solution solution);
	
	/**
	 * successfully uploaded a part 
	 * @param setting - the target {@link RepositorySetting}
	 * @param solution - the owning {@link Solution}
	 * @param part - the {@link Part} uploaded 
	 * @param time - the time it took to upload (ms)
	 * @param index - the index of the part in the overall list of parts to upload 
	 */
	void acknowledgeUploadedPart( RepositorySetting setting, Solution solution, Part part, long time, int index);
	
	/**
	 * failed to upload a part 
	 * @param setting - the target {@link RepositorySetting}
	 * @param solution - the owning {@link Solution}
	 * @param part - the {@link Part} uploaded 
	 * @param reason - the reason why it failed 
	 * @param index - the index of the part in the overall list of parts to upload 
	 */
	void acknowledgeFailedPart( RepositorySetting setting, Solution solution, Part part, String reason, int index);
	
	
	/**
	 * failed to validate a part either via MD5 or SHA1
	 * @param setting - the target {@link RepositorySetting}
	 * @param solution - the owning {@link Solution}
	 * @param part - the {@link Part} uploaded 
	 * @param reason - the reason why it failed 
	 * @param index - the index of the part in the overall list of parts to upload
	 */
	void acknowledgeFailPartCRC( RepositorySetting setting, Solution solution, Part part, String reason, int index);
		
}
