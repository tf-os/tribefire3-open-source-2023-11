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
package tribefire.extension.swagger.model_import.wire.space;

import com.braintribe.swagger.ConvertSwaggerModelProcessor;
import com.braintribe.swagger.ExportSwaggerModelProcessor;
import com.braintribe.swagger.ImportSwaggerModelProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * The Deployables Space of Swagger Import Module.
 * 
 */
@Managed
public class SwaggerModelImportDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	/**
	 * Import swagger model processor.
	 *
	 * @return the import swagger model processor
	 */
	@Managed
	public ImportSwaggerModelProcessor importSwaggerModelProcessor() {
		ImportSwaggerModelProcessor bean = new ImportSwaggerModelProcessor();
		return bean;
	}
	
	/**
	 * Export swagger model processor.
	 *
	 * @return the export swagger model processor
	 */
	@Managed
	public ExportSwaggerModelProcessor exportSwaggerModelProcessor() {
		ExportSwaggerModelProcessor bean = new ExportSwaggerModelProcessor();
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());
		return bean;
	}
	
	/**
	 * Convert swagger model processor.
	 *
	 * @return the convert swagger model processor
	 */
	@Managed
	public ConvertSwaggerModelProcessor convertSwaggerModelProcessor() {
		ConvertSwaggerModelProcessor bean = new ConvertSwaggerModelProcessor();
		return bean;
	}

}
