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
package com.liferay.faces.showcase.context;

import com.liferay.faces.util.context.MessageContext;
import com.liferay.faces.util.context.MessageContextFactory;


/**
 * @author  Neil Griffin
 */
public class MessageContextFactoryMetalShowcaseImpl extends MessageContextFactory {

	// Private Data Members
	private MessageContextFactory wrappedMessageContextFactory;
	private MessageContext messageContext;

	public MessageContextFactoryMetalShowcaseImpl(MessageContextFactory messageContextFactory) {
		this.wrappedMessageContextFactory = messageContextFactory;

		MessageContext wrappedMessageContext = messageContextFactory.getMessageContext();
		this.messageContext = new MessageContextMetalShowcaseImpl(wrappedMessageContext);
	}

	@Override
	public MessageContext getMessageContext() {
		return messageContext;
	}

	@Override
	public MessageContextFactory getWrapped() {
		return wrappedMessageContextFactory;
	}
}