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
package com.braintribe.model.processing.workbench.experts;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.StandardRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.model.workbench.instruction.CreateTemplateBasedAction;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.i18n.I18nTools;

public class CreateTemplateBasedActionExpert implements WorkbenchInstructionExpert<CreateTemplateBasedAction> {

	private static final Logger logger = Logger.getLogger(CreateTemplateBasedActionExpert.class);


	private static final List<Property> genericEntityProperties = GenericEntity.T.getProperties();
	// ServiceRequests
	private static final List<Property> serviceRequestProperties = ServiceRequest.T.getProperties();
	private static final List<Property> domainRequestProperties = DomainRequest.T.getProperties();
	private static final List<Property> standardRequestProperties = StandardRequest.T.getProperties();
	// QueryRequests
	private static final List<Property> queryRequestProperties = Query.T.getProperties();
	private static final List<Property> selectQueryRequestProperties = SelectQuery.T.getProperties();
	private static final List<Property> entityQueryRequestProperties = EntityQuery.T.getProperties();
	private static final List<Property> propertyQueryRequestProperties = PropertyQuery.T.getProperties();
	
	private static final List<Property> standardProperties = new ArrayList<>();
	
	static {
		standardProperties.addAll(genericEntityProperties);
		standardProperties.addAll(serviceRequestProperties);
		standardProperties.addAll(domainRequestProperties);
		standardProperties.addAll(standardRequestProperties);
		standardProperties.addAll(queryRequestProperties);
		standardProperties.addAll(selectQueryRequestProperties);
		standardProperties.addAll(entityQueryRequestProperties);
		standardProperties.addAll(propertyQueryRequestProperties);
	}

	@Override
	public void process(CreateTemplateBasedAction instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
		
		String actionName = instruction.getActionName();
		PersistenceGmSession workbenchSession = context.getSession();
		
		Set<String> variableProperties = determineVariableProperties(instruction.getPrototype(), instruction.getIgnoreStandardProperties(), instruction.getIgnoreProperties());
		GenericEntity prototype = syncPrototype(instruction.getPrototype(), workbenchSession);
		Manipulation manipulation = createScript(workbenchSession, prototype, variableProperties);
		
		
		Folder parentFolder = ensureFolderPath(workbenchSession, context, instruction.getPath());
		Folder actionFolder = acquireFolder(workbenchSession, actionName, parentFolder);
		
		EntityType<? extends TemplateBasedAction> actionType = GMF.getTypeReflection().getEntityType(instruction.getActionType());
		
		TemplateBasedAction folderContent = acquireFolderContent(workbenchSession, actionFolder, actionType, instruction.getCriterion(), instruction.getMultiSelectionSupport());

		Template template = folderContent.getTemplate();
		if (template == null) {
			template = workbenchSession.create(Template.T);
			folderContent.setTemplate(template);
		}
		
		EntityType<GenericEntity> prototypeType = prototype.entityType();
		template.setName(I18nTools.createLs(workbenchSession,prototypeType.getShortName()+" Template"));
		template.setTechnicalName(prototypeType.getShortName()+"Template");
		template.setPrototype(prototype);
		template.setPrototypeTypeSignature(prototypeType.getTypeSignature());
		template.setScript(manipulation);
		
		Map<String,Variable> variables = collectAndAdaptVariables(template, workbenchSession, instruction.getBeautifyVariableNames());
		
		if (!instruction.getTemplateMetaData().isEmpty()) {
			Set<TemplateMetaData> templateMetaData = 
					(Set<TemplateMetaData>) cloneToSession(
							workbenchSession, 
							instruction.getTemplateMetaData(), 
							Collections.emptySet(),
							(entity) -> {
								
								if (entity instanceof Variable) {
									Variable variable = (Variable) entity;
									return variables.get(variable.getName());
								}
								
								return null;
							
							});

			template.setMetaData(templateMetaData);
		}

	}
	
	
	private Set<String> determineVariableProperties(GenericEntity prototype, boolean ignoreStandardProperties, Set<String> ignoredProperties) {
		Set<String> variableProperties = new HashSet<>();
		EntityType<GenericEntity> templateType = prototype.entityType();
		for (Property templateProperty : templateType.getProperties()) {
			String propertyName = templateProperty.getName();
			
			if (ignoreStandardProperties && standardProperties.contains(templateProperty)) {
				continue;
			}
			
			if (ignoredProperties.contains(propertyName)) {
				continue;
			}
			variableProperties.add(propertyName);
		}
		
		return variableProperties;
	}
	
