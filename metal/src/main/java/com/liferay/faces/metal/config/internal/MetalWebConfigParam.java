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
package com.liferay.faces.metal.config.internal;

import javax.faces.context.ExternalContext;

import com.liferay.faces.metal.taghandler.LoadConstants;
import com.liferay.faces.util.config.WebConfigParamUtil;


/**
 * @author  Kyle Stiemann
 */
public enum MetalWebConfigParam {

	MetalLoadConstantsInitialCacheCapacity(LoadConstants.class.getName() + ".INITIAL_CACHE_CAPACITY", 16),
	MetalI18nBundleInitialCacheCapacity("com.liferay.faces.alloy.i18n.MetalI18nBundle.INITIAL_CACHE_CAPACITY", 16),
	MetalLoadConstantsMaxCacheCapacity(LoadConstants.class.getName() + ".MAX_CACHE_CAPACITY", -1),
	MetalI18nBundleMaxCacheCapacity("com.liferay.faces.alloy.i18n.MetalI18nBundle.MAX_CACHE_CAPACITY", -1),
	MetalTagDecoratorEnabled("com.liferay.faces.util.MetalTagDecoratorEnabled", true);

	// Private Data Members
	private boolean defaultBooleanValue;
	private int defaultIntegerValue;
	private String name;

	private MetalWebConfigParam(String name, int defaultIntegerValue) {
		this.name = name;
		this.defaultIntegerValue = defaultIntegerValue;
	}

	private MetalWebConfigParam(String name, boolean defaultBooleanValue) {
		this.name = name;
		this.defaultBooleanValue = defaultBooleanValue;
	}

	public boolean getBooleanValue(ExternalContext externalContext) {
		return WebConfigParamUtil.getBooleanValue(externalContext, name, null, defaultBooleanValue);
	}

	public int getDefaultIntegerValue() {
		return defaultIntegerValue;
	}

	public int getIntegerValue(ExternalContext externalContext) {
		return WebConfigParamUtil.getIntegerValue(externalContext, name, null, defaultIntegerValue);
	}

	public boolean isDefaultBooleanValue() {
		return defaultBooleanValue;
	}
}
