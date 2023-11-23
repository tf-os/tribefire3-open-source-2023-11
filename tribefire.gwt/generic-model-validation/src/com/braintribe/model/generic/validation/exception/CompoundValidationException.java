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
package com.braintribe.model.generic.validation.exception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class CompoundValidationException extends Throwable implements Iterable<Throwable>{
	
	private List<Throwable> throwableList;
	
	public CompoundValidationException(){
		throwableList = new ArrayList<Throwable>();
	}
	
	public boolean addThrowable(Throwable t){
		return throwableList.add(t);
	}

	@Override
	public Iterator<Throwable> iterator() {
		return throwableList.iterator();
	}
	
	@Override
	public void printStackTrace() {
		for(Throwable t : throwableList){
			t.printStackTrace();
		}
	}
	
	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		for (Throwable t : throwableList) {
			builder.append(t.getMessage()).append("\n");
		}
		
		return builder.toString();
	}

}
