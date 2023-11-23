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
package tribefire.extension.tutorial.processing;

import java.util.function.Consumer;

import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.HttpStatusCodeNotification;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.utils.CommonTools;

import tribefire.extension.tutorial.model.api.request.LetterCaseTransformRequest;
import tribefire.extension.tutorial.model.api.request.TransformToLowerCase;
import tribefire.extension.tutorial.model.api.request.TransformToUpperCase;

public class LetterCaseTransformProcessor extends AbstractDispatchingServiceProcessor<LetterCaseTransformRequest, Object> {

	@Override
	protected void configureDispatching(DispatchConfiguration<LetterCaseTransformRequest, Object> dispatching) {

		//Preparing internal logic. Depending on which request is incoming, execute the proper method (maps the request to
		//its implementation)
		dispatching.register(TransformToLowerCase.T, this::transformToLowerCase);
		dispatching.register(TransformToUpperCase.T, this::transformToUpperCase);
	}

	private String transformToLowerCase(ServiceRequestContext context, TransformToLowerCase request) {
		
		//check if the incoming sentence is not null or empty - throw illegalArgumentException in such case
		//Otherwise return the lower-case sentence
		String sentence = request.getSentence();
		
		if(sentence == null) {
			
			throw new IllegalArgumentException("Sentence must not be null or empty!");			
			
		}
		
		if(sentence.isEmpty()) {
			
			Consumer<Integer> statusCodeNotification = context.findAspect(HttpStatusCodeNotification.class);
			if (statusCodeNotification != null)
				statusCodeNotification.accept(204);
		
		}
		
		return sentence.toLowerCase();
	
			
	}
	
	private String transformToUpperCase(ServiceRequestContext context, TransformToUpperCase request) {
		
		//check if the incoming sentence is not null or empty - throw illegalArgumentException in such case
		//Otherwise return the lower-case sentence
		String sentence = request.getSentence();
		
		if(CommonTools.isEmpty(sentence)) {
			
			throw new IllegalArgumentException("Sentence must not be null or empty!");
		}
		
		return sentence.toUpperCase();
	}
	
}
