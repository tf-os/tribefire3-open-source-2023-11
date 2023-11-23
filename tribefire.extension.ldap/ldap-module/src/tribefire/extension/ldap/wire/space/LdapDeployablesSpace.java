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
package tribefire.extension.ldap.wire.space;

import com.braintribe.model.access.impl.LdapAccess;
import com.braintribe.model.access.impl.LdapUserAccess;
import com.braintribe.model.ldapauthenticationdeployment.LdapAuthentication;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.ldap.service.HealthCheckProcessor;
import com.braintribe.model.processing.securityservice.ldap.LdapAuthenticationService;
import com.braintribe.provider.Holder;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.StrictSslSocketFactoryProvider;
import com.braintribe.utils.ldap.LdapConnectionStack;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

@Managed
public class LdapDeployablesSpace implements WireSpace {

	@Import
	protected TribefirePlatformContract tfPlatform;

	@Import
	protected SystemUserRelatedContract systemUserRelated;

	/** ldap.authentication.service */
	@Managed
	public LdapAuthenticationService service(ExpertContext<LdapAuthentication> context) {

		LdapAuthentication deployable = context.getDeployable();

		LdapAuthenticationService bean = new LdapAuthenticationService();
		bean.setSessionFactory(systemUserRelated.sessionFactory());
		bean.setLdapAccessId(deployable.getLdapAccess().getExternalId());
		return bean;
	}

	/** ldap.access */
	@Managed
	public LdapAccess access(ExpertContext<com.braintribe.model.ldapaccessdeployment.LdapAccess> context) {

		LdapAccess bean = new LdapAccess();

		com.braintribe.model.ldapaccessdeployment.LdapAccess deployable = context.getDeployable();

		com.braintribe.utils.ldap.LdapConnection ldapConnectionStack = context.resolve(deployable.getLdapConnection(), LdapConnection.T);

		bean.setMetaModelProvider(new Holder<>(deployable.getMetaModel()));
		bean.setLdapConnectionStack(ldapConnectionStack);
		bean.setBase(deployable.getBase());
		bean.setSearchPageSize(deployable.getSearchPageSize());

		return bean;

	}

	/** ldap.user.access */
	@Managed
	public LdapUserAccess userAccess(ExpertContext<com.braintribe.model.ldapaccessdeployment.LdapUserAccess> context) {

		LdapUserAccess bean = new LdapUserAccess();

		com.braintribe.model.ldapaccessdeployment.LdapUserAccess deployable = context.getDeployable();

		com.braintribe.utils.ldap.LdapConnection ldapConnectionStack = context.resolve(deployable.getLdapConnection(), LdapConnection.T);

		bean.setMetaModelProvider(new Holder<>(deployable.getMetaModel()));
		bean.setGroupBase(deployable.getGroupBase());
		bean.setGroupIdAttribute(deployable.getGroupIdAttribute());
		bean.setGroupMemberAttribute(deployable.getGroupMemberAttribute());
		bean.setGroupNameAttribute(deployable.getGroupNameAttribute());
		bean.setGroupObjectClasses(deployable.getGroupObjectClasses());
		bean.setGroupsAreRoles(deployable.getGroupsAreRoles());
		bean.setLdapConnectionStack(ldapConnectionStack);
		bean.setMemberAttribute(deployable.getMemberAttribute());
		bean.setRoleIdAttribute(deployable.getRoleIdAttribute());
		bean.setRoleNameAttribute(deployable.getRoleNameAttribute());
		bean.setUserBase(deployable.getUserBase());
		bean.setUserDescriptionAttribute(deployable.getUserDescriptionAttribute());
		bean.setUserEmailAttribute(deployable.getUserEmailAttribute());
		bean.setUserFilter(deployable.getUserFilter());
		bean.setUserFirstNameAttribute(deployable.getUserFirstNameAttribute());
		bean.setUserIdAttribute(deployable.getUserIdAttribute());
		bean.setUserLastLoginAttribute(deployable.getUserLastLoginAttribute());
		bean.setUserLastNameAttribute(deployable.getUserLastNameAttribute());
		bean.setUserMemberOfAttribute(deployable.getUserMemberOfAttribute());
		bean.setUserNameAttribute(deployable.getUserNameAttribute());
		bean.setUserObjectClasses(deployable.getUserObjectClasses());
		bean.setSearchPageSize(deployable.getSearchPageSize());

		return bean;

	}

	/** ldap.connection */
	@Managed
	public LdapConnectionStack connection(ExpertContext<LdapConnection> context) {

		LdapConnection deployable = context.getDeployable();

		LdapConnectionStack bean = new LdapConnectionStack();
		bean.setConnectionUrl(deployable.getConnectionUrl());
		bean.setUsername(deployable.getUsername());
		bean.setPassword(deployable.getPassword());
		bean.setInitialContextFactory(deployable.getInitialContextFactory());
		bean.setReferralFollow(deployable.getReferralFollow());
		bean.setUseTLSExtension(deployable.getUseTLSExtension());
		bean.setConnectTimeout(deployable.getConnectTimeout());
		bean.setDnsTimeoutInitial(deployable.getDnsTimeoutInitial());
		bean.setDnsTimeoutRetries(deployable.getDnsTimeoutRetries());
		bean.setEnvironmentSettings(deployable.getEnvironmentSettings());
		bean.setSslSocketFactoryProvider(sslSocketFactoryProvider());

		return bean;

	}

	@Managed
	public SslSocketFactoryProvider sslSocketFactoryProvider() {
		SslSocketFactoryProvider bean = TribefireRuntime.getAcceptSslCertificates() ? new EasySslSocketFactoryProvider()
				: new StrictSslSocketFactoryProvider();

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor(ExpertContext<com.braintribe.model.ldapaccessdeployment.HealthCheckProcessor> context) {

		com.braintribe.model.ldapaccessdeployment.HealthCheckProcessor deployable = context.getDeployable();

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setSessionFactory(systemUserRelated.sessionFactory());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setTimeWarnThreshold(deployable.getTimeWarnThreshold());
		return bean;

	}
}
