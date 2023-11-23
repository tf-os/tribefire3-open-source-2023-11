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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.utils;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.deployment.hibernate.mapping.utils.CamelCaseStringShortener;

public class CamelCaseStringShortenerTest {
	
	private static int [] testTheseLength = {1, 2, 5, 7, 10, 20, 30, 50, 100};
	
	private static String [] testTheseStrings = {
			 "A"
			,"aA"
			,"Aa"
			,"Long"
			,"shortOne"
			,"ThisPropertyShouldProvideASmoothShortening"
			,"ThisPropertyShouldProvideSmoothShortening"
			,"ThisPropertyShouldProvideSmoothShorteningAsLongAsItIsPossible"
			,"CreditCardCompanyEmployeesList"
			,"FinancialInstitutionCompanyIssuedCreditCardList"
			,"myExtremellyHugePropertyNameThatShouldNeverBeUsedInRealLife"
			,"AnUnnecessarilyLargePropertyNameThatCouldExistInTheModel"
			,"UnnecessarilyLargePropertyNameThatMayExist"
			,"bankInternationalIdentificationCodeNumber"
			,"bankInternationalIdentificationCodeNumberSWIFTAddress"
			,"OneFewByTheSeaAreInTheToeQueOnTheFarmWhereTheCowIsFullOfGutMilk"
			,"extremellyLongPropertyNameThatMightClashWithSlightelyDifferentPropertyInSameClassOrHierarchya"
			,"extremellyLongPropertyNameThatMightClashWithSlightelyDifferentPropertyInSameClassOrHierarchyb"
			};
	
	@Test
	public void testShortener() throws Exception {
		
		StringBuffer logLine = null;
		for (int maxLength  : testTheseLength) {
			
			System.out.println("\r\n================== SHORTENING TO MAX "+maxLength+" CHAR(S) ==================");
			
			for (String testMe : testTheseStrings) {
				
				logLine = new StringBuffer(200);
				
				logLine.append("string before: \t\"").append(testMe).append("\" size: ").append(testMe.length());
				testMe = new CamelCaseStringShortener(testMe, maxLength).shorten();
				logLine.append("\r\nstring after:\t\"").append(testMe).append("\" size: ").append(testMe.length());
				
				String log = logLine.toString(); 
				System.out.println(log);
				if (testMe.length() > maxLength) {
					Assert.fail("Shortening Failed: "+log);
				}
			}
			
		}
	}
	

	
}
