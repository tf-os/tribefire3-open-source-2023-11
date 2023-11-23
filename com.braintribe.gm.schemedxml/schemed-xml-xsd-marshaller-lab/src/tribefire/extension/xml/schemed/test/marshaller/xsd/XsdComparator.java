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
package tribefire.extension.xml.schemed.test.marshaller.xsd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;

public class XsdComparator {
	private DifferenceEvaluator differenceEvaluator;
	
	
	

	public XsdComparator() { 
		differenceEvaluator = new IgnoreAttributeDifferenceEvaluator( getOptionalAttributeMap());
	}
	
	private Map<String, String> getOptionalAttributeMap() {
		Map<String, String> map = new HashMap<>();
		map.put( "attributeFormDefault", "unqualified");
		map.put( "elementFormDefault", "unqualified");
		map.put("minOccurs", "1");
		map.put("maxOccurs", "1");
		
		return map;
	}

	private class IgnoreAttributeDifferenceEvaluator implements DifferenceEvaluator {
	    private Map<String, String> attrToValueMap;;
	    
	    public IgnoreAttributeDifferenceEvaluator(Map<String, String> map) {
	        this.attrToValueMap = map;
	    }
	     
	    @Override
	    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
	        if (outcome == ComparisonResult.EQUAL)
	            return outcome;
	        
	        final Node controlNode = comparison.getControlDetails().getTarget();
	        final Node testNode = comparison.getTestDetails().getTarget();
	        
	        if (testNode instanceof Attr) {
	            Attr attr = (Attr) testNode;
	            String name = attr.getName();
				String controlValue = attrToValueMap.get( name);				
				System.out.println("[" + name + "]:[" + controlValue + "] -> [" + attr.getValue() + "]");	            
	        }
	        return outcome;
	    }
	}
	
	
	public Diff compare( File in, File out) {
	     
        Diff myDiff = DiffBuilder.compare( in).withDifferenceEvaluator( differenceEvaluator ).withTest( out).checkForSimilar().build();
        return myDiff;
             	       
}
}
