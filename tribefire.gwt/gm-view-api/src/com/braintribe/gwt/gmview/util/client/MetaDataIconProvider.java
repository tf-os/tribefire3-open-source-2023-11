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
package com.braintribe.gwt.gmview.util.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.function.Function;

import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.ConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Resource;

public class MetaDataIconProvider implements IconProvider {
	
	private static final Logger logger = new Logger(MetaDataIconProvider.class);
	
	public enum Mode {
		customSize, // using configured min and max
		largest,
		smallest
	}
	
	private PersistenceGmSession gmSession;
	private String useCase;
	private boolean lenient = true;
	private Function<? super Resource, String> resourceUrlProvider;
	private int minHeight = 8;
	private int maxHeight = 16;
	private boolean metaDataNotAvailable;
	
	private Mode mode = Mode.customSize;
	
	/**
	 * Configures the URL provider for resources.
	 * It is used when the session is not capable of handling resources.
	 */
	@Configurable
	public void setResourceUrlProvider(Function<? super Resource, String> resourceUrlProvider) {
		this.resourceUrlProvider = resourceUrlProvider;
	}
	
	/**
	 * Configures the minimum height (in pixels), the icon should have. Defaults to 8.
	 */
	@Configurable
	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}
	
	/**
	 * Configures the maximum height (in pixels), the icon should have. Defaults to 16.
	 */
	@Configurable
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 * Configures the mode to determine the icon. Defaults to {@link Mode#customSize}
	 */
	@Configurable
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		if (gmSession == null || (gmSession instanceof ModelEnvironmentDrivenGmSession && ((ModelEnvironmentDrivenGmSession) gmSession).getModelEnvironment() == null))
			metaDataNotAvailable = true;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public IconAndType apply(ModelPath modelPath) {
		if (modelPath == null)
			return null;
		
		ModelMdResolver modelMdResolver = null;
		try {
			modelMdResolver = metaDataNotAvailable ? null : gmSession.getModelAccessory().getMetaData();
		} catch (Exception ex) {
			metaDataNotAvailable = true;
			logger.error("Error while getting ModelMdResolver", ex);
		}
		
		ModelPathElement pathElement = modelPath.last();
		
		Resource resource = null;
		Icon icon = null;
		if (pathElement instanceof PropertyRelatedModelPathElement) {
			PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) pathElement;
			GenericEntity entity = propertyElement.getEntity();
			if (entity != null) {
				icon = getMetaData(entity).lenient(lenient).entity(entity).property(propertyElement.getProperty().getName()).useCase(useCase).meta(Icon.T).exclusive();
				
				if (icon != null) {
					resource = getIconResource(icon);
					
					if (resource != null) {
						GmImageResource imageResource = prepareGmImageResource(resource);
						if (imageResource != null)
							return new IconAndType(imageResource, false);
					}
				} else if (entity instanceof VirtualEnum) {
					VirtualEnum ve = (VirtualEnum) entity;
					for (VirtualEnumConstant aec : ve.getConstants()) {
						if (aec.getValue().equals(propertyElement.getValue()))
							resource = getIconResource(aec.getIcon());	
					}
										
					if (resource != null) {
						GmImageResource imageResource = prepareGmImageResource(resource);
						if (imageResource != null)
							return new IconAndType(imageResource, false);
					}
				}
			}
		}
		
		if (pathElement.getType().isEntity()) {
			Object value = pathElement.getValue();
			
			EntityMdResolver entityContextBuilder;
			if (value instanceof GenericEntity)
				entityContextBuilder = getMetaData((GenericEntity) value).lenient(lenient).entity((GenericEntity) value).useCase(useCase);
			else {
				if (modelMdResolver != null)
					entityContextBuilder = modelMdResolver.lenient(lenient).entityType((EntityType<?>) pathElement.getType()).useCase(useCase);
				else
					entityContextBuilder = null;
			}
			
			if (entityContextBuilder != null) {
				icon = entityContextBuilder.meta(Icon.T).exclusive();
				
				if (icon == null && value instanceof GmEnumConstant) {
					GmEnumConstant constant = (GmEnumConstant) value;
					if (constant.getDeclaringType() != null)
						icon = getMetaData((GenericEntity) value).lenient(lenient).enumConstant(constant).useCase(useCase).meta(Icon.T).exclusive();
				}
			}
		} else if (pathElement.getType().isEnum()) {
			Object value = pathElement.getValue();
			if (value != null && !(value instanceof EnumType)) {
				EnumType et = (EnumType) pathElement.getType();
				Enum<?> e = et.getEnumValue(value.toString());
				
				if (modelMdResolver != null) {
					ConstantMdResolver enumMdResolver = modelMdResolver.lenient(lenient).enumType(et).constant(e).useCase(useCase);
					if (enumMdResolver != null)
						icon = enumMdResolver.meta(Icon.T).exclusive();
				}
			}
		}
		
		if (icon != null)
			resource = getIconResource(icon);
		
		if (resource != null) {
			GmImageResource imageResource = prepareGmImageResource(resource);
			if (imageResource != null)
				return new IconAndType(imageResource, true);
		}
		
		return null;
	}

	private Resource getIconResource(Icon icon) {
		logger.debug("Resolve resource for icon: "+icon.getId()+" with mode: "+mode);
		switch (mode) {
			case largest: return GMEIconUtil.getLargestImageFromIcon(icon.getIcon());
			case smallest: return GMEIconUtil.getSmallestImageFromIcon(icon.getIcon());
			case customSize:
				Resource iconResource = GMEIconUtil.getImageFromIcon(icon.getIcon(), minHeight, maxHeight);
				if (iconResource == null) //get any image
					iconResource = GMEIconUtil.getImageFromIcon(icon.getIcon(), 0, Integer.MAX_VALUE);
				return iconResource;
		}
		return null;
		
	}
	
	private Resource getIconResource(com.braintribe.model.resource.Icon icon) {
		if(icon != null) {
			logger.debug("Resolve resource for icon: "+icon.getId()+" with mode: "+mode);
			switch (mode) {
				case largest: return GMEIconUtil.getLargestImageFromIcon(icon);
				case smallest: return GMEIconUtil.getSmallestImageFromIcon(icon);
				case customSize:
					Resource iconResource = GMEIconUtil.getImageFromIcon(icon, minHeight, maxHeight);
					if (iconResource == null) //get any image
						iconResource = GMEIconUtil.getImageFromIcon(icon, 0, Integer.MAX_VALUE);
					return iconResource;
			}
		}
		return null;
		
	}
	
	private GmImageResource prepareGmImageResource(Resource resource) {
		if (resourceUrlProvider != null)
			return new GmImageResource(resource, resourceUrlProvider);
		
		ManagedGmSession theSession = gmSession;
		if (theSession == null && !(resource.session() instanceof ManagedGmSession))
			return null;
		
		if (theSession == null)
			theSession = (ManagedGmSession) resource.session();
		
		ResourceAccess resourceAccess = null;
		try {
			resourceAccess = theSession.getModelAccessory().getModelSession().resources();
			return new SizeAwareGmImageResource(resource, resourceAccess.url(resource).asString());
		} catch (UnsupportedOperationException ex) {
			logger.info("The session used for streaming is not a resource ready session. " + theSession);
		}
		
		return null;
	}
	
	private class SizeAwareGmImageResource extends GmImageResource {
		
		public SizeAwareGmImageResource(Resource delegate, final String url) {
			super(delegate,url);
		}
		
		@Override
		public int getWidth() {
			if (mode == Mode.customSize)
				return maxHeight;
			
			return super.getWidth();
		}
		
		@Override
		public int getHeight() {
			if (mode == Mode.customSize)
				return maxHeight;
			
			return super.getHeight();
		}
	}
	
//	protected Icon resolveValueIcon (Set<ValueIconMapping> mappings, Object matchValue) {
//		for (ValueIconMapping mapping : mappings) {
//			if ((matchValue == null && mapping.getMatchValue() == null) || (matchValue != null && matchValue.equals(mapping.getMatchValue())))
//				return mapping.getIcon();
//		}
//		return null;
//	}

}
