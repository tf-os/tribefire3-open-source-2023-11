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
package com.braintribe.gwt.processdesigner.client.event;

import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerMode;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

public abstract class AbstractEventHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler, ClickHandler, ManipulationListener,
KeyDownHandler, KeyUpHandler, KeyPressHandler{
	
	private boolean logTime = false;
	private boolean idle = false;
	protected ProcessDesignerRenderer renderer;
	
	public void setProcessDesignerRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}	
	
	public void setIdle(boolean idle) {
		this.idle = idle;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		long start = System.currentTimeMillis();
		try{
			delegateNoticeManipulation(manipulation);
		}catch(Exception ex){
			showErrorAndReset("noticeManipulation", ex);
		}
		if(logTime)
			System.err.println("handle noticeManipulation " + (System.currentTimeMillis()-start));
	}
	
	public abstract void delegateNoticeManipulation(Manipulation manipulation);
	
	@Override
	public void onClick(ClickEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnClick(event);
			}catch(Exception ex){
				showErrorAndReset("onClick", ex);
			}
			if(logTime)
				System.err.println("handle onClick " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnClick(ClickEvent event);
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnMouseDown(event);
			}catch(Exception ex){
				showErrorAndReset("onMouseDown", ex);
			}
			if(logTime)
				System.err.println("handle onMouseDown " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnMouseDown(MouseDownEvent event);
	
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnMouseMove(event);
			}catch(Exception ex){
				showErrorAndReset("onMouseMove", ex);
			}
			if(logTime)
				System.err.println("handle onMouseMove " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnMouseMove(MouseMoveEvent event);
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnMouseOut(event);
			}catch(Exception ex){
				showErrorAndReset("onMouseOut", ex);
			}
			if(logTime)
				System.err.println("handle onMouseOut " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnMouseOut(MouseOutEvent event);
	
	@Override
	public void onMouseOver(MouseOverEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnMouseOver(event);
			}catch(Exception ex){
				showErrorAndReset("onMouseOver", ex);
			}
			if(logTime)
				System.err.println("handle onMouseOver " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnMouseOver(MouseOverEvent event);
	
	@Override
	public void onMouseUp(MouseUpEvent event) {
		if(!idle) {
			long start = System.currentTimeMillis();
			try{
				delegateOnMouseUp(event);
			}catch(Exception ex){
				showErrorAndReset("onMouseUp", ex);
			}
			if(logTime)
				System.err.println("handle onMouseUp " + (System.currentTimeMillis()-start));
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	public abstract void delegateOnMouseUp(MouseUpEvent event);
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		long start = System.currentTimeMillis();
		try{
			delegateOnKeyDown(event);
		}catch(Exception ex){
			showErrorAndReset("onKeyDown", ex);
		}
		if(logTime)
			System.err.println("handle onKeyDown " + (System.currentTimeMillis()-start));
		event.preventDefault();
		event.stopPropagation();
	}
	
	public abstract void delegateOnKeyDown(KeyDownEvent event);
	
	@Override
	public void onKeyUp(KeyUpEvent event) {
		long start = System.currentTimeMillis();
		try{
			delegateOnKeyUp(event);
		}catch(Exception ex){
			showErrorAndReset("onKeyUp", ex);
		}
		if(logTime)
			System.err.println("handle onKeyUp " + (System.currentTimeMillis()-start));
		event.preventDefault();
		event.stopPropagation();
	}
	
	public abstract void delegateOnKeyUp(KeyUpEvent event);
	
	@Override
	public void onKeyPress(KeyPressEvent event) {
		long start = System.currentTimeMillis();
		try{
			delegateOnKeyPress(event);
		}catch(Exception ex){
			showErrorAndReset("onKeyPressed", ex);
		}
		if(logTime)
			System.err.println("handle onKeyPress " + (System.currentTimeMillis()-start));
		event.preventDefault();
		event.stopPropagation();
	}
	
	public abstract void delegateOnKeyPress(KeyPressEvent event);
	
	private void showErrorAndReset(String method, Throwable ex) {
		renderer.getProcessDesigner().getProcessDesignerConfiguration().setProcessDesignerMode(ProcessDesignerMode.selecting);
		renderer.showMode(renderer.getProcessDesigner().getProcessDesignerConfiguration().getProcessDesignerMode());
		renderer.disableInteractions();
		renderer.hideEdgeKindChoice();
		renderer.hidePotentialEdgeGroup();
		renderer.render();
		ErrorDialog.show("Error while " + method + (ex.getMessage() != null ? ex.getMessage() : ""), ex);
		Console.log(ex + " " + (ex.getMessage() != null ? ex.getMessage() : ""));
	}
	
}
