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

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.logging.Logger;
import com.braintribe.model.ldap.Computer;
import com.braintribe.model.ldap.LdapAttribute;
import com.braintribe.model.ldap.LdapObjectClasses;
import com.braintribe.model.ldap.User;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.ldap.LdapConstants;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.ldap.templates.api.LdapTemplateContext;
import tribefire.extension.ldap.templates.wire.contract.LdapMetaDataContract;
import tribefire.extension.ldap.templates.wire.contract.LdapTemplatesContract;

@Managed
public class LdapMetaDataSpace implements WireSpace, LdapMetaDataContract {

	private static final Logger logger = Logger.getLogger(LdapMetaDataSpace.class);

	@Import
	private LdapTemplatesContract documentsTemplates;

	@Override
	@Managed
	public GmMetaModel dataModel(LdapTemplateContext context) {
		GmMetaModel rawDataModel = (GmMetaModel) context.lookup("model:" + LdapConstants.DATA_MODEL_QUALIFIEDNAME);
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel userModel = (GmMetaModel) context.lookup("model:tribefire.cortex.services:tribefire-user-model");
		setModelDetails(model, LdapConstants.DATA_MODEL_QUALIFIEDNAME + "-" + normalizeName(context), rawDataModel, userModel);
		return model;

	}

	@Override
	@Managed
	public GmMetaModel serviceModel(LdapTemplateContext context) {
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = (GmMetaModel) context.lookup("model:" + LdapConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, LdapConstants.SERVICE_MODEL_QUALIFIEDNAME + "-" + normalizeName(context), rawServiceModel);
		return model;
	}

	@Override
	@Managed
	public GmMetaModel deploymentModel(LdapTemplateContext context) {
		GmMetaModel rawDataModel = (GmMetaModel) context.lookup("model:" + LdapConstants.DEPLOYMENT_MODEL_QUALIFIEDNAME);
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		setModelDetails(model, LdapConstants.DEPLOYMENT_MODEL_QUALIFIEDNAME + "-" + normalizeName(context), rawDataModel);
		return model;

	}

	private static String normalizeName(LdapTemplateContext context) {
		String name = context.getName();
		if (name == null) {
			throw new IllegalArgumentException("The context does not contain a name.");
		}
		String newName = name.toLowerCase().replace(' ', '.');
		newName = newName.replace('/', '-');
		return newName;
	}

	@Override
	@Managed
	public Boolean metaData(LdapTemplateContext context) {

		GmMetaModel dataModel = dataModel(context);
		BasicModelMetaDataEditor modelEditor = new BasicModelMetaDataEditor(dataModel);

		//@formatter:off
		modelEditor.onEntityType(User.T)
			.addMetaData(ldapObjectClass(context, "user", "user"))
			.addPropertyMetaData(User.givenName, ldapAttribute(context, "givenName"))
			.addPropertyMetaData(User.mail, ldapAttribute(context, "mail"))
			.addPropertyMetaData(User.sn, ldapAttribute(context, "sn"))
			.addPropertyMetaData(User.dn, ldapAttribute(context, "dn"));
		modelEditor.onEntityType(Computer.T)
			.addMetaData(ldapObjectClass(context, "computer", "computer"))
			.addPropertyMetaData(Computer.cn, ldapAttribute(context, "cn"))
			.addPropertyMetaData(Computer.dnsHostName, ldapAttribute(context, "dnsHostName"))
			.addPropertyMetaData(Computer.dn, ldapAttribute(context, "dn"));
		
		GmMetaModel deploymentModel = deploymentModel(context);
		
		BasicModelMetaDataEditor deploymentModelEditor = new BasicModelMetaDataEditor(deploymentModel);
		
		deploymentModelEditor.onEntityType(LdapConnection.T)
			.addPropertyMetaData(LdapConnection.connectionUrl, mandatory(context))
			.addPropertyMetaData(LdapConnection.password, confidential(context));
		//@formatter:on

		return Boolean.TRUE;
	}

	@Managed
	public Mandatory mandatory(LdapTemplateContext context) {
		Mandatory bean = context.create(Mandatory.T, InstanceConfiguration.currentInstance());
		return bean;
	}
	@Managed
	private Confidential confidential(LdapTemplateContext context) {
		Confidential bean = context.create(Confidential.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	private LdapObjectClasses ldapObjectClass(LdapTemplateContext context, String type, String... objectClasses) {
		LdapObjectClasses bean = context.create(LdapObjectClasses.T, InstanceConfiguration.currentInstance());
		bean.setGlobalId(bean.getGlobalId() + "/" + type);
		bean.getObjectClasses().addAll(asList(objectClasses));
		return bean;
	}
	@Managed
	private LdapAttribute ldapAttribute(LdapTemplateContext context, String attributeName) {
		LdapAttribute bean = context.create(LdapAttribute.T, InstanceConfiguration.currentInstance());
		bean.setGlobalId(bean.getGlobalId() + "/" + attributeName);
		bean.setAttributeName(attributeName);
		return bean;
	}

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(LdapConstants.MAJOR_VERSION + ".0");
		if (dependencies != null) {
			for (GmMetaModel dependency : dependencies) {
				if (dependency != null) {
					targetModel.getDependencies().add(dependency);
				}
			}
		}
	}

	@Override
	@Managed
	public Boolean registerModels(LdapTemplateContext context) {
		GmMetaModel cortexModel = context.lookup(CoreInstancesContract.cortexModelName);
		GmMetaModel deploymentModel = deploymentModel(context);
		cortexModel.getDependencies().add(deploymentModel);

		GmMetaModel cortexServiceModel = context.lookup(CoreInstancesContract.cortexServiceModelName);
		GmMetaModel serviceModel = serviceModel(context);
		cortexServiceModel.getDependencies().add(serviceModel);

		return Boolean.TRUE;
	}
}
