/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.crystal.component.datatable;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;


/**
 * @author  Neil Griffin
 */
public class RowSelectEvent extends AjaxBehaviorEvent {

	// Public Constants
	public static final String ROW_SELECT = "rowSelect";

	// serialVersionUID
	private static final long serialVersionUID = 2311432761100123239L;

	// Private Data Members
	private Object rowData;
	private int rowIndex;

	public RowSelectEvent(UIComponent component, Behavior behavior, int rowIndex, Object rowData) {
		super(component, behavior);
		this.rowIndex = rowIndex;
		this.rowData = rowData;
		setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
	}

	public Object getRowData() {
		return rowData;
	}

	public int getRowIndex() {
		return rowIndex;
	}
}
