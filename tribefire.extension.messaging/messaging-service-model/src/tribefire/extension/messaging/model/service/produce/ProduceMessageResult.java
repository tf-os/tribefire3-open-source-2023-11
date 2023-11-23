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
package tribefire.extension.messaging.model.service.produce;

import java.util.List;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.messaging.model.service.MessagingResult;

public interface ProduceMessageResult extends MessagingResult {

	EntityType<ProduceMessageResult> T = EntityTypes.T(ProduceMessageResult.class);

	String hadErrors = "hadErrors";
	String errors = "errors";

	List<Reason> getErrors();
	void setErrors(List<Reason> errors);

	boolean getHadErrors();
	void setHadErrors(boolean hadErrors);
	// TODO: maybe a better naming - unsatisfied, reason,...

	// -----------------------------------------------------------------------

	static ProduceMessageResult create(Reason reason) {
		ProduceMessageResult result = ProduceMessageResult.T.create();
		result.setHadErrors(true);
		result.setErrors(List.of(reason));
		return result;
	}
}
