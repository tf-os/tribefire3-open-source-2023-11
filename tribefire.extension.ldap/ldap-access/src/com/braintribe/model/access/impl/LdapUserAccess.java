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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.naming.Context;
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
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.impl.useraccess.LdapPagingContext;
import com.braintribe.model.access.impl.useraccess.LdapPagingContext.RangeResult;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmMetaModel;
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
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ldap.LdapConnection;

/**
 * Specialized LDAP access that can be used for querying Users, Groups, and Roles (as defined in the UserModel) in an
 * LDAP / Active Directory. <br>
 * <br>
 * Configuring this access requires some knowledge about the directory structure of the server. A good way to get an
 * insight into the server structure is to use a third-party tool like
 * <a href="http://directory.apache.org/studio/">Apache Directory Studio</a>. Before this access can be configured, at
 * least the following information is necessary:
 * <ul>
 * <li><code>UserBase</code>: The path where users can be found. The more specific this path is, the better will be the
 * search performance of this access. (Example: &quot;OU=Accounts,OU=&lt;Organization&gt;,DC=&lt;Company&gt;&quot;)</li>
 * <li><code>GroupBase</code>: The path were groups can be found. As with the <code>UserBase</code>, this is taken as a
 * base path. Actual entries may be several levels down the tree structure. (Example:
 * &quot;OU=Groups,OU=&lt;Organization&gt;,DC=&lt;Company&gt;&quot;)</li>
 * <li><code>ConnectionUrl</code>: The URL where the service can be accessed. (Example:
 * &quot;ldap://&lt;host&gt;:389&quot;)</li>
 * <li><code>Username</code>: The technical username that can be used to access the server.</li>
 * <li><code>Password</code>: The password of the technical user.</li>
 * </ul>
 * <br>
 * When the <code>LdapCartridge</code> is synchronized, it creates an example <code>LdapUserAccess</code> instance that
 * has most properties set that are necessary for accessing an Active Directory server. Only the properties mentioned
 * above have to be completed. <br>
 * <br>
 * The following settings are also available: <br>
 * <br>
 * <ul>
 * <li><code>GroupIdAttribute</code>: LDAP attribute that contains the ID of a group entry, which is the DN of the
 * group. (Example: &quot;distinguishedName&quot;)</li>
 * <li><code>GroupMemberAttribute</code>: LDAP attribute that contains a list of members of this group. This list is
 * expressed as multiple entries with this attribute name. The entries in this list must be fully qualified DN entries.
 * (Example: &quot;member&quot;)</li>
 * <li><code>GroupNameAttribute</code>: LDAP attribute that contains the name of a group. (Example:
 * &quot;name&quot;)</li>
 * <li><code>GroupObjectClasses</code>: The LDAP class that identifies a group entry. (Example: &quot;group&quot;)</li>
 * <li><code>GroupsAreRoles</code>: When this is set to <code>true</code>, all groups that a user is a member of will
 * also be taken as role names. This is necessary when, for example, the LDAP server does not provide attributes for
 * handling user roles. (Example: &quot;true&quot;)</li>
 * <li><code>MemberAttribute</code>: LDAP attribute of a group that contains a list of all members of the group. Entries
 * must be DNs.(Example: &quot;memberOf&quot;)</li>
 * <li><code>RoleIdAttribute</code>: LDAP attribute that contains the ID of the role. (Example:
 * &quot;distinguishedName&quot;)</li>
 * <li><code>RoleNameAttribute</code>: LDAP attribute that contains the name of the role. (Example:
 * &quot;name&quot;)</li>
 * <li><code>UserDescriptionAttribute</code>: LDAP attribute that contains a display name of the user (e.g., the
 * fullname of the user). (Example: &quot;displayName&quot;)</li>
 * <li><code>UserEmailAttribute</code>: LDAP attribute that provides the email address of the user. (Example:
 * &quot;mail&quot;)</li>
 * <li><code>UserFilter</code>: This value is used as a template for searching for a specific user. This must be
 * specified as an LDAP search string with a placeholder (&quot;%s&quot;) that will be replaced by the users DN.
 * (Example: &quot;(sAMAccountName=%s)&quot;)</li>
 * <li><code>UserFirstNameAttribute</code>: LDAP attribute that holds the first name of a user. (Example:
 * &quot;givenName&quot;)</li>
 * <li><code>UserIdAttribute</code>: LDAP attribute that contains the DN of the user. (Example:
 * &quot;distinguishedName&quot;)</li>
 * <li><code>UserLastLoginAttribute</code>: LDAP attribute that holds the time of the last authentication of this user.
 * (Example: &quot;lastLogon&quot;)</li>
 * <li><code>UserLastNameAttribute</code>: LDAP attribute that holds the last name of a user. (Example:
 * &quot;sn&quot;)</li>
 * <li><code>UserMemberOfAttribute</code>: LDAP attribute that specifies the groups that a user is a member of.
 * (Example: &quot;memberOf&quot;)</li>
 * <li><code>UserNameAttribute</code>: LDAP attribute that holds the technical ID of the user. (Example:
 * &quot;sAMAccountName&quot;)</li>
 * <li><code>UserObjectClasses</code>: Name of the LDAP class that identifies user entries. (Example:
 * &quot;user&quot;)</li>
 * <li><code>SearchPageSize</code>: The size of a single search page. (Example: &quot;20&quot;)</li>
 * </ul>
 * <br>
 * It is important to note that the underlying LDAP service must provide a bi-directional relationship between users and
 * groups. That means that each user must contain a list of all groups that it is a member of and each group must
 * contain a list of all members. If this not the case (e.g., with OpenLDAP), the functionality might be limited.
 * 
 * @author roman.kurmanowytsch
 * 
 * @see com.braintribe.model.user.User
 * @see com.braintribe.model.user.Group
 * @see LdapAccess
 */
