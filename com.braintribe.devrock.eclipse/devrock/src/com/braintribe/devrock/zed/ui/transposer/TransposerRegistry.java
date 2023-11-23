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
package com.braintribe.devrock.zed.ui.transposer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;
import com.braintribe.zarathud.model.forensics.data.ModelPropertyReference;

/**
 * allows the fingerprints as returned from the forensics to be correlated with the data as extracted 
 * 
 *  1. build a fingerprint for each data-element, omit the issue
 *  2. put that into a map 
 *  3. iterate through all fingerprints and search for matches (ignore the issue)
 *  4. put the original fingerprint and the matched data-element into a new map
 * @author pit
 *
 */
public class TransposerRegistry implements HasFingerPrintTokens{
	private Map<FingerPrint, GenericEntity> cache = new HashMap<>();
	
	
	/**
	 * creates and instruments a functional {@link TransposerRegistry}
	 * @param fingerPrints - the {@link FingerPrint}s from forensics
	 * @param data - the {@link GenericEntity} from the extraction
	 */
	public TransposerRegistry( List<FingerPrint> fingerPrints, List<GenericEntity> data) {
	
		// build a map of the data with a derived fingerprint 
		Map<FingerPrint, GenericEntity> map = new HashMap<>(fingerPrints.size());
		
		for (GenericEntity ge : data) {
			System.out.println(ge.entityType().getTypeSignature());
			if (ge instanceof ModelEntityReference) {
				ModelEntityReference mer = (ModelEntityReference) ge;
				FingerPrint fp = FingerPrintExpert.build(mer);
				map.put(fp, mer);
			}
			else if (ge instanceof ModelPropertyReference) {
				ModelPropertyReference mpr = (ModelPropertyReference) ge;
				FingerPrint fp = FingerPrintExpert.build(mpr);
				map.put(fp, mpr);
			}
			else if (ge instanceof ModelEnumReference){
				ModelEnumReference ee = (ModelEnumReference) ge;
				FingerPrint fp = FingerPrintExpert.build(ee);
				map.put(fp, ee);
			}
			else if (ge instanceof MethodEntity) {
				MethodEntity me = (MethodEntity) ge;
				FingerPrint fp = FingerPrintExpert.build(me);
				map.put(fp, me);						
			}
			else if (ge instanceof FieldEntity) {
				FieldEntity fe = (FieldEntity) ge;
				FingerPrint fp = FingerPrintExpert.build(fe);
				map.put(fp, fe);
			}
		}
		// use the temp fingerprints to correlate  
		for (FingerPrint fp : fingerPrints) {
			if (fp == null) {
				System.out.println("null key");
				continue;
			}
			
			// key finger print have matches, but not the created ones, hence the comparison here may not process the 'issue' slot.
			FingerPrint key = FingerPrintExpert.from((FingerPrint) fp, ISSUE);
		
			FingerPrintFilter fpf = new FingerPrintFilter( key);
			List<FingerPrint> matches = map.keySet().stream().filter( fpf).collect(Collectors.toList());
			if (matches.size() > 1) {
				System.out.println("more than one result for : " + FingerPrintExpert.toString(fp));
			}
			else if (matches.size() == 0) {
				System.out.println("no matches for : " + FingerPrintExpert.toString(fp));
			}
			else {
				GenericEntity match = map.get(matches.get(0));
				cache.put(fp, match);
			}			
		}		
	}
	
	/**
	 * returns the {@link GenericEntity} extracted associated with the fingerprint
	 * @param fp - the {@link FingerPrint}
	 * @return - the {@link GenericEntity} associatged
	 */
	public GenericEntity getMatch( FingerPrint fp) {
		return cache.get(fp);
	}
	
	
	 
	
}
