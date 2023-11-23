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
package tribefire.extension.demo.processing.tools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;

import tribefire.extension.demo.model.data.Address;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Gender;
import tribefire.extension.demo.model.data.Person;

/**
 * Static definition of raw data that is used by the {@link DemoPopulationBuilder} to build a population of the DemoModel. 
 */
public abstract class DemoPopulation {

	/*
	 * Definition property order of each type. 
	 */
	public static Map<Class<? extends GenericEntity>, String[]> demoDescriptions = new HashMap<Class<? extends GenericEntity>, String[]>();
	static {
		demoDescriptions.put(Person.class, new String[] {Person.ssn, Person.firstName, Person.lastName, Person.gender, Person.address, Person.mother, Person.father, Person.children, Person.picture, Person.anything});
		demoDescriptions.put(Company.class, new String[] {Company.name, Company.address, Company.ceo, Company.averageRevenue, Company.employees, Company.departments, Company.paperworkByCategory});
		demoDescriptions.put(Department.class, new String[] {Department.name, Department.manager, Department.numberOfEmployees, Department.company});
		demoDescriptions.put(Address.class, new String[] {Address.street, Address.streetNumber, Address.postalCode, Address.city, Address.country});
	}
	
	/*
	 * Raw data definition 
	 */
	public static Map<Class<? extends GenericEntity>, Object[][]> demoRawData = new HashMap<Class<? extends GenericEntity>, Object[][]>();
	static {
		
		demoRawData.put(Person.class, new Object[][] {
			{"111", "John", "Doe", Gender.male, new Object[] {"Kandlgasse", 3, "1070", "Vienna", "Austria"}, "ref:333", "ref:444", new Object[] { "ref:555", "ref:666" }, "/static/images/johndoe.gif", 1},
			{"222", "Jane", "Doe", Gender.female, new Object[] {"Barisanistrasse", 5, "5020", "Salzburg", "Austria"}, null, null, new Object[] { "ref:555", "ref:666" }, "/static/images/janedoe.jpg", "MyTest"},
			{"333", "Sue", "St. Doe", Gender.female, new Object[] {"Oak St", 100, "21520", "Accident, MD", "USA"}, null, null, null, "/static/images/suedoe.jpg"}, 
			{"444", "James", "Doeman", Gender.male, new Object[] {"Peterson Rd", 26450, "97009", "Boring, OR", "USA"}, null, null, null, "/static/images/jamesdoe.jpg"},
			{"555", "J.J.", "Doesen", Gender.male, new Object[] {"Merianvej", 7, "5500", "Middlefart", "Denmark"}, "ref:222", "ref:111", null, "/static/images/jjdoe.jpg"},
			{"666", "Mary", "Doebauer", Gender.female, new Object[] {"Museumstrasse", 10, "91555", "Feuchtwangen", "Germany"}, "ref:222", "ref:111", null, "/static/images/marydoe.gif"}});

		demoRawData.put(Company.class, new Object[][] {
			{"Braintribe IT-Technologies GmbH", new Object[] {"Kandlgasse", 21, "1070", "Vienna", "Austria"}, "ref:111", new BigDecimal("800000000"), new Object[] {"ref:555", "ref:666"}, 
				new Object[][] {
					{"Marketing", "ref:555", 10, "ref:Braintribe IT-Technologies GmbH"}, 
					{"R&D", "ref:666", 15, "ref:Braintribe IT-Technologies GmbH"}, 
					{"Professional Services", "ref:555", 10, "ref:Braintribe IT-Technologies GmbH"}, 
					{"Backoffice", "ref:666", 20, "ref:Braintribe IT-Technologies GmbH"}},
				new Object[][] {
					{"Mission Statement","/static/paperwork/Mission-Statement.pdf"}, 
					{"Tribefire Whitepaper","/static/paperwork/Tribefire-Overview.pdf"}}},
			{"Agile Documents GmbH", new Object[] {"Kandlgasse", 19, "1070", "Vienna", "Austria"}, "ref:222", new BigDecimal("40000000"), new Object[] {"ref:333", "ref:444"}, 
				new Object[][] {
					{"Marketing", "ref:333", 3, "ref:Agile Documents GmbH"}, 
					{"R&D", "ref:444", 5, "ref:Agile Documents GmbH"}, 
					{"Professional Services", "ref:333", 5, "ref:Agile Documents GmbH"}, 
					{"Backoffice", "ref:444", 10, "ref:Agile Documents GmbH"}},
				new Object[][] {
					{"Mission Statement","/static/paperwork/Mission-Statement.pdf"}}}});
	}


}
