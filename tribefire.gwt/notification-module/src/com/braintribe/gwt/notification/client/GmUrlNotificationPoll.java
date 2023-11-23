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
package com.braintribe.gwt.notification.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.browserfeatures.client.HashChangeEventDistributor;
import com.braintribe.gwt.browserfeatures.client.HashChangeListener;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.codec.string.client.EnumCodec;
import com.braintribe.gwt.codec.string.client.PassThroughCodec;
import com.braintribe.model.generic.GMF;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;

/**
 * The NotificationPoll uses the {@link CrossDomainJsonRequest} to
 * poll {@link Notification} instances from a server. After a request
 * has transported one {@link Notification} or was been closed due to
 * timeout or error a new one will be started.
 * 
 * The NotificationPoll will start requests when it is initialized and will stop
 * the requests when it is disposed. 
 * 
 * The NotificationPoll will also stop with the automatic request series
 * after a configurable maximal amount of failures in sequence have happened.
 * 
 * @author Dirk
 *
 */
public class GmUrlNotificationPoll implements InitializableBean, DisposableBean {
	private static Logger logger = new Logger(GmUrlNotificationPoll.class);

	private Map<String, Supplier<? extends GmNotificationMapping<?>>> notificationMappings;
	private static Map<GenericModelType, Codec<?, ?>> codecs = new HashMap<>(); 
	
	private String urlActionBaseName = "do";
	private String urlParameterBaseName = "par";
	private String urlReadSessionActionName = "readSessionURL";

	private boolean pollQueryPart = true;
	private boolean pollHashPart = false;
	private boolean showWarnings = false;
	
	private String lastHash = "";
	private PersistenceGmSession session;
	
	private HashChangeListener hashChangeListener = newValue -> pollHashPart();
	
	@Configurable
	public void setPollHashPart(boolean pollHashPart) {
		this.pollHashPart = pollHashPart;
	}
	
	@Configurable
	public void setPollQueryPart(boolean pollQueryPart) {
		this.pollQueryPart = pollQueryPart;
	}
	
	@Configurable
	public void setShowWarnings(boolean showWarnings) {
		this.showWarnings = showWarnings;
	}
	
	@Configurable
	public void setUrlParameterBaseName(String urlParameterBaseName) {
		this.urlParameterBaseName = urlParameterBaseName;
	}
	
	@Configurable
	public void setUrlActionBaseName(String urlActionBaseName) {
		this.urlActionBaseName = urlActionBaseName;
	}
	
	/**
	 * Configures the name of the action responsible for reading the url parameters from the session.
	 * Defaults to "readSessionURL".
	 */
	@Configurable
	public void setUrlReadSessionActionName(String urlReadSessionActionName) {
		this.urlReadSessionActionName = urlReadSessionActionName;
	}
	
	@Configurable @Required
	public void setNotificationMappings(Map<String, Supplier<? extends GmNotificationMapping<?>>> notificationMappings) {
		this.notificationMappings = notificationMappings;
	}
	
	@Configurable
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	@Override
	public void intializeBean() throws Exception {
		if (pollQueryPart)
			pollQueryPart();
		
		if (pollHashPart) {
			HashChangeEventDistributor.getInstance().addHashChangeListener(hashChangeListener);
			pollHashPart();
		}
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (pollHashPart)
			HashChangeEventDistributor.getInstance().removeHashChangeListener(hashChangeListener);
	}
	
	protected void pollQueryPart() {
		poll(UrlParameters.getInstance());
	}
	
	protected void pollHashPart() {
		String hash = Window.Location.getHash();
		
		if (!lastHash.equals(hash)) {
			lastHash = hash;
			poll(new UrlParameters(hash));
		}
	}
	