public class LdapUserAccess extends AbstractAccess {

	protected static Logger logger = Logger.getLogger(LdapUserAccess.class);

	protected Supplier<GmMetaModel> metaModelProvider = null;
	protected LdapConnection ldapConnectionStack = null;

	protected String userFilter = "(sAMAccountName=%s)";
	protected List<String> userObjectClasses = null;
	protected List<String> groupObjectClasses = null;
	protected String userBase = null;
	protected String groupBase = null;

	protected String userFirstNameAttribute = "givenName";
	protected String userLastNameAttribute = "sn";
	protected String userLastLoginAttribute = "lastLogon";
	protected String userEmailAttribute = "mail";
	protected String userDescriptionAttribute = "displayName";
	protected String userMemberOfAttribute = "memberOf";
	protected String userIdAttribute = "distinguishedName";
	protected String userNameAttribute = "sAMAccountName";

	protected String groupNameAttribute = "name";
	protected String groupMemberAttribute = "member";
	protected String groupIdAttribute = "distinguishedName";

	protected String roleNameAttribute = "name";
	protected String roleIdAttribute = "distinguishedName";

	protected boolean groupsAreRoles = true;
	protected int searchPageSize = 100;

	protected String accessId;

	public LdapUserAccess() {
		logger.debug(() -> "LdapUserAccess has been instantiated.");
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		return null;
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();

		String entityTypeSignature = request.getEntityTypeSignature();
		EntityType<GenericEntity> entityType = typeReflection.getEntityType(entityTypeSignature);
		String typeName = entityType.getShortName();

		String shortTypeName = this.getShortName(typeName);

		if (debug)
			logger.debug("Searching for " + shortTypeName);

		List<GenericEntity> resultGenericEntities = new ArrayList<GenericEntity>();

		boolean hasMore = false;
		boolean isAuthenticationAttempt = false;
		if (shortTypeName.equals("User")) {
			if (debug)
				logger.debug("Checking whether the query is an authentication attempt.");

			isAuthenticationAttempt = this.authenticateUser(request, resultGenericEntities);
			if (debug)
				logger.debug("Is it an authentication attempt:" + isAuthenticationAttempt);
		} else {
			if (debug)
				logger.debug("This is a normal query and not an authentication attempt.");
		}

		if (!isAuthenticationAttempt) {
			hasMore = this.search(request, resultGenericEntities, shortTypeName);
		}

		if (debug)
			logger.debug("Query has finished. Preparing the result.");

		// Create the query result.
		EntityQueryResult entityQueryResult = EntityQueryResult.T.create();
		entityQueryResult.setEntities(resultGenericEntities);
		entityQueryResult.setHasMore(hasMore);

		return entityQueryResult;
	}

