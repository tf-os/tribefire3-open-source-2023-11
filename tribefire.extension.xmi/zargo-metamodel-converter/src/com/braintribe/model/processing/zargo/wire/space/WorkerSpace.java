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
package com.braintribe.model.processing.zargo.wire.space;


import java.util.function.Supplier;

import com.braintribe.model.processing.xmi.converter.experts.ClasspathResourceStringProvider;
import com.braintribe.model.processing.xmi.converter.wire.contract.XmiConverterContract;
import com.braintribe.model.processing.zargo.MetaModelToZargoConverterWorker;
import com.braintribe.model.processing.zargo.ZargoToMetaModelConverterWorker;
import com.braintribe.model.processing.zargo.wire.contract.WorkerContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * @author pit
 *
 */
@Managed
public class WorkerSpace implements WorkerContract {
	
	@Import
	XmiConverterContract xmiConverter;
	
	
	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	private Supplier<String> templateSupplier(String key) {
		ClasspathResourceStringProvider bean = new ClasspathResourceStringProvider(key);
		return bean;
	}

	@Override
	@Managed
	public MetaModelToZargoConverterWorker metaModelToZargoWorker() {
		MetaModelToZargoConverterWorker bean = new MetaModelToZargoConverterWorker();
		bean.setXmiToMetaModelCodec( xmiConverter.xmiConverter());
		
		bean.setArgoProvider( templateSupplier("templates/argo.argo"));
		bean.setProfileProvider( templateSupplier("templates/argo.profile"));
		bean.setToDoProvider( templateSupplier("templates/argo.todo"));
		bean.setDiagrammProvider( templateSupplier( "templates/argo.pgml"));
		
		return bean;
	}

	@Override
	@Managed
	public ZargoToMetaModelConverterWorker zargoToMetaModelWorker() {
		ZargoToMetaModelConverterWorker bean = new ZargoToMetaModelConverterWorker();
		bean.setXmiToMetaModelCodec( xmiConverter.xmiConverter());			
		return bean;
	}

}
