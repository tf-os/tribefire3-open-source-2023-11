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
package tribefire.extension.ldap.templates.wire.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.ldapaccessdeployment.LdapAccess;
import com.braintribe.model.ldapaccessdeployment.LdapUserAccess;
import com.braintribe.model.ldapauthenticationdeployment.LdapAuthentication;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.ldap.templates.api.LdapTemplateContext;
import tribefire.extension.ldap.templates.wire.contract.LdapTemplatesContract;

@Managed
public class LdapTemplatesSpace implements WireSpace, LdapTemplatesContract {

	private static final Logger logger = Logger.getLogger(LdapTemplatesSpace.class);

	@Import
	private LdapMetaDataSpace ldapMetaData;

	@Override
	public void attachToCortexConfiguration(LdapTemplateContext context) {
		CortexConfiguration cortexConfiguration = context.lookup("config:cortex");
		cortexConfiguration.setAuthenticationService(authentication(context));
	}

	@Managed
	@Override
	public LdapConnection connection(LdapTemplateContext context) {

		ldapMetaData.registerModels(context);
		ldapMetaData.metaData(context);

		LdapConnection bean = context.create(LdapConnection.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());
		bean.setName("LDAP Connection " + context.getName());
		bean.setConnectionUrl(context.getConnectionUrl());
		bean.setInitialContextFactory("com.sun.jndi.ldap.LdapCtxFactory");
		bean.setReferralFollow(context.getReferralFollow());
		bean.setConnectTimeout(context.getConnectTimeout());
		bean.setDnsTimeoutInitial(context.getDnsTimeout());
		bean.setDnsTimeoutRetries(context.getDnsRetries());
		bean.setUseTLSExtension(context.getUseTlsExtension());
		String username = context.getUsername();
		if (!StringTools.isBlank(username)) {
			bean.setUsername(username);
		}
		String password = context.getPassword();
		if (!StringTools.isBlank(password)) {
			bean.setPassword(password);
		}
		return bean;
	}

	@Managed
	@Override
	public LdapUserAccess ldapUserAccess(LdapTemplateContext context) {
		LdapUserAccess bean = context.create(LdapUserAccess.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());

		bean.setName("LDAP User Access " + context.getName());
		bean.setGroupBase(context.getGroupBase());
		bean.setGroupIdAttribute(context.getGroupIdAttribute());
		bean.setGroupMemberAttribute(context.getGroupMemberAttribute());
		bean.setGroupNameAttribute(context.getGroupNameAttribute());
		bean.setGroupObjectClasses(asList(context.getGroupObjectClasses()));
		bean.setGroupsAreRoles(context.getGroupsAreRoles());
		bean.setLdapConnection(connection(context));
		bean.setMemberAttribute(context.getMemberAttribute());
		bean.setRoleIdAttribute(context.getRoleIdAttribute());
		bean.setRoleNameAttribute(context.getRoleNameAttribute());
		bean.setUserBase(context.getUserBase());
		bean.setUserDescriptionAttribute(context.getUserDisplayNameAttribute());
		bean.setUserEmailAttribute(context.getEmailAttribute());
		bean.setUserFilter(context.getUserFilter());
		bean.setUserFirstNameAttribute(context.getUserFirstNameAttribute());
		bean.setUserIdAttribute(context.getUserIdAttribute());
		bean.setUserLastLoginAttribute(context.getLastLogonAttribute());
		bean.setUserLastNameAttribute(context.getUserLastNameAttribute());
		bean.setUserMemberOfAttribute(context.getMemberOfAttribute());
		bean.setUserNameAttribute(context.getUserUsernameAttribute());
		bean.setUserObjectClasses(asList(context.getUserObjectClasses()));
		bean.setSearchPageSize(context.getSearchPageSize());
		bean.setMetaModel(ldapMetaData.dataModel(context));

		if (context.getUseEmptyAspects()) {
			bean.setAspectConfiguration(aspectConfiguration(context));
		}

		return bean;
	}

	@Managed
	private AspectConfiguration aspectConfiguration(LdapTemplateContext context) {
		AspectConfiguration bean = context.create(AspectConfiguration.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	private static <T> List<T> asList(Set<T> set) {
		if (set == null) {
			return null;
		}
		return new ArrayList<T>(set);
	}

	@Managed
	@Override
	public LdapAccess ldapAccess(LdapTemplateContext context) {
		LdapAccess bean = context.create(LdapAccess.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());

		bean.setName("LDAP Access " + context.getName());
		bean.setBase(context.getBase());
		bean.setSearchPageSize(context.getSearchPageSize());
		bean.setLdapConnection(connection(context));
		bean.setMetaModel(ldapMetaData.dataModel(context));
		return bean;
	}

	@Managed
	@Override
	public LdapAuthentication authentication(LdapTemplateContext context) {
		LdapAuthentication bean = context.create(LdapAuthentication.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());

		bean.setName("LDAP Authentication Service " + context.getName());
		bean.setLdapAccess(ldapUserAccess(context));
		return bean;
	}

}
