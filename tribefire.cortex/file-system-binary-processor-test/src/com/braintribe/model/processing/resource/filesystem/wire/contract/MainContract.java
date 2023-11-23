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
package com.braintribe.model.processing.resource.filesystem.wire.contract;

import java.nio.file.Path;
import java.util.Map;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.processing.resource.filesystem.common.ProcessorConfig;
import com.braintribe.model.processing.resource.filesystem.common.TestFile;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.provider.Hub;
import com.braintribe.wire.api.space.WireSpace;

public interface MainContract extends WireSpace {

	Hub<Path> tempPathHolder();

	IncrementalAccess access1();

	IncrementalAccess access2();

	Path access1Path();

	Path access2Path();

	ProcessorConfig simpleFileSystemBinaryProcessorConfig();

	FileSystemBinaryProcessor simpleFileSystemBinaryProcessor();

	ProcessorConfig enrichingFileSystemBinaryProcessorConfig();

	FileSystemBinaryProcessor enrichingFileSystemBinaryProcessor();

	Map<String, TestFile> testFiles();
	
	Evaluator<ServiceRequest> evaluator();

}
