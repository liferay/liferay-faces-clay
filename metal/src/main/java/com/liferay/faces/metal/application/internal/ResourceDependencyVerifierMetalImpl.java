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
package com.liferay.faces.metal.application.internal;

import com.liferay.faces.util.application.ResourceDependencyVerifier;
import com.liferay.faces.util.application.ResourceDependencyVerifierWrapper;
import com.liferay.faces.util.application.ResourceUtil;
import com.liferay.faces.util.product.ProductConstants;
import com.liferay.faces.util.product.ProductMap;
import javax.faces.component.UIComponent;

/**
 * @author  Kyle Stiemann
 */
public class ResourceDependencyVerifierMetalImpl extends ResourceDependencyVerifierWrapper {

	// Private Constants
	private static final boolean LIFERAY_PORTAL_DETECTED = ProductMap.getInstance().get(ProductConstants.LIFERAY_PORTAL)
		.isDetected();
	private static final String BOOTSTRAP_CSS_RESOURCE_ID = ResourceUtil.getResourceDependencyId("liferay-faces-metal-reslib", "css/bootstrap.min.css");

	// Private Members
	private ResourceDependencyVerifier wrappedResourceDependencyVerifier;

	public ResourceDependencyVerifierMetalImpl(ResourceDependencyVerifier wrappedResourceDependencyVerifier) {
		this.wrappedResourceDependencyVerifier = wrappedResourceDependencyVerifier;
	}

	@Override
	public boolean isResourceDependencySatisfied(UIComponent componentResource) {

		boolean resourceDependencySatisfied;

		if (LIFERAY_PORTAL_DETECTED && BOOTSTRAP_CSS_RESOURCE_ID.equals(ResourceUtil.getResourceDependencyId(componentResource))) {
			resourceDependencySatisfied = true;
		}
		else {
			resourceDependencySatisfied = super.isResourceDependencySatisfied(componentResource);
		}

		return resourceDependencySatisfied;
	}

	@Override
	public ResourceDependencyVerifier getWrapped() {
		return wrappedResourceDependencyVerifier;
	}
}
