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
package tribefire.extension.xml.schemed.test.commons.commons.synch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncTuple {	
	private String name;
	private String source;
	private String target;
	private List<String> namesToCopy = new ArrayList<String>();
	
	public SyncTuple(String name, String source, String target, String ... files) {
		this.name = name;
		this.source = source;
		this.target = target;
		if (files != null) {
			this.namesToCopy.addAll( Arrays.asList(files));
		}		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public List<String> getNamesToCopy() {
		return namesToCopy;
	}
	public void setNamesToCopy(List<String> namesToCopy) {
		this.namesToCopy = namesToCopy;
	}		
	
	public String toString() {
		return name + ":" + source + " -> " + target;
	}
	
	// format <name>,<source>,<target>,<file>[,<file>,..]
	public static SyncTuple fromString( String string) {
		String [] split = string.trim().split( ",");
		SyncTuple tuple = new SyncTuple(split[0], split[1], split[2]);
		if (split.length > 3) {
			for (int i = 3; i < split.length; i++) {
				tuple.getNamesToCopy().add( split[i]);
			}
		}
		return tuple;
	}
}
