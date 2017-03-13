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
package com.liferay.faces.metal.application.internal;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.liferay.faces.util.application.ResourceUtil;
import com.liferay.faces.util.application.ResourceVerifier;
import com.liferay.faces.util.application.ResourceVerifierWrapper;
import com.liferay.faces.util.product.Product;
import com.liferay.faces.util.product.ProductFactory;


/**
 * @author  Kyle Stiemann
 */
public class ResourceVerifierMetalImpl extends ResourceVerifierWrapper implements Serializable {

	// serialVersionUID
	private static final long serialVersionUID = 8437791501446799339L;

	// Private Constants
	private static final boolean LIFERAY_PORTAL_DETECTED = ProductFactory.getProduct(Product.Name.LIFERAY_PORTAL)
		.isDetected();
	private static final String BOOTSTRAP_CSS_RESOURCE_ID = ResourceUtil.getResourceId("liferay-faces-metal-reslib",
			"css/bootstrap.min.css");

	// Private Members
	private ResourceVerifier wrappedResourceVerifier;

	public ResourceVerifierMetalImpl(ResourceVerifier wrappedResourceVerifier) {
		this.wrappedResourceVerifier = wrappedResourceVerifier;
	}

	@Override
	public ResourceVerifier getWrapped() {
		return wrappedResourceVerifier;
	}

	@Override
	public boolean isDependencySatisfied(FacesContext facesContext, UIComponent componentResource) {

		boolean dependencySatisfied;

		if (LIFERAY_PORTAL_DETECTED &&
				BOOTSTRAP_CSS_RESOURCE_ID.equals(ResourceUtil.getResourceId(componentResource))) {
			dependencySatisfied = true;
		}
		else {
			dependencySatisfied = super.isDependencySatisfied(facesContext, componentResource);
		}

		return dependencySatisfied;
	}
}
