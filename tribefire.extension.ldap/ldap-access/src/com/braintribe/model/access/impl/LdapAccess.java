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
package com.braintribe.model.access.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.registry.BasicCodecs;
import com.braintribe.codec.registry.CodecRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.impl.useraccess.LdapPagingContext;
import com.braintribe.model.access.impl.useraccess.LdapPagingContext.RangeResult;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.ldap.LdapFulltextProperty;
import com.braintribe.model.ldap.LdapObjectClasses;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextInfo;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Comparison;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;

import com.braintribe.utils.ldap.LdapConnection;

/**
 * Generic LDAP Access that requires meta-data on entity types and properties to
 * match them with LDAP entries and attributes. This is not yet a finished
 * implementation, but basic searches work. Next step would be to automatically
 * extract the LDAP objectClasses and create the models on the fly.
 * 
 * @author roman.kurmanowytsch
 */
public class LdapAccess extends AbstractAccess implements InitializationAware {

	protected static Logger logger = Logger.getLogger(LdapAccess.class);

	protected Supplier<GmMetaModel> metaModelProvider = null;
	protected LdapConnection ldapConnectionStack = null;
	protected String base = null;
	protected Supplier<Set<String>> rolesProvider = null;
	protected ModelOracle modelOracle = null;
	protected CmdResolver resolver = null;
	protected BasicModelAccessory modelAccessory = null;

	protected String accessId;
	protected Set<String> useCases = null;

	protected CodecRegistry codecRegistry = null;

	protected int searchPageSize = 100;

	@Override
	public void postConstruct() {
		GmMetaModel metaModel = this.metaModelProvider.get();
		modelOracle = new BasicModelOracle(metaModel);
		
		modelAccessory = new BasicModelAccessory() {
			@Override
			protected GmMetaModel loadModel() {
				return metaModel;
			}
			@Override
			protected boolean adoptLoadedModel() {
				return false;
			}
		};
		
		ResolutionContextInfo resolutionContextInfo = new ResolutionContextInfo(modelOracle);

		Map<Class<? extends SelectorContextAspect<?>>, Object> aspectValues = new HashMap<Class<? extends SelectorContextAspect<?>>, Object>();
		aspectValues.put(AccessAspect.class, this.accessId);

		if ((this.useCases != null) && (!this.useCases.isEmpty())) {
			aspectValues.put(UseCaseAspect.class, this.useCases);
		}

		resolutionContextInfo.setStaticAspects(aspectValues);
		this.resolver = new CmdResolverImpl(modelOracle);

		this.codecRegistry = new CodecRegistry();
		BasicCodecs.registerCodecs(codecRegistry);

	}

