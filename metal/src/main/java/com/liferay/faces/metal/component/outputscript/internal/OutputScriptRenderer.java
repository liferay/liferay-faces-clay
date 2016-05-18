/**
 * Copyright (c) 2000-2016 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.metal.component.outputscript.internal;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.metal.component.outputscript.OutputScript;
import com.liferay.faces.util.client.Script;
import com.liferay.faces.util.client.ScriptFactory;
import com.liferay.faces.util.context.FacesRequestContext;
import com.liferay.faces.util.render.BufferedScriptResponseWriter;


/**
 * @author  Kyle Stiemann
 */
@FacesRenderer(componentFamily = OutputScript.COMPONENT_FAMILY, rendererType = OutputScript.RENDERER_TYPE)
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class OutputScriptRenderer extends OutputScriptRendererBase {

	@Override
	public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		OutputScript outputScript = (OutputScript) uiComponent;
		String name = outputScript.getName();

		// If the name attribute is specified, then simply delegate to the JSF runtime renderer since it knows how to
		// render a JSF resource.
		if ((name != null) && (name.length() > 0)) {
			super.encodeChildren(facesContext, uiComponent);
		}

		// Otherwise, since it is not a JSF resource:
		else {

			String target = outputScript.getTarget();

			if ("body".equals(target) && !facesContext.getPartialViewContext().isAjaxRequest()) {

				ResponseWriter responseWriter = facesContext.getResponseWriter();
				BufferedScriptResponseWriter bufferedScriptResponseWriter = new BufferedScriptResponseWriter();
				facesContext.setResponseWriter(bufferedScriptResponseWriter);
				super.encodeChildren(facesContext, uiComponent);
				facesContext.setResponseWriter(responseWriter);

				String bufferedScriptString = bufferedScriptResponseWriter.toString();
				Script script = ScriptFactory.getScriptInstance(bufferedScriptString);
				FacesRequestContext facesRequestContext = FacesRequestContext.getCurrentInstance();
				facesRequestContext.addScript(script);
			}

			// Otherwise, simply delegate to the JSF runtime renderer since target="head" or target="form".
			else {
				super.encodeChildren(facesContext, uiComponent);
			}
		}
	}

	@Override
	public void processEvent(ComponentSystemEvent componentSystemEvent) throws AbortProcessingException {

		UIComponent uiComponent = componentSystemEvent.getComponent();
		final Map<String, Object> attributes = uiComponent.getAttributes();
		String target = (String) attributes.get("target");
		FacesContext facesContext = FacesContext.getCurrentInstance();

		// If the script is target is not "body" or this is an Ajax request, then call through to the parent's
		// processEvent() method.
		if (!"body".equals(target) || facesContext.getPartialViewContext().isAjaxRequest()) {
			super.processEvent(componentSystemEvent);
		}
		// Otherwise, do nothing in order to avoid adding script as a component resource, so that it can be rendered at
		// the bottom of the page via FacesRequestContext.addScript() instead. See FACES-2549 for more details.
	}
}
