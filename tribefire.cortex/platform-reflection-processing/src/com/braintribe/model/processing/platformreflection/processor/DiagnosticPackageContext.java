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
package com.braintribe.model.processing.platformreflection.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.model.platformreflection.Healthz;
import com.braintribe.model.platformreflection.PackagingInformation;
import com.braintribe.model.platformreflection.tf.DeployablesInfo;
import com.braintribe.model.resource.Resource;

public class DiagnosticPackageContext {

	protected String threadDump;
	protected String platformReflectionJson;
	protected String hotThreads;
	protected Healthz healthz;
	protected String processesJson;
	protected File heapDump;
	protected String heapDumpFilename;
	protected File logs;
	protected String logsFilename;
	protected PackagingInformation packagingInformation;
	protected DeployablesInfo deployablesInfo;
	protected Resource setupDescriptorResource;
	protected File configurationFolderAsZip;
	protected String configurationFolderAsZipFilename;
	protected File modulesFolderAsZip;
	protected String modulesFolderAsZipFilename;
	protected File sharedStorageAsZip;
	protected String sharedStorageAsZipFilename;
	protected File accessDataFolderAsZip;
	protected String accessDataFolderAsZipFilename;
	protected String setupAssetsAsJson;
	protected List<String> errors = Collections.synchronizedList(new ArrayList<>());

}
