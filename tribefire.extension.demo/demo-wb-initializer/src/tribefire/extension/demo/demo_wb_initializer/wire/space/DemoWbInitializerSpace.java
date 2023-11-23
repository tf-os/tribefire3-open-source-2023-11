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
package tribefire.extension.demo.demo_wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.gm.model.svd.EvaluateRequest;
import com.braintribe.model.bvd.navigation.PropertyPath;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.selector.Operator;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.securityservice.GetCurrentUser;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.AsyncEvaluation;
import com.braintribe.model.user.User;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.darktheme_wb_initializer.wire.contract.DarkthemeWbIconContract;
import tribefire.cortex.assets.darktheme_wb_initializer.wire.contract.DarkthemeWbStyleContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerIconContract;
import tribefire.extension.demo.model.api.GetEmployeesByGenderAsNotifications;
import tribefire.extension.demo.model.api.GetOrphanedEmployees;
import tribefire.extension.demo.model.api.GetPersonsByName;
import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Person;

/**
 * <p>
 * This space takes care of managed instances around the demo workbench setup. <br />
 * 
 */
@Managed
public class DemoWbInitializerSpace extends AbstractInitializerSpace implements DemoWbInitializerContract {

	@Import
	DemoWbInitializerIconContract icons;
	
	@Import
	DarkthemeWbStyleContract style;
	
	@Import
	DarkthemeWbIconContract grayishBlueStyleIcon;
	
	@Managed
	@Override
	public Folder entryPointFolder() {
		Folder bean = create(Folder.T).initFolder("tribefire", "Tribefire");
		
		bean.setIcon(icons.tribefireIcon());
		
		bean.getSubFolders().add(demoFolder());
		
		return bean;
	}

	@Managed
	private Folder demoFolder() {
		Folder bean = create(Folder.T).initFolder("demo", "Demo");
		
		bean.getSubFolders().addAll(list(
				personFolder(),
				companyFolder(),
				newEmployeeFolder(),
				employeesByGenderFolder(),
				employeeCountFolder(),
				orphanedEmployeesFolder(),
				personsPerNameFolder(),
				personsByNameFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder personFolder() {
		Folder bean = create(Folder.T).initFolder("persons", "Persons");

		bean.setIcon(icons.personIcon());
		bean.setContent(personQueryAction());

		return bean;
	}

	@Managed
	@Override
	public Folder companyFolder() {
		Folder bean = create(Folder.T).initFolder("companies", "Companies");

		bean.setIcon(icons.companyIcon());
		bean.setContent(companyQueryAction());

		return bean;
	}

	@Managed
	private SimpleQueryAction companyQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		bean.setTypeSignature(Company.T.getTypeSignature());

		return bean;
	}

	@Managed
	private SimpleQueryAction personQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);

		bean.setTypeSignature(Person.T.getTypeSignature());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction getEmployeesByGenderAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault("Employees by Gender"));
		bean.setTemplate(getEmployeesByGenderAsNotificationsTemplate());
		bean.setInplaceContextCriterion(companyCriterion());

