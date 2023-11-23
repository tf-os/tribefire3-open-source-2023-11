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
package tribefire.extension.ldap.initializer.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.ldapaccessdeployment.HealthCheckProcessor;
import com.braintribe.model.ldapaccessdeployment.LdapAccess;
import com.braintribe.model.ldapaccessdeployment.LdapUserAccess;
import com.braintribe.model.ldapauthenticationdeployment.LdapAuthentication;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.ldap.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.ldap.initializer.wire.contract.LdapInitializerModuleContract;
import tribefire.extension.ldap.initializer.wire.contract.LdapInitializerModuleModelsContract;
import tribefire.extension.ldap.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.ldap.templates.api.LdapTemplateContext;
import tribefire.extension.ldap.templates.wire.contract.LdapTemplatesContract;

@Managed
public class LdapInitializerModuleSpace extends AbstractInitializerSpace implements LdapInitializerModuleContract {

	private static final Logger logger = Logger.getLogger(LdapInitializerModuleSpace.class);

	@Import
	private LdapInitializerModuleModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private LdapTemplatesContract ldapTemplates;

	@Managed
	protected LdapTemplateContext defaultContext() {

		//@formatter:off
		LdapTemplateContext context = LdapTemplateContext.builder()
			.setConnectionUrl(properties.LDAP_CONN_URL("ldap://<host>:389"))
			.setUsername(properties.LDAP_CONN_USERNAME())
			.setPassword(properties.LDAP_CONN_PASSWORD_ENCRYPTED())
			.setGroupBase(properties.LDAP_BASE_GROUPS())
			.setUserBase(properties.LDAP_BASE_USERS())
			.setGroupIdAttribute(properties.LDAP_GROUP_ID())
			.setGroupMemberAttribute(properties.LDAP_GROUP_MEMBER())
			.setGroupNameAttribute(properties.LDAP_GROUP_NAME())
			.setGroupsAreRoles(properties.LDAP_GROUPS_ARE_ROLES())
			.setMemberAttribute(properties.LDAP_MEMBER_ATTRIBUTE())
			.setGroupObjectClasses(asSet(StringTools.splitCommaSeparatedString(properties.LDAP_GROUP_OBJECT_CLASSES(), true)))
			.setRoleIdAttribute(properties.LDAP_ROLE_ID())
			.setRoleNameAttribute(properties.LDAP_ROLE_NAME())
			.setUserIdAttribute(properties.LDAP_USER_ID())
			.setUserFirstNameAttribute(properties.LDAP_USER_FIRSTNAME())
			.setUserLastNameAttribute(properties.LDAP_USER_LASTNAME())
			.setUserUsernameAttribute(properties.LDAP_USER_NAME())
			.setUserDisplayNameAttribute(properties.LDAP_USER_DESCRIPTION())
			.setEmailAttribute(properties.LDAP_USER_MAIL())
			.setUserFilter(properties.LDAP_USER_FILTER())
			.setLastLogonAttribute(properties.LDAP_USER_LASTLOGON())
			.setMemberOfAttribute(properties.LDAP_USER_MEMBER_OF())
			.setUserObjectClasses(asSet(StringTools.splitCommaSeparatedString(properties.LDAP_USER_OBJECT_CLASSES(), true)))
			.setSearchPageSize(properties.LDAP_SEARCH_PAGESIZE())
			.setReferralFollow(properties.LDAP_REFERRAL_FOLLOW())
			.setConnectTimeout(properties.LDAP_CONNECT_TIMEOUT())
			.setDnsTimeout(properties.LDAP_DNS_TIMEOUT_INITIAL())
			.setDnsRetries(properties.LDAP_DNS_TIMEOUT_RETRIES())
			.setBase(properties.LDAP_BASE())
			.setUseEmptyAspects(properties.LDAP_USE_EMPTY_ASPECTS())
			.setIdPrefix("Ldap.Default")
			.setEntityFactory(super::create)
			.setModule(existingInstances.module())
			.setLookupFunction(super::lookup)
			.setLookupExternalIdFunction(super::lookupExternalId)
			.setName("Default")
			.build();
		//@formatter:on

		return context;

	}

	@Override
	public void cortexConfiguration() {
		if (properties.LDAP_ATTACH_TO_CORTEXCONFIGURATION()) {
			LdapTemplateContext context = defaultContext();
			ldapTemplates.attachToCortexConfiguration(context);
		}
	}

	@Override
	public boolean isLdapConfigured() {
		return !StringTools.isBlank(properties.LDAP_CONN_URL(null));
	}

	@Override
	@Managed
	public LdapConnection connection() {
		LdapTemplateContext context = defaultContext();
		return ldapTemplates.connection(context);
	}

	@Override
	@Managed
	public LdapUserAccess ldapUserAccess() {
		LdapTemplateContext context = defaultContext();
		return ldapTemplates.ldapUserAccess(context);
	}

	@Override
	@Managed
	public LdapAccess ldapAccess() {
		LdapTemplateContext context = defaultContext();
		return ldapTemplates.ldapAccess(context);
	}

	@Override
	@Managed
	public LdapAuthentication authentication() {
		LdapTemplateContext context = defaultContext();
		return ldapTemplates.authentication(context);
	}

	@Override
	@Managed
	public CheckBundle connectivityCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(connectivityHealthCheck());
		bean.setName("LDAP Connectivity Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Override
	@Managed
	public HealthCheckProcessor connectivityHealthCheck() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setExternalId("ldap-connectivity-check-processor");
		bean.setModule(existingInstances.module());
		bean.setName("LDAP Connectivity Check Processor");
		String thresholdString = TribefireRuntime.getProperty("LDAP_CONNECTIVITY_TIME_WARN_THRESHOLD", "150");
		long threshold = 150L;
		try {
			threshold = Long.parseLong(thresholdString);
		} catch (Exception e) {
			logger.warn(() -> "Could not parse LDAP_CONNECTIVITY_TIME_WARN_THRESHOLD value " + thresholdString, e);
		}
		bean.setTimeWarnThreshold(threshold);
		return bean;
	}

}