	protected void poll(UrlParameters urlParameters) {
		String action = urlParameters.getParameter(urlActionBaseName);
		if (action == null || action.isEmpty())
			return;
		
		if(session != null)session.suspendHistory();
		Map<String, Object> parameterStructure = new HashMap<>();	
		for (Map.Entry<String, String> entry: urlParameters.getParameters().entrySet()) {
			String key = entry.getKey();
			List<String> path = Arrays.asList(key.split("\\."));
			if (path.size() > 0 && path.get(0).equals(urlParameterBaseName)) {
				path = path.subList(1, path.size());
				putToStructuredMap(parameterStructure, path, entry.getValue());
			}
		}
		
		Supplier<? extends GmNotificationMapping<?>> supplier = notificationMappings.get(action);
		if (supplier != null) {
			GmNotificationMapping<GenericEntity> notificationMapping = (GmNotificationMapping<GenericEntity>) supplier.get();
			try {
				Class<? extends GenericEntity> entityClass = notificationMapping.getEntityClass();
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityClass);
				Codec<GenericEntity, Object> mapCodec = aquireCodec(entityType);
				GenericEntity entity = mapCodec.decode(parameterStructure);
				Notification<GenericEntity> notification = new Notification<GenericEntity>(action, entityType.getTypeSignature(), entity);
				notificationMapping.getNotificationListener().onNotificationReceived(notification);
			} catch (Exception e) {
				logger.error("error while decoding action " + action, e);
			}
		} else if (action.equals(urlReadSessionActionName)) {
			throw new UnsupportedOperationException("The '" + urlReadSessionActionName + "' parameter is not supported.");
		} else {
			if(showWarnings){
				AlertMessageBox alert = new AlertMessageBox("No action found!", "no notification mapping found for '" + action + "'");
				alert.show();
			}
			logger.error("no notification mapping found for " + action);
			
		}
		if(session != null)session.resumeHistory();
	}
	
	protected void putToStructuredMap(Map<String, Object> rootMap, List<String> path, String value) {
		String key = path.get(0);	

		if (path.size() == 1) {
			rootMap.put(key, value);
		}
		else {
			Map<String, Object> map = (Map<String, Object>)rootMap.get(key);
			if (map == null) {
				map = new HashMap<String, Object>();
				rootMap.put(key, map);
			}
			
			int size = path.size();
			List<String> subPath = path.subList(1, size);
			putToStructuredMap(map, subPath, value);
		}
	}
	
	protected <T> Codec<T, Object> aquireCodec(final GenericModelType type) {
		Codec<?, ?> codec = codecs.get(type);
		if (codec != null)
			return (Codec<T, Object>) codec; 
		
		if (type instanceof EntityType) {
			final EntityType<GenericEntity> entityType = (EntityType<GenericEntity>)type;
			codec = new Codec<GenericEntity, Map<String, Object>>() {
				@Override
				public GenericEntity decode(Map<String, Object> encodedValue) throws CodecException {
					GenericEntity entity = session != null ? session.create(entityType) : entityType.create();
					for (Map.Entry<String, Object> entry: encodedValue.entrySet()) {
						Property property = entityType.getProperty(entry.getKey());
						Object encodedPropertyValue = entry.getValue();
						if (encodedPropertyValue != null) {
							GenericModelType propertyType = property.getType();
							Codec<Object, Object> propertyCodec = aquireCodec(propertyType);
							Object propertyValue = propertyCodec.decode(encodedPropertyValue);
							property.set(entity, propertyValue);
						}
					}
					return entity;
				}
				
				@Override
				public Map<String, Object> encode(GenericEntity value) throws CodecException {
					throw new UnsupportedOperationException();
				}
				
				@Override
				public Class<GenericEntity> getValueClass() {
					return entityType.getJavaType();
				}
			};
		}
		if (type instanceof SimpleType) {
			final SimpleType simpleType = (SimpleType)type;
			
			Codec<T, String> simpleCodec = new Codec<T, String>() {
				@Override
				public T decode(String encodedValue) throws CodecException {
					if (encodedValue == null) 
						return null;
					return (T)simpleType.instanceFromString(encodedValue);
				}
				
				@Override
				public String encode(T value) throws CodecException {
					if (value == null) return null;
					else return simpleType.instanceToString(value);
				}
				
				@Override
				public Class<T> getValueClass() {
					return (Class<T>)simpleType.getJavaType();
				}
			};
			
			codec = simpleCodec;
		}
		else if (type instanceof EnumType) {
			EnumType enumType = (EnumType)type;
			@SuppressWarnings("rawtypes")
			EnumCodec enumCodec = new EnumCodec();
			enumCodec.setEnumClass(enumType.getJavaType());
			codec = enumCodec;
		}
		else if (type instanceof BaseType) {
			codec = new PassThroughCodec<>(String.class);
		}
		else if (type instanceof CollectionType) {
			CollectionType collectionType = (CollectionType)type;
			final Codec<Object, Object> elementCodec = aquireCodec(collectionType.getCollectionElementType());
			switch (collectionType.getCollectionKind()) {
			case list:
				codec = new Codec<List<Object>, Map<String, Object>>() {
					@Override
					public List<Object> decode(Map<String, Object> encodedValue) throws CodecException {
						List<Object> list = new ArrayList<>();
						
						for (Map.Entry<String, Object> entry: encodedValue.entrySet()) {
							Object value = elementCodec.decode(entry.getValue());
							list.add(value);
						}
						return list;
					}
					@Override
					public Map<String, Object> encode(List<Object> value) throws CodecException {
						throw new UnsupportedOperationException();
					}
					@Override
					@SuppressWarnings("rawtypes")
					public Class<List<Object>> getValueClass() {
						return (Class)List.class;
					}
				};
				break;
			case set:
				codec = new Codec<Set<Object>, Map<String, Object>>() {
					@Override
					public Set<Object> decode(Map<String, Object> encodedValue) throws CodecException {
						Set<Object> set = new HashSet<>();
						
						for (Map.Entry<String, Object> entry: encodedValue.entrySet()) {
							Object value = elementCodec.decode(entry.getValue());
							set.add(value);
						}
						return set;
					}
					@Override
					public Map<String, Object> encode(Set<Object> value) throws CodecException {
						throw new UnsupportedOperationException();
					}
					@Override
					@SuppressWarnings("rawtypes")
					public Class<Set<Object>> getValueClass() {
						return (Class)Set.class;
					}
				};
				break;
			case map:
				codec = new Codec<Map<String, Object>, Map<String, Object>>() {
					@Override
					public Map<String, Object> decode(Map<String, Object> encodedValue) throws CodecException {
						Map<String, Object> map = new HashMap<>();
						
						for (Map.Entry<String, Object> entry: encodedValue.entrySet()) {
							String key = entry.getKey();
							Object value = elementCodec.decode(entry.getValue());
							map.put(key, value);
						}
						return map;
					}
					@Override
					public Map<String, Object> encode(Map<String, Object> value) throws CodecException {
						throw new UnsupportedOperationException();
					}
					@Override
					@SuppressWarnings("rawtypes")
					public Class<Map<String, Object>> getValueClass() {
						return (Class)Map.class;
					}
				};
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		
		return (Codec<T, Object>) codec; 
	}
	
}
