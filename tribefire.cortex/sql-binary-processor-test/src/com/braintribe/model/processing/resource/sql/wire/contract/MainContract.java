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
package com.braintribe.model.processing.resource.sql.wire.contract;

import java.util.Map;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.resource.sql.JdbcSqlBinaryProcessor;
import com.braintribe.model.processing.resource.sql.common.ProcessorConfig;
import com.braintribe.model.processing.resource.sql.common.TestFile;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.space.WireSpace;

public interface MainContract extends WireSpace {

	IncrementalAccess access();

	ProcessorConfig simpleSqlBinaryProcessorConfig();

	JdbcSqlBinaryProcessor simpleSqlBinaryProcessor();

	ProcessorConfig enrichingSqlBinaryProcessorConfig();

	JdbcSqlBinaryProcessor enrichingSqlBinaryProcessor() throws Exception;

	Map<String, TestFile> testFiles();
	
	Evaluator<ServiceRequest> evaluator();

}
