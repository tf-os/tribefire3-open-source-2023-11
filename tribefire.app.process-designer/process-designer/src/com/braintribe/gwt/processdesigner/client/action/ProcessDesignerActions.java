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
package com.braintribe.gwt.processdesigner.client.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.OMSVGPolygonElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerConfiguration;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerMode;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.gwt.processdesigner.client.element.EdgeSvgElement;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.EdgeKind;
import com.braintribe.gwt.processdesigner.client.resources.LocalizedText;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;

import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class ProcessDesignerActions{

	private PersistenceGmSession session;
	private ProcessDesigner processDesigner;
	private ProcessDesignerRenderer processDesignerRenderer;
	private ProcessDesignerConfiguration processDesignerConfiguration;
	private OMSVGSVGElement svg;
	
	private ProcessDesignerActionMenuElement zoomIn;
	private ProcessDesignerActionMenuElement zoomOut;
	private AddNodeAction addStandardNode;
	private AddNodeAction addRestartNode;
	private ProcessDesignerActionMenuElement removeElements;
	private ProcessDesignerActionMenuElement setInitNode;
	private ChangeProcessDesignerModeAction selectAction;
	private ChangeProcessDesignerModeAction connectAction;
	private ChangeProcessDesignerModeAction groupAction;
	private ProcessDesignerActionMenuElement maskAction;
	private ChangeElementColorAction changeElementColorAction;
	private ProcessDesignerActionMenuElement printAction;
	
	private List<ModelPath> selection;
	
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	List<Pair<ActionTypeAndName, ModelAction>> actions;
	
//	private Action zoomIn;
//	private Action zoomOut;
//	private Action addNode;
//	private Action removeElements;
//	private Action testAction;
//	private Action selectAction;
//	private Action connectAction;
//	private Action groupAction;
	
	public void setSvg(OMSVGSVGElement svg) {
		this.svg = svg;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	public void setProcessDesigner(ProcessDesigner processDesigner) {
		this.processDesigner = processDesigner;
	}

	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration processDesignerConfiguration) {
		this.processDesignerConfiguration = processDesignerConfiguration;
	}

	public void setProcessDesignerRenderer(ProcessDesignerRenderer processDesignerRenderer) {
		this.processDesignerRenderer = processDesignerRenderer;
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public List<ProcessDesignerActionMenuElement> getActionElements(){
		return Arrays.asList(getAddStandardNode(), getAddRestartNode(), getRemoveElements(),getZoomIn(), getZoomOut(), getSelectAction(), 
				getConnectAction(), getGroupAction(), getChangeElementColorAction());
	}
	
	public List<Pair<ActionTypeAndName, ModelAction>> getActions(){
//		return java.util.Collections.emptyList();
		if(actions == null){
			actions = new ArrayList<>();
			
			ModelAction addNode = wrap(getAddStandardNode());
			actions.add(new Pair<>(new ActionTypeAndName(addNode.getName()), addNode));
			
			ModelAction addRestartNode = wrap(getAddRestartNode());
			actions.add(new Pair<>(new ActionTypeAndName(addRestartNode.getName()), addRestartNode));
			
			ModelAction remove = wrap(getRemoveElements());
			actions.add(new Pair<>(new ActionTypeAndName(remove.getName()), remove));
			
			ModelAction zoomIn = wrap(getZoomIn());
			actions.add(new Pair<>(new ActionTypeAndName(zoomIn.getName()), zoomIn));
			
			ModelAction zoomOut = wrap(getZoomOut());
			actions.add(new Pair<>(new ActionTypeAndName(zoomOut.getName()), zoomOut));
			
			ModelAction select = wrap(getSelectAction());
			actions.add(new Pair<>(new ActionTypeAndName(select.getName()), select));
			
			ModelAction connect = wrap(getConnectAction());
			actions.add(new Pair<>(new ActionTypeAndName(connect.getName()), connect));
		}
		return actions;
	}
	
	private ModelAction wrap(final ProcessDesignerActionMenuElement element){
		ModelAction action = new ModelAction() {
			
			@Override
			public void perform(TriggerInfo triggerInfo) {
				element.perform();
			}
			
			@Override
			protected void updateVisibility() {
				setHidden(false);
			}
		};
		action.setName(element.getName());
		action.setIcon(element.getIcon());
		action.setHoverIcon(element.getIcon());
		action.setHidden(false);
		action.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		return action;
	}
	
	public AddNodeAction getAddStandardNode() {
		if(addStandardNode == null){
			addStandardNode = new AddNodeAction("Add a Standard Node");
			addStandardNode.setName(LocalizedText.INSTANCE.standardNode());
			addStandardNode.setProcessDesigner(processDesigner);
			addStandardNode.setQuickAccessPanelProvider(quickAccessPanelProvider);
		}
		return addStandardNode;
	}
	
	public AddNodeAction getAddRestartNode() {
		if(addRestartNode == null){
			addRestartNode = new AddNodeAction("Add a Restart Node");
			addRestartNode.setNodeType(RestartNode.T);
			addRestartNode.setName(LocalizedText.INSTANCE.restartNode());
			addRestartNode.setProcessDesigner(processDesigner);
			addRestartNode.setQuickAccessPanelProvider(quickAccessPanelProvider);
		}
		return addRestartNode;
	}
	
	public ProcessDesignerActionMenuElement getRemoveElements() {
		if(removeElements == null){
			removeElements = new ProcessDesignerActionMenuElement() {
				@Override
				public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
					selection = gmSelectionSupport.getCurrentSelection();
					if (selection == null || selection.isEmpty()){
						getRemoveElements().setEnabled(false);
						return;
					}
					
					if(selection.size() == 1){
						ModelPath modelPath = selection.get(0);
						if(modelPath.last().getValue() instanceof Node){
							Node node = (Node) modelPath.last().getValue();
							getRemoveElements().setEnabled(node.getState() != null);
							return;
						}
					}						
					for(ModelPath modelPath : selection){
						if(modelPath.last() != null && modelPath.last().getValue() instanceof ProcessDefinition){
							getRemoveElements().setEnabled(false);
							return;
						}
						getRemoveElements().setEnabled(true);
					}						
				}
				
				@Override
				public void noticeManipulation(Manipulation manipulation) {
					//NOP
				}
				
				@Override
				public void perform() {
					
					ConfirmMessageBox confirm = new ConfirmMessageBox(LocalizedText.INSTANCE.deleteElements(), LocalizedText.INSTANCE.deleteConfirmation());
					confirm.addDialogHideHandler(event -> {
						if(event.getHideButton() == PredefinedButton.YES)
							processDesigner.removeElements(selection);
					});
					confirm.show();
				}
				
				@Override
				public void configure() {
					//NOP
				}
				
				@Override
				public void handleDipose() {
					processDesigner.removeSelectionListener(removeElements);
				}
				
				@Override
				public OMSVGGElement prepareIconElement() {
					return null;
				}
			};
			removeElements.setName(LocalizedText.INSTANCE.remove());
			//removeElements.setTooltip("Remove");
			removeElements.setIcon(ProcessDesignerResources.INSTANCE.remove());
			removeElements.setEnabled(false);
			processDesigner.addSelectionListener(removeElements);
		}
		return removeElements;
	}
	
	public ProcessDesignerActionMenuElement getSetInitNode() {
		if (setInitNode != null)
			return setInitNode;
		
		setInitNode = new ProcessDesignerActionMenuElement() {
			
			OMSVGGElement iconElement;
			OMSVGCircleElement iconCircle;
			boolean define = true;
			
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				selection = gmSelectionSupport.getCurrentSelection();
				setEnabled(false);
				if(selection != null && !selection.isEmpty()){
					for(ModelPath modelPath : selection){
						if(modelPath.last() != null && modelPath.last().getValue() instanceof Node){
							Node candidate = (Node)modelPath.last().getValue();
							setEnabled(true);
							define = processDesigner.getInitEdge(candidate) == null;
							if(define)
								setName(LocalizedText.INSTANCE.defineInitNode());
							else
								setName(LocalizedText.INSTANCE.undefineInitNode());
						}
					}
				}					
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public OMSVGGElement prepareIconElement() {
				if(iconElement == null){
					iconElement = new OMSVGGElement();
					iconCircle = new OMSVGCircleElement((float)(getX()+width/2),(float)(getY()+height/2), 16);
					iconCircle.setAttribute("style", "fill:silver;stroke:black;stroke-width:1");
					iconElement.appendChild(iconCircle);
				}
				return iconElement;
			}
			
			@Override
			public void perform() {
				if (selection == null || selection.isEmpty())
					return;
				
				List<ModelPath> elementsToRemove = new ArrayList<>();
				for(ModelPath modelPath : selection){
					if(modelPath.last() != null && modelPath.last().getValue() instanceof Node){
						StandardNode candidate = (StandardNode)modelPath.last().getValue();
						Edge initEdge = processDesigner.getInitEdge(candidate);
						if(define && initEdge == null){
							processDesigner.addEdge(null, candidate, EdgeKind.normal);
						}else if(!define && initEdge != null){
							ModelPath modelPathToDelete = new ModelPath();
							modelPathToDelete.add(new RootPathElement(Edge.T, initEdge));
							elementsToRemove.add(modelPathToDelete);
						}
					}
				}
				if(!elementsToRemove.isEmpty())
					processDesigner.removeElements(elementsToRemove);
			}
			
			@Override
			public void handleDipose() {
				processDesigner.removeSelectionListener(setInitNode);
			}
			
			@Override
			public void configure() {
				//NOP
			}
			
			@Override
			public void init() {
				super.init();
				iconCircle.setAttribute("cx", (getX()+width/2)+"");
				iconCircle.setAttribute("cy", (getY()+height/2)+"");
			}
		};
		setInitNode.setName(LocalizedText.INSTANCE.defineInitNode());
		//setInitNode.setTooltip("Define Init Node");
		setInitNode.setIcon(ProcessDesignerResources.INSTANCE.remove());
		setInitNode.setEnabled(false);
		setInitNode.setUseIcon(false);
		processDesigner.addSelectionListener(setInitNode);
		
		return setInitNode;
	}
	
	public ProcessDesignerActionMenuElement getZoomIn() {
		if (zoomIn != null)
			return zoomIn;
		
		zoomIn = new ProcessDesignerActionMenuElement() {
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				//NOP
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public void perform() {
				if(processDesignerConfiguration.getScaleLevel() + processDesignerConfiguration.getScaleChangeFactor() <= 2){
					processDesignerConfiguration.setScaleLevel(processDesignerConfiguration.getScaleLevel() + processDesignerConfiguration.getScaleChangeFactor());
					System.err.println(processDesignerConfiguration.getScaleLevel());
					processDesignerRenderer.adaptScale();
				}
			}
			
			@Override
			public void configure() {
				//NOP
			}
			
			@Override
			public void handleDipose() {
				//NOP
			}
			
			@Override
			public OMSVGGElement prepareIconElement() {
				return null;
			}
		};
		zoomIn.setName(LocalizedText.INSTANCE.zoomIn());
		//zoomIn.setTooltip("Zoom In");
		zoomIn.setIcon(ProcessDesignerResources.INSTANCE.zoomIn());
		
		return zoomIn;
	}
	
	public ProcessDesignerActionMenuElement getZoomOut() {
		if (zoomOut != null)
			return zoomOut;
		
		zoomOut = new ProcessDesignerActionMenuElement() {
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				//NOP
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public void perform() {
				if(processDesignerConfiguration.getScaleLevel() - processDesignerConfiguration.getScaleChangeFactor() > 0){
					processDesignerConfiguration.setScaleLevel(processDesignerConfiguration.getScaleLevel() - processDesignerConfiguration.getScaleChangeFactor());
					System.err.println(processDesignerConfiguration.getScaleLevel());
					processDesignerRenderer.adaptScale();
				}
			}
			
			@Override
			public void configure() {
				//NOP
			}
			
			@Override
			public void handleDipose() {
				//NOP
			}
			
			@Override
			public OMSVGGElement prepareIconElement() {
				return null;
			}
		};
		zoomOut.setName(LocalizedText.INSTANCE.zoomOut());
		//zoomOut.setTooltip("Zoom Out");
		zoomOut.setIcon(ProcessDesignerResources.INSTANCE.zoomOut());
		
		return zoomOut;
	}
	
	public ProcessDesignerActionMenuElement getSelectAction() {
		if (selectAction != null)
			return selectAction;
		
		selectAction = new ChangeProcessDesignerModeAction(){
			OMSVGGElement iconElement = null;
			OMSVGRectElement rectElement = null;
			double elementWidth = 32;
			
			@Override
			public OMSVGGElement prepareIconElement() {
				if(iconElement == null){
					iconElement = new OMSVGGElement();
					rectElement = new OMSVGRectElement(0, 0, 0, 0, 0, 0);
					rectElement.setAttribute("width", elementWidth+"");
					rectElement.setAttribute("height", elementWidth+"");
					rectElement.setAttribute("x",((getX()+(width/2)) - (elementWidth / 2))+"");
					rectElement.setAttribute("y",((getY()+(height/2)) - (elementWidth / 2))+"");
					rectElement.setAttribute("style", "fill:none;stroke:blue;stroke-width:1;stroke-dasharray:2,3;stroke-linecap:round");
					iconElement.appendChild(rectElement);
				}
				return iconElement;
			}
			
			@Override
			public void init() {
				super.init();
				rectElement.setAttribute("width", elementWidth+"");
				rectElement.setAttribute("height", elementWidth+"");
				rectElement.setAttribute("x",((getX()+(width/2)) - (elementWidth / 2))+"");
				rectElement.setAttribute("y",((getY()+(height/2)) - (elementWidth / 2))+"");
			}
		};
		selectAction.setConfiguration(processDesignerConfiguration);
		selectAction.setProcessDesigner(processDesigner);
		selectAction.setMode(ProcessDesignerMode.selecting);
		selectAction.setSession(session);
		selectAction.setName(LocalizedText.INSTANCE.select());
		//selectAction.setTooltip("Select");
		selectAction.setIcon(ProcessDesignerResources.INSTANCE.select());
		selectAction.setUseIcon(false);
		selectAction.setActive(true);
		
		return selectAction;
	}
	
	public ProcessDesignerActionMenuElement getConnectAction() {
		if (connectAction != null)
			return connectAction;
		
		connectAction = new ChangeProcessDesignerModeAction(){
			OMSVGGElement arrowElement = null;
			OMSVGLineElement line = new OMSVGLineElement(0, 0, 0, 0);
			OMSVGPolygonElement arrow = new OMSVGPolygonElement();
			double elementWidth = 32;
			
			@Override
			public OMSVGGElement prepareIconElement() {
				if(arrowElement == null){
					arrowElement = new OMSVGGElement();
					Complex start = new Complex(((getX()+(width/2)) - (elementWidth / 2)), ((getY()+(height/2)) + (elementWidth / 2)));
					Complex end = new Complex(((getX()+(width/2)) + (elementWidth / 2)), ((getY()+(height/2)) - (elementWidth / 2)));
					
					line.setAttribute("x1",start.x+"");
					line.setAttribute("y1",start.y+"");
					line.setAttribute("x2",end.x+"");
					line.setAttribute("y2",end.y+"");
					line.setAttribute("style", "fill:silver;stroke:silver;stroke-width:2;stroke-dasharray:5,5");
					
					arrow.setAttribute("style", "fill:silver;stroke:silver;stroke-width:2");
					
					List<Complex> arrowPaths = EdgeSvgElement.createArrowPath(end, end.minus(start), 5, 10);
					
					String points = "";
					for(int i = 0; i< arrowPaths.size();i++){
						points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
					}
		
					arrow.setAttribute("points", points);
					
					arrowElement.appendChild(line);
					arrowElement.appendChild(arrow);
				}
				return arrowElement;
			}
			
			@Override
			public void init() {
				super.init();
				
				Complex start = new Complex(((getX()+(width/2)) - (elementWidth / 2)), ((getY()+(height/2)) + (elementWidth / 2)));
				Complex end = new Complex(((getX()+(width/2)) + (elementWidth / 2)), ((getY()+(height/2)) - (elementWidth / 2)));
				
				line.setAttribute("x1",start.x+"");
				line.setAttribute("y1",start.y+"");
				line.setAttribute("x2",end.x+"");
				line.setAttribute("y2",end.y+"");
			
				List<Complex> arrowPaths = EdgeSvgElement.createArrowPath(end, end.minus(start), 5, 10);
				
				String points = "";
				for(int i = 0; i< arrowPaths.size();i++){
					points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
				}
	
				arrow.setAttribute("points", points);
			}
		};
		connectAction.setConfiguration(processDesignerConfiguration);
		connectAction.setProcessDesigner(processDesigner);
		connectAction.setMode(ProcessDesignerMode.connecting);
		connectAction.setSession(session);
		connectAction.setName(LocalizedText.INSTANCE.connect());
		//connectAction.setTooltip("Connect");
		connectAction.setIcon(ProcessDesignerResources.INSTANCE.connect());
		connectAction.setUseIcon(false);
		
		return connectAction;
	}
	
	public ProcessDesignerActionMenuElement getGroupAction() {
		if (groupAction != null)
			return groupAction;
		
		groupAction = new ChangeProcessDesignerModeAction(){
			OMSVGGElement iconElement = null;
			OMSVGRectElement rectElement = null;
			double elementWidth = 32;
			
			@Override
			public OMSVGGElement prepareIconElement() {
				if(iconElement == null){
					iconElement = new OMSVGGElement();
					rectElement = new OMSVGRectElement(0, 0, 0, 0, 5, 5);
					rectElement.setAttribute("width", elementWidth+"");
					rectElement.setAttribute("height", elementWidth+"");
					rectElement.setAttribute("x",((getX()+(width/2)) - (elementWidth / 2))+"");
					rectElement.setAttribute("y",((getY()+(height/2)) - (elementWidth / 2))+"");
					rectElement.setAttribute("style", "fill:#f3f3f3;stroke:silver;stroke-width:1;stroke-dasharray:2,3;stroke-linecap:round");
					iconElement.appendChild(rectElement);
				}
				return iconElement;
			}
			
			@Override
			public void init() {
				super.init();
				rectElement.setAttribute("width", elementWidth+"");
				rectElement.setAttribute("height", elementWidth+"");
				rectElement.setAttribute("x",((getX()+(width/2)) - (elementWidth / 2))+"");
				rectElement.setAttribute("y",((getY()+(height/2)) - (elementWidth / 2))+"");
			}
		};
		groupAction.setConfiguration(processDesignerConfiguration);
		groupAction.setMode(ProcessDesignerMode.grouping);
		groupAction.setSession(session);
		groupAction.setName(LocalizedText.INSTANCE.group());
		//groupAction.setTooltip("Group");
		groupAction.setUseIcon(false);
//		groupAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
		
		return groupAction;
	}
	
	public ChangeElementColorAction getChangeElementColorAction() {
		if(changeElementColorAction == null){
			changeElementColorAction = new ChangeElementColorAction();
			changeElementColorAction.setName(LocalizedText.INSTANCE.color());
			//changeElementColorAction.setTooltip("Color");
			changeElementColorAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
			changeElementColorAction.setEnabled(false);
			changeElementColorAction.setSvg(svg);
			processDesigner.addSelectionListener(changeElementColorAction);
		}
		return changeElementColorAction;
	}
	
	public void dispose(){
		processDesigner.removeSelectionListener(changeElementColorAction);
		processDesigner.removeSelectionListener(removeElements);
		processDesigner.removeSelectionListener(setInitNode);
	}
	
//	public List<Action> getActions(){
//		return Arrays.asList(getAddNode(),getRemoveElements(),getZoomIn(), getZoomOut(), getSelectAction(), getConnectAction(), getGroupAction());
//	}
	
//	public Action getZoomIn() {
//		if(zoomIn == null){
//			zoomIn = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					if(processDesignerConfiguration.scaleLevel + 0.25 < 2){
//						processDesignerConfiguration.setScaleLevel(processDesignerConfiguration.scaleLevel + 0.25);
//						System.err.println(processDesignerConfiguration.scaleLevel);
//						processDesignerRenderer.adaptScale();
//					}
//				}
//			};
//			zoomIn.setTooltip("");
//			zoomIn.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			zoomIn.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			zoomIn.setName("Zoom In");
//		}
//		return zoomIn;
//	}
//	
//	public Action getZoomOut() {
//		if(zoomOut == null){
//			zoomOut = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					if(processDesignerConfiguration.scaleLevel - 0.25 > 0){
//						processDesignerConfiguration.setScaleLevel(processDesignerConfiguration.scaleLevel - 0.25);
//						System.err.println(processDesignerConfiguration.scaleLevel);
//						processDesignerRenderer.adaptScale();
//					}
//				}
//			};
//			zoomOut.setTooltip("");
//			zoomOut.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			zoomOut.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			zoomOut.setName("Zoom Out");
//		}
//		return zoomOut;
//	}
//	
//	public Action getAddNode() {
//		if(addNode == null){
//			addNode = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					MessageBox.prompt("New State", "Please enter a node state", new Listener<MessageBoxEvent>() {
//						@Override
//						public void handleEvent(MessageBoxEvent be) {
//							if(be.getButtonClicked().getItemId().equals(Dialog.OK)){
//								processDesigner.addNode(be.getValue());
//							}
//						}
//					});					
//				}
//			};
//			addNode.setTooltip("");
//			addNode.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			addNode.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			addNode.setName("Add Node");
//		}
//		return addNode;
//	}
//	
//	public Action getRemoveElements() {
//		if(removeElements == null){
//			removeElements = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					MessageBox.confirm("Delete element(s)?", "Are you sure you want to the delete the selected element(s)?", new Listener<MessageBoxEvent>() {
//						@Override
//						public void handleEvent(MessageBoxEvent be) {
//							if(be.getButtonClicked().getItemId().equals(Dialog.YES))
//								processDesigner.removeElements(selection);
//						}
//					});					
//				}
//			};
//			removeElements.setTooltip("Remove selected Elements");
//			removeElements.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			removeElements.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			removeElements.setName("Remove");
//			removeElements.setEnabled(false);
//		}
//		return removeElements;
//	}
//	
//	public Action getTestAction() {
//		if(testAction == null){
//			testAction = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					MessageBox.prompt("New Definition", "Please enter a definition name", new Listener<MessageBoxEvent>() {
//						@Override
//						public void handleEvent(MessageBoxEvent be) {
//							if(be.getButtonClicked().getItemId().equals(Dialog.OK)){
//								ProcessDefinition processDefinition = processDesignerTest.createProcessDefinition(be.getValue());
//								ModelPath modelPath = new ModelPath();
//								modelPath.add(new RootPathElement(ProcessDefinition.T, processDefinition));
//								processDesigner.setContent(modelPath);
//							}
//						}
//					});					
//				}
//			};
//			testAction.setTooltip("");
//			testAction.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			testAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			testAction.setName("Generate Ncc Process");
//		}
//		return testAction;
//	}
//	
//	public Action getSelectAction() {
//		if(selectAction == null){
//			selectAction = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					processDesignerConfiguration.processDesignerMode = ProcessDesignerMode.selecting;
//				}
//			};
//			selectAction.setTooltip("");
//			selectAction.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			selectAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			selectAction.setName("Select");
//		}
//		return selectAction;
//	}
//	
//	public Action getConnectAction() {
//		if(connectAction == null){
//			connectAction = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					processDesignerConfiguration.processDesignerMode = ProcessDesignerMode.connecting;
//				}
//			};
//			connectAction.setTooltip("");
//			connectAction.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			connectAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			connectAction.setName("Connect");
//		}
//		return connectAction;
//	}
//	
//	public Action getGroupAction() {
//		if(groupAction == null){
//			groupAction = new Action() {
//				public void perform(TriggerInfo triggerInfo) {
//					processDesignerConfiguration.processDesignerMode = ProcessDesignerMode.grouping;
//				}
//			};
//			groupAction.setTooltip("");
//			groupAction.setHoverIcon(ProcessDesignerResources.INSTANCE.add());
//			groupAction.setIcon(ProcessDesignerResources.INSTANCE.addBig());
//			groupAction.setName("Group");
//		}
//		return groupAction;
//	}
	
//	@Override
//	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
//		selection = gmSelectionSupport.getCurrentSelection();
//		if(selection != null && !selection.isEmpty()){
//			for(ModelPath modelPath : selection){
//				if(modelPath.last() != null && modelPath.last().getValue() instanceof ProcessDefinition){
//					getRemoveElements().setEnabled(false);
//					return;
//				}
//				getRemoveElements().setEnabled(true);
//			}
//		}else
//			getRemoveElements().setEnabled(false);		
//	}
	
	public ProcessDesignerActionMenuElement getMaskAction() {
		if (maskAction != null)
			return maskAction;
		
		maskAction = new ProcessDesignerActionMenuElement() {
			Timer timer;
			
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				//NOP
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public OMSVGGElement prepareIconElement() {
				return null;
			}
			
			@Override
			public void perform() {
				if(timer == null){
					timer = new Timer() {
						@Override
						public void run() {
							processDesignerRenderer.unmask();
						}
					};
				}else{
					timer.cancel();
				}
				processDesignerRenderer.mask("...test...");
				timer.schedule(2000);					
			}
			
			@Override
			public void handleDipose() {
				//NOP
			}
			
			@Override
			public void configure() {
				//NOP
			}
		};
		maskAction.setIcon(ProcessDesignerResources.INSTANCE.zoomIn());
		maskAction.setName(LocalizedText.INSTANCE.mask());
		//maskAction.setTooltip("Mask");
		
		return maskAction;
	}
	
	public ProcessDesignerActionMenuElement getPrint() {
		if (printAction != null)
			return printAction;
		
		printAction = new ProcessDesignerActionMenuElement() {
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				//NOP
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public void perform() {
				try{
					StringBuilder sb = new StringBuilder();
					sb.append("<?xml version='1.0' encoding='utf-8'?>");
					sb.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>");
					sb.append(svg.getElement().getString());
					System.err.println(sb.toString());
				}catch(Exception ex){
					//NOP
				}
			}
			
			@Override
			public void configure() {
				//NOP
			}
			
			@Override
			public void handleDipose() {
				//NOP
			}
			
			@Override
			public OMSVGGElement prepareIconElement() {
				return null;
			}
		};
		printAction.setName(LocalizedText.INSTANCE.print());
		//printAction.setTooltip("Print");
		printAction.setIcon(ProcessDesignerResources.INSTANCE.zoomIn());
		
		return printAction;
	}

}
