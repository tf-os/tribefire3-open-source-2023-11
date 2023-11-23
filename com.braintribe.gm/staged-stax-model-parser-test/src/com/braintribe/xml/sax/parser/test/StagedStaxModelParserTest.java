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
package com.braintribe.xml.sax.parser.test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.xml.sax.parser.test.model.Container;
import com.braintribe.xml.sax.parser.test.model.Grouping;
import com.braintribe.xml.stagedstax.parser.ContentHandler;
import com.braintribe.xml.stagedstax.parser.StagedStaxModelParser;
import com.braintribe.xml.stagedstax.parser.registry.ContentExpertRegistry;

public class StagedStaxModelParserTest {
	private static final String PROCESSION_INSTRUCTION = "blabla";
	private static final int INT_VALUE = 23;
	private static final String STRING_VALUE = "This is a string";
	private static final Grouping GROUPING = Grouping.model;
	
	private static final String ENTRY_VALUE_1 = "entry 1";
	private static final String ENTRY_AUTO_1 = "auto 1";
	private static final String ENTRY_ENRICHED_1 = "enriched 1";
	
	private static final String ENTRY_VALUE_2 = "entry 2";
	private static final String ENTRY_AUTO_2 = "auto 2";
	private static final String ENTRY_ENRICHED_2 = "enriched 2";
	
	private File contents = new File( "res/parse");

	
	private <T> T test(File file, ContentExpertRegistry registry) {
		try {
			
			ContentHandler<T> handler = new ContentHandler<T>();								
			handler.setRegistry(registry);				
			
			StagedStaxModelParser parser = new StagedStaxModelParser();
			parser.read( file, handler);
						
			return handler.getResult();						
					
		} catch (Exception e) {
			Assert.fail("exception [" + e.getMessage() + "] thrown in processing");
			return null;
		}
	}
	
	@Test
	public void testSimple() {
		StagedStaxParserTestRegistry registry = new StagedStaxParserTestRegistry();
		Container container = test( new File( contents, "simpleParseModelTest.xml"), registry);		
	
	
		Assert.assertTrue("container.string expected as [" + STRING_VALUE + "], found [" + container.getStringValue() + "]", STRING_VALUE.equalsIgnoreCase(container.getStringValue()));
		Assert.assertTrue("container.boolean expected as [" + true + "], found [" + container.getBooleanValue() + "]", Boolean.TRUE.equals(container.getBooleanValue()));
		Assert.assertTrue("container.intValue expected as [" + INT_VALUE + "], found [" + container.getIntegerValue() + "]", 23 == container.getIntegerValue());
	
		// set
		Map<String, Boolean> foundMap = new HashMap<>();
		foundMap.put( "S1", false);
		foundMap.put( "S2", false);
		foundMap.put( "S3", false);
		Set<String> stringSet = container.getStringSet();
		validateStringSet(foundMap, stringSet);
		
		// list
		String [] values = new String[] {"L1", "L2", "L3"};
		List<String> stringList = container.getStringList();
		validateStringList(values, stringList);
		
		//Assert.assertTrue("container.instruction expected as [" + PROCESSION_INSTRUCTION + "], found [" + container.getProcessingInstruction() + "]", PROCESSION_INSTRUCTION.equalsIgnoreCase(container.getProcessingInstruction()));
		//Assert.assertTrue("container.grouping expected as [" + GROUPING + "], found [" + container.getGrouping() + "]", GROUPING.equals(container.getGrouping()));
		
	}
	
	@Test
	public void testAutoProcessing() {
		StagedStaxParserTestRegistry registry = new StagedStaxParserTestRegistry();
		Container container = test( new File( contents, "autoProcessingInstructionTest.xml"), registry);		
		Assert.assertTrue("container.string expected as [" + STRING_VALUE + "], found [" + container.getStringValue() + "]", STRING_VALUE.equalsIgnoreCase(container.getStringValue()));
		Assert.assertTrue("container.boolean expected as [" + true + "], found [" + container.getBooleanValue() + "]", Boolean.TRUE.equals(container.getBooleanValue()));
		Assert.assertTrue("container.intValue expected as [" + INT_VALUE + "], found [" + container.getIntegerValue() + "]", 23 == container.getIntegerValue());
	
		// set
		Map<String, Boolean> foundMap = new HashMap<>();
		foundMap.put( "S1", false);
		foundMap.put( "S2", false);
		foundMap.put( "S3", false);
		Set<String> stringSet = container.getStringSet();
		validateStringSet(foundMap, stringSet);
		
		// list
		String [] values = new String[] {"L1", "L2", "L3"};
		List<String> stringList = container.getStringList();
		validateStringList(values, stringList);
		
		Assert.assertTrue("container.instruction expected as [" + PROCESSION_INSTRUCTION + "], found [" + container.getProcessingInstruction() + "]", PROCESSION_INSTRUCTION.equalsIgnoreCase(container.getProcessingInstruction()));
		Assert.assertTrue("container.grouping expected as [" + GROUPING + "], found [" + container.getGrouping() + "]", GROUPING.equals(container.getGrouping()));
	
		// complex
		List<com.braintribe.xml.sax.parser.test.model.Entry> entries = container.getEntries();
		Assert.assertTrue("expected [" + 2 + "] complex entries, found [" + entries.size() + "]" , entries.size() == 2);
		
		com.braintribe.xml.sax.parser.test.model.Entry entry1 = entries.get(0);
		Assert.assertTrue( "expected [" + ENTRY_VALUE_1 + "] for value of entry @ 0, found [" + entry1.getValue() + "]", ENTRY_VALUE_1.equalsIgnoreCase( entry1.getValue()));
		Assert.assertTrue( "expected [" + ENTRY_AUTO_1 + "] for auto of entry @ 0, found [" + entry1.getAutoValue() + "]", ENTRY_AUTO_1.equalsIgnoreCase( entry1.getAutoValue()));
		
		com.braintribe.xml.sax.parser.test.model.Entry entry2 = entries.get(1);
		Assert.assertTrue( "expected [" + ENTRY_VALUE_2 + "] for value of entry #2, found [" + entry2.getValue() + "]", ENTRY_VALUE_2.equalsIgnoreCase( entry2.getValue()));
		Assert.assertTrue( "expected [" + ENTRY_AUTO_2 + "] for auto of entry #2, found [" + entry2.getAutoValue() + "]", ENTRY_AUTO_2.equalsIgnoreCase( entry2.getAutoValue()));
	}
	
