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
package tribefire.extension.spreadsheet.model.exchange.api.request;

import java.util.Map;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.spreadsheet.model.exchange.api.data.ImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.data.PushAddress;

@Abstract
public interface ImportSpreadsheetRequest extends SpreadsheetExchangeRequest {
	EntityType<ImportSpreadsheetRequest> T = EntityTypes.T(ImportSpreadsheetRequest.class);

	String sheet = "sheet";
	String useCase = "useCase";
	String targetType = "targetType";
	String lenient = "lenient";
	String enrichments = "enrichments";
	String statusMonitor = "statusMonitor";

	Resource getSheet();
	void setSheet(Resource sheet);

	@Description("Sets the explicit usecase to be used for metadata resolution.")
	String getUseCase();
	void setUseCase(String useCase);

	String getTargetType();
	void setTargetType(String targetType);

	boolean getLenient();
	void setLenient(boolean lenient);

	boolean getTransient();
	void setTransient(boolean lenient);

	Integer getStartRow();
	void setStartRow(Integer startRow);

	Integer getMaxRows();
	void setMaxRows(Integer maxRows);

	Object getBundleId();
	void setBundleId(Object id);

	Map<String, Object> getEnrichments();
	void setEnrichments(Map<String, Object> enrichments);

	PushAddress getStatusMonitor();
	void setStatusMonitor(PushAddress statusMonitor);

	// String updateExisting = "updateExisting";
	// boolean getUpdateExisting();
	// void setUpdateExisting(boolean updateExisting);

	@Override
	EvalContext<? extends ImportReport> eval(Evaluator<ServiceRequest> evaluator);
}