		return bean;
	}

	private TraversingCriterion companyCriterion() {
		return importEntities(TC.create().typeCondition(TypeConditions.isAssignableTo(Company.T)).done());
	}

	@Managed
	private Template getEmployeesByGenderAsNotificationsTemplate() {
		return importEntities(Templates.template(create(LocalizedString.T).putDefault("Get Employees by Gender"))

				.prototype(c -> c.create(GetEmployeesByGenderAsNotifications.T))

				.record(c -> {
					GetEmployeesByGenderAsNotifications prototype = c.getPrototype();

					c.pushVariable("company") //
							.addMetaData(create(Name.T).name("Choose a Company")) //
							.addMetaData(create(Description.T).description("The company to be searched for employees matching the specified gender.")) //
							.addMetaData(create(Mandatory.T)) //
							.addMetaData(create(Hidden.T, h -> h
									.setSelector(create(PropertyValueComparator.T).initPropertyValueComparator("company", Operator.notEqual, null)))); //
					prototype.setCompany(null);

					c.pushVariable("gender") //
							.addMetaData(create(Name.T).name("Choose a Gender")) //
							.addMetaData(create(Description.T).description("The gender the employees should match to.")) //
							.addMetaData(create(Mandatory.T)); //
					prototype.setGender(null);

				})

				.addMetaData(create(Name.T).name("Employees by Gender"))
				.addMetaData(create(Description.T).description("Searches for employees of the given company that have the given gender."))

				.build());
	}
	
	@Managed
	private TemplateServiceRequestAction getOrphanedEmployeesAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault("Orphaned Employees"));
		bean.setTemplate(getOrphanedEmployeesTemplate());

		return bean;
	}

	@Managed
	private Template getOrphanedEmployeesTemplate() {
		return importEntities(Templates
			.template(create(LocalizedString.T).putDefault("Get Orphaned Employees"))
			.prototype(c -> c.create(GetOrphanedEmployees.T))
			.build()
			);
	}

	@Managed
	private FolderContent personsByNameAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault("Persons By Name"));
		bean.setTemplate(personsByNameTemplate());
		bean.setForceFormular(true);

		return bean;
	}

	@Managed
	private Template personsByNameTemplate() {
		return importEntities(Templates.template(create(LocalizedString.T).putDefault("Get Persons By Name"))
				.prototype(c -> c.create(GetPersonsByName.T)).record(c -> {
					GetPersonsByName prototype = c.getPrototype();

					GetCurrentUser request = GetCurrentUser.T.create();

					EvaluateRequest vdRequest = EvaluateRequest.T.create();
					vdRequest.setRequest(request);

					PropertyPath path = PropertyPath.T.create();
					path.setEntity(vdRequest);
					path.setPropertyPath(User.name);

					Variable nameVar = Variable.T.create();
					nameVar.setName("name");
					nameVar.setDefaultValue(path);

					c.pushVd(nameVar) //
							.addMetaData(create(Name.T).name("Choose a Name")) //
							.addMetaData(create(Description.T).description("The first name of Persons to find.")) //
							.addMetaData(create(Mandatory.T));
					prototype.setName(null);

					Variable limitVar = Variable.T.create();
					limitVar.setName("limit");
					limitVar.setDefaultValue(0);

					c.pushVd(limitVar) //
							.addMetaData(create(Name.T).name("Choose a Limit")) //
							.addMetaData(create(Description.T).description("Maximum number of results.")) //
							.addMetaData(create(Mandatory.T));

					prototype.setPageLimit(0);

					Variable offsetVar = Variable.T.create();
					offsetVar.setName("offset");
					offsetVar.setDefaultValue(0);

					c.pushVd(offsetVar) //
							.addMetaData(create(Name.T).name("Choose an Offset")) //
							.addMetaData(create(Description.T).description("Number of results to be skipped.")) //
							.addMetaData(create(Mandatory.T));

					prototype.setPageOffset(0);
				})

				.addMetaData(create(Name.T).name("Persons By Name"))
				.addMetaData(create(Description.T).description("Searches for Persons by first name.")).addMetaData(create(AsyncEvaluation.T))

				.build());
	}

	@Managed
	@Override
	public Folder employeesByGenderFolder() {
		Folder bean = create(Folder.T).initFolder("employeesByGender", "Employees By Gender");

		bean.setIcon(grayishBlueStyleIcon.magnifier());
		bean.setContent(getEmployeesByGenderAction());

		return bean;
	}

	@Managed
	@Override
	public Folder orphanedEmployeesFolder() {
		Folder bean = create(Folder.T).initFolder("orphanedEmployees", "Orphaned Employees");

		bean.setIcon(grayishBlueStyleIcon.magnifier());
		bean.setContent(getOrphanedEmployeesAction());

		return bean;
	}

	@Managed
	@Override
	public Folder personsByNameFolder() {
		Folder bean = create(Folder.T).initFolder("personsByName", "Persons By Name (Request)");

		bean.setIcon(grayishBlueStyleIcon.magnifier());
		bean.setContent(personsByNameAction());

		return bean;
	}

	@Managed
	@Override
	public Folder accessDemoFolder() {
		Folder bean = create(Folder.T).initFolder("access.demo", "Access Demo");

		bean.getSubFolders().add(tribefireFolder());

		return bean;
	}

	@Managed
	private Folder tribefireFolder() {
		Folder bean = create(Folder.T).initFolder("tribefire", "Tribefire");

		bean.getSubFolders().add(demoFolder());

		return bean;
	}

	@Managed
	@Override
	public Folder newEmployeeFolder() {
		Folder bean = create(Folder.T).initFolder("newEmployee", "New Employee");

		bean.setIcon(grayishBlueStyleIcon.newIcon());
		bean.setContent(newEmployeeAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction newEmployeeAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault("New Employee"));
		bean.setTemplate(newEmployeeTemplate());
		bean.setInplaceContextCriterion(newEmployeeCriterion());

		return bean;
	}
	
	@Managed
	private Template newEmployeeTemplate() {
		return importEntities(Templates
				.template(create(LocalizedString.T).putDefault("New Employee Template"))
				
				.prototype(c -> c.create(NewEmployee.T))
				
				.record(c -> {
					
					NewEmployee prototype = c.getPrototype();
					
					c.pushVariable("company");
					prototype.setCompany(null);
					
					c.pushVariable("department");
					prototype.setDepartment(null);
					
					c.pushVariable("employee");
					prototype.setEmployee(null);
					
				})
				
				.build()
				);
	}

	@Managed
	private TraversingCriterion newEmployeeCriterion() {
		return importEntities(TC.create()
				.disjunction()
					.typeCondition(TypeConditions.isAssignableTo(Company.T))
					.typeCondition(TypeConditions.isAssignableTo(Person.T))
				.close()
				.done());
	}
	
	@Managed
	private Folder employeeCountFolder() {
		Folder bean = create(Folder.T).initFolder("employeeCount", "Employee Count");
		
		bean.setIcon(grayishBlueStyleIcon.magnifier());
		bean.setContent(employeeCountAction());
		
		return bean;
	}
	
	@Managed
	private TemplateQueryAction employeeCountAction() {
		TemplateQueryAction bean = create(TemplateQueryAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Employee Count"));
		bean.setTemplate(employeeCountTemplate());
		
		return bean;
	}
	
	@Managed
	private Template employeeCountTemplate() {
		return importEntities(Templates
			.template(create(LocalizedString.T).putDefault("Employee Count Template"))
			
			.prototype(c -> new SelectQueryBuilder()
					.select("c")
					.select().count("e")
					.from(Company.T, "c")
					.join("c", Company.employees, "e")
					.orderBy().property("c", Company.name)
					.done())
			
			.build()
			);
	}

	@Managed
	private Folder personsPerNameFolder() {
		Folder bean = create(Folder.T).initFolder("personsPerName", "Persons per Name (Query)");
		
		bean.setIcon(grayishBlueStyleIcon.magnifier());
		bean.setContent(personsPerNameAction());
		
		return bean;
	}

	private TemplateQueryAction personsPerNameAction() {
		TemplateQueryAction bean = create(TemplateQueryAction.T);
		
		bean.setDisplayName(create(LocalizedString.T).putDefault("Persons per Name"));
		bean.setTemplate(personsPerNameTemplate());
		bean.setForceFormular(true);
		
		return bean;
	}

	@Managed
	private Template personsPerNameTemplate() {
		String query = "from " + Person.T.getTypeSignature() + 
				" p where p." + Person.firstName + 
				" ilike :firstName(string,'*') and p." +
				Person.lastName + " ilike :lastName(string,'*')";
		
		return importEntities(Templates
			.template(create(LocalizedString.T).putDefault("Persons per Name Template"))
			
			.prototype(c -> QueryParser.parse(query).getQuery())
			
			.build()
			);
	}
}
