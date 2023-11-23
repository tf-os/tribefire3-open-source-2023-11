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
package tribefire.extension.wopi.wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Lists.list;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_PRINT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_TRANSLATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_USER_FRIENDLY_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.URL_BREADCRUMB_BRAND_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.URL_BREADCRUMB_FOLDER_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.ComparisonOperator;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.DisjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.template.building.api.TemplateBuilder;
import com.braintribe.model.processing.template.building.api.TemplatePrototypingContext;
import com.braintribe.model.processing.template.building.api.TemplateRecordingContext;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.WopiStatus;
import com.braintribe.model.wopi.service.integration.AddDemoDocs;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessions;
import com.braintribe.model.wopi.service.integration.CloseWopiSession;
import com.braintribe.model.wopi.service.integration.DownloadCurrentResource;
import com.braintribe.model.wopi.service.integration.EnsureTestDoc;
import com.braintribe.model.wopi.service.integration.ExportWopiSession;
import com.braintribe.model.wopi.service.integration.OpenWopiDocument;
import com.braintribe.model.wopi.service.integration.OpenWopiSession;
import com.braintribe.model.wopi.service.integration.RemoveAllWopiSessions;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocs;
import com.braintribe.model.wopi.service.integration.RemoveWopiSession;
import com.braintribe.model.wopi.service.integration.WopiHealthCheck;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.wopi.wb_initializer.wire.contract.IconContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.WopiWbInitializerContract;

@Managed
public class WopiWbInitializerSpace extends AbstractInitializerSpace implements WopiWbInitializerContract {

	@Import
	IconContract icons;

	@Import
	DefaultWbContract defaultWorkbench;

	static Set<String> fromWorkbench() {
		return Stream.of("workbench").collect(Collectors.toSet());
	}

	// -----------------------------------------------------------------------
	// FODLER STRUCTURE
	// -----------------------------------------------------------------------

	@Managed
	@Override
	public Folder entryPointFolder() {
		Folder bean = create(Folder.T).initFolder(WOPI_FOLDER, WOPI_FOLDER);
		bean.setIcon(icons.adxIcon());

		//@formatter:off
		bean.setSubFolders(list(
				wopiSessions(),
				demoAndTesting()
		));
		//@formatter:on

		return bean;
	}

	// -----------------------------------------------------------------------
	// MODEL FODLERs
	// -----------------------------------------------------------------------

	@Managed
	private Folder wopiSessions() {
		Folder bean = create(Folder.T).initFolder(WOPI_SESSIONS_FOLDER, WOPI_SESSIONS_FOLDER);
		bean.setIcon(icons.officeIcon());
		bean.setParent(entryPointFolder());
		//@formatter:off
		bean.setSubFolders(list(
				openWopiSessions(),
				expiredWopiSessions(),
				closedWopiSessions(),
				wopiSessionSearch(),
				wopiSessionStatistics(),
				openWopiSession(),
				closeAllWopiSessions(),
				removeAllWopiSessions(),
				wopiHealthCheck()
				));
		//@formatter:on
		return bean;
	}

	@Managed
	private Folder demoAndTesting() {
		Folder bean = create(Folder.T).initFolder(WOPI_DEMO_TESTING_FOLDER, WOPI_DEMO_TESTING_FOLDER);
		bean.setIcon(icons.addDemoAndTestingDocsIcon());
		bean.setParent(entryPointFolder());
		//@formatter:off
		bean.setSubFolders(list(
				addDemoDocs(),
				removeDemoDocs(),
				ensureTestDoc()
		));
		//@formatter:on
		return bean;
	}

	// -----------------------------------------------------------------------
	// ACTIONBAR
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public List<Folder> actionbarFolders() {
		//@formatter:off
		return list(
				openWopiDocumentActionBar(),
				downloadCurrentResourceActionBar(),
				closeWopiSessionActionBar(),
				removeWopiSessionActionBar(),
				exportWopiSessionActionBar()
				
		);
		//@formatter:on
	}

	private TraversingCriterion wopiSessionCriterion() {
		return importEntities(TC.create().typeCondition(TypeConditions.isAssignableTo(WopiSession.T)).done());
	}

	private TraversingCriterion notClosedWopiSessionCriterion() {
		//@formatter:off
		return and(
			wopiSessionCriterion(),	
			or(
				isOpenWopiSessionStatusCriterion(),
				isExpiredWopiSessionStatusCriterion()
			)
		);
		//@formatter:on
	}

	// OpenWopiDocument
	@Managed
	@Override
	public Folder openWopiDocumentActionBar() {
		Folder bean = create(Folder.T).initFolder(OPEN_WOPI_DOCUMENT, OPEN_WOPI_DOCUMENT);

		bean.setIcon(icons.viewIcon());
		bean.setContent(openWopiDocumentActionActionBar());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction openWopiDocumentActionActionBar() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(OPEN_WOPI_DOCUMENT));
		bean.setTemplate(openWopiDocumentTemplateActionBar());
		bean.setInplaceContextCriterion(wopiSessionCriterion());

