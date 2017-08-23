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
package com.liferay.faces.metal.i18n.internal;

import java.io.Serializable;

import javax.faces.context.ExternalContext;

import com.liferay.faces.metal.config.internal.MetalWebConfigParam;
import com.liferay.faces.util.cache.Cache;
import com.liferay.faces.util.cache.CacheFactory;
import com.liferay.faces.util.i18n.I18n;
import com.liferay.faces.util.i18n.I18nBundleBase;


/**
 * @author  Neil Griffin
 */
public class I18nMetalImpl extends I18nBundleBase implements Serializable {

	// serialVersionUID
	private static final long serialVersionUID = 3479979656530147715L;

	public I18nMetalImpl(I18n i18n) {
		super(i18n);
	}

	@Override
	public String getBundleKey() {
		return "i18n-metal";
	}

	@Override
	protected Cache<String, String> newConcurrentMessageCache(ExternalContext externalContext) {

		Cache<String, String> concurrentMessageCache;
		int initialCacheCapacity = MetalWebConfigParam.MetalI18nBundleInitialCacheCapacity.getIntegerValue(
				externalContext);
		MetalWebConfigParam MetalI18nBundleMaxCacheCapacity = MetalWebConfigParam.MetalI18nBundleMaxCacheCapacity;
		int maxCacheCapacity = MetalI18nBundleMaxCacheCapacity.getIntegerValue(externalContext);

		if (maxCacheCapacity != MetalI18nBundleMaxCacheCapacity.getDefaultIntegerValue()) {
			concurrentMessageCache = CacheFactory.getConcurrentLRUCacheInstance(externalContext, initialCacheCapacity,
					maxCacheCapacity);
		}
		else {
			concurrentMessageCache = CacheFactory.getConcurrentCacheInstance(externalContext, initialCacheCapacity);
		}

		return concurrentMessageCache;
	}
}
