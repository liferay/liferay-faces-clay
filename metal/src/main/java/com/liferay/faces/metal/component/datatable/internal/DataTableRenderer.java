/**
 * Copyright (c) 2000-2017 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.metal.component.datatable.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.html.HtmlColumn;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.metal.component.column.Column;
import com.liferay.faces.metal.component.commandlink.CommandLink;
import com.liferay.faces.metal.component.datatable.DataTable;
import com.liferay.faces.metal.component.outputtext.OutputText;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.faces.util.render.RendererUtil;


/**
 * @author  Neil Griffin
 */

//J-
@FacesRenderer(componentFamily = DataTable.COMPONENT_FAMILY, rendererType = DataTable.RENDERER_TYPE)
@ResourceDependencies(
	{
		@ResourceDependency(library = "liferay-faces-metal-reslib", name = "css/bootstrap.min.css"),
		@ResourceDependency(library = "liferay-faces-metal", name = "metal.css")
	}
)
//J+
public class DataTableRenderer extends DataTableRendererBase {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(DataTableRenderer.class);

	@Override
	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// If the rows attribute has changed since the last render, then reset the first row that is to be displayed
		// back to zero. This takes care of any page number rendering difficulties.
		DataTable dataTable = (DataTable) uiComponent;
		Map<String, Object> dataTableAttributes = dataTable.getAttributes();
		Integer oldRows = (Integer) dataTableAttributes.remove("oldRows");

		if ((oldRows != null) && (oldRows != dataTable.getRows())) {
			dataTable.setFirst(0);
		}

		// Encode the starting <table> element that represents the metal:table.
		DataTableInfo dataTableInfo = new DataTableInfo(dataTable);
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.startElement("table", dataTable);
		responseWriter.writeAttribute("id", dataTable.getClientId(facesContext), "id");
		RendererUtil.encodeStyleable(responseWriter, dataTable);

		// If present, encode the child <f:facet name="caption" ... />
		encodeCaptionFacet(facesContext, responseWriter, dataTable);

		// If present, encode the child <f:facet name="colGroups" ... />
		encodeColGroupsFacet(facesContext, dataTable);

		// Encode the table <thead> ... </thead> section.
		encodeHeader(facesContext, responseWriter, dataTable, dataTableInfo);

