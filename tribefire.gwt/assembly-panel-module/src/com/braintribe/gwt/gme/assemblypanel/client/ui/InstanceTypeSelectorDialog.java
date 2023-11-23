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
package com.braintribe.gwt.gme.assemblypanel.client.ui;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.isType;
import static com.braintribe.model.generic.typecondition.TypeConditions.not;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.assemblypanel.client.LocalizedText;
import com.braintribe.gwt.gme.assemblypanel.client.ui.InstanceTypeSelectorDialog.InstanceTypeSelectorDialogParameters;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.data.constraint.Instantiable;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.Radio;

/**
 * This dialog is responsible for providing the user with a selection between link, move, simple copy, shallow copy, deep copy and custom copy
 * when performing a DnD operation with an entity.
 * @author michel.docouto
 *
 */
public class InstanceTypeSelectorDialog extends ClosableWindow implements Function<InstanceTypeSelectorDialogParameters, Future<List<GMTypeInstanceBean>>> {
	
	public static final InstanceTypeSelectorDialog INSTANCE = new InstanceTypeSelectorDialog();

	private String selectedReferenceType;
	private Future<List<GMTypeInstanceBean>> future;
	private List<ModelPath> clipboardModelPaths;
	private GmSession gmSession;
	private List<Radio> radios;
	
	protected InstanceTypeSelectorDialog() {
		this.setSize("550px", "75px");
		this.setClosable(false);
		this.setModal(true);
		this.setBodyBorder(false);
		this.setBorders(false);
		getHeader().setHeight(20);
		
		this.add(prepareFormPanel());
		this.addButton(prepareCancelButton());
		this.addButton(prepareOkButton());
	}
	
	@Override
	public Future<List<GMTypeInstanceBean>> apply(InstanceTypeSelectorDialogParameters parameters) {
		this.clipboardModelPaths = parameters.getModelPaths();
		this.gmSession = parameters.getGmSession();
		future = new Future<>();
		checkInstantiableMd(parameters.useCase);
		show();
		return future;
	}
	
	public GMTypeInstanceBean getClone(InstanceTypeSelectorDialogParameters parameters) {
		this.gmSession = parameters.getGmSession();
		ModelPath modelPath = parameters.getModelPaths().get(0);
		StrategyOnCriterionMatch strategy = StrategyOnCriterionMatch.reference;
		ModelPathElement last = modelPath.last();
		GenericModelType type = last.getType();
		Object cloneValue = type.clone(getCloningContext(false), last.<Object>getValue(), strategy);
		return new GMTypeInstanceBean(type, cloneValue);
	}
	
	@Override
	public void hide() {
		future.onSuccess(null);
		super.hide();
	}
	
	private FormPanel prepareFormPanel() {
		FormPanel formPanel = new FormPanel();
		formPanel.setLabelWidth(100);
		
		ToggleGroup toggleGroup = new ToggleGroup();
		toggleGroup.addValueChangeHandler(event -> selectedReferenceType = ((Radio) event.getValue()).getBoxLabel().asString());
		HorizontalPanel referenceTypeGroup = new HorizontalPanel();
		
		radios = new ArrayList<>();
		boolean setValue = true;
		for (PasteType pasteType : PasteType.values()) {
			Radio radio = getPanelRadio(pasteType.getDescription());
			referenceTypeGroup.add(radio);
			toggleGroup.add(radio);
			if (setValue) {
				toggleGroup.setValue(radio);
				selectedReferenceType = radio.getBoxLabel().asString();
				setValue = false;
			}
			radios.add(radio);
		}
		
		formPanel.add(new FieldLabel(referenceTypeGroup, LocalizedText.INSTANCE.copyType()));
		return formPanel;
	}
	
	private Radio getPanelRadio(String name) {		
		final Radio radio =  new Radio();
		radio.setBoxLabel(name);
		
		return radio;
	}
	
