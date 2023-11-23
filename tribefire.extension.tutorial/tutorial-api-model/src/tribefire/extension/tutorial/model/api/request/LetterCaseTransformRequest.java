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
package tribefire.extension.tutorial.model.api.request;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

@Abstract
public interface LetterCaseTransformRequest extends AuthorizedRequest {
	
	EntityType<LetterCaseTransformRequest> T = EntityTypes.T(LetterCaseTransformRequest.class);

	@Mandatory
	String getSentence();
	void setSentence(String sentence);
	
	@Override
	EvalContext<String> eval(Evaluator<ServiceRequest> evaluator);
	
	/** From a modeling point of view one always needs to design the architecture in 
	 * terms of "thinking as abstract as possible and as concrete as necessary" 
	 * and "avoiding redundancies" and "bundle commonalities" - in our example 
	 * both concrete requests have a sentence as an input, and both requests return a String
	 *  -> that is why we can put the sentence property as well as the eval method (response) 
	 *  to the abstract base type.
	*/

	
}
