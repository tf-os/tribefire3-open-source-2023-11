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
package tribefire.platform.impl.preprocess;

/**
 * Replaces all values of the deployable where the meta-data RuntimeValue is set.
 * This must be activated by setting the TribefireRuntime variable TRIBEFIRE_DEPLOYMENT_RUNTIMEREPLACEMENT to true.
 * 
 * @author roman
 */
public class RuntimeValueReplacer /* implements DeploymentPreProcessor */ {

//	private static final Logger logger = Logger.getLogger(RuntimeValueReplacer.class);
//
//	public final static String REPLACEMENT_ACTIVATION_KEY = "TRIBEFIRE_DEPLOYMENT_RUNTIMEREPLACEMENT";
//
//	protected CodecRegistry codecRegistry = null;
//
//	@Override
//	public void preprocessDeployment(DeploymentContext<?> context) throws DeploymentException {
//
//		String activationString = TribefireRuntime.getProperty(REPLACEMENT_ACTIVATION_KEY);
//		if ((activationString == null) || (!activationString.equalsIgnoreCase("true"))) {
//			if (logger.isTraceEnabled()) {
//				logger.trace("RuntimeValueReplacer is deactivated. This can be activated by setting "+REPLACEMENT_ACTIVATION_KEY+"=true");
//			}
//			return;
//		}
//
//		if (codecRegistry == null) {
//			this.codecRegistry = new CodecRegistry();
//			BasicCodecs.registerCodecs(codecRegistry);
//		}
//
//
//		PersistenceGmSession session = context.getSession();
//		if (session == null) {
//			throw new DeploymentException("The session of the deployment context is null.");
//		}
//		Deployable deployable = context.getDeployable();
//		if (deployable == null) {
//			throw new DeploymentException("The deployable of the deployment context is null.");
//		}
//
//		ModelAccessory modelAccessory = session.getModelAccessory();
//		ModelMetaDataContextBuilder metaDataBuilder = modelAccessory.getMetaData();
//		EntityMetaDataContextBuilder entityMetaDataBuilder = metaDataBuilder.entity(deployable);
//
//		if (entityMetaDataBuilder == null) {
//			logger.debug("Cannot access the meta-data of deployable "+deployable);
//			return;
//		}
//
//		List<Property> propertyList = deployable.entityType().getProperties();
//		if ((propertyList == null) || (propertyList.isEmpty())) {
//			logger.debug("No properties defined on "+deployable);
//			return;
//		}
//
//		boolean changed = false;
//
//		for (Property property : propertyList) {
//			RuntimeValue replaceValue = entityMetaDataBuilder.property(property).meta(RuntimeValue.T).exclusive();
//
//			if (replaceValue != null) {
//				String runtimePropertyName = replaceValue.getRuntimePropertyName();
//
//				if ((runtimePropertyName != null) && (runtimePropertyName.trim().length() > 0)) {
//
//					String runtimeValue = TribefireRuntime.getProperty(runtimePropertyName);
//					
//					if (runtimeValue != null) {
//						GenericModelType propertyType = property.getPropertyType();
//						
//						Object newValueObject = null;
//						try {
//							newValueObject = this.codecRegistry.decode(runtimeValue, propertyType.getJavaType());
//						} catch (Exception e) {
//							logger.warn("Could not decode value "+runtimeValue+" of type "+propertyType+". Trying alternative way.", e);
//							if (propertyType instanceof SimpleType) {
//								SimpleType simpleType = (SimpleType) propertyType;
//								newValueObject = simpleType.instanceFromString(runtimeValue);
//							} else {
//								logger.warn("Could not decode value "+runtimeValue+" of type "+propertyType, e);
//							}
//						}
//						
//						if (isUpdateNecessary(deployable, property, newValueObject)) {
//							property.setProperty(deployable, newValueObject);
//							changed = true;
//						}
//					}
//				}
//			}
//		}
//
//		if (changed) {
//			try {
//				session.commit();
//			} catch (Exception e) {
//				throw new DeploymentException("Could not commit preprocessing changes from Runtime configuration.", e);
//			}
//		}
//	}
//
//	private boolean isUpdateNecessary(Deployable deployable, Property property, Object newValue) {
//		Object currentValue = property.getProperty(deployable);
//		if (currentValue == null) {
//			return (newValue != null);
//		}
//		return (!currentValue.equals(newValue));
//	}


}
