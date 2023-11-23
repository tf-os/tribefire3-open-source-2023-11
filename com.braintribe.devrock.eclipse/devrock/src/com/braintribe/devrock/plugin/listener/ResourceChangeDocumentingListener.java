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
package com.braintribe.devrock.plugin.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import com.braintribe.utils.IOTools;

public class ResourceChangeDocumentingListener implements IResourceChangeListener, IResourceDeltaVisitor {
	 private File outfile = new File( "event-log.txt");
	 private String buffer = new String();
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		String rc, dc;
		
		String eventType = extractResourceChangeEventType(event);		
	
		IResource eventResource = event.getResource();
		
		String eventResourceName = eventResource != null ? "'" + eventResource.getName() + "'" : "no resource"; 				
				
		rc = "RC-Notification :" + eventType + " -> " + eventResourceName + ":";
		
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			dc = "no delta";
		}
		else {
			String deltaKind = extractResourDeltaType( delta);												
			try {
				delta.accept( this);
			} catch (CoreException e) {	
				e.printStackTrace();
			}
			
			dc = deltaKind + " -> " + buffer ;
		}
		
		
		try {
			System.err.println("logging to -> " + outfile.getAbsolutePath());
			IOTools.spit( outfile, rc + dc + "\n", "UTF-8", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String extractResourDeltaType(IResourceDelta delta) {
		int kind = delta.getKind();		
		if (kind == 0) {
			return "NO_CHANGE";
		}
		
		List<String> codes = new ArrayList<>();
		if ((kind & IResourceDelta.ADDED) == IResourceDelta.ADDED) {
			codes.add( "ADDED");
		}
		if ((kind & IResourceDelta.REMOVED) == IResourceDelta.REMOVED) {
			codes.add( "REMOVED");
		}		
		if ((kind & IResourceDelta.CHANGED) == IResourceDelta.CHANGED) {
			codes.add( "CHANGED");
		}
		if ((kind & IResourceDelta.ADDED_PHANTOM) == IResourceDelta.ADDED_PHANTOM) {
			codes.add( "ADDED_PHANTOM");
		}
		if ((kind & IResourceDelta.REMOVED_PHANTOM) == IResourceDelta.REMOVED_PHANTOM) {
			codes.add( "REMOVED_PHANTOM");
		}		
		if ((kind & IResourceDelta.ALL_WITH_PHANTOMS) == IResourceDelta.ALL_WITH_PHANTOMS) {
			codes.add( "ALL_WITH_PHANTOMS");
		}
		if ((kind & IResourceDelta.CONTENT) == IResourceDelta.CONTENT) {
			codes.add( "CONTENT");
		}		
		if ((kind & IResourceDelta.MOVED_FROM) == IResourceDelta.MOVED_FROM) {
			codes.add( "MOVED_FROM");
		}
		if ((kind & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
			codes.add( "MOVED_TO");
		}
		if ((kind & IResourceDelta.COPIED_FROM) == IResourceDelta.COPIED_FROM) {
			codes.add( "COPIED_FROM");
		}
		if ((kind & IResourceDelta.OPEN) == IResourceDelta.OPEN) {
			codes.add( "OPEN");
		}
		if ((kind & IResourceDelta.TYPE) == IResourceDelta.TYPE) {
			codes.add( "TYPE");
		}		
		if ((kind & IResourceDelta.SYNC) == IResourceDelta.SYNC) {
			codes.add( "SYNC");
		}
		if ((kind & IResourceDelta.MARKERS) == IResourceDelta.MARKERS) {
			codes.add( "MARKERS");
		}
		if ((kind & IResourceDelta.REPLACED) == IResourceDelta.REPLACED) {
			codes.add( "REPLACED");
		}
		
		if ((kind & IResourceDelta.DESCRIPTION) == IResourceDelta.DESCRIPTION) {
			codes.add( "DESCRIPTION");
		}
		
		if ((kind & IResourceDelta.ENCODING) == IResourceDelta.ENCODING) {
			codes.add( "ENCODING");
		}
		if ((kind & IResourceDelta.LOCAL_CHANGED) == IResourceDelta.LOCAL_CHANGED) {
			codes.add( "LOCAL_CHANGED");
		}
		if ((kind & IResourceDelta.DERIVED_CHANGED) == IResourceDelta.DERIVED_CHANGED) {
			codes.add( "DERIVED_CHANGED");
		}
		
		
		return codes.stream().collect(Collectors.joining(","));
	}

	private String extractResourceChangeEventType(IResourceChangeEvent event) {
		int type = event.getType();
		
		List<String> codes = new ArrayList<>();
		if ((type & IResourceChangeEvent.POST_CHANGE) == IResourceChangeEvent.POST_CHANGE) { 
			codes.add("POST_CHANGE");
		}
		
		if ((type & IResourceChangeEvent.PRE_CLOSE) == IResourceChangeEvent.PRE_CLOSE) { 
			codes.add("PRE_CLOSE");
		}
		
		if ((type & IResourceChangeEvent.PRE_DELETE) == IResourceChangeEvent.PRE_DELETE) { 
			codes.add("PRE_DELETE");
		}
		
		if ((type & IResourceChangeEvent.PRE_BUILD) == IResourceChangeEvent.PRE_BUILD) { 
			codes.add("PRE_BUILD");
		}
		
		if ((type & IResourceChangeEvent.POST_BUILD) == IResourceChangeEvent.POST_BUILD) { 
			codes.add("POST_BUILD");
		}
		
		if ((type & IResourceChangeEvent.PRE_REFRESH) == IResourceChangeEvent.PRE_REFRESH) { 
			codes.add("PRE_REFRESH");
		}
								
		String eventType = "event type : " + codes.stream().collect(Collectors.joining(","));
		return eventType;
	}
	
	
	private String extractResourceDelta(IResourceDelta delta,String indent) {
		IResource resource = delta.getResource();
		if (resource == null) {
			return "no resource";
		}
		IJavaElement javaElement = JavaCore.create(resource);
		String resourceName = null;
		if (javaElement instanceof IJavaProject) { 
			resourceName = "Java project " + resource.getName();
		} else if (javaElement instanceof IPackageFragmentRoot) { 
			resourceName = "PFR " + resource.getName();
		} else if (javaElement instanceof IPackageFragment) { 
			resourceName = "PF " + resource.getName();
			
		} else if (javaElement instanceof ICompilationUnit) { 
			resourceName = "CU " + resource.getName();
		} else {
			resourceName = resource.getName();
		}
		
		String result = indent.concat(resourceName);
						
		for (IResourceDelta childDelta : delta.getAffectedChildren()) {			
			result.concat(extractResourceDelta(childDelta, indent.concat("\t")));
		}
		return result;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource() == null)
			return true;
		
		buffer = extractResourceDelta(delta, "");
				
		return true;
	}
	
}