	private static GenericEntity syncPrototype(GenericEntity prototype, PersistenceGmSession workbenchSession) {
		GenericEntity syncedPrototype = 
				(GenericEntity) cloneToSession(
						workbenchSession, 
						prototype, 
						Collections.singleton(ServiceRequest.globalId)
						/*CollectionTools.getUnion(
								variableProperties, 
								Collections.singleton(ServiceRequest.globalId))*/);
		
		
		workbenchSession.commit();
		return syncedPrototype;
	}

	
	private static Object cloneToSession(final PersistenceGmSession session, final Object toBeCloned, final Collection<String> ignoredProperties) {
		return cloneToSession(session, toBeCloned, ignoredProperties, (entity) -> { 
			
			if (entity instanceof LocalEntityProperty) {
				LocalEntityProperty lep = (LocalEntityProperty) entity;
				GenericEntity refEntity = session.query().entity(lep.getEntity().entityType(), lep.getEntity().getId()).require();
				LocalEntityProperty result = session.create(LocalEntityProperty.T);
				result.setEntity(refEntity);
				result.setPropertyName(lep.getPropertyName());
				return result;
			}

			return null;
		
		});
	}
	
	private static Object cloneToSession(final PersistenceGmSession session, final Object toBeCloned, final Collection<String> ignoredProperties, final Function<GenericEntity, GenericEntity> entityProvider) {
		
		return GMF.getTypeReflection().getType(toBeCloned).clone(new StandardCloningContext() {
			
			
			@Override
			public <T> T getAssociated(GenericEntity entity) {
				if (entityProvider != null) {
					try {
						T provided = (T) entityProvider.apply(entity);
						if (provided != null) {
							return provided;
						}
					} catch (Exception e) {
						throw new RuntimeException("entityProvider failed for entity: "+entity,e);
					}
				}
				return super.getAssociated(entity);
			}
			
			
			@Override
			public GenericEntity supplyRawClone(
					EntityType<? extends GenericEntity> entityType,
					GenericEntity instanceToBeCloned) {

				return session.create(entityType);
			}
			@Override
			public boolean canTransferPropertyValue(
					EntityType<? extends GenericEntity> entityType,
					Property property, GenericEntity instanceToBeCloned,
					GenericEntity clonedInstance,
					AbsenceInformation sourceAbsenceInformation) {
				
				if (property.isIdentifier()) {
					return false;
				}
				
				if (property.getName().equals(GenericEntity.globalId)) {
					return false;
				}
				
				if (ignoredProperties.contains(property.getName())) {
					return false;
				}
				
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned,
						clonedInstance, sourceAbsenceInformation);
			}
		}, toBeCloned, StrategyOnCriterionMatch.skip);
		
	}
	
	private static Manipulation createScript(final PersistenceGmSession workbenchSession, GenericEntity prototype, Set<String> variablePropertyNames) throws TransactionException {
		Manipulation script = null;
		Manipulation clonedScript = null;
		NestedTransaction scriptTransaction = workbenchSession.getTransaction().beginNestedTransaction();
		
		final Map<String, Property> variableProperties = new HashMap<>();
		try {
			
			EntityType<GenericEntity> type = prototype.entityType();
			for (Property property : type.getProperties()) {
				if (variablePropertyNames.contains(property.getName())) {
					property.set(prototype, property.get(prototype));
					variableProperties.put(property.getName(), property);
				}
			}
			
		} finally {
			script = compound(ManipulationTools.inline(scriptTransaction.getManipulationsDone()));
			scriptTransaction.rollback();
		}
		
		
		clonedScript = (Manipulation) cloneToSession(workbenchSession, script, Collections.singleton("inverseManipulation"));
		
		
		EntityType<CompoundManipulation> type = clonedScript.entityType();
		type.traverse(clonedScript, null, new EntityVisitor() {
			
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				if (entity instanceof ChangeValueManipulation) {
					ChangeValueManipulation cvm = (ChangeValueManipulation) entity;
					String propertyName = cvm.getOwner().getPropertyName();
					Property property = variableProperties.get(propertyName);
					if (property != null) {
						
						Variable variable = workbenchSession.create(Variable.T);
						variable.setName(propertyName);
						variable.setTypeSignature(property.getType().getTypeSignature());
						
						Object propertyValue = property.get(prototype);
						if (propertyValue != null) {
							// set the property value as default value of the variable
							variable.setDefaultValue(propertyValue);
						}
						
						cvm.setNewValue(variable);

					}
				}
			}

		});
		return clonedScript;
	}
	
	
	private static Map<String,Variable> collectAndAdaptVariables(Template template, PersistenceGmSession workbenchSession, boolean beautifyName) {
		Map<String,Variable> variables = new HashMap<>();
		
		Template.T.traverse(template, null, new EntityVisitor() {
			
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				if (entity instanceof Variable) {
					Variable variable = (Variable) entity;
					String name = variable.getName();
					if (name != null) {
						variables.put(name, variable);
						if (beautifyName && variable.getLocalizedName() == null) {
							variable.setLocalizedName(I18nTools.createLs(workbenchSession, beautifyPropertyName(name)));
						}
						if (variable.getTypeSignature() == null) {
							variable.setTypeSignature("string");
						}
					} else {
						logger.warn("Variable without a name found: "+variable);
					}
				}
			}

		});
		
		return variables;

	}
	
	/**
	 * Returns the last folder of the path
	 */
	private Folder ensureFolderPath(PersistenceGmSession session, WorkbenchInstructionContext context, String path) {
		List<String> pathElements = context.getPathElements(path);
		Folder current = null;
		for (String element : pathElements) {
			if (element.isEmpty()) {
				logger.warn("Ignoring empty path element found in path: "+path);
				continue;
			}
			
			current = acquireFolder(session, element, current);
		}
		return current;
	}

	private Folder acquireFolder(PersistenceGmSession session, String folderName, Folder parent) throws GmSessionException {
		
		if (parent != null) {
			
			// Search requested folder in sub folders of given parent
			for (Folder subFolder : parent.getSubFolders()) {
				if (subFolder.getName().equalsIgnoreCase(folderName)) {
					return subFolder;
				}
			}
			
		} else {
			
			// No Parent given. Try to find requested folder as root folder (parent=null).
			// @formatter:off
			EntityQuery lookup = 
					EntityQueryBuilder
					.from(Folder.class)
					.where()
					.conjunction()
						.property("name").eq(folderName)
						.property("parent").eq(null)
					.close()
					.done();	

			Folder folder = 
					session
						.query()
						.entities(lookup)
						.first();
			// @formatter:on
			
			if (folder != null) {
				return folder;
			}
			
		}
		
		// We coudldn't find the folder. Let's create it.
		Folder folder = session.create(Folder.T);
		folder.setName(folderName);
		folder.setDisplayName(createLs(session, folderName));
		
		if (parent != null) {
			parent.getSubFolders().add(folder);
			folder.setParent(parent);
			logger.debug("Created new folder: "+folderName+" as subFolder of: "+parent.getName());
		} else {
			logger.debug("Created new folder: "+folderName+" as root folder.");
		}
		
		return folder;
	}
	
	private TemplateBasedAction acquireFolderContent(PersistenceGmSession workbenchSession, Folder actionFolder, EntityType<? extends TemplateBasedAction> expectedActionType, TraversingCriterion criterion, boolean multiSelectionSupport) {
		TemplateBasedAction templatePrototype = acquireTemplateBasedAction(workbenchSession, actionFolder, expectedActionType);
		if (criterion != null) {
			TraversingCriterion clonedCriterion = (TraversingCriterion) cloneToSession(workbenchSession, criterion, Collections.emptySet());
			templatePrototype.setInplaceContextCriterion(clonedCriterion);
			templatePrototype.setMultiSelectionSupport(multiSelectionSupport);
		}
		actionFolder.setContent(templatePrototype);
		return templatePrototype;
	}

	
	private static TemplateBasedAction acquireTemplateBasedAction(PersistenceGmSession workbenchSession, Folder actionFolder, EntityType<? extends TemplateBasedAction> expectedActionType) {
		FolderContent content = actionFolder.getContent();
		String actionName = actionFolder.getName();
		TemplateBasedAction action = null;
		
		if (content != null && content.entityType().equals(expectedActionType) ) {
			// The currently set folderContent is an instance of our expectedAction. Thus we can reuse it. 
			action = (TemplateBasedAction) content;
		}
		
		if (action == null) {
			action = workbenchSession.create(expectedActionType);
			action.setDisplayName(createLs(workbenchSession, actionName));
		}
		
		return action;
	}
	
	private static String beautifyPropertyName(String propertyName) {
		return StringTools.prettifyCamelCase(propertyName);
	}

	private static LocalizedString createLs(PersistenceGmSession session, String actionName) {
		LocalizedString ls = session.create(LocalizedString.T);
		ls.setLocalizedValues(new HashMap<String, String>());
		ls.getLocalizedValues().put("default", actionName);
		return ls;
	}
	

}
