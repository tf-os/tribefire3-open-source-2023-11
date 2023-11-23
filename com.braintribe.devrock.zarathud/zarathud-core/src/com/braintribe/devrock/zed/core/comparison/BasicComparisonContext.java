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
package com.braintribe.devrock.zed.core.comparison;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.braintribe.devrock.zed.api.comparison.ComparisonContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.ComparisonProcessFocus;

/**
 * basic internal context for all comparators
 * 
 * @author pit
 */
public class BasicComparisonContext implements ComparisonContext {	
	private Set<String> processed = new HashSet<>();
	private List<FingerPrint> fingerPrints = new ArrayList<>();
	
	private Stack<GenericEntity> processing = new Stack<>();
	private Stack<GenericEntity> comparing = new Stack<>();
	
	private Stack<ComparisonProcessFocus> focus = new Stack<>(); 
	
	@Override
	public void addProcessed( String name) {
		processed.add(name);
	}
	
	@Override
	public boolean isProcessed( String name) {
		return processed.contains(name);
	}
	
	@Override
	public void addFingerPrint( FingerPrint fp) {
		GenericEntity entitySource = fp.getEntitySource();
		if (entitySource != null) {
			if (entitySource instanceof ZedEntity) {
				ZedEntity ze = (ZedEntity) entitySource;
				if (ze instanceof AnnotationEntity == false && ze.getDefinedInTerminal() == false) {
					System.out.println("Fingerprint on entity not defined in terminal?");
					//return;
				}
			}
		}
		fingerPrints.add(fp);
	}
	
	@Override
	public List<FingerPrint> getFingerPrints() {
		return fingerPrints;
	}
	
	@Override
	public GenericEntity getCurrentEntity() {	
		return processing.isEmpty() ? null : processing.peek();	
	}

	@Override
	public void pushCurrentEntity(GenericEntity current) {	
		processing.push(current);		
	}

	@Override
	public void popCurrentEntity() {		
		processing.pop();			
	}
	
	@Override
	public GenericEntity getCurrentOther() {		
		return comparing.peek();
	}

	@Override
	public void pushCurrentOther(GenericEntity current) {
		comparing.push(current);
		
	}

	@Override
	public void popCurrentOther() {
		comparing.pop();
	}

	@Override
	public ComparisonProcessFocus getCurrentProcessFocus() {	
		return focus.peek();
	}		
	@Override
	public void pushCurrentProcessFocus(ComparisonProcessFocus focus) {
		this.focus.push(focus);
		
	}

	@Override
	public void popCurrentProcessFocus() {
		focus.pop();
		
	}
	
}