	private static Role convertToRole(Group group) {

		if (group == null)
			return null;

		Role role = Role.T.create();
		role.setId(group.getId());
		role.setName(group.getName());
		return role;
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {

		try {
			PersistentEntityReference entityReference = request.getEntityReference();
			LdapName entityId = new LdapName((String) entityReference.getRefId());
			Class<? extends GenericEntity> entityClass = getEntityClassOf(entityReference.getTypeSignature());
			String propertyName = request.getPropertyName();
			Property requestedProperty = request.property();
			Object resultObject = null;

			if (User.class.isAssignableFrom(entityClass)) {

				if (propertyName.equals("groups")) {
					// Get groups for a specific user
					Set<Group> groups = this.getGroupsForUserDn(entityId);
					resultObject = groups;
				} else if (propertyName.equals("roles")) {
					if (groupsAreRoles) {
						Set<Role> roles = new HashSet<Role>();
						Set<Group> groups = this.getGroupsForUserDn(entityId);
						if (groups != null) {
							groups.forEach(e -> roles.add(convertToRole(e)));
						}
						resultObject = roles;
					} else {
						logger.debug("Roles are not yet supported.");
					}
				} else {
					logger.debug("Getting a single attribute (" + propertyName + ") is not yet supported.");
				}

			} else if (Group.class.isAssignableFrom(entityClass)) {

				if (propertyName.equals("users")) {
					Set<User> users = this.getMembersOfGroup(entityId);
					resultObject = users;
				}

			}

			PropertyQueryResult result = PropertyQueryResult.T.create();

			result.setPropertyValue(clonePropertyQueryResult(requestedProperty, resultObject, request));
			result.setHasMore(false);

			return result;

		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to get properties: " + request, e);
		}

	}

	protected Set<User> getMembersOfGroup(LdapName groupDn) throws ModelAccessException {
		LdapContext dirContext = null;
		Set<User> result = new HashSet<User>();

		Map<LdapName, User> userCache = new HashMap<LdapName, User>();
		try {

			Attributes attributes = null;
			try {
				dirContext = this.ldapConnectionStack.pop();

				String range = null;
				int interval = 5;
				int start = 0;
				int stop = start + interval;
				boolean stopLoop = true;

				boolean useRange = false;

				if (this.ldapConnectionStack.isActiveDirectory()) {
					useRange = true;
					stopLoop = false;
					range = "" + start + "-" + stop;
				}

				String returnedAtts[] = null;
				do {

					if ((useRange) && (range != null)) {
						returnedAtts = new String[] { this.groupMemberAttribute + ";Range=" + range };
					} else {
						returnedAtts = new String[] { this.groupMemberAttribute };
					}

					attributes = dirContext.getAttributes(groupDn, returnedAtts);

					if ((attributes != null) && (attributes.size() > 0)) {

						NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
						for (; attributeEnumeration.hasMore();) {
							Attribute attr = attributeEnumeration.next();

							String attributeId = attr.getID();
							if (attributeId != null) {
								if ((attributeId.toLowerCase().startsWith(this.groupMemberAttribute.toLowerCase()))) {
									NamingEnumeration<?> valueEnumeration = attr.getAll();
									for (; valueEnumeration.hasMore();) {
										Object value = valueEnumeration.next();
										if (value instanceof String) {
											LdapName memberDn = new LdapName((String) value);
											User user = userCache.get(memberDn);
											if (user == null) {
												user = this.getUser(dirContext, memberDn);
												if (user != null) {
													result.add(user);
													userCache.put(memberDn, user);
												}
											}
										}
									}
								}
								if (attributeId.endsWith("*")) {
									stopLoop = true;
								}
							}
						}

					} else {
						stopLoop = true;
					}

					start = stop;
					stop = start + interval;
					range = "" + start + "-" + stop;

				} while ((useRange) && (!stopLoop));

			} catch (Exception e) {
				throw new ModelAccessException("Error while trying to get members of " + groupDn, e);
			}

		} finally {
			this.ldapConnectionStack.push(dirContext);
		}

		return result;
	}

	protected String createFilter(EntityQuery request, String shortTypeName) {

		Restriction restriction = request.getRestriction();
		String result = null;
		if (restriction != null) {
			Condition condition = restriction.getCondition();
			result = this.processCondition(condition, shortTypeName);
		}

		if (shortTypeName.equals("User")) {
			result = this.extendFilterByObjectClasses(result, userObjectClasses);
		} else if (shortTypeName.equals("Group") || (shortTypeName.equals("Role") && groupsAreRoles)) {
			result = this.extendFilterByObjectClasses(result, groupObjectClasses);
		}

		return result;

	}

	protected String extendFilterByObjectClasses(String filter, List<String> objectClasses) {
		if ((objectClasses != null) && (objectClasses.size() > 0)) {
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

			objectClasses.forEach(e -> sb.append("(objectClass=" + e + ")"));

			sb.append(")");
			String extendedFilter = sb.toString();
			return extendedFilter;
		} else {
			return filter;
		}
	}

	protected String processCondition(Condition condition, String shortTypeName) {

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
				return this.processCondition(operands.get(0), shortTypeName);
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
				String subConsitionString = this.processCondition(subCondition, shortTypeName);
				if (subConsitionString != null) {
					boolean isEnclosedInParantheses = subConsitionString.startsWith("(") && subConsitionString.endsWith(")");
					if (!isEnclosedInParantheses) {
						sb.append("(");
					}
					sb.append(subConsitionString);
					if (!isEnclosedInParantheses) {
						sb.append(")");
					}
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
				String left = this.getOperand(valComp.getLeftOperand(), shortTypeName);
				String right = this.getOperand(valComp.getRightOperand(), shortTypeName);
				if ((left == null) || (right == null)) {
					logger.debug("Either the left (" + left + ") of right side (" + right + ") is null. No comparison possible.");
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

				return processFulltextComparison((FulltextComparison) condition, shortTypeName);

			} else {
				logger.warn("Unsupported comparison " + comparison.getClass());
				return null;
			}

		} else if (condition instanceof Negation) {

			Negation negation = (Negation) condition;
			Condition subCondition = negation.getOperand();
			String subConditionString = this.processCondition(subCondition, shortTypeName);
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

	protected String processFulltextComparisonForProperties(String text, String shortTypeName, String... properties) {

		text = "*" + text + "*";

		Disjunction disjunction = Disjunction.T.create();
		disjunction.setOperands(new ArrayList<Condition>(properties.length));

		for (String propertyName : properties) {

			PropertyOperand leftPropertyOperand = PropertyOperand.T.create();
			leftPropertyOperand.setPropertyName(propertyName);

			ValueComparison valueComparison = ValueComparison.T.create();
			valueComparison.setLeftOperand(leftPropertyOperand);
			valueComparison.setOperator(Operator.equal);
			valueComparison.setRightOperand(text);

			disjunction.getOperands().add(valueComparison);
		}

		return processCondition(disjunction, shortTypeName);
	}

	protected String processFulltextComparison(FulltextComparison fulltextComparison, String shortTypeName) {

		String text = (fulltextComparison.getText() == null) ? "" : fulltextComparison.getText().trim();

		if (shortTypeName.equals("Group")) {
			if (text.isEmpty()) {
				return this.groupIdAttribute + "=*";
			} else {
				return processFulltextComparisonForProperties(text, shortTypeName, "id", "localizedName", "name");
			}
		} else if (shortTypeName.equals("User")) {
			if (text.isEmpty()) {
				return this.userNameAttribute + "=*";
			} else {
				return processFulltextComparisonForProperties(text, shortTypeName, "id", "firstName", "lastName", "name", "email");
			}
		} else if (shortTypeName.equals("Role")) {
			if (text.isEmpty()) {
				return this.roleIdAttribute + "=*";
			} else {
				return processFulltextComparisonForProperties(text, shortTypeName, "id", "name");
			}
		} else {
			logger.warn("Unsupported Entity type: " + shortTypeName);
			return null;
		}

	}

	protected String getOperand(Object operand, String shortTypeName) {

		if (operand instanceof PropertyOperand) {
			PropertyOperand propOperand = (PropertyOperand) operand;
			String propertyName = propOperand.getPropertyName();

			if (shortTypeName.equals("Group")) {
				if (propertyName.equals("name")) {
					return this.groupNameAttribute;
				} else if (propertyName.equals("localizedName")) {
					return this.groupNameAttribute;
				} else if (propertyName.equals("id")) {
					return this.groupIdAttribute;
				} else {
					logger.warn("Unsupported Group property name " + propertyName);
					return null;
				}
			} else if (shortTypeName.equals("User")) {
				if (propertyName.equals("firstName")) {
					return this.userFirstNameAttribute;
				} else if (propertyName.equals("lastName")) {
					return this.userLastNameAttribute;
				} else if (propertyName.equals("lastLogin")) {
					return this.userLastLoginAttribute;
				} else if ((propertyName.equals("name")) || (propertyName.equals("id"))) {
					return this.userNameAttribute;
				} else if (propertyName.equals("email")) {
					return this.userEmailAttribute;
				} else {
					logger.warn("Unsupported User property name " + propertyName);
					return null;
				}
			} else if (shortTypeName.equals("Role")) {
				if (propertyName.equals("name")) {
					return this.roleNameAttribute;
				} else if (propertyName.equals("id")) {
					return this.roleIdAttribute;
				} else {
					logger.warn("Unsupported Role property name " + propertyName);
					return null;
				}
			} else {
				logger.warn("Unsupported Entity type property name " + shortTypeName);
				return null;
			}

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
		} else if (operand instanceof EntitySignature) {
			return null;
		} else {
			logger.debug("Unknown type " + operand.getClass());
			return operand.toString();
		}

	}

	protected boolean authenticateUser(EntityQuery request, List<GenericEntity> resultGenericEntities) throws ModelAccessException {
		Restriction restriction = request.getRestriction();
		if (restriction == null) {
			logger.debug("Request has no restrictions");
			return false;
		}
		Condition condition = restriction.getCondition();

		String username = null;
		String password = null;

		if (condition instanceof Conjunction) {
			Conjunction conjunction = (Conjunction) condition;
			List<Condition> authConditions = conjunction.getOperands();

			if (authConditions.size() != 2) {
				return false;
			}

			for (Condition authCondition : authConditions) {
				if (authCondition instanceof ValueComparison) {
					ValueComparison valComp = (ValueComparison) authCondition;
					String key = null;
					String value = null;
					Object leftObject = valComp.getLeftOperand();
					if (leftObject instanceof PropertyOperand) {
						PropertyOperand propOperand = (PropertyOperand) leftObject;
						key = propOperand.getPropertyName();
					}
					Object rightObject = valComp.getRightOperand();
					if (rightObject instanceof String) {
						value = (String) rightObject;
					}
					Operator op = valComp.getOperator();
					if (op.equals(Operator.equal)) {
						if (key.equals("name")) {
							username = value;
						} else if (key.equals("password")) {
							password = value;
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} else {
			return false;
		}

		if ((username != null) && (password != null)) {
			LdapName dn = this.getUserDn(username);
			logger.debug("Identified user " + username + " as " + dn);

			if (dn != null) {
				User user = this.authenticateDn(dn, password);
				if (user != null) {
					resultGenericEntities.add(user);
				}
			}

			return true;

		} else {
			logger.debug("Could not identify DN for user " + username);
		}

		return false;
	}

	protected User authenticateDn(LdapName dn, String password) throws ModelAccessException {

		LdapContext dirContext = null;

		try {

			try {
				dirContext = this.ldapConnectionStack.pop();
				dirContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
				dirContext.addToEnvironment(Context.SECURITY_PRINCIPAL, dn.toString());
				dirContext.addToEnvironment(Context.SECURITY_CREDENTIALS, password);

				User user = getUser(dirContext, dn);

				return user;

			} catch (Exception e) {
				throw new ModelAccessException(
						"Error while trying to authenticate user " + dn + " with password " + StringTools.simpleObfuscatePassword(password), e);
			}

		} finally {
			this.ldapConnectionStack.push(dirContext);
		}

	}

	protected User getUser(LdapContext dirContext, LdapName dn) throws Exception {
		Attributes attributes = dirContext.getAttributes(dn, null);
		if (attributes != null) {

			User user = User.T.create();
			user.setId(dn.toString());

			NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
			for (; attributeEnumeration.hasMore();) {
				Attribute attr = attributeEnumeration.next();

				String attributeId = attr.getID();
				if (attributeId.equalsIgnoreCase(userFirstNameAttribute)) {
					user.setFirstName(this.getFirstAttributeValue(attr));
				} else if (attributeId.equalsIgnoreCase(userLastNameAttribute)) {
					user.setLastName(this.getFirstAttributeValue(attr));
				} else if (attributeId.equalsIgnoreCase(userLastLoginAttribute)) {
					user.setLastLogin(this.getFirstAttributeValueAsDate(attr));
				} else if (attributeId.equalsIgnoreCase(userEmailAttribute)) {
					user.setEmail(this.getFirstAttributeValue(attr));
				} else if (attributeId.equalsIgnoreCase(userNameAttribute)) {
					user.setName(this.getFirstAttributeValue(attr));
				} else if (attributeId.equalsIgnoreCase(userDescriptionAttribute)) {
					Map<String, String> localizedValues = new HashMap<String, String>();
					localizedValues.put("default", this.getFirstAttributeValue(attr));
					LocalizedString localizedString = LocalizedString.T.create();
					localizedString.setLocalizedValues(localizedValues);
					user.setDescription(localizedString);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("" + attr);
				}
			}

			EntityType<User> et = user.entityType();
			et.getProperty("groups").setAbsenceInformation(user, GMF.absenceInformation());
			et.getProperty("roles").setAbsenceInformation(user, GMF.absenceInformation());

			return user;
		}
		return null;
	}

	protected Set<Group> getGroupsForUserDn(LdapName dn) throws Exception {
		LdapContext dirContext = null;
		Set<Group> result = new HashSet<Group>();
		Map<LdapName, Group> groupCache = new ConcurrentHashMap<>();

		try {

			Attributes attributes = null;
			try {
				dirContext = this.ldapConnectionStack.pop();

				attributes = dirContext.getAttributes(dn, new String[] { this.userMemberOfAttribute });

				if (attributes != null) {

					List<LdapName> groupNames = new ArrayList<>();

					NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
					for (; attributeEnumeration.hasMore();) {
						Attribute attr = attributeEnumeration.next();

						String attributeId = attr.getID();
						if (attributeId.equalsIgnoreCase(this.userMemberOfAttribute)) {
							NamingEnumeration<?> valueEnumeration = attr.getAll();
							for (; valueEnumeration.hasMore();) {
								Object value = valueEnumeration.next();
								if (value instanceof String) {
									LdapName groupDn = new LdapName((String) value);
									groupNames.add(groupDn);
								}
							}
						}

					}

					groupNames.stream().forEach(groupDn -> {
						Group group = groupCache.get(groupDn);
						if (group == null) {
							try {
								group = this.getGroup(groupDn);
								if (group != null) {
									groupCache.put(groupDn, group);
								}
							} catch (Exception e) {
								logger.error("Error while trying to get group " + groupDn, e);
							}
						}

					});

					result.addAll(groupCache.values());

				}

			} catch (Exception e) {
				throw new ModelAccessException("Error while trying to get groups for " + dn, e);
			}

		} finally {
			this.ldapConnectionStack.push(dirContext);
		}

		return result;
	}

	protected Group getGroup(LdapName groupDn) throws Exception {
		LdapContext dirContext = null;

		try {

			dirContext = this.ldapConnectionStack.pop();

			Group group = this.getGroup(dirContext, groupDn);

			return group;

		} finally {
			this.ldapConnectionStack.push(dirContext);
		}
	}

	protected Group getGroup(LdapContext dirContext, LdapName groupDn) throws Exception {

		Attributes attributes = null;
		try {
			attributes = dirContext.getAttributes(groupDn, null);

			if (attributes != null) {

				Group group = Group.T.create();
				group.setId(groupDn.toString());

				NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
				for (; attributeEnumeration.hasMore();) {
					Attribute attr = attributeEnumeration.next();

					String attributeId = attr.getID();
					if (attributeId.equalsIgnoreCase(groupNameAttribute)) {
						group.setName(this.getFirstAttributeValue(attr));
					}
				}

				EntityType<User> et = group.entityType();
				et.getProperty("users").setAbsenceInformation(group, GMF.absenceInformation());
				et.getProperty("roles").setAbsenceInformation(group, GMF.absenceInformation());

				return group;
			}

		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to get group " + groupDn, e);
		}

		return null;
	}

	protected Date getFirstAttributeValueAsDate(Attribute attr) throws Exception {
		String stringValue = this.getFirstAttributeValue(attr);
		if (stringValue != null) {
			Date date = this.convertActiveDirectoryTimestamp(stringValue);
			return date;
		}
		return null;
	}

	protected String getFirstAttributeValue(Attribute attr) throws Exception {
		Object o = attr.get();
		if (o != null) {
			return o.toString();
		}
		return null;
	}

	protected boolean search(EntityQuery request, List<GenericEntity> resultGenericEntities, String shortTypeName) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();
		boolean trace = logger.isTraceEnabled();

		boolean hasMore = false;

		LdapContext dirContext = null;
		LdapContext retrieveEntryDirContext = null;
		String ldapFilter = null;
		Control[] originalRequestControls = null;
		try {
			dirContext = ldapConnectionStack.pop();

			logger.debug(() -> "Successfully retrieved an LDAP connection.");

			originalRequestControls = dirContext.getRequestControls();

			LdapPagingContext pagingContext = new LdapPagingContext(request.getRestriction());
			int currentSearchPageSize = this.computeLdapPageSize(pagingContext);

			dirContext.setRequestControls(new Control[] { new PagedResultsControl(currentSearchPageSize, Control.NONCRITICAL) });

			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			ldapFilter = this.createFilter(request, shortTypeName);
			String base = this.userBase;
			if (shortTypeName.equals("Group") || (shortTypeName.equals("Role") && groupsAreRoles)) {
				base = this.groupBase;
			}

			if (debug)
				logger.debug(
						"Final filter to be used on [ " + shortTypeName + " ]: " + ldapFilter + " with base " + base + ", paging: " + pagingContext);

			if (ldapFilter == null) {

				logger.info(() -> "No filter has been created due to search request. Ignoring the search.");

			} else {

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
							if (retrieveEntryDirContext == null) {
								retrieveEntryDirContext = ldapConnectionStack.pop();
							}
							this.retrieveEntry(resultGenericEntities, shortTypeName, trace, retrieveEntryDirContext, nextResult);
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
						dirContext.setRequestControls(new Control[] { new PagedResultsControl(currentSearchPageSize, cookie, Control.NONCRITICAL) });

						if (debug)
							logger.debug("Received a cookie to continue the search request: " + (cookie != null));

					} else {
						cookie = null;
					}

				} while (cookie != null);
			}

			logger.debug(() -> "Done processing all search results.");

		} catch (Exception e) {
			throw new ModelAccessException("Error while searching " + shortTypeName + " with filter " + ldapFilter, e);
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
			if ((currentSearchPageSize >= pagingContext.getPageSize()) && ((currentSearchPageSize % pagingContext.getPageSize()) == 0)) {
				if (debug)
					logger.debug("The LDAP page size " + currentSearchPageSize + " is bigger than or equal to the requested page size "
							+ pagingContext.getPageSize() + " and it is a multiple of it.");
				currentSearchPageSize++;
			} else if ((currentSearchPageSize < pagingContext.getPageSize()) && ((pagingContext.getPageSize() % currentSearchPageSize) == 0)) {
				if (debug)
					logger.debug("The requested page size " + pagingContext.getPageSize() + " is bigger than or equal to the LDAP page size "
							+ currentSearchPageSize + " and it is a multiple of it.");
				currentSearchPageSize++;
			}
		}
		return currentSearchPageSize;
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

	protected void retrieveEntry(List<GenericEntity> resultGenericEntities, String shortTypeName, boolean trace, LdapContext dirContext,
			SearchResult nextResult) throws Exception {
		LdapName dn = new LdapName(nextResult.getNameInNamespace());

		if (shortTypeName.equals("User")) {

			if (trace)
				logger.trace("Creating User for dn " + dn);
			User user = this.getUser(dirContext, dn);
			resultGenericEntities.add(user);

		} else if (shortTypeName.equals("Group") || (shortTypeName.equals("Role") && groupsAreRoles)) {

			if (trace)
				logger.trace("Creating Group for dn " + dn);
			Group group = this.getGroup(dirContext, dn);

			if (shortTypeName.equals("Role") && groupsAreRoles) {
				resultGenericEntities.add(convertToRole(group));
			} else {
				resultGenericEntities.add(group);
			}
		}
	}

	protected LdapName getUserDn(String username) throws ModelAccessException {

		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Trying to get dn for username " + username);

		LdapContext dirContext = null;
		try {
			dirContext = ldapConnectionStack.pop();

			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			String uidfilter = String.format(userFilter, username);

			if (debug)
				logger.debug("UID filter is " + uidfilter);

			NamingEnumeration<SearchResult> results = dirContext.search(this.userBase, uidfilter, constraints);

			while (results != null && results.hasMoreElements()) {

				SearchResult nextResult = results.next();
				LdapName dn = new LdapName(nextResult.getNameInNamespace());

				if (debug)
					logger.debug("Resolved dn " + dn + " for username " + username);

				return dn;
			}

		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to get DN for user " + username, e);
		} finally {
			this.ldapConnectionStack.push(dirContext);
		}

		if (debug)
			logger.debug("Could not find a User for username " + username);

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
		try {
			/* PGA: Generally, the provider should be called just once and the value should be cached. I will not touch
			 * this though, as I have no clue whether something could break that way. Could a model change over time? */
			return this.metaModelProvider.get();

		} catch (RuntimeException e) {
			logger.error("Could not get meta model from provider " + this.metaModelProvider, e);
			throw new GenericModelException(e);
		}
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

	public Supplier<GmMetaModel> getMetaModelProvider() {
		return metaModelProvider;
	}

	@Required
	public void setMetaModelProvider(Supplier<GmMetaModel> metaModelProvider) {
		this.metaModelProvider = metaModelProvider;
	}
	@Required
	public void setLdapConnectionStack(LdapConnection ldapConnectionStack) {
		this.ldapConnectionStack = ldapConnectionStack;
	}
	@Configurable
	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}
	@Required
	public void setUserBase(String userBase) {
		this.userBase = userBase;
	}
	@Configurable
	public void setMemberAttribute(String memberAttribute) {
		this.userMemberOfAttribute = memberAttribute;
	}
	@Configurable
	public void setUserFirstNameAttribute(String userFirstNameAttribute) {
		this.userFirstNameAttribute = userFirstNameAttribute;
	}
	@Configurable
	public void setUserLastNameAttribute(String userLastNameAttribute) {
		this.userLastNameAttribute = userLastNameAttribute;
	}
	@Configurable
	public void setUserLastLoginAttribute(String userLastLoginAttribute) {
		this.userLastLoginAttribute = userLastLoginAttribute;
	}
	@Configurable
	public void setUserEmailAttribute(String userEmailAttribute) {
		this.userEmailAttribute = userEmailAttribute;
	}
	@Configurable
	public void setUserDescriptionAttribute(String userDescriptionAttribute) {
		this.userDescriptionAttribute = userDescriptionAttribute;
	}
	@Configurable
	public void setUserMemberOfAttribute(String userMemberOfAttribute) {
		this.userMemberOfAttribute = userMemberOfAttribute;
	}
	@Configurable
	public void setGroupNameAttribute(String groupNameAttribute) {
		this.groupNameAttribute = groupNameAttribute;
	}
	@Configurable
	public void setGroupMemberAttribute(String groupMemberAttribute) {
		this.groupMemberAttribute = groupMemberAttribute;
	}
	@Configurable
	public void setGroupsAreRoles(boolean groupsAreRoles) {
		this.groupsAreRoles = groupsAreRoles;
	}
	@Configurable
	public void setGroupBase(String groupBase) {
		this.groupBase = groupBase;
	}
	@Configurable
	public void setUserObjectClasses(List<String> userObjectClasses) {
		this.userObjectClasses = userObjectClasses;
	}
	@Configurable
	public void setUserIdAttribute(String userIdAttribute) {
		this.userIdAttribute = userIdAttribute;
	}
	@Configurable
	public void setUserNameAttribute(String userNameAttribute) {
		this.userNameAttribute = userNameAttribute;
	}
	@Configurable
	public void setGroupIdAttribute(String groupIdAttribute) {
		this.groupIdAttribute = groupIdAttribute;
	}
	@Configurable
	public void setRoleNameAttribute(String roleNameAttribute) {
		this.roleNameAttribute = roleNameAttribute;
	}
	@Configurable
	public void setRoleIdAttribute(String roleIdAttribute) {
		this.roleIdAttribute = roleIdAttribute;
	}
	@Configurable
	public void setGroupObjectClasses(List<String> groupObjectClasses) {
		this.groupObjectClasses = groupObjectClasses;
	}

	@Configurable
	public void setSearchPageSize(int searchPageSize) {
		if (searchPageSize > 0) {
			this.searchPageSize = searchPageSize;
		}
	}

}
