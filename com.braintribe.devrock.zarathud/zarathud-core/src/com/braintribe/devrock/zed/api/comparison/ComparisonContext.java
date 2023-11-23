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
package com.braintribe.devrock.zed.api.comparison;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.ComparisonProcessFocus;

/** 
 * a context used during the comparison process 
 * @author pit
 */
public interface ComparisonContext {

	/**
	 * eagerly add name of currently processed entity 
	 * @param name - the name of the entity 
	 */
	void addProcessed(String name);
	boolean isProcessed(String name);

	/**
	 * @param fp - add a {@link FingerPrint} to the context
	 */
	void addFingerPrint(FingerPrint fp);
	
	/**
	 * @return - the accumulated {@link FingerPrint}s of differences found while comparing
	 */
	List<FingerPrint> getFingerPrints();
	
	/**
	 * @return - the entity currently being processed
	 */
	GenericEntity getCurrentEntity();
	
	/**
	 * @param current - the {@link GenericEntity} to be the new current entity 
	 */
	void pushCurrentEntity( GenericEntity current);
	
	/**
	 * drop the last pushed entitiy  
	 */
	void popCurrentEntity();
	
	/**
	 * @return - the entity currently being compared to the current base 
	 */
	GenericEntity getCurrentOther();
	
	/**
	 * @param current - the {@link GenericEntity} to be the next comparison target
	 */
	void pushCurrentOther( GenericEntity current);	
	
	/**
	 * drop the last pushed compared-to entity 
	 */
	void popCurrentOther();

	/**
	 * @return - the current {@link ComparisonProcessFocus} to contextualize generic comparators 
	 */
	ComparisonProcessFocus getCurrentProcessFocus();
	
	/**
	 * @param focus - the next {@link ComparisonProcessFocus}
	 */
	void pushCurrentProcessFocus( ComparisonProcessFocus focus);
	
	/**
	 * drop the last pushed {@link ComparisonProcessFocus} 
	 */
	void popCurrentProcessFocus();
}