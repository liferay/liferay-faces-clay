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
package com.liferay.faces.crystal.component.inputfile.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.crystal.component.inputfile.FileUploadEvent;
import com.liferay.faces.crystal.component.inputfile.InputFile;
import com.liferay.faces.util.context.MessageContext;
import com.liferay.faces.util.context.MessageContextFactory;
import com.liferay.faces.util.context.map.MultiPartFormData;
import com.liferay.faces.util.factory.FactoryExtensionFinder;
import com.liferay.faces.util.model.UploadedFile;
import com.liferay.faces.util.product.ProductConstants;
import com.liferay.faces.util.product.ProductMap;
import com.liferay.faces.util.render.internal.DelegationResponseWriter;


/**
 * @author  Neil Griffin
 */
//J-
@FacesRenderer(componentFamily = InputFile.COMPONENT_FAMILY, rendererType = InputFile.RENDERER_TYPE)
@ResourceDependencies(
		{
			@ResourceDependency(library = "liferay-faces-crystal", name = "crystal.css"),
			@ResourceDependency(library = "liferay-faces-crystal-reslib", name = "css/bootstrap.min.css")
		}
	)
//J+
public class InputFileRenderer extends InputFileRendererBase {

	// Private Constants
	private static final boolean LIFERAY_FACES_BRIDGE_DETECTED = ProductMap.getInstance().get(
			ProductConstants.LIFERAY_FACES_BRIDGE).isDetected();

	@Override
	public void decode(FacesContext facesContext, UIComponent uiComponent) {

		InputFile inputFile = (InputFile) uiComponent;

		Map<String, List<UploadedFile>> uploadedFileMap = getUploadedFileMap(facesContext, inputFile.getLocation());

		if (uploadedFileMap != null) {

			String clientId = uiComponent.getClientId(facesContext);
			List<UploadedFile> uploadedFiles = uploadedFileMap.get(clientId);

			if ((uploadedFiles != null) && (uploadedFiles.size() > 0)) {

				inputFile.setSubmittedValue(uploadedFiles);

				// Queue the FileUploadEvent so that each uploaded file can be handled individually with an
				// ActionListener.
				for (UploadedFile uploadedFile : uploadedFiles) {

					FileUploadEvent fileUploadEvent = new FileUploadEvent(uiComponent, uploadedFile);
					uiComponent.queueEvent(fileUploadEvent);
				}
			}
		}
	}

	@Override
	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// Delegate writing of the entire <input type="file"...> ... </input> element to the delegate
		// renderer.
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		DelegationResponseWriter delegationResponseWriter = new InputFileDelegationResponseWriter(responseWriter);
		super.encodeEnd(facesContext, uiComponent, delegationResponseWriter);
	}

	@Override
	public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent, Object submittedValue)
		throws ConverterException {
		return submittedValue;
	}

	protected MessageContext getMessageContext() {

		MessageContextFactory messageContextFactory = (MessageContextFactory) FactoryExtensionFinder.getFactory(
				MessageContextFactory.class);

		return messageContextFactory.getMessageContext();
	}

	protected String getParentFormClientId(UIComponent uiComponent) {

		String parentFormClientId = null;

		if (uiComponent != null) {

			if (uiComponent instanceof UIForm) {
				parentFormClientId = uiComponent.getClientId();
			}
			else {
				parentFormClientId = getParentFormClientId(uiComponent.getParent());
			}
		}

		return parentFormClientId;
	}

	protected Map<String, List<UploadedFile>> getUploadedFileMap(FacesContext facesContext, String location) {

		Map<String, List<UploadedFile>> uploadedFileMap = null;

		if (LIFERAY_FACES_BRIDGE_DETECTED) {
			Map<String, Object> requestAttributeMap = facesContext.getExternalContext().getRequestMap();
			MultiPartFormData multiPartFormData = (MultiPartFormData) requestAttributeMap.get(MultiPartFormData.class
					.getName());

			if (multiPartFormData != null) {
				uploadedFileMap = multiPartFormData.getUploadedFileMap();
			}
		}
		else {
			InputFileDecoder inputFileDecoder = getWebappInputFileDecoder(facesContext);
			uploadedFileMap = inputFileDecoder.decode(facesContext, location);
		}

		return uploadedFileMap;
	}
}
