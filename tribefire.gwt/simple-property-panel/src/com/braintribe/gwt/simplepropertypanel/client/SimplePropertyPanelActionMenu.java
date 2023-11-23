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
package com.braintribe.gwt.simplepropertypanel.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.braintribe.gwt.simplepropertypanel.client.resources.SimplePropertyPanelResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class SimplePropertyPanelActionMenu {
	
	protected List<GmTypeKind> simpleTypeKinds = Arrays.asList(GmTypeKind.BASE, GmTypeKind.BOOLEAN, GmTypeKind.DATE, GmTypeKind.DECIMAL, GmTypeKind.DOUBLE,
			GmTypeKind.FLOAT, GmTypeKind.INTEGER, GmTypeKind.LONG, GmTypeKind.STRING);
	
	protected SimplePropertySection section;
	protected PersistenceGmSession session;
	protected Menu cardinalities;
//	protected Menu simpleTypes;
	
	protected Map<String, SVGResource> svgResources = new HashMap<>();
	
	public SimplePropertyPanelActionMenu() {
		svgResources.put("SINGLE", SimplePropertyPanelResources.INSTANCE.circle());
		svgResources.put("LIST", SimplePropertyPanelResources.INSTANCE.doubleCircleArrow());
		svgResources.put("SET", SimplePropertyPanelResources.INSTANCE.doubleCircle());
		svgResources.put("MAP", SimplePropertyPanelResources.INSTANCE.keyCircle());
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public SVGResource getIcon(String cardinality){
		return svgResources.get(cardinality);
	}
	
	private SafeHtml getIconWrapper(String cardinality){
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		
		builder.appendHtmlConstant("<div class='simplePropertyPanelButtonWrapper'>");
		builder.appendHtmlConstant(getIcon(cardinality).getSvg().getElement().getString());
		builder.appendHtmlConstant("<div>"+cardinality+"</div>");
		builder.appendHtmlConstant("</div>");
		
		return builder.toSafeHtml();
	}
	
	public void showCardinalities(SimplePropertySection section, Widget widget){
		this.section = section;
		getCardinalities().show(widget);
	}
	
	public void showSimpleTypes(SimplePropertySection section, Widget widget, boolean key){
		this.section = section;
		getSimpleTypes(key).show(widget);
	}
	
	public Menu getSimpleTypes(boolean key) {
//		if(simpleTypes == null){
		Menu simpleTypes = new Menu();
		
		for (GmTypeKind kind : simpleTypeKinds) {
			MenuItem kindItem = new MenuItem(kind != GmTypeKind.BASE ? kind.name().toLowerCase() : "object");
			
			kindItem.addSelectionHandler(event -> changeType(section.getProperty(), section.getCurrentCollectionTypeKind(),
					key ? kind : section.getCurrentKeyType(), key ? section.getCurrentValueType() : kind));
			
			simpleTypes.add(kindItem);
		}
//		}
		return simpleTypes;
	}
	
	public Menu getCardinalities() {
		if (cardinalities != null)
			return cardinalities;
		
		cardinalities = new Menu();
		
		MenuItem single = new MenuItem(getIconWrapper("SINGLE"));
		single.addSelectionHandler(event -> changeType(section.getProperty(), null, section.getCurrentKeyType(), section.getCurrentValueType()));
		cardinalities.add(single);
		
		MenuItem list = new MenuItem(getIconWrapper("LIST"));
		list.addSelectionHandler(event -> changeType(section.getProperty(), GmTypeKind.LIST, section.getCurrentKeyType(), section.getCurrentValueType()));
		cardinalities.add(list);
		
		MenuItem set = new MenuItem(getIconWrapper("SET"));
		set.addSelectionHandler(event -> changeType(section.getProperty(), GmTypeKind.SET, section.getCurrentKeyType(), section.getCurrentValueType()));
		cardinalities.add(set);
		
		MenuItem map = new MenuItem(getIconWrapper("MAP"));
		map.addSelectionHandler(event -> changeType(section.getProperty(), GmTypeKind.MAP, section.getCurrentKeyType(), section.getCurrentValueType()));
		cardinalities.add(map);
		
		return cardinalities;
	}
	
	public void changeType(GmProperty property, GmType type) {
		property.setType(type);
		if (section != null)
			section.handleAutoCommit();
	}
	
	public void changeType(GmProperty property, GmTypeKind collectionTypeKind, GmTypeKind keyTypeKind, GmTypeKind valueTypeKind) {
		String typeGlobalId = GmTypeRendering.getTypeGlobalId(collectionTypeKind, keyTypeKind, valueTypeKind);
		
		GmType type = session.findEntityByGlobalId(typeGlobalId);
		if (type != null) {
			changeType(property, type);
			return;
		}
		
		EntityQuery query = EntityQueryBuilder.from(GenericEntity.class).where().property(GenericEntity.globalId).eq(typeGlobalId).tc().negation()
				.joker().done();
		session.query().entities(query).result(AsyncCallback.of(future -> {
			GmType gmType = future.first();
			if (gmType != null)
				changeType(property, gmType);
			else
				createType(property, collectionTypeKind, keyTypeKind, valueTypeKind, typeGlobalId);
		}, e -> e.printStackTrace()));
	}

	public void createType(GmProperty property, GmTypeKind collectionTypeKind, GmTypeKind keyKind, GmTypeKind valueKind, String typeGlobalId){
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		
		EntityType<? extends GmType> type = null;
		
		if (collectionTypeKind != null) {
			switch(collectionTypeKind){
			case LIST:
				type = GmListType.T;
				break;
			case SET:
				type = GmSetType.T;
				break;
			case MAP:
				type = GmMapType.T;
				break;
			default:
				break;
			}
		}
		
//		String typeGlobalId = GmTypeRendering.getTypeGlobalId(collectionTypeKind, keyKind, valueKind);
		
		GmType gmType = session.findEntityByGlobalId(typeGlobalId) != null ? session.findEntityByGlobalId(typeGlobalId)  : session.create(type, typeGlobalId);
		GmType childType = session.findEntityByGlobalId("type:" + keyKind.name().toLowerCase());
		GmType valueType = session.findEntityByGlobalId("type:" + valueKind.name().toLowerCase());
		
		if (gmType instanceof GmLinearCollectionType) {
			((GmLinearCollectionType) gmType).setElementType(childType);
		} else if(gmType instanceof GmMapType) {
			GmMapType mapType = (GmMapType) gmType;
			mapType.setKeyType(childType);
			mapType.setValueType(valueType);
		}
			
		gmType.setTypeSignature(typeGlobalId.split(":")[1]);
		
		property.setType(gmType);
		nt.commit();
		
		if (section != null)
			section.handleAutoCommit();
	}
}
