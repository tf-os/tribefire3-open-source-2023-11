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
package com.braintribe.model.access.impl.useraccess;

import com.braintribe.logging.Logger;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;

public class LdapPagingContext {

	protected static Logger logger = Logger.getLogger(LdapPagingContext.class);
	
	protected boolean pagingActivated = false;
	protected int startIndex = 0;
	protected int pageSize = 20;

	public enum RangeResult {
		inRange, beforeRange, afterRange
	}

	public LdapPagingContext(Restriction restriction) {
		this.pagingActivated = false;
		if (restriction != null) {
			Paging paging = restriction.getPaging();
			if (paging != null) {

				int start = paging.getStartIndex();
				int size = paging.getPageSize();
				if ((start >= 0) && (size > 0)) {
					this.pagingActivated = true;

					this.startIndex = start;
					this.pageSize = size;
				} else {
					logger.debug("Ignoring invalid paging information: startIndex="+start+", pageSize="+size);
				}
			} 
		}
	}

	public RangeResult indexInRange(int index) {
		if (!this.pagingActivated) {
			return RangeResult.inRange;
		}
		if (index < this.startIndex) {
			return RangeResult.beforeRange;
		}
		int stop = this.startIndex + pageSize;
		if (index >= stop) {
			return RangeResult.afterRange;
		}
		return RangeResult.inRange;
	}

	public boolean isPagingActivated() {
		return pagingActivated;
	}
	public void setPagingActivated(boolean pagingActivated) {
		this.pagingActivated = pagingActivated;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Paging activated: ");
		sb.append(this.pagingActivated);
		if (this.pagingActivated) {
			sb.append("; startIndex");
			sb.append(this.startIndex);
			sb.append(", pageSize:");
			sb.append(this.pageSize);
		}
		return sb.toString();
	}
}
