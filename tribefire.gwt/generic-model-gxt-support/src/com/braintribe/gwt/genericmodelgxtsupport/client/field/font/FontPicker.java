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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.font;

import java.util.List;
import java.util.Map;

import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.color.ColorPickerWindow;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedListViewDefaultResources;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.style.Color;
import com.braintribe.model.style.Font;
import com.braintribe.model.style.FontWeight;
import com.braintribe.model.style.ValueWithUnit;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Dialog to define text Font
 *
 */
public class FontPicker extends ClosableWindow implements EntityFieldDialog<Font> {

	private TextButton cancelButton;
	private TextButton confirmButton;
	private ToolBar toolBar;
	private HTML colorPreview;
	private HTML textPreview;
	private Boolean confirmed = false;
	private Font font;
	private BorderLayoutContainer panel = null;
	//private Boolean colorPickerShowed = false;
	private Boolean hasChange = false;
	private Color saveColor = null;
	private String saveOriginalColor = "";
	private String saveFontFamily = "";
	private String saveFontStyle = "";
	private String saveFontWeight = "";
	private String saveFontSize = "";
	private Color newColor = null;
	private String newColorString = null;
	private String newFontFamily = null;
	private String newFontStyle = null;
	private String newFontWeight = null;
	private String newFontSize = null;	
	private int textHeight = 100;
	private int dialogWidth = 670;
	private int dialogHeight = 390;
	private Map<String, ListView<FontSelection, String>> mapListView = new FastMap<>();
	private PersistenceGmSession gmSession;

	
	/*
	interface ComboBoxTemplates extends XTemplates {
		    @XTemplate("<div style=\"font-family:{name} !important\">{name}</div>")
		    SafeHtml comboFont(String name);
		 
		    @XTemplate("<div style=\"font-size:{size} !important\">{text}</div>")
		    SafeHtml comboSize(String size, String text);

		    @XTemplate("<div style=\"font-weight:{weight} !important\">{text}</div>")
		    SafeHtml comboWeight(String weight, String text);
		    
		    @XTemplate("<div style=\"font-style:{style} !important\">{text}</div>")
		    SafeHtml comboStyle(String style, String text);		    
	}	
	*/

	interface FontProperties extends PropertyAccess<FontSelection> {
	    ModelKeyProvider<FontSelection> id();	 
	    LabelProvider<FontSelection> name();
	}
	
	interface FontPropertiesList extends PropertyAccess<FontSelection> {
		    ModelKeyProvider<FontSelection> id();		 
		    ValueProvider<FontSelection, String> name();
	}	
	public FontPicker() {
		setModal(true);
		setHeaderVisible(true);
		setHeading(LocalizedText.INSTANCE.fontDialog());
		addStyleName("fontDialog");
		setSize(dialogWidth + "px", dialogHeight + "px");
		setClosable(false);
		setBodyBorder(false);
		setBorders(false);
		setResizable(false);
		hasChange = false;				
		confirmed = false;
		this.add(preparePicker());
	}
	
	@Override
	public void setEntityValue(Font font) {
		this.font = font;
		this.hasChange = false;
		this.confirmed = false;
		
		newColor = null;
		if (saveColor == null)
			saveColor = Color.T.create();
		saveColor.setRed(0);
		saveColor.setGreen(0);
		saveColor.setBlue(0);

		saveFontFamily = "";
		saveFontStyle = "";
		saveFontWeight = "";
		saveFontSize = "";
		saveOriginalColor = "";
		
		if (font != null) {
			saveFontFamily = font.getFamily();
			if (font.getStyle() != null)
				saveFontStyle = font.getStyle().toString();
			if (font.getWeight() != null)
				saveFontWeight = font.getWeight().toString();
			if (font.getSize() != null)
				saveFontSize = font.getSize().getValue() + font.getSize().getUnit();
			if (font.getColor() != null) {
				saveOriginalColor = getRgbColor(font.getColor());
				saveColor.setRed(font.getColor().getRed());
				saveColor.setGreen(font.getColor().getGreen());
				saveColor.setBlue(font.getColor().getBlue());
			}
		}
		
		newFontFamily = saveFontFamily;
		newFontSize = saveFontSize;
		newFontStyle = saveFontStyle;
		newFontWeight = saveFontWeight;
		newColorString = saveOriginalColor;
		
		updateSelection();
		updatePreview();
	}

