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
package com.braintribe.gwt.gmview.client;

import java.util.List;

import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class SelectionConfig {

	private GenericModelType gmType;
	private List<SelectionTabConfig> tabsConfig;
	private int maxSelection;
	private EntityProperty queryEntityProperty;
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private boolean useQueryTabAsDefault;
	private TypeCondition typeCondition;
	private boolean instantiable;
	private boolean referenceable;
	private boolean simplified;
	private boolean useDetail = true;
	private boolean singleInstantiationAndAssignment = false;
	private String title;
	private String subTitle;
	private GmContentView parentContentView;
	private boolean addingToSet;
	private List<Object> possibleValues;
	private PropertyRelatedModelPathElement propertyElement;
	private boolean handlingCollection;
	private Integer dialogWidth;
	private Integer dialogHeight;

	public SelectionConfig(GenericModelType gmType, int maxSelection, EntityProperty queryEntityProperty, PersistenceGmSession gmSession,
			PersistenceGmSession workbenchSession, boolean instantiable, boolean referenceable, boolean simplified, boolean useDetail,
			PropertyRelatedModelPathElement propertyElement) {
		this(gmType, null, maxSelection, queryEntityProperty, gmSession, workbenchSession, false, instantiable, referenceable, simplified, useDetail,
				propertyElement);
	}
	
	public SelectionConfig(GenericModelType gmType, int maxSelection, EntityProperty queryEntityProperty, PersistenceGmSession gmSession,
			PersistenceGmSession workbenchSession, PropertyMdResolver propertyMdResolver, ModelMdResolver modelMdResolver, PropertyRelatedModelPathElement propertyElement) {
		this(gmType, null, maxSelection, queryEntityProperty, gmSession, workbenchSession, false,
				GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver),
				GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver), isSimplified(propertyMdResolver) || gmType.isSimple(),
				isSimplifiedDetail(propertyMdResolver), propertyElement);
		
		if (simplified && gmType.isEntity()) {
			ModelOracle modelOracle = gmSession.getModelAccessory().getOracle();
			EntityType<?> entityType = (EntityType<?>) gmType;
			EntityTypeOracle entityTypeOracle = modelOracle.findEntityTypeOracle(entityType);
			boolean hasSubTypes = !entityTypeOracle.getSubTypes().asGmTypes().isEmpty();
			boolean sia = !entityType.isAbstract() && !hasSubTypes && !referenceable && instantiable;
			setSingleInstantiationAndAssignment(sia);
		}
	}

	private static boolean isSimplified(PropertyMdResolver propertyMdResolver) {
		SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
		return sa != null;
	}

	private static boolean isSimplifiedDetail(PropertyMdResolver propertyMdResolver) {
		SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
		if (sa == null)
			return true;
		
		return sa.getShowDetails();
	}

	public SelectionConfig(GenericModelType gmType, List<SelectionTabConfig> tabsConfig, int maxSelection, EntityProperty queryEntityProperty,
			PersistenceGmSession gmSession, PersistenceGmSession workbenchSession, boolean useQueryTabAsDefault, boolean instantiable,
			boolean referenceable, boolean simplified, boolean useDetail, PropertyRelatedModelPathElement propertyElement) {
		this.gmType = gmType;
		this.tabsConfig = tabsConfig;
		this.maxSelection = maxSelection;
		this.handlingCollection = maxSelection > 1;
		this.queryEntityProperty = queryEntityProperty;
		this.gmSession = gmSession;
		this.workbenchSession = workbenchSession;
		this.useQueryTabAsDefault = useQueryTabAsDefault;
		this.instantiable = instantiable;
		this.referenceable = referenceable;
		this.simplified = simplified;
		this.useDetail = useDetail;
		this.propertyElement = propertyElement;
	}

//	public SelectionConfig(TypeCondition typeCondition, GenericModelType gmType, int maxSelection, EntityProperty queryEntityProperty,
//			PersistenceGmSession gmSession, PersistenceGmSession workbenchSession, boolean instantiable, boolean referenceable, boolean simplified) {
//		this(gmType, null, maxSelection, queryEntityProperty, gmSession, workbenchSession, false, instantiable, referenceable, simplified);
//		this.typeCondition = typeCondition;
//	}

	public GenericModelType getGmType() {
		return this.gmType;
	}

	public void setGmType(GenericModelType gmType) {
		this.gmType = gmType;
	}

	public int getMaxSelection() {
		return this.maxSelection;
	}

	public void setMaxSelection(int maxSelection) {
		this.maxSelection = maxSelection;
	}
	
	public boolean isHandlingCollection() {
		return handlingCollection;
	}
	
	public void setHandlingCollection(boolean handlingCollection) {
		this.handlingCollection = handlingCollection;
	}

	public EntityProperty getQueryEntityProperty() {
		return this.queryEntityProperty;
	}

	public void setQueryEntityProperty(EntityProperty queryEntityProperty) {
		this.queryEntityProperty = queryEntityProperty;
	}

	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}

	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	public PersistenceGmSession getWorkbenchSession() {
		return this.workbenchSession;
	}

	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}

	public void setUseQueryTabAsDefault(boolean useQueryTabAsDefault) {
		this.useQueryTabAsDefault = useQueryTabAsDefault;
	}

	public boolean isUseQueryTabAsDefault() {
		return this.useQueryTabAsDefault;
	}

	public void setEntityQueries(List<SelectionTabConfig> tabsConfig) {
		this.tabsConfig = tabsConfig;
	}

	public List<SelectionTabConfig> getEntityQueries() {
		return this.tabsConfig;
	}

	public TypeCondition getTypeCondition() {
		return this.typeCondition;
	}

	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}
	
	public void setReferenceable(boolean referenceable) {
		this.referenceable = referenceable;
	}
	
	public boolean isReferenceable() {
		return referenceable;
	}
	
	public void setInstantiable(boolean instantiable) {
		this.instantiable = instantiable;
	}
	
	public boolean isInstantiable() {
		return instantiable;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

 	public String getSubTitle() {
		return subTitle;
	}
	
	public void setParentContentView(GmContentView parentContentView) {
		this.parentContentView = parentContentView;
	}

 	public GmContentView getParentContentView() {
		return parentContentView;
	}
	
	public boolean isSimplified() {
		return simplified;
	}
	
	public void setSimplified(boolean simplified) {
		this.simplified = simplified;
	}
	
	public void setUseDetail(boolean useDetail) {
		this.useDetail = useDetail;
	}
	
	public boolean getUseDetail() {
		return useDetail;
	}
	
	public void setPossibleValues(List<Object> possibleValues) {
		this.possibleValues = possibleValues;
	}
	
	public List<Object> getPossibleValues() {
		return possibleValues;
	}
	
	public void setSingleInstantiationAndAssignment(boolean sia) {
		this.singleInstantiationAndAssignment = sia;
	}

	public boolean isSingleInstantiationAndAssignment() {
		return singleInstantiationAndAssignment;
	}
	
	public boolean isAddingToSet() {
		return addingToSet;
	}

 	public void setAddingToSet(boolean addingToSet) {
		this.addingToSet = addingToSet;
	}
 	
 	public PropertyRelatedModelPathElement getPropertyElement() {
		return propertyElement;
	}
 	
 	public void setDialogWidth(Integer dialogWidth) {
		this.dialogWidth = dialogWidth;
	}
 	
 	public Integer getDialogWidth() {
		return dialogWidth;
	}
 	
 	public void setDialogHeight(Integer dialogHeight) {
		this.dialogHeight = dialogHeight;
	}
 	
 	public Integer getDialogHeight() {
		return dialogHeight;
	}
	
}