		// Encode the table <tfoot> ... </tfoot> section.
		encodeFooter(facesContext, responseWriter, dataTable, dataTableInfo);
	}

	@Override
	public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		DataTable dataTable = (DataTable) uiComponent;
		DataTableInfo dataTableInfo = new DataTableInfo(dataTable);
		ResponseWriter responseWriter = facesContext.getResponseWriter();

		int totalRenderedColumns = dataTableInfo.getTotalRenderedColumns();

		if (totalRenderedColumns == 0) {
			responseWriter.startElement("tbody", dataTable);
			responseWriter.endElement("tbody");
		}
		else {

			int rows = dataTable.getRows();
			int rowIndex = dataTable.getFirst() - 1;
			int totalRowsEncoded = 0;

			int[] bodyRows = dataTable.toIntArray(dataTable.getBodyrows());

			boolean wroteTBody = false;

			if (bodyRows == null) {
				responseWriter.startElement("tbody", dataTable);
				wroteTBody = true;
			}

			if (rows >= 0) {

				ItemCycler rowClasses = new ItemCycler(dataTable.getRowClasses());
				ItemCycler columnClasses = new ItemCycler(dataTable.getColumnClasses());

				while ((totalRowsEncoded < rows) || (rows == 0)) {

					rowIndex++;
					dataTable.setRowIndex(rowIndex);
					columnClasses.reset();

					// If there is data in the model for the current row index, then encode the row.
					if (dataTable.isRowAvailable()) {

						if (bodyRows != null) {

							for (int bodyRow : bodyRows) {

								if (bodyRow == rowIndex) {

									if (wroteTBody) {
										responseWriter.endElement("tbody");
									}

									responseWriter.startElement("tbody", dataTable);
									wroteTBody = true;

									break;
								}
							}
						}

						encodeRow(facesContext, responseWriter, dataTable, rowIndex, rowClasses, columnClasses);

						totalRowsEncoded++;
					}

					// Otherwise, encoding of rows is complete since the last row has been encoded.
					else {

						break;
					}
				}
			}

			if (totalRowsEncoded == 0) {
				encodeEmptyTableRow(responseWriter, dataTable, totalRenderedColumns);
			}

			responseWriter.endElement("tbody");
		}
	}

	@Override
	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// Encode the closing <table> element that represents the metal:table.
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.endElement("table");
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	protected void encodeCaptionFacet(FacesContext facesContext, ResponseWriter responseWriter, DataTable dataTable)
		throws IOException {

		UIComponent captionFacet = dataTable.getFacet("caption");

		if (captionFacet != null) {

			responseWriter.startElement("caption", null);

			String captionClass = dataTable.getCaptionClass();

			if (captionClass != null) {
				responseWriter.writeAttribute("class", captionClass, "captionClass");
			}

			String captionStyle = dataTable.getCaptionStyle();

			if (captionStyle != null) {
				responseWriter.writeAttribute("style", captionStyle, "captionStyle");
			}

			encodeRecurse(facesContext, captionFacet);

			responseWriter.endElement("caption");
		}
	}

	protected void encodeColGroupsFacet(FacesContext facesContext, DataTable dataTable) throws IOException {

		UIComponent colGroupsFacet = dataTable.getFacet("colGroups");

		if (colGroupsFacet != null) {
			encodeRecurse(facesContext, colGroupsFacet);
		}
	}

	protected void encodeEmptyTableRow(ResponseWriter responseWriter, DataTable dataTable, int totalColumns)
		throws IOException {

		responseWriter.startElement("tr", dataTable);

		for (int i = 0; i < totalColumns; i++) {
			responseWriter.startElement("td", dataTable);
			responseWriter.endElement("td");
		}

		responseWriter.endElement("tr");
	}

	protected void encodeFooter(FacesContext facesContext, ResponseWriter responseWriter, DataTable dataTable,
		DataTableInfo dataTableInfo) throws IOException {

		UIComponent footerFacet = dataTable.getFacet("footer");

		if ((footerFacet != null) || dataTableInfo.isFooterFacetPresentInColumn()) {
			responseWriter.startElement("tfoot", null);
		}

		String footerClass = dataTable.getFooterClass();

		if (dataTableInfo.isFooterFacetPresentInColumn()) {

			responseWriter.startElement("tr", null);

			List<UIComponent> children = dataTable.getChildren();

			for (UIComponent child : children) {

				if (child instanceof HtmlColumn) {

					HtmlColumn htmlColumn = (HtmlColumn) child;

					if (htmlColumn.isRendered()) {

						responseWriter.startElement("td", null);

						String columnFooterClass = htmlColumn.getFooterClass();

						if (columnFooterClass != null) {
							responseWriter.writeAttribute("class", columnFooterClass, "columnFooterClass");
						}
						else if (footerClass != null) {
							responseWriter.writeAttribute("class", footerClass, "footerClass");
						}

						UIComponent columnFooterFacet = htmlColumn.getFacet("footer");

						if (columnFooterFacet != null) {
							encodeRecurse(facesContext, columnFooterFacet);
						}

						responseWriter.endElement("td");
					}
				}
			}

			responseWriter.endElement("tr");
		}

		int totalRenderedColumns = dataTableInfo.getTotalRenderedColumns();
		int colspan = totalRenderedColumns;

		if (footerFacet != null) {

			responseWriter.startElement("tr", null);
			responseWriter.startElement("td", null);

			if (footerClass == null) {
				responseWriter.writeAttribute("class", "facet", "footerClass");
			}
			else {
				responseWriter.writeAttribute("class", footerClass.concat(" facet"), "footerClass");
			}

			if (totalRenderedColumns > 1) {
				responseWriter.writeAttribute("colspan", colspan, null);
			}

			encodeRecurse(facesContext, footerFacet);
			responseWriter.endElement("td");
			responseWriter.endElement("tr");
		}

		if ((footerFacet != null) || dataTableInfo.isFooterFacetPresentInColumn()) {
			responseWriter.endElement("tfoot");
		}
	}

	protected void encodeHeader(FacesContext facesContext, ResponseWriter responseWriter, DataTable dataTable,
		DataTableInfo dataTableInfo) throws IOException {

		UIComponent headerFacet = dataTable.getFacet("header");

		if ((headerFacet != null) || dataTableInfo.isHeaderFacetOrTextPresentInColumn()) {
			responseWriter.startElement("thead", null);
			responseWriter.writeAttribute("class", "table-columns", null);
		}

		int totalRenderedColumns = dataTableInfo.getTotalRenderedColumns();
		int colspan = totalRenderedColumns;

		String headerClass = dataTable.getHeaderClass();

		if (headerFacet != null) {

			responseWriter.startElement("tr", null);
			responseWriter.startElement("th", null);

			if (headerClass == null) {
				responseWriter.writeAttribute("class", "facet", "headerClass");
			}
			else {
				responseWriter.writeAttribute("class", headerClass.concat(" facet"), "headerClass");
			}

			if (totalRenderedColumns > 1) {
				responseWriter.writeAttribute("colspan", colspan, null);
			}

			responseWriter.writeAttribute("scope", "colgroup", null);
			encodeRecurse(facesContext, headerFacet);
			responseWriter.endElement("th");
			responseWriter.endElement("tr");
		}

		if (dataTableInfo.isHeaderFacetOrTextPresentInColumn()) {

			responseWriter.startElement("tr", null);

			List<UIComponent> children = dataTable.getChildren();

			for (UIComponent child : children) {

				if (child instanceof UIColumn) {

					UIColumn uiColumn = (UIColumn) child;

					if (uiColumn.isRendered()) {
						responseWriter.startElement("th", null);

						if (child instanceof HtmlColumn) {

							HtmlColumn htmlColumn = (HtmlColumn) child;
							String columnHeaderClass = htmlColumn.getHeaderClass();

							String sortClass = null;
							Column metalColumn = null;

							if (child instanceof Column) {

								metalColumn = (Column) htmlColumn;

								String sortOrder = metalColumn.getSortOrder();

								if ("ASCENDING".equals(sortOrder)) {
									sortClass = " table-sortable-column table-sorted";
								}
								else if ("DESCENDING".equals(sortOrder)) {
									sortClass = " table-sortable-column table-sorted table-sorted-desc";
								}
							}

							if (columnHeaderClass != null) {

								if (sortClass != null) {
									columnHeaderClass = columnHeaderClass.concat(sortClass);
								}

								responseWriter.writeAttribute("class", columnHeaderClass, "columnHeaderClass");
							}
							else if (headerClass != null) {

								if (sortClass != null) {
									headerClass = headerClass.concat(sortClass);
								}

								responseWriter.writeAttribute("class", headerClass, "headerClass");
							}
							else if (sortClass != null) {
								responseWriter.writeAttribute("class", sortClass, null);
							}

							responseWriter.writeAttribute("scope", "col", null);

							if (metalColumn != null) {

								String headerText = metalColumn.getHeaderText();

								if (headerText != null) {
									encodeHeaderText(facesContext, responseWriter, dataTable, metalColumn, headerText);
								}
							}

							UIComponent columnHeaderFacet = htmlColumn.getFacet("header");

							if (columnHeaderFacet != null) {
								encodeRecurse(facesContext, columnHeaderFacet);
							}
						}

						responseWriter.endElement("th");
					}
				}
			}

			responseWriter.endElement("tr");
		}

		if ((headerFacet != null) || dataTableInfo.isHeaderFacetOrTextPresentInColumn()) {
			responseWriter.endElement("thead");
		}
	}

	protected void encodeHeaderText(FacesContext facesContext, ResponseWriter responseWriter, DataTable dataTable,
		Column column, String headerText) throws IOException {

		ValueExpression sortByValueExpression = column.getValueExpression("sortBy");

		if (sortByValueExpression == null) {
			responseWriter.writeText(headerText, column, null);
		}
		else {

			responseWriter.startElement("div", dataTable);
			responseWriter.writeAttribute("class", "table-sort-liner", null);

			// If the metal:column has a nested f:ajax tag, then encode a hyperlink that contains the client
			// behavior script in the onclick attribute.
			String dataTableClientId = dataTable.getClientId(facesContext);
			String clientBehaviorScript = getColumnClientBehaviorScript(facesContext, dataTable, column,
					dataTableClientId);

			if (clientBehaviorScript != null) {

				// Write the client behavior script in the onclick attribute on the <div> element because writing it on
				// the <a> element will have the side-effect of a new browser tab opening for each sort column that is
				// selected with Left Click + Meta.
				responseWriter.writeAttribute("onclick", clientBehaviorScript, null);
				responseWriter.startElement("a", null);
				responseWriter.writeText(headerText, null);
				responseWriter.startElement("span", dataTable);
				responseWriter.writeAttribute("class", "table-sort-indicator", null);
				responseWriter.endElement("span");
				responseWriter.endElement("a");
			}

			// Otherwise, encode an metal:commandLink that can submit the form via full-page postback.
			else {
				Application application = facesContext.getApplication();
				CommandLink commandLink = (CommandLink) application.createComponent(facesContext,
						CommandLink.COMPONENT_TYPE, CommandLink.RENDERER_TYPE);
				commandLink.setAjax(column.isAjax());

				OutputText outputText1 = (OutputText) application.createComponent(facesContext,
						OutputText.COMPONENT_TYPE, OutputText.RENDERER_TYPE);
				outputText1.setValue(headerText);

				OutputText outputText2 = (OutputText) application.createComponent(facesContext,
						OutputText.COMPONENT_TYPE, OutputText.RENDERER_TYPE);
				outputText2.setStyleClass("table-sort-indicator");

				List<UIComponent> paginatorChildren = column.getChildren();
				paginatorChildren.add(commandLink);

				UIParameter uiParameter = new UIParameter();
				String sortColumnClientIdParamName = dataTableClientId.concat("_sortColumnClientId");
				uiParameter.setName(sortColumnClientIdParamName);
				uiParameter.setValue(column.getClientId(facesContext));

				List<UIComponent> commandLinkChildren = commandLink.getChildren();
				commandLinkChildren.add(uiParameter);
				commandLinkChildren.add(outputText1);
				commandLinkChildren.add(outputText2);
				outputText2.setEscape(false);
				commandLink.encodeAll(facesContext);
				commandLinkChildren.remove(outputText2);
				commandLinkChildren.remove(outputText1);
				commandLinkChildren.remove(uiParameter);
				paginatorChildren.remove(commandLink);
			}

			responseWriter.endElement("div");
		}
	}

	protected void encodeRecurse(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		if (uiComponent.isRendered()) {

			uiComponent.encodeBegin(facesContext);

			if (uiComponent.getRendersChildren()) {
				uiComponent.encodeChildren(facesContext);
			}
			else {
				List<UIComponent> children = uiComponent.getChildren();

				for (UIComponent child : children) {
					encodeRecurse(facesContext, child);
				}
			}

			uiComponent.encodeEnd(facesContext);
		}
	}

	protected void encodeRow(FacesContext facesContext, ResponseWriter responseWriter, DataTable dataTable,
		int rowIndex, ItemCycler rowClasses, ItemCycler columnClasses) throws IOException {

		responseWriter.startElement("tr", dataTable);

		String rowClass = rowClasses.getNextItem();

		if (rowClass != null) {
			responseWriter.writeAttribute("class", rowClass, "rowClasses");
		}

		List<UIComponent> children = dataTable.getChildren();

		for (UIComponent child : children) {

			if (child instanceof HtmlColumn) {

				HtmlColumn htmlColumn = (HtmlColumn) child;

				if (htmlColumn.isRendered()) {

					if (htmlColumn.isRowHeader()) {
						responseWriter.startElement("th", htmlColumn);
						responseWriter.writeAttribute("scope", "row", null);
					}
					else {
						responseWriter.startElement("td", htmlColumn);
					}

					String columnClass = columnClasses.getNextItem();

					if (columnClass != null) {
						responseWriter.writeAttribute("class", columnClass, "columnClasses");
					}

					List<UIComponent> htmlColumnChildren = htmlColumn.getChildren();

					for (UIComponent htmlColumnChild : htmlColumnChildren) {
						encodeRecurse(facesContext, htmlColumnChild);
					}

					if (htmlColumn.isRowHeader()) {
						responseWriter.endElement("th");
					}
					else {
						responseWriter.endElement("td");
					}
				}
			}
		}

		responseWriter.endElement("tr");
	}

	protected String getColumnClientBehaviorScript(FacesContext facesContext, DataTable dataTable, Column column,
		String clientId) {

		String clientBehaviorScript = null;
		Map<String, List<ClientBehavior>> clientBehaviorMap = column.getClientBehaviors();
		String defaultEventName = column.getDefaultEventName();
		List<ClientBehavior> clientBehaviorsForEvent = clientBehaviorMap.get(defaultEventName);

		if (clientBehaviorsForEvent != null) {

			for (ClientBehavior clientBehavior : clientBehaviorsForEvent) {

				List<ClientBehaviorContext.Parameter> parameters = new ArrayList<ClientBehaviorContext.Parameter>();
				String sortColumnClientIdParamName = clientId.concat("_sortColumnClientId");
				String sortColumnClientId = column.getClientId(facesContext);
				parameters.add(new ClientBehaviorContext.Parameter(sortColumnClientIdParamName, sortColumnClientId));

				String eventMetaKeyParamName = clientId.concat("_eventMetaKey");
				parameters.add(new ClientBehaviorContext.Parameter(eventMetaKeyParamName, "event.metaKey"));

				ClientBehaviorContext clientBehaviorContext = ClientBehaviorContext.createClientBehaviorContext(
						facesContext, dataTable, defaultEventName, clientId, parameters);
				clientBehaviorScript = clientBehavior.getScript(clientBehaviorContext);
			}
		}

		if (clientBehaviorScript != null) {
			clientBehaviorScript = clientBehaviorScript.replaceFirst("'event.metaKey'", "event.metaKey");
		}

		return clientBehaviorScript;
	}
}
