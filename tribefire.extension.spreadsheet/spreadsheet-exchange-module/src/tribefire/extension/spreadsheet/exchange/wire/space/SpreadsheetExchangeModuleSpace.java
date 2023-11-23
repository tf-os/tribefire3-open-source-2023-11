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
package tribefire.extension.spreadsheet.exchange.wire.space;

import com.braintribe.cartridge.common.processing.accessrequest.InternalizingAccessRequestProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.spreadsheet.model.exchange.api.request.SpreadsheetExchangeRequest;
import tribefire.extension.spreadsheet.processing.service.SpreadsheetExchangeProcessor;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class SpreadsheetExchangeModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract moduleResources;

	@Override
	public void bindHardwired() {
		// Bind hardwired deployables here.
		tfPlatform.hardwiredDeployables()
				.bindOnNewServiceDomain("domain.spreadsheet.exchange", "Spreadsheet Service Domain") //
				.model("tribefire.extension.spreadsheet:spreadsheet-exchange-api-model") //
				.serviceProcessor( //
						"processor.spreadsheet.exchange", //
						"Spreadsheet Exchange Processor", //
						SpreadsheetExchangeRequest.T, //
						this::spreadsheetExchangeServiceProcessor) //
				.please();
	}

	@Managed
	private InternalizingAccessRequestProcessor<SpreadsheetExchangeRequest, Object> spreadsheetExchangeServiceProcessor() {
		return new InternalizingAccessRequestProcessor<SpreadsheetExchangeRequest, Object>(
				spreadsheetExchangeProcessor(), tfPlatform.requestUserRelated().sessionFactory(),
				tfPlatform.systemUserRelated().sessionFactory());
	}

	@Managed
	private SpreadsheetExchangeProcessor spreadsheetExchangeProcessor() {
		SpreadsheetExchangeProcessor bean = new SpreadsheetExchangeProcessor();
		bean.setEngines(tfPlatform.scripting().engines());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());
		return bean;
	}

}
