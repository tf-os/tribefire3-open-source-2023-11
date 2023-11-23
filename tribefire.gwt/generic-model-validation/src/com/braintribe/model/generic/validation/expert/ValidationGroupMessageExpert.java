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
package com.braintribe.model.generic.validation.expert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.validation.ValidatorResult;

public class ValidationGroupMessageExpert {
	
	public static List<ValidatorResult> prepareGroupValidatorResult(List<ValidatorResult> listValidatorResult) {
		if (listValidatorResult == null)
			return null;
		
		List<ValidatorResult> result = null;

		Map<String,List<ValidatorResult>> mapByPropertyName = new HashMap<>();
		
		//split by propertyName
        for (ValidatorResult validatorResult : listValidatorResult) {
        	if (validatorResult == null || validatorResult.getPropertyName() == null || validatorResult.getPropertyName().isEmpty()) 
        		continue;

        	List<ValidatorResult> listPropertyValidatorResult = mapByPropertyName.get(validatorResult.getPropertyName());
        	if (listPropertyValidatorResult == null)
        		listPropertyValidatorResult = new ArrayList<>();
        	
        	listPropertyValidatorResult.add(validatorResult);
        	mapByPropertyName.put(validatorResult.getPropertyName(), listPropertyValidatorResult);
        }
		
        if (mapByPropertyName.isEmpty())
        	return null;
        
        result = new ArrayList<>();
        for (String propertyName : mapByPropertyName.keySet()) {        
			String prior1 = null;
			String prior2 = null;
			String prior3 = null;
			
			List<Manipulation> listManipulation = new ArrayList<>();
			
	        for (ValidatorResult validatorResult : mapByPropertyName.get(propertyName)) {
	        		switch(validatorResult.getMessageType()) {
	        		  case mandatory:
	          			prior1 = LocalizedText.INSTANCE.stringDenyEmpty();
	        		    break;
	        		  case regex:
	           			prior2 = LocalizedText.INSTANCE.stringRegexpMessage();
	        		    break;
	        		  case greatherThan:
	             		prior2 = LocalizedText.INSTANCE.stringGreatherThan(validatorResult.getMessageParameter());
	          		    break;
	        		  case greatherEqual:
	             		prior2 = LocalizedText.INSTANCE.stringGreatherEqual(validatorResult.getMessageParameter());
	          		    break;
	        		  case greatherEqualLength:
		             		prior2 = LocalizedText.INSTANCE.stringGreatherEqualLength(validatorResult.getMessageParameter());
		          		    break;
	        		  case lessThan:
	        			  prior3 = LocalizedText.INSTANCE.stringLesserThan(validatorResult.getMessageParameter());
	            		   break;
	        		  case lessEqual:
	        			  prior3 = LocalizedText.INSTANCE.stringLesserEqual(validatorResult.getMessageParameter());
	          		    break;
	        		  case lessEqualLength:
	        			  prior3 = LocalizedText.INSTANCE.stringLesserEqualLength(validatorResult.getMessageParameter());
	          		    break;
					default:
						break;        		    
	        		}  
	        		listManipulation.addAll(validatorResult.getListManipulation());
	        }
	        
	        if (prior1 == null && prior2 == null && prior3 == null) {
	        	return null;
	        }
	
	        StringBuilder sb = new StringBuilder();
	        if (prior1 != null) {
	        	sb.append(prior1);
	        	if (prior2 != null || prior3 != null)
	        		sb.append(" ").append(LocalizedText.INSTANCE.stringAnd()).append(" ");
	        	
	        } else {
	        	sb.append(LocalizedText.INSTANCE.stringAllowEmpty());
	        	if (prior2 != null || prior3 != null)
	        		sb.append(" ").append(LocalizedText.INSTANCE.stringOr()).append(" ");
	        }
	        
	        if (prior2 != null) {
	        	sb.append(prior2);
	        	if (prior3 != null)
	        		sb.append(" ").append(LocalizedText.INSTANCE.stringAnd()).append(" ");        		
	        }
	        
	        if (prior3 != null) 
	        	sb.append(prior3);        
	        
	        ValidatorResult groupValidatorResult = new ValidatorResult();
	        groupValidatorResult.setPropertyName(propertyName);
	        groupValidatorResult.setResult(false);
	        groupValidatorResult.setResultMessage(sb.toString());
	        groupValidatorResult.getListManipulation().addAll(listManipulation);
	        result.add(groupValidatorResult); 
        }
		return result;
	}
}