	protected ModelMdResolver getMetaData() {
		ModelMdResolver metaDataContextBuilder = this.resolver.getMetaData();
		if (this.rolesProvider != null) {
			Set<String> roles = null;
			try {
				roles = this.rolesProvider.get();
			} catch (RuntimeException e) {
				logger.error("Could not acquire current roles.", e);
			}
			if (roles != null) {
				metaDataContextBuilder.with(RoleAspect.class, roles);
			}
		}
		return metaDataContextBuilder;
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		return null;
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {

		ModelMdResolver metaData = getMetaData();

		LdapQueryContext queryContext = null;
		try {
			queryContext = new LdapQueryContext(metaData, modelAccessory);
		} catch (Exception e) {
			throw new ModelAccessException("Could not initialize query context.", e);
		}

		queryContext.entityTypeSignature = request.getEntityTypeSignature();
		EntityType<GenericEntity> entityType = typeReflection.getEntityType(queryContext.entityTypeSignature);

		try {
			queryContext.setEntityType(entityType);
			queryContext.ldapObjectClasses = this.getObjectClasses(queryContext);
		} catch (Exception e) {
			throw new ModelAccessException("Could not get the object classes for type " + entityType, e);
		}

		List<GenericEntity> resultGenericEntities = new ArrayList<GenericEntity>();

		boolean hasMore = this.search(request, resultGenericEntities, queryContext);

		// Create the query result.
		EntityQueryResult entityQueryResult = EntityQueryResult.T.create();
		entityQueryResult.setEntities(resultGenericEntities);
		entityQueryResult.setHasMore(hasMore);

		return entityQueryResult;
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {

		try {
			ModelMdResolver metaData = getMetaData();

			LdapQueryContext queryContext = null;
			try {
				queryContext = new LdapQueryContext(metaData, modelAccessory);
			} catch (Exception e) {
				throw new ModelAccessException("Could not initialize query context.", e);
			}

			PersistentEntityReference entityReference = request.getEntityReference();
			LdapName entityId = new LdapName((String) entityReference.getRefId());
			Property requestedProperty = request.property();

			queryContext.entityTypeSignature = entityReference.getTypeSignature();
			EntityType<GenericEntity> entityType = typeReflection.getEntityType(queryContext.entityTypeSignature);
			try {
				queryContext.setEntityType(entityType);
			} catch (Exception e) {
				throw new ModelAccessException("Could not get the object classes for type " + entityType, e);
			}

			Object resultObject = this.getAttributeValuesForDn(entityId, requestedProperty, queryContext);

			PropertyQueryResult result = PropertyQueryResult.T.create();

			result.setPropertyValue(clonePropertyQueryResult(requestedProperty, resultObject, request));
			result.setHasMore(false);

			return result;

		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to get properties: " + request, e);
		}

	}

	protected Object getAttributeValuesForDn(LdapName dn, Property property, LdapQueryContext queryContext)
			throws Exception {
		LdapContext dirContext = null;
		try {
			dirContext = this.ldapConnectionStack.pop();

			Attributes attributes = null;
			try {

				String attributeName = this.getAttributeName(queryContext, property.getName());
				if (attributeName == null) {
					return null;
				}
				attributes = dirContext.getAttributes(dn, new String[] { attributeName });

				if (attributes != null) {

					NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
					if (attributeEnumeration.hasMore()) {

						Attribute attr = attributeEnumeration.next();
						Object valueObject = this.getAttributeValuesAsObject(property, attr);
						return valueObject;
					}
				}

			} catch (Exception e) {
				throw new ModelAccessException("Error while trying to get groups for " + dn, e);
			}

		} finally {
			this.ldapConnectionStack.push(dirContext);
		}

		return null;
	}

	protected String createFilter(EntityQuery request, LdapQueryContext queryContext) throws Exception {

		Restriction restriction = request.getRestriction();
		String result = null;
		if (restriction != null) {
			Condition condition = restriction.getCondition();
			result = this.processCondition(condition, queryContext);
		}

		result = this.extendFilterByObjectClasses(result, queryContext.ldapObjectClasses);

		return result;

	}

	protected String extendFilterByObjectClasses(String filter, Set<String> ldapObjectClasses) {
		if ((ldapObjectClasses != null) && (ldapObjectClasses.size() > 0)) {
			StringBuilder sb = new StringBuilder();
			sb.append("(&");
			if (filter != null && !filter.isEmpty()) {
				if (!filter.startsWith("(")) {
					sb.append("(");
					sb.append(filter);
					sb.append(")");
				} else {
					sb.append(filter);
				}
			}
			ldapObjectClasses.forEach(e -> sb.append("(objectClass=" + e + ")"));
			sb.append(")");
			String extendedFilter = sb.toString();
			return extendedFilter;
		} else {
			return filter;
		}
	}

	protected String processCondition(Condition condition, LdapQueryContext queryContext) throws Exception {

		if (condition == null) {
			return null;
		}

		if (condition instanceof AbstractJunction) {

			AbstractJunction junction = (AbstractJunction) condition;
			List<Condition> operands = junction.getOperands();

			if ((operands == null) || (operands.size() == 0)) {
				return null;
			}
			if (operands.size() == 1) {
				return this.processCondition(operands.get(0), queryContext);
			}

			StringBuilder sb = new StringBuilder();
			sb.append("(");

			if (condition instanceof Conjunction) {
				sb.append("&");
			} else if (condition instanceof Disjunction) {
				sb.append("|");
			}

			boolean foundValidCondition = false;
			for (Condition subCondition : operands) {
				String subConsitionString = this.processCondition(subCondition, queryContext);
				if (subConsitionString != null) {
					sb.append("(");
					sb.append(subConsitionString);
					sb.append(")");
					foundValidCondition = true;
				}
			}

			sb.append(")");

			if (!foundValidCondition) {
				return null;
			}

			return sb.toString();

		} else if (condition instanceof Comparison) {

			Comparison comparison = (Comparison) condition;

			if (comparison instanceof ValueComparison) {
				ValueComparison valComp = (ValueComparison) comparison;
				String left = this.getOperand(valComp.getLeftOperand(), queryContext);
				String right = this.getOperand(valComp.getRightOperand(), queryContext);
				if ((left == null) || (right == null)) {
					logger.debug("Either the left (" + left + ") of right side (" + right
							+ ") is null. No comparison possible.");
					return null;
				}
				Operator op = valComp.getOperator();
				switch (op) {
				case equal:
					return left + "=" + right;
				case notEqual:
					return "!" + left + "=" + right;
				case greater:
					return left + ">" + right;
				case less:
					return left + "<" + right;
				case greaterOrEqual:
					return left + ">=" + right;
				case lessOrEqual:
					return left + "<=" + right;
				case like:
					return left + "=" + right;
				case ilike:
					return left + "=" + right;
				default:
					logger.error("Unsupported operator " + op);
					return null;
				}
			} else if (comparison instanceof FulltextComparison) {

				return processFulltextComparison((FulltextComparison) condition, queryContext);

			} else {
				logger.warn("Unsupported comparison " + comparison.getClass());
				return null;
			}

		} else if (condition instanceof Negation) {

			Negation negation = (Negation) condition;
			Condition subCondition = negation.getOperand();
			String subConditionString = this.processCondition(subCondition, queryContext);
			if (subConditionString != null) {
				return "(!(" + subConditionString + "))";
			} else {
				return null;
			}

		} else {

			logger.warn("Unsupported condition type " + condition.getClass());
			return null;
		}

	}

	protected String processFulltextComparisonForProperties(String text, LdapQueryContext queryContext,
			Set<String> properties) throws Exception {

		text = "*" + text + "*";

		Disjunction disjunction = Disjunction.T.create();
		disjunction.setOperands(new ArrayList<Condition>(properties.size()));

		for (String propertyName : properties) {

			PropertyOperand leftPropertyOperand = PropertyOperand.T.create();
			leftPropertyOperand.setPropertyName(propertyName);

			ValueComparison valueComparison = ValueComparison.T.create();
			valueComparison.setLeftOperand(leftPropertyOperand);
			valueComparison.setOperator(Operator.equal);
			valueComparison.setRightOperand(text);

			disjunction.getOperands().add(valueComparison);
		}

		return processCondition(disjunction, queryContext);
	}

	protected String processFulltextComparison(FulltextComparison fulltextComparison, LdapQueryContext queryContext)
			throws Exception {

		String text = (fulltextComparison.getText() == null) ? "" : fulltextComparison.getText().trim();

		if (text.isEmpty()) {
			return null;
		}
		Set<String> attributeNames = null;
		try {
			attributeNames = this.getFulltextAttributes(queryContext);
		} catch (Exception e) {
			throw new Exception("Could not identify fulltext attributes of type " + queryContext.entityType, e);
		}
		if ((attributeNames == null) || (attributeNames.isEmpty())) {
			return null;
		}
		return processFulltextComparisonForProperties(text, queryContext, attributeNames);
	}

	protected String getOperand(Object operand, LdapQueryContext queryContext) throws Exception {

		if (operand instanceof PropertyOperand) {
			PropertyOperand propOperand = (PropertyOperand) operand;
			String propertyName = propOperand.getPropertyName();

			String attributeName = null;
			try {
				attributeName = this.getAttributeName(queryContext, propertyName);
			} catch (Exception e) {
				throw new Exception("Could not get attribute name for property " + propertyName, e);
			}
			return attributeName;

		} else if (operand instanceof String) {
			String value = (String) operand;
			return value;
		} else if (operand instanceof LocalDate) {
			LocalDate dateValue = (LocalDate) operand;
			String adDateValue = this.convertActiveDirectoryTimestamp(dateValue);
			return adDateValue;
		} else if (operand instanceof LocalDateTime) {
			LocalDateTime dateValue = (LocalDateTime) operand;
			String adDateValue = this.convertActiveDirectoryTimestamp(dateValue);
			return adDateValue;
		} else if (operand instanceof Date) {
			Date dateValue = (Date) operand;
			String adDateValue = this.convertActiveDirectoryTimestamp(dateValue);
			return adDateValue;
		} else if (operand instanceof Long) {
			Long longValue = (Long) operand;
			return "" + longValue;
		} else {
			logger.debug("Unknown type " + operand.getClass());
			return operand.toString();
		}

	}

	protected String getFirstAttributeValue(Attribute attr) throws Exception {
		Object o = attr.get();
		if (o != null) {
			return o.toString();
		}
		return null;
	}

	protected boolean search(EntityQuery request, List<GenericEntity> resultGenericEntities,
			LdapQueryContext queryContext) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();

		boolean hasMore = false;

		LdapContext dirContext = null;
		LdapContext retrieveEntryDirContext = null;
		String ldapFilter = null;
		Control[] originalRequestControls = null;
		try {
			dirContext = ldapConnectionStack.pop();

			if (debug)
				logger.debug("Successfully retrieved an LDAP connection.");

			originalRequestControls = dirContext.getRequestControls();

			LdapPagingContext pagingContext = new LdapPagingContext(request.getRestriction());
			int currentSearchPageSize = this.computeLdapPageSize(pagingContext);

			dirContext.setRequestControls(
					new Control[] { new PagedResultsControl(currentSearchPageSize, Control.NONCRITICAL) });

			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			ldapFilter = this.createFilter(request, queryContext);

			if (debug)
				logger.debug(
						"Final filter to be used: " + ldapFilter + " with base " + base + ", paging: " + pagingContext);

			byte[] cookie = null;
			int index = 0;
			boolean exitLoop = false;

			do {

				NamingEnumeration<SearchResult> results = dirContext.search(base, ldapFilter, constraints);

				if (debug)
					logger.debug("Processing results from LDAP search.");

				while (results != null && results.hasMoreElements()) {

					SearchResult nextResult = results.next();

					RangeResult rangeResult = pagingContext.indexInRange(index);
					if (rangeResult.equals(RangeResult.inRange)) {

						LdapName dn = new LdapName(nextResult.getNameInNamespace());
						if (retrieveEntryDirContext == null) {
							retrieveEntryDirContext = ldapConnectionStack.pop();
						}

						GenericEntity entity = this.createGenericEntity(queryContext, retrieveEntryDirContext, dn);
						resultGenericEntities.add(entity);

					} else if (rangeResult.equals(RangeResult.afterRange)) {

						if (debug)
							logger.debug("Search result has exceeded the requested page.");
						hasMore = true;
						exitLoop = true;
						break;
					}

					index++;

				}

				if (!exitLoop) {

					// Examine the paged results control response
					cookie = this.parseResponseControls(dirContext);

					// Re-activate paged results
					dirContext.setRequestControls(new Control[] {
							new PagedResultsControl(currentSearchPageSize, cookie, Control.NONCRITICAL) });

					if (debug)
						logger.debug("Received a cookie to continue the search request: " + (cookie != null));

				} else {
					cookie = null;
				}

			} while (cookie != null);

			if (debug)
				logger.debug("Done processing all search results.");

		} catch (Exception e) {
			throw new ModelAccessException(
					"Error while searching " + queryContext.entityTypeSignature + " with filter " + ldapFilter, e);
		} finally {
			if (dirContext != null) {
				try {
					dirContext.setRequestControls(originalRequestControls);
				} catch (Exception e) {
					logger.debug("Could not reset original controls in LdapContext: " + originalRequestControls, e);
				}
			}
			this.ldapConnectionStack.push(dirContext);
			this.ldapConnectionStack.push(retrieveEntryDirContext);
		}

		return hasMore;
	}

	protected int computeLdapPageSize(LdapPagingContext pagingContext) {
		boolean debug = logger.isDebugEnabled();

		int currentSearchPageSize = this.searchPageSize;

		// To guarantee that we get at least one or more entry than requested,
		// we increase the LDAP page size by one (if necessary). Otherwise,
		// hasMore might be wrong.
		if (pagingContext.isPagingActivated()) {
			if (debug)
				logger.debug("Paging is activated. Checking whether page sizes are in order.");
			if ((currentSearchPageSize >= pagingContext.getPageSize())
					&& ((currentSearchPageSize % pagingContext.getPageSize()) == 0)) {
				if (debug)
					logger.debug("The LDAP page size " + currentSearchPageSize
							+ " is bigger than or equal to the requested page size " + pagingContext.getPageSize()
							+ " and it is a multiple of it.");
				currentSearchPageSize++;
			} else if ((currentSearchPageSize < pagingContext.getPageSize())
					&& ((pagingContext.getPageSize() % currentSearchPageSize) == 0)) {
				if (debug)
					logger.debug("The requested page size " + pagingContext.getPageSize()
							+ " is bigger than or equal to the LDAP page size " + currentSearchPageSize
							+ " and it is a multiple of it.");
				currentSearchPageSize++;
			}
		}
		return currentSearchPageSize;
	}

	protected GenericEntity createGenericEntity(LdapQueryContext queryContext, LdapContext retrieveEntryDirContext,
			LdapName dn) throws Exception {
		try {
			GenericEntity entity = queryContext.entityType.create();
			queryContext.idProperty.set(entity, dn.toString());
			
			Property dnProperty = queryContext.attributeToPropertyMap.get("dn");
			if (dnProperty != null) {
				dnProperty.set(entity, dn.toString());
			}
			
			Attributes attributes = retrieveEntryDirContext.getAttributes(dn, null);

			NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
			for (; attributeEnumeration.hasMore();) {
				Attribute attr = attributeEnumeration.next();

				String attributeId = attr.getID();

				Property property = queryContext.attributeToPropertyMap.get(attributeId.toLowerCase());
				if (property == null) {
					logger.debug("No property for LDAP attribute " + attributeId + " configured.");
					continue;
				}

				this.setPropertyValueFromAttribute(entity, property, attr);

			}
			return entity;
		} catch (Exception e) {
			throw new Exception("Could not get generic entity for DN " + dn, e);
		}
	}

	protected byte[] parseResponseControls(LdapContext dirContext) throws NamingException {

		boolean debug = logger.isDebugEnabled();

		byte[] cookie = null;
		Control[] controls = dirContext.getResponseControls();
		if (controls != null) {
			if (debug)
				logger.debug("Examining " + controls.length + " response controls.");

			for (int i = 0; i < controls.length; i++) {
				if (controls[i] instanceof PagedResultsResponseControl) {
					if (debug)
						logger.debug("Found a PagedResultsResponseControl object.");
					PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
					int total = prrc.getResultSize();
					if (debug)
						logger.debug("End of search result page reached; total: " + total);
					cookie = prrc.getCookie();
				}
			}

		} else {
			if (debug)
				logger.debug("No controls were sent from the server");
		}
		return cookie;
	}

	protected void setPropertyValueFromAttribute(GenericEntity entity, Property property, Attribute attr)
			throws Exception {
		Object value = this.getAttributeValuesAsObject(property, attr);
		if (value != null) {
			property.set(entity, value);
		}
	}

	protected Object getAttributeValuesAsObject(Property property, Attribute attr) throws Exception {

		GenericModelType propertyType = property.getType();
		Class<?> cls = propertyType.getJavaType();
		boolean isList = List.class.isAssignableFrom(cls);
		List<Object> valueList = isList ? new ArrayList<Object>() : null;
		boolean isSet = Set.class.isAssignableFrom(cls);
		Set<Object> valueSet = isSet ? new HashSet<Object>() : null;
		boolean isCollection = isList || isSet;

		NamingEnumeration<?> attributeValues = null;
		try {
			attributeValues = attr.getAll();
		} catch (NamingException e) {
			throw new Exception("Could not read attribute values for property " + property, e);
		}
		if ((attributeValues == null) || (!attributeValues.hasMoreElements())) {
			return null;
		}

		while (attributeValues.hasMoreElements()) {
			Object valueObject = attributeValues.nextElement();

			if (!isCollection) {
				return valueObject;
			} else {
				if (isList) {
					valueList.add(valueObject);
				} else if (isSet) {
					valueSet.add(valueObject);
				}
			}
		}

		if (isList) {
			return valueList;
		} else if (isSet) {
			return valueSet;
		}
		return null;
	}

	protected String getShortName(String type) {
		String shortType = type.replaceAll(".*\\.", "");
		return shortType;
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		throw new ModelAccessException("applyManipulation not yet implemented.");
	}

	@Override
	public GmMetaModel getMetaModel() {
		return modelOracle.getGmMetaModel();
	}

	protected Date convertActiveDirectoryTimestamp(String adTimestampString) {

		long adTimestamp = Long.parseLong(adTimestampString);

		// Filetime Epoch is 01 January, 1601
		// java date Epoch is 01 January, 1970
		// so take the number and subtract java Epoch:
		long javaTime = adTimestamp - 0x19db1ded53e8000L;

		// convert UNITS from (100 nano-seconds) to (milliseconds)
		javaTime /= 10000;

		// Date(long date)
		// Allocates a Date object and initializes it to represent
		// the specified number of milliseconds since the standard base
		// time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
		Date theDate = new Date(javaTime);
		return theDate;
	}

	protected String convertActiveDirectoryTimestamp(Date date) {

		long javaTime = date.getTime();

		javaTime *= 10000;

		long adTime = javaTime + 0x19db1ded53e8000L;

		return "" + adTime;
	}

	protected String convertActiveDirectoryTimestamp(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		return this.convertActiveDirectoryTimestamp(date);
	}

	protected String convertActiveDirectoryTimestamp(LocalDateTime localDateTime) {
		Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		return this.convertActiveDirectoryTimestamp(date);
	}

	protected Set<String> getObjectClasses(LdapQueryContext queryContext) throws Exception {

		LdapObjectClasses metaData = queryContext.entityMetaDataBuilder.meta(LdapObjectClasses.T).exclusive();
		if (metaData == null) {
			throw new Exception(
					"The entity type " + queryContext.entityType + " does not contain a LdapObjectClasses meta-data.");
		}

		Set<String> objectClasses = metaData.getObjectClasses();
		return objectClasses;
	}

	protected String getAttributeName(LdapQueryContext queryContext, String propertyName) {

		String attributeName = queryContext.propertyToAttributeMap.get(propertyName);
		if (attributeName == null) {
			logger.debug("Property " + propertyName + " of type " + queryContext.entityType
					+ " does not have a LdapAttribute meta-data.");
			return null;
		}

		return attributeName;
	}

	protected Set<String> getFulltextAttributes(LdapQueryContext queryContext) throws Exception {

		if (queryContext.propertyList == null) {
			return null;
		}
		Set<String> fulltextAttributeNames = new HashSet<String>();
		for (Property property : queryContext.propertyList) {
			LdapFulltextProperty ftProperty = queryContext.entityMetaDataBuilder.property(property)
					.meta(LdapFulltextProperty.T).exclusive();
			if (ftProperty != null) {
				String propertyName = property.getName();
				String attributeName = this.getAttributeName(queryContext, propertyName);
				if (attributeName != null) {
					logger.debug("The property " + propertyName
							+ " has a LdapFulltextProperty meta-data, but no LdapAttribute meta-data.");
				} else {
					fulltextAttributeNames.add(attributeName);
				}
			}
		}

		return fulltextAttributeNames;
	}

	public Supplier<GmMetaModel> getMetaModelProvider() {
		return metaModelProvider;
	}

	@Required
	public void setMetaModelProvider(Supplier<GmMetaModel> metaModelProvider) {
		this.metaModelProvider = metaModelProvider;
	}

	public LdapConnection getLdapConnectionStack() {
		return ldapConnectionStack;
	}

	@Required
	public void setLdapConnectionStack(LdapConnection ldapConnectionStack) {
		this.ldapConnectionStack = ldapConnectionStack;
	}

	@Required
	public void setBase(String base) {
		this.base = base;
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Configurable
	public void setRolesProvider(Supplier<Set<String>> rolesProvider) {
		this.rolesProvider = rolesProvider;
	}

	@Configurable
	public void setUseCases(Set<String> useCases) {
		this.useCases = useCases;
	}

	@Configurable
	public void setSearchPageSize(int searchPageSize) {
		if (searchPageSize > 0) {
			this.searchPageSize = searchPageSize;
		}
	}

}