	@Override
	public void performManipulations() {
		if (this.confirmed)
			getManipulations();		
	}

	private Manipulation getManipulations() {
		if (this.font == null)
			return null;
		
		if (newFontFamily != null && !newFontFamily.isEmpty())
			font.setFamily(newFontFamily);
		if (newFontStyle != null && !newFontStyle.isEmpty())
			font.setStyle(getFontStyle(newFontStyle));
		if (newFontSize != null && !newFontSize.isEmpty()) {
			if (font.getSize() == null)
				font.setSize(gmSession.create(ValueWithUnit.T));
			font.setSize(getFontSize(font.getSize(), newFontSize));
		}
		if (newFontWeight != null && !newFontWeight.isEmpty())
			font.setWeight(getFontWeight(newFontWeight));	
		if (newColor != null) {
			if (font.getColor() == null) {
				Color color = gmSession.create(Color.T);
				font.setColor(color);
			}
			font.getColor().setRed(newColor.getRed());
			font.getColor().setGreen(newColor.getGreen());
			font.getColor().setBlue(newColor.getBlue());					
		}
		
		return null;
	}
	
	@Override
	public boolean hasChanges() {
		return (hasChange);
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public boolean isFontSet() {
		if (font == null) {
			Font newFont = Font.T.create();
			setEntityValue(newFont);
		}
		
		show();		
		
		return confirmed;
	}
	
	@Override
	public void hide() {
		confirmed = false;
		super.hide();
	}
	
	private Widget preparePicker() {
		this.panel = new BorderLayoutContainer();
		
		prepareToolBar();
		Widget widget = prepareFontContainer();
		
		this.panel.setSouthWidget(toolBar, new BorderLayoutData(70));
		//this.panel.setEastWidget(prepareColorPicker(), new BorderLayoutData(405));
		this.panel.setCenterWidget(widget);
		
		return this.panel;
	}

	private ToolBar prepareToolBar() {
		confirmButton = prepareConfirmButton();		
		cancelButton = prepareCancelButton();		
		
		toolBar = new ToolBar();		
		toolBar.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		toolBar.setBorders(false);	
		toolBar.add(new FillToolItem());
		
		toolBar.add(confirmButton);
		toolBar.add(cancelButton);
		//toolBar.add(colorButton);
		
		return toolBar;
	}

	private TextButton prepareCancelButton() {
		TextButton button;
		button = new FixedTextButton(LocalizedText.INSTANCE.cancel());
		button.setToolTip(LocalizedText.INSTANCE.cancel());
		button.setIconAlign(IconAlign.TOP);
		button.setScale(ButtonScale.LARGE);
		button.setIcon(GMGxtSupportResources.INSTANCE.delete());
		button.addSelectHandler(event -> {
			confirmed = false;
			FontPicker.super.hide();
		});
		button.setVisible(true);
		return button;
	}
	private TextButton prepareConfirmButton() {
		TextButton button;
		button = new FixedTextButton(LocalizedText.INSTANCE.ok());
		button.setToolTip(LocalizedText.INSTANCE.ok());
		button.setIconAlign(IconAlign.TOP);
		button.setScale(ButtonScale.LARGE);
		button.setIcon(GMGxtSupportResources.INSTANCE.save());
		button.addSelectHandler(event -> {
			confirmed = true;
			FontPicker.super.hide();
		});
		button.setVisible(true);
		return button;
	}
	private Widget prepareFontContainer() {
		mapListView.clear();
		VerticalLayoutContainer container = new VerticalLayoutContainer();
		container.setBorders(false);
		container.setStyleName("fontPickerContainer");		
		HorizontalLayoutContainer horizontalContainer;
						
		horizontalContainer = new HorizontalLayoutContainer();
		horizontalContainer.setBorders(false);
		horizontalContainer.setHeight(300);
		horizontalContainer.setStyleName("fontPickerHorizontalContainer");		
		horizontalContainer.add(prepareFontListView(FontKind.family, 150), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		horizontalContainer.add(prepareFontListView(FontKind.size, 150), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		
		VerticalLayoutContainer verticalContainer = new VerticalLayoutContainer();
		verticalContainer.setBorders(false);
		verticalContainer.setStyleName("fontPickerVerticalContainer");	
		verticalContainer.add(prepareFontListView(FontKind.style, 60) , new VerticalLayoutData(-1, 60, new Margins(0,5,0,0)));
		verticalContainer.add(prepareFontListView(FontKind.weight, 45) , new VerticalLayoutData(-1, 45, new Margins(5,5,0,0)));
		verticalContainer.add(prepareColorPreview(213, 38) , new VerticalLayoutData(-1, 38, new Margins(5,5,0,0)));
		
		horizontalContainer.add(verticalContainer, new HorizontalLayoutData(220, -1, new Margins(5,5,0,0)));
		//horizontalContainer.add(prepareFontListView(FontKind.style, 50), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		//horizontalContainer.add(prepareFontListView(FontKind.weight, 100), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		//horizontalContainer.add(prepareFontSizeCombo(), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		container.add(horizontalContainer, new VerticalLayoutData(-1, 170, new Margins(5,5,0,0)));
		
		/*
		horizontalContainer = new HorizontalLayoutContainer();
		horizontalContainer.setBorders(false);
		horizontalContainer.setHeight(300);
		horizontalContainer.setStyleName("fontPickerHorizontalContainer");		
		//horizontalContainer.add(prepareFontStyleCombo(), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		//horizontalContainer.add(prepareFontWeightCombo(), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		horizontalContainer.add(prepareFontListView(FontKind.style), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		horizontalContainer.add(prepareFontListView(FontKind.weight), new HorizontalLayoutData(220, -1, new Margins(5,5,0,0))); 
		container.add(horizontalContainer, new VerticalLayoutData(-1, 170, new Margins(5,5,0,0)));
		*/
		
		horizontalContainer = new HorizontalLayoutContainer();
		horizontalContainer.setBorders(false);
		horizontalContainer.setHeight(300);
		horizontalContainer.setStyleName("fontPickerHorizontalContainer");		
		horizontalContainer.add(prepareTextPreview(), new HorizontalLayoutData(-1, -1, new Margins(5,5,0,0))); 
		//horizontalContainer.add(prepareColorPreview(60,50), new HorizontalLayoutData(-1, -1, new Margins(5,5,0,0)));		
		container.add(horizontalContainer, new VerticalLayoutData(-1, 120, new Margins(5,5,0,0)));
						
		return container;
	}
	
	private Widget prepareTextPreview() {
		this.textPreview = new HTML("");
		this.textPreview.setStyleName("fontTextPreview");
		this.textPreview.setWidth("655px");
		this.textPreview.setHeight(textHeight +"px");
		this.textPreview.getElement().getStyle().setBorderColor("silver");
		this.textPreview.getElement().getStyle().setBorderStyle(BorderStyle.NONE);
		this.textPreview.getElement().getStyle().setBorderWidth(1, Unit.PX);
		this.textPreview.getElement().getStyle().setMarginBottom(12, Unit.PX);		
		return this.textPreview;
	}
	
	private Widget prepareColorPreview(int width, int height) {
		VerticalLayoutContainer container = new VerticalLayoutContainer();
		container.setBorders(false);
		container.setStyleName("fontPickerColorContainer");		
		
		this.colorPreview = new HTML("");
		this.colorPreview.setStyleName("fontColorPreview");
		this.colorPreview.setWidth(width+"px");
		this.colorPreview.setHeight(height+"px");
		this.colorPreview.getElement().getStyle().setBorderColor("silver");
		this.colorPreview.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		this.colorPreview.getElement().getStyle().setBorderWidth(1, Unit.PX);
		this.colorPreview.getElement().getStyle().setMarginBottom(12, Unit.PX);
		this.colorPreview.addClickHandler(event -> {
			if (newColor == null) {
				newColor = Color.T.create();
				if (saveColor == null) {											
					newColor.setRed(0);
					newColor.setGreen(0);
					newColor.setBlue(0);
				} else {
					newColor.setRed(saveColor.getRed());
					newColor.setGreen(saveColor.getGreen());
					newColor.setBlue(saveColor.getBlue());
				}
			}
			prepareColorPicker(newColor);
		});
		
		container.add(this.colorPreview);
		container.setToolTip(LocalizedText.INSTANCE.color());
		container.setWidth(width);
	    return container;
	}
		
	private Widget prepareFontListView(final FontKind fontKind, int height) {
		FontPropertiesList properties = GWT.create(FontPropertiesList.class);
		ListStore<FontSelection> store1 = new ListStore<FontSelection>(properties.id());
		if (fontKind.equals(FontKind.family))
			store1.addAll(FontData.getFontFamily());
		if (fontKind.equals(FontKind.style))
			store1.addAll(FontData.getFontStyle());
		if (fontKind.equals(FontKind.size))
			store1.addAll(FontData.getFontSize());
		if (fontKind.equals(FontKind.weight))
			store1.addAll(FontData.getFontWeight());

	    ListViewCustomAppearance<FontSelection> appearance = new ListViewCustomAppearance<FontSelection>("." + "fontDialogListItem",
	              "fontDialogListItemOver", "fontDialogListItemSelected" ) {
	            @Override
	            public void renderEnd(SafeHtmlBuilder builder) {
	              //String markup = new StringBuilder("<div class=\"").append(CommonStyles.get().clear()).append("\"></div>").toString();
	              //builder.appendHtmlConstant(markup);
	            }
	            
	            /*
	            background: none repeat scroll 0px 50% rgb(255, 230, 186);
	            cursor: pointer;
	            */
	     
	            @Override
	            public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
				builder.appendHtmlConstant("<div class='fontDialogListItem" + " " + ExtendedListViewDefaultResources.GME_LIST_VIEW_ITEM
						+ "' style='font-" + fontKind.toString() + ":" + content.asString() + " !important'>");
	            	builder.append(content);
	            	builder.appendHtmlConstant("</div>");
	            }
	            
	            @Override
	    		public void onSelect(XElement item, boolean select) {
	    			super.onSelect(item, select);
	    			item.setClassName(ExtendedListViewDefaultResources.GME_LIST_VIEW_SEL, select);
	    		}
	    }; 	    
	    
	    ListView<FontSelection, String> listView1 = new ListView<FontSelection, String>(store1, properties.name(), appearance);
	    //ListView<FontSelection, String> listView1 = new ListView<FontSelection, String>(store1, properties.name());
	    listView1.getSelectionModel().addSelectionChangedHandler(event -> {
			FontSelection selectedModel = null;
			List<FontSelection> selectedModels = event.getSelection();
			if (selectedModels != null && !selectedModels.isEmpty())
				selectedModel = selectedModels.get(0);
			if (selectedModel == null)
				return;
			
			if (fontKind.equals(FontKind.family))
				newFontFamily = selectedModel.getName();
			if (fontKind.equals(FontKind.style))
				newFontStyle = selectedModel.getName();
			if (fontKind.equals(FontKind.size))
				newFontSize = selectedModel.getName();
			if (fontKind.equals(FontKind.weight))
				newFontWeight = selectedModel.getName();
			hasChange = true;
			updatePreview();
		}); 	    
	    listView1.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	    listView1.setBorders(true);	
		listView1.setVisible(true);
		listView1.setHeight(height);
		listView1.setStyleName("fontPickerListView");
		listView1.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
		listView1.getElement().getStyle().setOverflowY(Overflow.AUTO); 
		if (fontKind.equals(FontKind.family))
			listView1.setToolTip(LocalizedText.INSTANCE.fontName());
		if (fontKind.equals(FontKind.size))
			listView1.setToolTip(LocalizedText.INSTANCE.fontSize());
		if (fontKind.equals(FontKind.style))
			listView1.setToolTip(LocalizedText.INSTANCE.fontStyle());
		if (fontKind.equals(FontKind.weight))
			listView1.setToolTip(LocalizedText.INSTANCE.fontWeight());
		mapListView.put(fontKind.toString(), listView1);
		return listView1;
	}
	
	/*
	private Widget prepareFontFamilyCombo() {
		FontProperties properties = GWT.create(FontProperties.class);
		ListStore<FontSelection> store1 = new ListStore<FontSelection>(properties.id());
	    store1.addAll(FontData.getFontFamily());
	    //ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name());		
	    ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name(),
	            new AbstractSafeHtmlRenderer<FontSelection>() {
	              public SafeHtml render(FontSelection item) {
	                final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
	                return comboBoxTemplates.comboFont(item.getName());
	              }
	    });	    	    
	    combo.setEmptyText("Select a Font...");
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.setVisible(true);
	    combo.setName("Font name");
	    combo.setId("fontName");
		addHandlersForEventObservation(combo, properties.name());
		return combo;
	}
	private Widget prepareFontSizeCombo() {
		FontProperties properties = GWT.create(FontProperties.class);
		ListStore<FontSelection> store1 = new ListStore<FontSelection>(properties.id());
	    store1.addAll(FontData.getFontSize());
	    //ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name());	    
	    ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name(),
	            new AbstractSafeHtmlRenderer<FontSelection>() {
	              public SafeHtml render(FontSelection item) {
	                final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
	                return comboBoxTemplates.comboSize(item.getName(), item.getName());
	              }
	    });	   	    	    
	    combo.setEmptyText("Select a Font Size...");
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.setVisible(true);
	    combo.setName("Size");
	    combo.setId("fontSize");
		addHandlersForEventObservation(combo, properties.name());
		return combo;
	}
	private Widget prepareFontStyleCombo() {
		FontProperties properties = GWT.create(FontProperties.class);
		ListStore<FontSelection> store1 = new ListStore<FontSelection>(properties.id());
	    store1.addAll(FontData.getFontStyle());
	    //ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name());	
	    ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name(),
	            new AbstractSafeHtmlRenderer<FontSelection>() {
	              public SafeHtml render(FontSelection item) {
	                final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
	                return comboBoxTemplates.comboStyle(item.getName(), item.getName());
	              }
	    });	   	    	    	    
	    combo.setEmptyText("Select a Font Style...");
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.setVisible(true);
	    combo.setName("Style");
	    combo.setId("fontStyle");
		addHandlersForEventObservation(combo, properties.name());
		return combo;
	}
	private Widget prepareFontWeightCombo() {
		FontProperties properties = GWT.create(FontProperties.class);
		ListStore<FontSelection> store1 = new ListStore<FontSelection>(properties.id());
	    store1.addAll(FontData.getFontWeight());
	    //ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name());				
	    ComboBox<FontSelection> combo = new ComboBox<FontSelection>(store1, properties.name(),
	            new AbstractSafeHtmlRenderer<FontSelection>() {
	              public SafeHtml render(FontSelection item) {
	                final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
	                return comboBoxTemplates.comboWeight(item.getName(), item.getName());
	              }
	    });	   	    	    
	    combo.setEmptyText("Select a Font Weight...");
	    combo.setTypeAhead(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.setVisible(true);
	    combo.setName("Weight");
	    combo.setId("fontWeight");	    
		addHandlersForEventObservation(combo, properties.name());
		return combo;
	}
	
	private <T> void addHandlersForEventObservation(final ComboBox<T> combo, final LabelProvider<T> labelProvider) {
		    combo.addValueChangeHandler(new ValueChangeHandler<T>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<T> event) {
		      }

		    });
		 
		    combo.addSelectionHandler(new SelectionHandler<T>() {
		      @Override
		      public void onSelection(SelectionEvent<T> event) {
		    	  //TODO - need update FONT depending on selected values in COMBO!!!
		    	  String selected = (event.getSelectedItem() == null ? "" : labelProvider.getLabel(event.getSelectedItem()));
		    	  if (combo.getId().equals("fontName"))
		    		  font.setFamily(selected);
			      if (combo.getId().equals("fontSize"))
			    	  font.setSize(getFontSize(font.getSize(), selected));
				  if (combo.getId().equals("fontStyle")) 
				   	  font.setStyle(getFontStyle(selected));
		    	  if (combo.getId().equals("fontWeight")) 
		    		  font.setWeight(getFontWeight(selected));		    	  
		    	  updatePicker();  
		      }

		    });
		  }	
	*/
	
	/* choose font color from color dialog */
	private void prepareColorPicker(Color color) {
		ColorPickerWindow colorPickerWindow = new ColorPickerWindow();
		colorPickerWindow.setEntityValue(color);
		colorPickerWindow.addHideHandler((HideHandler) event -> {
			Color colorFromPicker = null;
			if (event.getSource() instanceof ColorPickerWindow)
				colorFromPicker = ((ColorPickerWindow) event.getSource()).getColor();
			if (colorFromPicker != null) {
				newColor = colorFromPicker;
				newColorString = getRgbColor(colorFromPicker);
				if (!saveOriginalColor.equals(newColorString)) 
					hasChange = true;
				updatePreview();
			}
		});
		colorPickerWindow.show();
	}
	
	private String getRgbColor(com.braintribe.model.style.Color color) {
		if (color != null && color.getRed() != null && color.getGreen() != null && color.getBlue() != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("(").append(color.getRed().toString()).append(",").append(color.getGreen().toString()).append(",").append(color.getBlue().toString()).append(")");
			return builder.toString();
		}
		return null;
	}
	
	/*private void setPreviewColor(Color color) {	
		/*
		if (color == null) {
			this.colorPreview.getElement().getStyle().setBackgroundColor("black");
		} else {
			this.colorPreview.getElement().getStyle().setBackgroundColor("rgb"+getRgbColor(color));
		}
		*
		setPreviewColor(getRgbColor(color));
	}*/

	private void setPreviewColor(String colorString) {
		if (colorString == null)
			return;
		if (this.colorPreview == null)
			return;		
		
		if (colorString.isEmpty())
			this.colorPreview.getElement().getStyle().setBackgroundColor("black");
		else
			this.colorPreview.getElement().getStyle().setBackgroundColor("rgb"+colorString);
	}
			
	private void setPreviewText() {
		if (this.textPreview == null)
			return;		
		
		StringBuilder htmlString = new StringBuilder();
		StringBuilder style = new StringBuilder();
		style.append("style='margin: 5px 5px 0px 0px;text-align: center;line-height:").append(this.textHeight).append("px;");
		if (newFontFamily != null)
			style.append("font-family:").append(newFontFamily).append(" !important;");
		if (newFontStyle != null)
			style.append("font-style:").append(newFontStyle).append(";");
		if (newFontSize != null) 
			style.append("font-size:").append(newFontSize).append(";");
		if (newFontWeight != null)
			style.append("font-weight: ").append(newFontWeight).append(";");
		if (newColor == null) {
			if (saveColor != null)
				style.append("color: rgb" + getRgbColor(saveColor) +";'");				
		} else {
			style.append("color: rgb" + getRgbColor(newColor) +";'");
		}
		htmlString.append("<div  ").append(style).append(" />");
		htmlString.append("Text Example");
		htmlString.append("</div>");
		
		this.textPreview.setHTML(htmlString.toString());
	}
	
	private void updateSelection() {
		for (FontKind fontKind : FontKind.values()) {
			ListView<FontSelection, String> listView = mapListView.get(fontKind.toString());
			if (listView == null)
				continue;
			
			String findString;
			if (fontKind.equals(FontKind.family))
				findString = newFontFamily;
			else if (fontKind.equals(FontKind.style))
				findString = newFontStyle;
			else if (fontKind.equals(FontKind.size))
				findString = newFontSize;
			else if (fontKind.equals(FontKind.weight))
				findString = newFontWeight;
			else
				findString = null;
			
			if (findString == null || findString.isEmpty())
				listView.getSelectionModel().deselectAll();
			else {
				listView.getStore().getAll().stream().filter(i -> i.getName().toLowerCase().equals(findString.toLowerCase()))
						.forEach(item -> listView.getSelectionModel().select(item, false));
			}
		}
	}
	
	private void updatePreview() {
		setPreviewColor(newColorString);
		setPreviewText();
	}
	
	public com.braintribe.model.style.FontStyle getFontStyle(String styleString) {
		com.braintribe.model.style.FontStyle fontStyle = com.braintribe.model.style.FontStyle.normal;		
		if ((styleString != null) && (!styleString.isEmpty()))	{
			for (com.braintribe.model.style.FontStyle style : com.braintribe.model.style.FontStyle.values()) 
				if (styleString.toLowerCase().equals(style.toString().toLowerCase())) {
					fontStyle = style;
					break;
				}
		}		
		return fontStyle;
	}
	
	public com.braintribe.model.style.FontWeight getFontWeight(String weightString) {
		com.braintribe.model.style.FontWeight fontWeight = FontWeight.normal;		
		if ((weightString != null) && (!weightString.isEmpty())) {
			for (com.braintribe.model.style.FontWeight weight : com.braintribe.model.style.FontWeight.values()) 
				if (weightString.toLowerCase().equals(weight.toString().toLowerCase())) {
					fontWeight = weight;
					break;
				}
		}		
		return fontWeight;
	}
	
	public ValueWithUnit getFontSize(ValueWithUnit fontSize, String sizeString) {
		if (fontSize == null)
			fontSize = ValueWithUnit.T.create();
		fontSize.setValue(sizeString);
		fontSize.setUnit("");
		return fontSize;
	}

	@Override
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		// NOP		
	}	
	
}