	@Test
	public void testManipProcessing() {
		StagedStaxParserTestRegistry registry = new StagedStaxParserTestRegistry();
		Container container = test( new File( contents, "manipulationParserProcessingInstructionTest.xml"), registry);		
		// set
		Map<String, Boolean> foundMap = new HashMap<>();
		foundMap.put( "S1", false);
		foundMap.put( "S2", false);
		foundMap.put( "S3", false);
		Set<String> stringSet = container.getStringSet();
		validateStringSet(foundMap, stringSet);
		
		// list
		String [] values = new String[] {"L1", "L2", "L3"};
		List<String> stringList = container.getStringList();
		validateStringList(values, stringList);
		
		Assert.assertTrue("container.instruction expected as [" + PROCESSION_INSTRUCTION + "], found [" + container.getProcessingInstruction() + "]", PROCESSION_INSTRUCTION.equalsIgnoreCase(container.getProcessingInstruction()));
		Assert.assertTrue("container.grouping expected as [" + GROUPING + "], found [" + container.getGrouping() + "]", GROUPING.equals(container.getGrouping()));
	
		// complex
		List<com.braintribe.xml.sax.parser.test.model.Entry> entries = container.getEntries();
		Assert.assertTrue("expected [" + 2 + "] complex entries, found [" + entries.size() + "]" , entries.size() == 2);
		
		com.braintribe.xml.sax.parser.test.model.Entry entry1 = entries.get(0);
		Assert.assertTrue( "expected [" + ENTRY_VALUE_1 + "] for value of entry @ 0, found [" + entry1.getValue() + "]", ENTRY_VALUE_1.equalsIgnoreCase( entry1.getValue()));
		Assert.assertTrue( "expected [" + ENTRY_AUTO_1 + "] for auto of entry @ 0, found [" + entry1.getAutoValue() + "]", ENTRY_AUTO_1.equalsIgnoreCase( entry1.getAutoValue()));
		Assert.assertTrue( "expected [" + ENTRY_ENRICHED_1 + "] for enriched of entry @ 0, found [" + entry1.getEnriched() + "]", ENTRY_ENRICHED_1.equalsIgnoreCase( entry1.getEnriched()));
		
		com.braintribe.xml.sax.parser.test.model.Entry entry2 = entries.get(1);
		Assert.assertTrue( "expected [" + ENTRY_VALUE_2 + "] for value of entry #2, found [" + entry2.getValue() + "]", ENTRY_VALUE_2.equalsIgnoreCase( entry2.getValue()));
		Assert.assertTrue( "expected [" + ENTRY_AUTO_2 + "] for auto of entry #2, found [" + entry2.getAutoValue() + "]", ENTRY_AUTO_2.equalsIgnoreCase( entry2.getAutoValue()));
		Assert.assertTrue( "expected [" + ENTRY_ENRICHED_2 + "] for enriched of entry @ 1, found [" + entry2.getEnriched() + "]", ENTRY_ENRICHED_2.equalsIgnoreCase( entry2.getEnriched()));
				
	}
	
	private void validateStringList(String[] values, List<String> stringList) {
		int len;
		len = stringList.size();
		Assert.assertTrue("expected container.stringList size [" + values.length + "], found [" + len + "]", len == values.length);
		for (int i = 0; i < len; i++) {
			Assert.assertTrue( "expected [" + values[i] + "] at position [" + i + "], yet found [" + stringList.get(i) + "]", values[i].equalsIgnoreCase(stringList.get(i)));
			i++;
		}
	}
	
	private void validateStringSet(Map<String, Boolean> foundMap, Set<String> stringSet) {
		int len = stringSet.size();
		Assert.assertTrue("expected container.stringset size [" + foundMap.size() + "], found [" + len + "]", len == foundMap.size());
		
		for (String value : stringSet) {
			foundMap.put( value, true);
			
		}
		for (Entry<String, Boolean> entry : foundMap.entrySet()) {
			if (entry.getValue() == null) {
				Assert.fail("unexpected value ["+ entry.getKey() + "] found");
			}
			else if (entry.getValue() == false) {
				Assert.fail("value ["+ entry.getKey() + "] not found");
			}
		}
	}
	
	
}