	private TextButton prepareCancelButton() {
		TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> InstanceTypeSelectorDialog.super.hide());
		return cancelButton;
	}
	
	private TextButton prepareOkButton() {
		TextButton okButton = new TextButton(LocalizedText.INSTANCE.ok());
		okButton.addSelectHandler(event -> {
			InstanceTypeSelectorDialog.super.hide();
			try {
				PasteType pasteType = getSelectedPasteType();
				
				switch (pasteType) {
				case LINK:
						handleLink();
					break;
				case DEEP_COPY:
				case SHALLOW_COPY:
				case SIMPLE_COPY:
					handleCopy(pasteType);
					break;
				case MOVE:
					handleMove();
					break;
				}
			} catch (GenericModelException e) {
				future.onFailure(e);
			}
		});
		
		return okButton;
	}

	private void handleLink() {
		future.onSuccess(prepareInstanceBeans(clipboardModelPaths));
	}
	
	public PasteType getSelectedPasteType() {
		for (PasteType pasteType : PasteType.values()) {
			if (selectedReferenceType.equals(pasteType.getDescription()))
				return pasteType;
		}
		
		return null;
	}
	
	private CloningContext getCloningContext(boolean deepCopy) {
		StandardCloningContext cloningContext = new StandardCloningContext() {
			@SuppressWarnings("unusable-by-js")
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
					GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				if (property.isIdentifier() || property.isGlobalId())
					return false;
				else 
					return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
			}
			
			@SuppressWarnings("unusable-by-js")
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return gmSession.create(entityType);
			}
		};
		
		if (!deepCopy)
			cloningContext.setMatcher(matchEntitiesAndCollectionsMatcher());
		
		return cloningContext;
	}
	
	private List<GMTypeInstanceBean> prepareInstanceBeans(List<ModelPath> modelPaths) {
		List<GMTypeInstanceBean> beans = new ArrayList<>();
		for (ModelPath modelPath : modelPaths) {
			beans.addAll(prepareInstanceBean(modelPath));
		}
		
		return beans;
	}
	
	private List<GMTypeInstanceBean> prepareInstanceBean(ModelPath modelPath) {
		if (modelPath.last() instanceof MapValuePathElement) {
			List<GMTypeInstanceBean> list = new ArrayList<>();
			MapValuePathElement element = (MapValuePathElement) modelPath.last();
			if (element.getKeyType().isEntity())
				list.add(new GMTypeInstanceBean(element.getKeyType(), element.getKey()));
			if (element.getMapValueType().isEntity())
				list.add(new GMTypeInstanceBean(element.getMapValueType(), element.getMapValue()));
			return list;
		} else
			return Collections.singletonList(new GMTypeInstanceBean(modelPath.last().getType(), modelPath.last().getValue())); 
	}
	
	private void handleMove() {
		unlink();
		handleLink();
	}

	private void unlink() {
		for (ModelPath modelPath : clipboardModelPaths) {
			ModelPathElement last = modelPath.last();
			if (!last.isPropertyRelated())
				continue;
			
			if (last.isCollectionElementRelated()) {
				PropertyPathElement collectionElement = (PropertyPathElement) last.getPrevious();
				Object collection = collectionElement.getValue();
				
				switch (last.getElementType()) {
					case ListItem:
						((List<?>) collection).remove(((ListItemPathElement) last).getIndex());
						break;
					case SetItem:
						((Set<?>) collection).remove(last.getValue());
						break;
					case MapKey:
						((Map<?, ?>) collection).remove(last.getValue());
						break;
					case MapValue:
						((Map<?, ?>) collection).remove(((MapValuePathElement) last).getKey());
						break;
					default:
						break;
				}
			} else {
				PropertyRelatedModelPathElement lastElement = (PropertyRelatedModelPathElement) modelPath.last();
				GenericEntity entity = lastElement.getEntity();
				lastElement.getProperty().set(entity, null);
			}
		}
	}
	
	private void handleCopy(PasteType pasteType) {
		StrategyOnCriterionMatch strategy = PasteType.SIMPLE_COPY.equals(pasteType) ? StrategyOnCriterionMatch.skip : StrategyOnCriterionMatch.reference;
		boolean deepCopy = PasteType.DEEP_COPY.equals(pasteType);
		List<GMTypeInstanceBean> clones = new ArrayList<>();
		for (ModelPath modelPath : this.clipboardModelPaths) {
			ModelPathElement last = modelPath.last();
			if (last instanceof MapValuePathElement) {
				MapValuePathElement element = (MapValuePathElement) last;
				GenericModelType type = element.getKeyType();
				if (type.isEntity())
					clones.add(new GMTypeInstanceBean(type, type.clone(getCloningContext(deepCopy), element.<Object>getKey(), strategy)));
				type = element.getMapValueType();
				if (type.isEntity())
					clones.add(new GMTypeInstanceBean(type, type.clone(getCloningContext(deepCopy), element.<Object>getMapValue(), strategy)));
			} else {
				GenericModelType type = last.getType();
				clones.add(new GMTypeInstanceBean(type, type.clone(getCloningContext(deepCopy), last.<Object>getValue(), strategy)));
			}
		}
		this.future.onSuccess(clones);
	}
	
	private static Matcher matchEntitiesAndCollectionsMatcher() {
		StandardMatcher standardMatcher = new StandardMatcher();
		standardMatcher.setCriterion(matchEntitiesAndCollectionsTc());
		return standardMatcher;
	}

	/**
	 * LocalizedString is always cloned.
	 * <p>
	 * if entity is not a LS, its properties are cloned if they are simple/enum/LS/ACL
	 */
	private static TraversingCriterion matchEntitiesAndCollectionsTc() {
		// @formatter:off
		TraversingCriterion tc = TC.create()
			.pattern()
				.negation()
					.entity(LocalizedString.T)
				.conjunction()
					.property()
					.typeCondition(
						not(
							or(
								isKind(TypeKind.simpleType),
								isKind(TypeKind.enumType),
								isType(LocalizedString.T)
							)
						)
					)
				.close()
			.close()
		.done();
		// @formatter:on
		
		return GMEUtil.expandTc(tc);
	}
	
	private void checkInstantiableMd(String useCase) {
		boolean disableClone = false;
		for (ModelPath modelPath : clipboardModelPaths) {
			if (modelPath.last().getValue() instanceof GenericEntity) {
				GenericEntity entity = modelPath.last().getValue();
				if (!getMetaData(entity).entity(entity).useCase(useCase).is(Instantiable.T)) {
					disableClone = true;
					break;
				}
			}
		}
		
		changeCloneRadioEnablement(!disableClone);
	}
	
	private void changeCloneRadioEnablement(boolean visible) {
		for (Radio radio : radios) {
			String label = radio.getBoxLabel().asString();
			if (label.equals(PasteType.SIMPLE_COPY.getDescription()) || label.equals(PasteType.SHALLOW_COPY.getDescription()) || label.equals(PasteType.DEEP_COPY.getDescription())) {
				radio.setEnabled(visible);
			}
		}
	}

	public enum PasteType {
		LINK(LocalizedText.INSTANCE.link()), MOVE(LocalizedText.INSTANCE.move()), SIMPLE_COPY(LocalizedText.INSTANCE.simpleCopy()),
				SHALLOW_COPY(LocalizedText.INSTANCE.shallowCopy()), DEEP_COPY(LocalizedText.INSTANCE.deepCopy());
		
		private String description;
		
		PasteType(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
	}
	
	public static class InstanceTypeSelectorDialogParameters {
		private List<ModelPath> modelPaths;
		private GmSession gmSession;
		private String useCase;
		
		public InstanceTypeSelectorDialogParameters(List<ModelPath> modelPaths, GmSession gmSession, String useCase) {
			this.modelPaths = modelPaths;
			this.gmSession = gmSession;
			this.useCase = useCase;
		}
		
		public List<ModelPath> getModelPaths() {
			return modelPaths;
		}
		
		public void setModelPaths(List<ModelPath> modelPaths) {
			this.modelPaths = modelPaths;
		}
		
		public GmSession getGmSession() {
			return gmSession;
		}
		
		public void setGmSession(GmSession gmSession) {
			this.gmSession = gmSession;
		}
		
	}

}