		return bean;
	}

	@Managed
	private Template openWopiDocumentTemplateActionBar() {

		return importEntities(Templates.template(create(LocalizedString.T).putDefault(OpenWopiDocument.T.getShortName()))

				.prototype(c -> {
					return c.create(OpenWopiDocument.T);
				})

				.record(c -> {
					OpenWopiDocument prototype = c.getPrototype();

					prototype.setSendNotifications(true);

					c.pushVariable(OpenWopiDocument.wopiSession).addMetaData(create(Name.T).name(WOPI_SESSION_NAME))
							.addMetaData(create(Description.T).description(WOPI_SESSION_DESCRIPTION));
					prototype.setWopiSession(null); // get value from selection - some magic behind ;)
				})

				.build());
	}

	// DownloadCurrentResource
	@Managed
	@Override
	public Folder downloadCurrentResourceActionBar() {
		Folder bean = create(Folder.T).initFolder(DOWNLOAD_CURRENT_RESOURCE, DOWNLOAD_CURRENT_RESOURCE);

		bean.setIcon(icons.downloadIcon());
		bean.setContent(downloadCurrentResourceActionActionBar());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction downloadCurrentResourceActionActionBar() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(DOWNLOAD_CURRENT_RESOURCE));
		bean.setTemplate(downloadCurrentResourceTemplateActionBar());
		bean.setInplaceContextCriterion(wopiSessionCriterion());

		return bean;
	}

	@Managed
	private Template downloadCurrentResourceTemplateActionBar() {

		return importEntities(Templates.template(create(LocalizedString.T).putDefault(DownloadCurrentResource.T.getShortName()))

				.prototype(c -> {
					return c.create(DownloadCurrentResource.T);
				})

				.record(c -> {
					DownloadCurrentResource prototype = c.getPrototype();

					prototype.setSendNotifications(true);

					c.pushVariable(DownloadCurrentResource.wopiSession).addMetaData(create(Name.T).name(WOPI_SESSION_NAME))
							.addMetaData(create(Description.T).description(WOPI_SESSION_DESCRIPTION));
					prototype.setWopiSession(null); // get value from selection - some magic behind ;)
				})

				.build());
	}

	// CloseWopiSession
	@Managed
	@Override
	public Folder closeWopiSessionActionBar() {
		Folder bean = create(Folder.T).initFolder(CLOSE_WOPI_SESSION, CLOSE_WOPI_SESSION);

		bean.setIcon(icons.removeIcon());
		bean.setContent(closeWopiSessionActionActionBar());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction closeWopiSessionActionActionBar() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(CLOSE_WOPI_SESSION));
		bean.setTemplate(closeWopiSessionTemplateActionBar());
		bean.setInplaceContextCriterion(notClosedWopiSessionCriterion());

		return bean;
	}

	@Managed
	private Template closeWopiSessionTemplateActionBar() {

		return importEntities(Templates.template(create(LocalizedString.T).putDefault(CloseWopiSession.T.getShortName()))

				.prototype(c -> {
					return c.create(CloseWopiSession.T);
				})

				.record(c -> {
					CloseWopiSession prototype = c.getPrototype();

					prototype.setSendNotifications(true);

					c.pushVariable(CloseWopiSession.wopiSession).addMetaData(create(Name.T).name(WOPI_SESSION_NAME))
							.addMetaData(create(Description.T).description(WOPI_SESSION_DESCRIPTION));
					prototype.setWopiSession(null); // get value from selection - some magic behind ;)
				})

				.build());
	}

	// RemoveWopiSession
	@Managed
	@Override
	public Folder removeWopiSessionActionBar() {
		Folder bean = create(Folder.T).initFolder(REMOVE_WOPI_SESSION, REMOVE_WOPI_SESSION);

		bean.setIcon(icons.deleteIcon());
		bean.setContent(removeWopiSessionActionActionBar());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction removeWopiSessionActionActionBar() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(REMOVE_WOPI_SESSION));
		bean.setTemplate(removeWopiSessionTemplateActionBar());
		bean.setInplaceContextCriterion(wopiSessionCriterion());

		return bean;
	}

	@Managed
	private Template removeWopiSessionTemplateActionBar() {

		return importEntities(Templates.template(create(LocalizedString.T).putDefault(RemoveWopiSession.T.getShortName()))

				.prototype(c -> {
					return c.create(RemoveWopiSession.T);
				})

				.record(c -> {
					RemoveWopiSession prototype = c.getPrototype();

					prototype.setSendNotifications(true);

					c.pushVariable(RemoveWopiSession.wopiSession).addMetaData(create(Name.T).name(WOPI_SESSION_NAME))
							.addMetaData(create(Description.T).description(WOPI_SESSION_DESCRIPTION));
					prototype.setWopiSession(null); // get value from selection - some magic behind ;)
				})

				.build());
	}

	// ExportWopiSession
	@Managed
	@Override
	public Folder exportWopiSessionActionBar() {
		Folder bean = create(Folder.T).initFolder(EXPORT_WOPI_SESSION, EXPORT_WOPI_SESSION);

		bean.setIcon(icons.downloadIcon());
		bean.setContent(exportWopiSessionActionActionBar());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction exportWopiSessionActionActionBar() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(EXPORT_WOPI_SESSION));
		bean.setTemplate(exportWopiSessionTemplateActionBar());
		bean.setInplaceContextCriterion(wopiSessionCriterion());
		bean.setForceFormular(true);
		bean.setMultiSelectionSupport(true);

		return bean;
	}

	@Managed
	private Template exportWopiSessionTemplateActionBar() {

		return importEntities(Templates.template(create(LocalizedString.T).putDefault(ExportWopiSession.T.getShortName()))

				.prototype(c -> {
					return c.create(ExportWopiSession.T);
				})

				.record(c -> {
					ExportWopiSession prototype = c.getPrototype();

					prototype.setSendNotifications(true);

					c.pushVariable(ExportWopiSession.includeDiagnosticPackage);
					prototype.setIncludeDiagnosticPackage(
							(Boolean) prototype.entityType().getProperty(ExportWopiSession.includeDiagnosticPackage).getInitializer());
					c.pushVariable(ExportWopiSession.includeCurrentResource);
					prototype.setIncludeCurrentResource(
							(Boolean) prototype.entityType().getProperty(ExportWopiSession.includeCurrentResource).getInitializer());
					c.pushVariable(ExportWopiSession.includeResourceVersions);
					prototype.setIncludeResourceVersions(
							(Boolean) prototype.entityType().getProperty(ExportWopiSession.includeResourceVersions).getInitializer());
					c.pushVariable(ExportWopiSession.includePostOpenResourceVersions);
					prototype.setIncludePostOpenResourceVersions(
							(Boolean) prototype.entityType().getProperty(ExportWopiSession.includePostOpenResourceVersions).getInitializer());

					c.pushVariable(RemoveWopiSession.wopiSession).addMetaData(create(Name.T).name(WOPI_SESSION_NAME))
							.addMetaData(create(Description.T).description(WOPI_SESSION_DESCRIPTION));
					prototype.setWopiSessions(Collections.emptySet()); // get value from selection - some magic behind
																		// ;)
				})

				.build());
	}

	// -----------------------------------------------------------------------
	// FOLDERS and ACTIONS
	// -----------------------------------------------------------------------

	// --------------
	// ADMINISTRATION
	// --------------

	// Query open 'WopiSession'
	@Managed
	@Override
	public Folder openWopiSessions() {
		Folder bean = create(Folder.T).initFolder(OPEN_WOPI_SESSIONS, OPEN_WOPI_SESSIONS);

		bean.setIcon(icons.sessionOpenIcon());
		bean.setContent(openWopiSessionsQuery());

		return bean;
	}

	@Managed
	private TemplateQueryAction openWopiSessionsQuery() {
		TemplateQueryAction bean = create(TemplateQueryAction.T);
		bean.setDisplayName(create(LocalizedString.T).putDefault(OPEN_WOPI_SESSIONS));
		bean.setTemplate(openWopiSessionsTemplate());
		bean.setForceFormular(true);
		return bean;
	}

	@Managed
	private Template openWopiSessionsTemplate() {
		String query = "from " + WopiSession.T.getTypeName() + " s where s.status=enum(" + WopiStatus.class.getTypeName() + "," + WopiStatus.open
				+ ") order by " + WopiSession.creationDate + " asc";
		return importEntities(Templates.template(create(LocalizedString.T).putDefault(WopiSession.T.getShortName()))
				.prototype(c -> QueryParser.parse(query).getQuery()).build());
	}

	// Query expired 'WopiSession'
	@Managed
	@Override
	public Folder expiredWopiSessions() {
		Folder bean = create(Folder.T).initFolder(EXPIRED_WOPI_SESSIONS, EXPIRED_WOPI_SESSIONS);

		bean.setIcon(icons.sessionExpiredIcon());
		bean.setContent(expiredWopiSessionsQuery());

		return bean;
	}

	@Managed
	private TemplateQueryAction expiredWopiSessionsQuery() {
		TemplateQueryAction bean = create(TemplateQueryAction.T);
		bean.setDisplayName(create(LocalizedString.T).putDefault(EXPIRED_WOPI_SESSIONS));
		bean.setTemplate(expiredWopiSessionsTemplate());
		bean.setForceFormular(true);
		return bean;
	}

	@Managed
	private Template expiredWopiSessionsTemplate() {
		String query = "from " + WopiSession.T.getTypeName() + " s where s.status=enum(" + WopiStatus.class.getTypeName() + "," + WopiStatus.expired
				+ ") order by " + WopiSession.creationDate + " asc";
		return importEntities(Templates.template(create(LocalizedString.T).putDefault(WopiSession.T.getShortName()))
				.prototype(c -> QueryParser.parse(query).getQuery()).build());
	}

	// Query closed 'WopiSession'
	@Managed
	@Override
	public Folder closedWopiSessions() {
		Folder bean = create(Folder.T).initFolder(CLOSED_WOPI_SESSIONS, CLOSED_WOPI_SESSIONS);

		bean.setIcon(icons.sessionClosedIcon());
		bean.setContent(closedWopiSessionsQuery());

		return bean;
	}

	@Managed
	private TemplateQueryAction closedWopiSessionsQuery() {
		TemplateQueryAction bean = create(TemplateQueryAction.T);
		bean.setDisplayName(create(LocalizedString.T).putDefault(CLOSED_WOPI_SESSIONS));
		bean.setTemplate(closedWopiSessionsTemplate());
		bean.setForceFormular(true);
		return bean;
	}

	@Managed
	private Template closedWopiSessionsTemplate() {
		String query = "from " + WopiSession.T.getTypeName() + " s where s.status=enum(" + WopiStatus.class.getTypeName() + "," + WopiStatus.closed
				+ ") order by " + WopiSession.creationDate + " asc";
		return importEntities(Templates.template(create(LocalizedString.T).putDefault(WopiSession.T.getShortName()))
				.prototype(c -> QueryParser.parse(query).getQuery()).build());
	}

	// Query "WopiSession search ..."
	@Managed
	@Override
	public Folder wopiSessionSearch() {
		Folder bean = create(Folder.T).initFolder(WOPI_SESSIONS_SEARCH, WOPI_SESSIONS_SEARCH);
		bean.setIcon(icons.magnifierIcon());
		bean.setContent(wopiSessionSearchAction());
		return bean;
	}

	@Managed
	private TemplateQueryAction wopiSessionSearchAction() {
		//@formatter:off
		TemplateQueryAction queryAction = queryAction("WOPI Sessions", null, CollectionTools2.asSet(wopiStatusTemplateMd()), true,  
				c -> {
					SelectQuery q =  new SelectQueryBuilder()
							.from(WopiSession.T, "w")
							.where()
								.conjunction()
									.property("w", WopiSession.correlationId).comparsion(Operator.ilike, correlationIdVariable())
									.property("w", WopiSession.status).comparsion(Operator.ilike, wopiStatusVariable())
									//TODO: equal is also not working
//									.property("w", WopiSession.status).comparsion(Operator.equal, wopiStatusVariable())

									.property("w", WopiSession.context).comparsion(Operator.ilike, contextVariable())
									.property("w", WopiSession.tenant).comparsion(Operator.ilike, tenantVariable())
									.property("w", WopiSession.creator).comparsion(Operator.ilike, creatorVariable())
									.property("w", WopiSession.creatorId).comparsion(Operator.ilike, creatorIdVariable())

								.close()
								.select("w")
								.orderBy()
									.property("w", WopiSession.creationDate).orderingDirection(OrderingDirection.descending)
							.done();
					return q;
				});
		//@formatter:on
		return queryAction;
	}

	@Managed
	private DynamicPropertyMetaDataAssignment wopiStatusTemplateMd() {
		DynamicPropertyMetaDataAssignment bean = create(DynamicPropertyMetaDataAssignment.T);
		bean.setVariable(wopiStatusVariable());
		bean.setMetaData(CollectionTools2.asSet(virtualEnumWopiStatus()));
		return bean;
	}

	@Managed
	private Variable wopiStatusVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Status");
		bean.setDefaultValue("*");
		bean.setTypeSignature("object");
		return bean;
	}

	@Managed
	private VirtualEnum virtualEnumWopiStatus() {
		VirtualEnum bean = create(VirtualEnum.T);
		//@formatter:off
		bean.setConstants(list(
				virtualEnumConstant("*","SHOW ALL",null),
				virtualEnumConstant(WopiStatus.open.name(), WopiStatus.open.name().toUpperCase(), null),
				virtualEnumConstant(WopiStatus.expired.name(), WopiStatus.expired.name().toUpperCase(), null),
				virtualEnumConstant(WopiStatus.closed.name(), WopiStatus.closed.name().toUpperCase(), null)
		));
		
		//formatter:on
		bean.setForceSelection(true);
		return bean;
	}
	
	private VirtualEnumConstant virtualEnumConstant(Object constant, String displayName, com.braintribe.model.resource.Icon icon) {
		VirtualEnumConstant bean = create(VirtualEnumConstant.T);
		bean.setValue(constant);
		if (displayName == null) {
			displayName = constant.toString();
		}
		bean.setDisplayValue(createLs(displayName));
		if (icon != null) {
			bean.setIcon(icon);
		}
		return bean;
	}

	@Managed
	private Variable correlationIdVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Job Id");
		bean.setDefaultValue("*");
		bean.setTypeSignature("string");
		return bean;
	}
	
	@Managed
	private Variable contextVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Context");
		bean.setDefaultValue("*");
		bean.setTypeSignature("string");
		return bean;
	}
	
	@Managed
	private Variable tenantVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Tenant");
		bean.setDefaultValue("*");
		bean.setTypeSignature("string");
		return bean;
	}
	
	@Managed
	private Variable creatorVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Creator");
		bean.setDefaultValue("*");
		bean.setTypeSignature("string");
		return bean;
	}
	
	@Managed
	private Variable creatorIdVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Creator ID");
		bean.setDefaultValue("*");
		bean.setTypeSignature("string");
		return bean;
	}

	@Managed
	private Variable wopiSessionStatusVariable() {
		Variable bean = create(Variable.T);
		bean.setName("Status");
		bean.setDefaultValue("*");
		bean.setTypeSignature("object");
		return bean;
	}


	// WOPI session statistics
	@Managed
	@Override
	public Folder wopiSessionStatistics() {
		Folder bean = create(Folder.T).initFolder("WOPI Statistics", "WOPI statistics");
		bean.setIcon(icons.magnifierIcon());
		//@formatter:off
		bean.setContent(
			queryAction("WOPI Statistics",null,list(),false,  
			c -> {
				SelectQuery q = new SelectQueryBuilder()									
						.from(WopiSession.T,"w")
							
						.groupBy()
							.property(WopiSession.context)
						.groupBy()
							.property(WopiSession.status)
						.groupBy()
							.property(WopiSession.tenant)
						.groupBy()
							.property(WopiSession.documentMode)
							
						.select("w", WopiSession.context)
						.select("w", WopiSession.tenant)
						.select("w", WopiSession.status)
						.select("w", WopiSession.documentMode)
						.select().count("w",WopiSession.correlationId)
						
						.orderBy()
							.property("w", WopiSession.context).orderingDirection(OrderingDirection.descending)
						.orderBy()
							.property("w", WopiSession.tenant).orderingDirection(OrderingDirection.descending)
						.orderBy()
							.property("w", WopiSession.status).orderingDirection(OrderingDirection.descending)
						.done();
				return q;
			}));
		//@formatter:on
		return bean;
	}

	// Open WOPI session
	@Managed
	@Override
	public Folder openWopiSession() {
		Folder bean = create(Folder.T).initFolder(WOPI_SESSION_OPEN, WOPI_SESSION_OPEN);

		bean.setIcon(icons.addIcon());
		bean.setContent(openWopiSessionAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction openWopiSessionAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_SESSION_OPEN));
		bean.setTemplate(openWopiSessionTemplate());
		bean.setForceFormular(true);

		return bean;
	}

	@Managed
	private Template openWopiSessionTemplate() {
		return importEntities(
				Templates.template(create(LocalizedString.T).putDefault(WOPI_SESSION_OPEN)).prototype(c -> c.create(OpenWopiSession.T)).record(c -> {
					OpenWopiSession prototype = c.getPrototype();

					c.pushVariable(OpenWopiSession.correlationId).addMetaData(create(Name.T).name(CORRELATIONID_NAME))
							.addMetaData(create(Description.T).description(CORRELATIONID_DESCRIPTION));
					prototype.setCorrelationId((String) prototype.entityType().getProperty(OpenWopiSession.correlationId).getInitializer());

					c.pushVariable(OpenWopiSession.documentMode).addMetaData(create(Name.T).name(DOCUMENT_MODE_NAME))
							.addMetaData(create(Description.T).description(DOCUMENT_MODE_DESCRIPTION));
					EnumReference orientationValue = (EnumReference) prototype.entityType().getProperty(OpenWopiSession.documentMode)
							.getInitializer();
					DocumentMode documentMode = null;
					if (orientationValue != null) {
						documentMode = DocumentMode.valueOf(orientationValue.getConstant());
					}
					prototype.setDocumentMode(documentMode);

					c.pushVariable(OpenWopiSession.allowedRoles).addMetaData(create(Name.T).name(ALLOWED_ROLES_NAME))
							.addMetaData(create(Description.T).description(ALLOWED_ROLES_DESCRIPTION));
					prototype.setAllowedRoles((Set<String>) prototype.entityType().getProperty(OpenWopiSession.allowedRoles).getInitializer());

					c.pushVariable(OpenWopiSession.resource).addMetaData(create(Name.T).name(RESOURCE_NAME))
							.addMetaData(create(Description.T).description(RESOURCE_DESCRIPTION));
					prototype.setResource((Resource) prototype.entityType().getProperty(OpenWopiSession.resource).getInitializer());

					c.pushVariable(OpenWopiSession.tenant).addMetaData(create(Name.T).name(TENANT_NAME))
							.addMetaData(create(Description.T).description(TENANT_DESCRIPTION));
					prototype.setTenant((String) prototype.entityType().getProperty(OpenWopiSession.tenant).getInitializer());

					// UI customization
					c.pushVariable(OpenWopiSession.showUserFriendlyName).addMetaData(create(Name.T).name(SHOW_USER_FRIENDLY_NAME_NAME));
					prototype.setShowUserFriendlyName(
							(Boolean) prototype.entityType().getProperty(OpenWopiSession.showUserFriendlyName).getInitializer());

					c.pushVariable(OpenWopiSession.showBreadcrumbBrandName).addMetaData(create(Name.T).name(SHOW_BREADCRUMB_BRAND_NAME_NAME));
					prototype.setShowBreadcrumbBrandName(
							(Boolean) prototype.entityType().getProperty(OpenWopiSession.showBreadcrumbBrandName).getInitializer());

					c.pushVariable(OpenWopiSession.breadcrumbBrandName).addMetaData(create(Name.T).name(BREADCRUMB_BRAND_NAME_NAME));
					prototype.setBreadcrumbBrandName(
							(String) prototype.entityType().getProperty(OpenWopiSession.breadcrumbBrandName).getInitializer());

					c.pushVariable(OpenWopiSession.breadcrumbBrandNameUrl).addMetaData(create(Name.T).name(URL_BREADCRUMB_BRAND_NAME));
					prototype.setBreadcrumbBrandNameUrl(
							(String) prototype.entityType().getProperty(OpenWopiSession.breadcrumbBrandNameUrl).getInitializer());

					c.pushVariable(OpenWopiSession.showBreadcrumbDocName).addMetaData(create(Name.T).name(SHOW_BREADCRUMB_DOC_NAME_NAME));
					prototype.setShowBreadcrumbDocName(
							(Boolean) prototype.entityType().getProperty(OpenWopiSession.showBreadcrumbDocName).getInitializer());

					c.pushVariable(OpenWopiSession.breadcrumbDocName).addMetaData(create(Name.T).name(BREADCRUMB_DOC_NAME_NAME));
					prototype.setBreadcrumbDocName((String) prototype.entityType().getProperty(OpenWopiSession.breadcrumbDocName).getInitializer());

					c.pushVariable(OpenWopiSession.showBreadcrumbFolderName).addMetaData(create(Name.T).name(SHOW_BREADCRUMB_FOLDER_NAME_NAME));
					prototype.setShowBreadcrumbFolderName(
							(Boolean) prototype.entityType().getProperty(OpenWopiSession.showBreadcrumbFolderName).getInitializer());

					c.pushVariable(OpenWopiSession.breadcrumbFolderName).addMetaData(create(Name.T).name(BREADCRUMB_FOLDER_NAME_NAME));
					prototype.setBreadcrumbFolderName(
							(String) prototype.entityType().getProperty(OpenWopiSession.breadcrumbFolderName).getInitializer());

					c.pushVariable(OpenWopiSession.breadcrumbFolderNameUrl).addMetaData(create(Name.T).name(URL_BREADCRUMB_FOLDER_NAME));
					prototype.setBreadcrumbFolderNameUrl(
							(String) prototype.entityType().getProperty(OpenWopiSession.breadcrumbFolderNameUrl).getInitializer());

					c.pushVariable(OpenWopiSession.disablePrint).addMetaData(create(Name.T).name(DISABLE_PRINT_NAME));
					prototype.setDisablePrint((Boolean) prototype.entityType().getProperty(OpenWopiSession.disablePrint).getInitializer());

					c.pushVariable(OpenWopiSession.disableTranslation).addMetaData(create(Name.T).name(DISABLE_TRANSLATION_NAME));
					prototype
							.setDisableTranslation((Boolean) prototype.entityType().getProperty(OpenWopiSession.disableTranslation).getInitializer());

					prototype.setSendNotifications(true);
				}).build());
	}

	// Close all WOPI sessions
	@Managed
	@Override
	public Folder closeAllWopiSessions() {
		Folder bean = create(Folder.T).initFolder(WOPI_SESSION_CLOSE_ALL, WOPI_SESSION_CLOSE_ALL);

		bean.setIcon(icons.removeIcon());
		bean.setContent(closeAllWopiSessionsAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction closeAllWopiSessionsAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_SESSION_CLOSE_ALL));
		bean.setTemplate(closeAllWopiSessionsTemplate());

		return bean;
	}

	@Managed
	private Template closeAllWopiSessionsTemplate() {
		return importEntities(Templates.template(create(LocalizedString.T).putDefault(WOPI_SESSION_CLOSE_ALL))
				.prototype(c -> c.create(CloseAllWopiSessions.T)).record(c -> {
					CloseAllWopiSessions prototype = c.getPrototype();

					prototype.setSendNotifications(true);
				}).build());
	}

	// Remove all WOPI sessions
	@Managed
	@Override
	public Folder removeAllWopiSessions() {
		Folder bean = create(Folder.T).initFolder(WOPI_SESSION_REMOVE_ALL, WOPI_SESSION_REMOVE_ALL);

		bean.setIcon(icons.deleteIcon());
		bean.setContent(removeAllWopiSessionsAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction removeAllWopiSessionsAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_SESSION_REMOVE_ALL));
		bean.setTemplate(removeAllWopiSessionsTemplate());
		bean.setForceFormular(true);

		return bean;
	}

	@Managed
	private Template removeAllWopiSessionsTemplate() {
		return importEntities(Templates.template(create(LocalizedString.T).putDefault(WOPI_SESSION_REMOVE_ALL))
				.prototype(c -> c.create(RemoveAllWopiSessions.T)).record(c -> {
					RemoveAllWopiSessions prototype = c.getPrototype();

					c.pushVariable(RemoveAllWopiSessions.forceRemove);
					prototype.setForceRemove((Boolean) prototype.entityType().getProperty(RemoveAllWopiSessions.forceRemove).getInitializer());

					c.pushVariable(RemoveAllWopiSessions.context);
					prototype.setContext((String) prototype.entityType().getProperty(RemoveAllWopiSessions.context).getInitializer());

					prototype.setSendNotifications(true);
				}).build());
	}

	// WOPI health check
	@Managed
	@Override
	public Folder wopiHealthCheck() {
		Folder bean = create(Folder.T).initFolder(WOPI_HEALTH_CHECK, WOPI_HEALTH_CHECK);

		bean.setIcon(icons.healthIcon());
		bean.setContent(wopiHealthCheckAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction wopiHealthCheckAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_HEALTH_CHECK));
		bean.setTemplate(wopiHealthCheckTemplate());
		bean.setForceFormular(true);

		return bean;
	}

	@Managed
	private Template wopiHealthCheckTemplate() {
		return importEntities(
				Templates.template(create(LocalizedString.T).putDefault(WOPI_HEALTH_CHECK)).prototype(c -> c.create(WopiHealthCheck.T)).record(c -> {

					WopiHealthCheck prototype = c.getPrototype();

					c.pushVariable(WopiHealthCheck.simple);
					prototype.setSimple((Boolean) prototype.entityType().getProperty(WopiHealthCheck.simple).getInitializer());

					c.pushVariable(WopiHealthCheck.numberOfChecks);
					prototype.setNumberOfChecks((Integer) prototype.entityType().getProperty(WopiHealthCheck.numberOfChecks).getInitializer());

					prototype.setSendNotifications(true);
				}).build());
	}

	// -----------------------------------------------------------------------
	// DEMO and TESTING
	// -----------------------------------------------------------------------

	// Add DemoDocs
	@Managed
	@Override
	public Folder addDemoDocs() {
		Folder bean = create(Folder.T).initFolder(WOPI_DEMO_ADD, WOPI_DEMO_ADD);

		bean.setIcon(icons.addIcon());
		bean.setContent(addDemoDocsAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction addDemoDocsAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_DEMO_ADD));
		bean.setTemplate(addDemoDocsTemplate());
		bean.setForceFormular(true);

		return bean;
	}

	@Managed
	private Template addDemoDocsTemplate() {
		return importEntities(
				Templates.template(create(LocalizedString.T).putDefault(WOPI_DEMO_ADD)).prototype(c -> c.create(AddDemoDocs.T)).record(c -> {
					AddDemoDocs prototype = c.getPrototype();

					c.pushVariable(AddDemoDocs.onlyMainTypes);
					prototype.setOnlyMainTypes((Boolean) prototype.entityType().getProperty(AddDemoDocs.onlyMainTypes).getInitializer());

					prototype.setSendNotifications(true);
				}).build());
	}

	// Remove DemoDocs
	@Managed
	@Override
	public Folder removeDemoDocs() {
		Folder bean = create(Folder.T).initFolder(WOPI_DEMO_REMOVE, WOPI_DEMO_REMOVE);

		bean.setIcon(icons.removeIcon());
		bean.setContent(removeDemoDocsAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction removeDemoDocsAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_DEMO_REMOVE));
		bean.setTemplate(removeDemoDocsTemplate());

		return bean;
	}

	@Managed
	private Template removeDemoDocsTemplate() {
		return importEntities(
				Templates.template(create(LocalizedString.T).putDefault(WOPI_DEMO_REMOVE)).prototype(c -> c.create(RemoveDemoDocs.T)).record(c -> {
					RemoveDemoDocs prototype = c.getPrototype();

					prototype.setSendNotifications(true);
				}).build());
	}

	// Ensure Test Doc
	@Managed
	@Override
	public Folder ensureTestDoc() {
		Folder bean = create(Folder.T).initFolder(WOPI_ENSURE_TES_DOC, WOPI_ENSURE_TES_DOC);

		bean.setIcon(icons.nextIcon());
		bean.setContent(ensureTestDocAction());

		return bean;
	}

	@Managed
	private TemplateServiceRequestAction ensureTestDocAction() {
		TemplateServiceRequestAction bean = create(TemplateServiceRequestAction.T);

		bean.setDisplayName(create(LocalizedString.T).putDefault(WOPI_ENSURE_TES_DOC));
		bean.setTemplate(ensureTestDocTemplate());

		return bean;
	}

	@Managed
	private Template ensureTestDocTemplate() {
		return importEntities(
				Templates.template(create(LocalizedString.T).putDefault(WOPI_ENSURE_TES_DOC)).prototype(c -> c.create(EnsureTestDoc.T)).record(c -> {
					EnsureTestDoc prototype = c.getPrototype();

					c.pushVariable(EnsureTestDoc.testNames);
					prototype.setTestNames(null);

					c.pushVariable(OpenWopiSession.documentMode).addMetaData(create(Name.T).name(DOCUMENT_MODE_NAME))
							.addMetaData(create(Description.T).description(DOCUMENT_MODE_DESCRIPTION));
					EnumReference orientationValue = (EnumReference) prototype.entityType().getProperty(OpenWopiSession.documentMode)
							.getInitializer();
					DocumentMode documentMode = null;
					if (orientationValue != null) {
						documentMode = DocumentMode.valueOf(orientationValue.getConstant());
					}
					prototype.setDocumentMode(documentMode);

					prototype.setSendNotifications(true);
				}).build());
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	@Managed
	private IsAssignableTo isAssignableToWopiSession() {
		IsAssignableTo bean = create(IsAssignableTo.T);
		bean.setTypeSignature(WopiSession.T.getTypeSignature());
		return bean;
	}

	private ConjunctionCriterion and(TraversingCriterion... criteria) {
		ConjunctionCriterion bean = create(ConjunctionCriterion.T);
		bean.setCriteria(Arrays.asList(criteria));
		return bean;
	}

	private DisjunctionCriterion or(TraversingCriterion... criteria) {
		DisjunctionCriterion bean = create(DisjunctionCriterion.T);
		bean.setCriteria(Arrays.asList(criteria));
		return bean;
	}

	@Managed
	private ValueConditionCriterion isOpenWopiSessionStatusCriterion() {
		ValueConditionCriterion bean = create(ValueConditionCriterion.T);
		bean.setPropertyPath(WopiSession.status);
		bean.setOperator(ComparisonOperator.equal);
		bean.setOperand(WopiStatus.open);
		return bean;
	}

	@Managed
	private ValueConditionCriterion isExpiredWopiSessionStatusCriterion() {
		ValueConditionCriterion bean = create(ValueConditionCriterion.T);
		bean.setPropertyPath(WopiSession.status);
		bean.setOperator(ComparisonOperator.equal);
		bean.setOperand(WopiStatus.expired);
		return bean;
	}

	// ---------

	private <P extends GenericEntity> TemplateQueryAction queryAction(String actionName, TraversingCriterion contextCriterion,
			Collection<TemplateMetaData> templateMetaData, boolean forceForm, Function<TemplatePrototypingContext, P> prototypeFactory) {
		return queryAction(actionName, contextCriterion, templateMetaData, forceForm, prototypeFactory, null);
	}

	private <P extends GenericEntity> TemplateQueryAction queryAction(String actionName, TraversingCriterion contextCriterion,
			Collection<TemplateMetaData> templateMetaData, boolean forceForm, Function<TemplatePrototypingContext, P> prototypeFactory,
			Consumer<TemplateRecordingContext<P>> recorder) {
		return action(TemplateQueryAction.T, actionName, contextCriterion, templateMetaData, forceForm, prototypeFactory, recorder);
	}

	private <P extends GenericEntity, A extends TemplateBasedAction> A action(EntityType<A> actionType, String actionName,
			TraversingCriterion contextCriterion, Collection<TemplateMetaData> templateMetaData, boolean forceForm,
			Function<TemplatePrototypingContext, P> prototypeFactory, Consumer<TemplateRecordingContext<P>> recorder) {
		A action = create(actionType);
		action.setDisplayName(createLs(actionName));
		action.setForceFormular(forceForm);

		//@formatter:off
		action.setTemplate(
			template(
				actionName, 
				prototypeFactory, 
				recorder, 
				templateMetaData));
		//@formatter:off
		
		if (contextCriterion != null) {
			action.setInplaceContextCriterion(contextCriterion);
		}
		
		return action;
	}
	
	private <P> Template template(String folderDisplayName, Function<TemplatePrototypingContext, P> prototypeFactory, Consumer<TemplateRecordingContext<P>> recorder, Collection<TemplateMetaData> templateMetaData) {
		//@formatter:off
		TemplateBuilder<P> builder = Templates
				.template(createLs(folderDisplayName))
				.prototype(prototypeFactory);
		//@formatter:on

		if (templateMetaData != null) {
			//@formatter:off
			templateMetaData
				.stream()
				.forEach(builder::addMetaData);
			//@formatter:on
		}
		if (recorder != null) {
			builder.record(recorder);
		}
		return importEntities(builder.build());
	}

	private LocalizedString createLs(String displayName) {
		return create(LocalizedString.T).putDefault(displayName);
	}
}
