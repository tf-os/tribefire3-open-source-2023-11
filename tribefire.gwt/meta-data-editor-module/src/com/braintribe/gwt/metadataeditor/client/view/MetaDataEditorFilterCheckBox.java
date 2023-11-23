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
package com.braintribe.gwt.metadataeditor.client.view;

import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.form.CheckBox;

public class MetaDataEditorFilterCheckBox extends HorizontalLayoutContainer implements HasChangeHandlers {
     private String text = "";
     private String colorUseCase= "rgb(198, 218, 251)";
     private String colorRole= "rgb(252, 202, 202)";
     private String colorAccess= "rgb(255, 230, 186)";
     private String colorCurrentSessionContext= "rgb(243, 243, 243)";
     private CheckBox checkbox;
     //private FieldLabel fieldLabel;
     private Label labelText;
     private MetaDataEditorFilterType type;
     private Boolean checked = false;
     private String id = "";
     //private String filterId = "";
     private Boolean userRemoved = false;
	 private String caption = "";
    // private HorizontalLayoutContainer container = new HorizontalLayoutContainer();
     
     public MetaDataEditorFilterCheckBox(String id, String text, Boolean checked, MetaDataEditorFilterType type) {
    	 this.id = id;
    	 this.type = type;
    	 this.text = text;
    	 this.checked = checked;
     	 this.setBorders(false);
     	 this.setHeight(24);
    	    	 
    	 //HorizontalLayoutContainer layoutContainer = new HorizontalLayoutContainer();    	 
    	 //layoutContainer.setBorders(false);
    	 HTML panel = new HTML();
    	 
 		 setBorders(false);
 		 String style = "filterCheckBox";
 		 //String styleCss = null;
 		 String color = this.colorUseCase;
 		 
    	 if (this.type == MetaDataEditorFilterType.UseCase) {
    		 //styleCss = MetaDataEditorResources.INSTANCE.constellationCss().filterCheckBoxUseCase(); 
    		 style = "filterCheckBoxUseCase";
    		 color = this.colorUseCase;
    	 } else if (this.type == MetaDataEditorFilterType.Role) {
    		 //styleCss = MetaDataEditorResources.INSTANCE.constellationCss().filterCheckBoxRole(); 
    		 style = "filterCheckBoxRole";
    		 color = this.colorRole;
    	 } else if (this.type == MetaDataEditorFilterType.Access) {
    		 //styleCss = MetaDataEditorResources.INSTANCE.constellationCss().filterCheckBoxAccess(); 
    		 style = "filterCheckBoxAccess";
    		 color = this.colorAccess;
    	 } else if (this.type == MetaDataEditorFilterType.CurrentSessionContext) {
    		 //styleCss = MetaDataEditorResources.INSTANCE.constellationCss().filterCheckBoxAccess(); 
    		 style = "filterCheckBoxCurrentSessionContext";
    		 color = this.colorCurrentSessionContext;
    	 }    	 
    	 
    	 
    	 this.setStyleName(style);
         //this.setBodyStyle("borderRadius: 5px; backgroundColor:" + color);
         
    	 //if (styleCss != null) 
    	//	 addStyleName(styleCss); 
    	 
    	 this.checkbox = new CheckBox();
    	 this.checkbox.setBorders(false);
    	 this.checkbox.setWidth("auto");
    	 this.checkbox.setHeight("auto");
    	 this.checkbox.setVisible(true);
    	 this.checkbox.setStyleName("filterMetaDataCheckBoxClass");
    	 /*
    	 checkbox.addChangeHandler(new ChangeHandler() {					
				public void onChange(ChangeEvent event) {
					fireEvent(event);
				}
    	 });  
    	 */
    	 //this fire onChange only once -addChangeHandler is fired two times!!!
    	 this.checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
    		    @Override
    		    public void onValueChange(ValueChangeEvent<Boolean> event) {
    		    	fireEvent(event);
    		    }
    	 });
    	 
    	 this.labelText = new Label();
    	 this.labelText.setStyleName("filterMetaDataLabel"); 
    	 this.labelText.setText(this.caption);
    	 
    	 /*
    	 fieldLabel = new FieldLabel();
    	 fieldLabel.setBorders(false);
    	 fieldLabel.setText("caption");
    	 fieldLabel.setStyleName("filterMetaDataFieldLabel");
    	 fieldLabel.setLabelSeparator("");
    	 */
    	 
 		 ContentPanel checkboxPanel = new ContentPanel();
  		 checkboxPanel.setHeaderVisible(false);
  		 checkboxPanel.setBodyBorder(false);
  		 checkboxPanel.setBorders(false);
		 checkboxPanel.setBodyStyle("width: auto; borderColor: #c0c0c0; borderRadius: 5px; backgroundColor:" + color);
		 //checkboxPanel.setBodyStyle("width: auto; backgroundColor:" + color);
		 checkboxPanel.setStyleName("filterMetaDataCheckBoxPanel");
		 //bannerPanel.setWidth("auto");
		 //bannerPanel.setHeight("auto");
		 //bannerPanel.add(checkbox, new MarginData(2,2,2,2));
		 checkboxPanel.add(this.checkbox, new MarginData(4,2,2,2));
    	
		 /*
		 ContentPanel textPanel = new ContentPanel();
		 textPanel.setHeaderVisible(false);
		 textPanel.setBodyBorder(false);
		 textPanel.setBorders(false);
		 //textPanel.setBodyStyle("width: auto; borderColor: #c0c0c0; borderRadius: 5px; backgroundColor:" + color);
		 textPanel.setBodyStyle("width: auto; backgroundColor:" + color);
		 textPanel.setStyleName("filterMetaDataTextPanel");
		 textPanel.add(labelText);
		 */		 

    	 //this.add(bannerPanel, new HorizontalLayoutData(-1, -1, new Margins(2)));
    	 this.add(checkboxPanel, new HorizontalLayoutData(-1, -1, new Margins(2,2,2,0)));
    	 //this.add(textPanel, new HorizontalLayoutData(-1, -1, new Margins(2)));		 
		 
    	 if (this.type != MetaDataEditorFilterType.CurrentSessionContext) {
			 StringBuilder htmlString = new StringBuilder();
			 htmlString.append("<a id='anchor").append("' class='" + "closeHorizontalIcon" + "'></a>"); //"closeIcon"
			 panel.setHTML(htmlString.toString());
			 panel.setStyleName("filterMetaDataClose");
			 
			 this.add(panel, new HorizontalLayoutData(-1, -1, new Margins(6,2,2,0)));
			 
			 panel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					EventTarget target = event.getNativeEvent().getEventTarget();
					if (Element.is(target)) {
						Element targetElement = Element.as(target);
						String id = targetElement.getId();
						if (id.contains("anchor"))
							id = id.replace("anchor", "");
						if (targetElement.getClassName().equals("closeIcon") || targetElement.getClassName().equals("closeHorizontalIcon"))
						{						
								//DomEvent.fireNativeEvent(event.getNativeEvent(), MetaDataEditorFilterCheckBox.this);
								//DomEvent.fireNativeEvent(Document.get().createChangeEvent(), MetaDataEditorFilterCheckBox.this);
							    //if (MetaDataEditorFilterCheckBox.this.checked)
							    //	MetaDataEditorFilterCheckBox.this.checkbox.setValue(false, true);
								//fireChange();
								MetaDataEditorFilterCheckBox.this.userRemoved = true;
								MetaDataEditorFilterCheckBox.this.hide();
								
								/*
							    if (MetaDataEditorFilterCheckBox.this.getParent() != null & MetaDataEditorFilterCheckBox.this.getParent() instanceof ContentPanel) {
							    	ContentPanel contentPanel = (ContentPanel) MetaDataEditorFilterCheckBox.this.getParent();						    	
							    	if (contentPanel.getStyleName().contains("filterMetaDataContentPanel")) {
										contentPanel.removeFromParent();
							    	} else {
										MetaDataEditorFilterCheckBox.this.removeFromParent();						    		
							    	}						    	
							    } else {						
									MetaDataEditorFilterCheckBox.this.removeFromParent();						    		
							    }
							    */
							    
						} else {
							MetaDataEditorFilterCheckBox.this.checked = !MetaDataEditorFilterCheckBox.this.checked;
							updateCheckBox();
						}
					}
					
				}
			 });
    	 }
    	     	
    	 //this.add(layoutContainer);
    	 
    	 updateCheckBox();
    	 this.setVisible(true);
     }
     
      	
     public void setText(String text)
     {
    	 this.text = text;
    	 updateCheckBox();
     }
     
     public String getText()
     {
    	 return this.text;
     }
     
     public void setColorUseCase(String color)
     {
    	 if (color != null)
    		 this.colorUseCase = color;
    	 updateCheckBox();
     }
     
     public String getColorUseCase()
     {
    	 return this.colorUseCase;
     }     
     public void setColorRole(String color)
     {
    	 if (color != null)
    		 this.colorRole = color;
    	 updateCheckBox();
     }
     
     public String getColorRole()
     {
    	 return this.colorRole;
     }     
     public void setColorAccess(String color)
     {
    	 if (color != null)
    		 this.colorAccess = color;
    	 updateCheckBox();
     }
     
     public String getColorAccess()
     {
    	 return this.colorAccess;
     }     
     
     public void setColorCurrentSessionSontext(String color)
     {
    	 if (color != null)
    		 this.colorCurrentSessionContext = color;
    	 updateCheckBox();
     }
     
     public String getColorCurrentSessionSontext()
     {
    	 return this.colorCurrentSessionContext;
     }     
     
     
     public void setChecked(Boolean checked)
     {
    	 if (this.checkbox != null)
    		this.checkbox.setValue(checked);
    	 updateCheckBox();
     }

     public Boolean getChecked()
     {
    	 return this.checkbox.getValue();
     }
     
     public CheckBox getCheckbox() 
     {
    	 return this.checkbox;
     }
     
     @Override
	public String getId()
     {
    	 return this.id;
     }     
     
     public MetaDataEditorFilterType getFilterType() {
    	 return this.type;
     }
     
     public Boolean getUserRemoved() {
    	 return this.userRemoved;
     }
     
     private void updateCheckBox()
     {
     	if (this.checkbox != null)
     	{
     		if (this.type == MetaDataEditorFilterType.UseCase) 
     			this.caption = LocalizedText.INSTANCE.displayUseCase(); 
     		else if (this.type == MetaDataEditorFilterType.Role) 
     			this.caption = LocalizedText.INSTANCE.displayRole(); 
     		else if (this.type == MetaDataEditorFilterType.Access) 
     			this.caption = LocalizedText.INSTANCE.displayAccess();      		     		   
     		
     		if (this.type == MetaDataEditorFilterType.CurrentSessionContext) { 
     			this.caption = LocalizedText.INSTANCE.currentSessionContext();      		     		   
     		} else {
         		this.caption = this.caption + ": " + this.text;     		     		     			
     		}
     		this.checkbox.setBoxLabel(this.caption);
     		//labelText.setText(caption);
     		this.checkbox.setValue(this.checked);     		
     	}
     	
     	//TO DO - set background color
   	    
     	//this.doLayout();
     	this.forceLayout();
     }
     
     /*
     private void fireChange() {
			new Timer() {
				@Override
				public void run() {								
			    	 DomEvent.fireNativeEvent(Document.get().createChangeEvent(), checkbox);					
				}
			}.schedule(10);																
     }
     */
     

	@Override
	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
	    return addDomHandler(handler, ChangeEvent.getType());
	}
}
