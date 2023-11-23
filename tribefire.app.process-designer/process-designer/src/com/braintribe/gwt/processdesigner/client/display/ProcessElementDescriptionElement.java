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
package com.braintribe.gwt.processdesigner.client.display;

import java.util.List;

import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.model.descriptive.HasDescription;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;

public class ProcessElementDescriptionElement extends ProcessDesignerStatusBarElement{
	
	public ProcessElementDescriptionElement() {
		
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
	}

	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		List<ModelPath> selection = gmSelectionSupport.getCurrentSelection();
		if(selection != null && selection.size() == 1){
			for(ModelPath modelPath : selection){
				Object object = modelPath.last().getValue();
				if(object instanceof HasDescription){
					setName(((HasDescription)object).getDescription());
				}
			}
		}else
			setName("");
		
	}

	@Override
	public void handleDipose() {
		//NOP
	}

	@Override
	public void configure() {
		//NOP
	}
	
}
