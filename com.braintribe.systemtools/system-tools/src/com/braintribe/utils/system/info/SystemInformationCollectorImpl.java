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
package com.braintribe.utils.system.info;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.system.SystemTools;

public class SystemInformationCollectorImpl implements SystemInformationCollector {
	
	public static String lineSeparator = System.getProperty("line.separator");

	protected SystemTools systemTools = null;
	
	@Override
	public String collectSystemInformation() {

		StringBuilder sb = new StringBuilder();

		String os = SystemTools.getOperatingSystem();
		sb.append("Operating System: ");
		sb.append(os);
		sb.append(lineSeparator);

		int availableProcessors = SystemTools.getAvailableProcessors();
		sb.append("Available Processors: ");
		sb.append(availableProcessors);
		sb.append(lineSeparator);

		String detailedProcessorInformation = this.systemTools.getDetailedProcessorInformation();
		if ((detailedProcessorInformation != null) && (detailedProcessorInformation.trim().length() > 0)) {
			sb.append("Detailed Processor Information:");
			sb.append(lineSeparator);
			sb.append(detailedProcessorInformation);
			sb.append(lineSeparator);
		}

		long freeMemory = SystemTools.getFreeMemory();
		long totalMemory = SystemTools.getTotalMemory();
		sb.append("Free Memory: ");
		sb.append(SystemTools.prettyPrintBytes(freeMemory));
		sb.append(" (");
		sb.append(freeMemory);
		sb.append(')');
		sb.append(lineSeparator);

		sb.append("Total Memory: ");
		sb.append(SystemTools.prettyPrintBytes(totalMemory));
		sb.append(" (");
		sb.append(totalMemory);
		sb.append(')');
		sb.append(lineSeparator);

		String fileSystemInformation = SystemTools.getFileSystemInformation();
		sb.append("File Systems:");
		sb.append(lineSeparator);
		sb.append(fileSystemInformation);
		sb.append(lineSeparator);

		return sb.toString();
	}

	@Required
	@Configurable
	public void setSystemTools(SystemTools systemTools) {
		this.systemTools = systemTools;
	}


}
