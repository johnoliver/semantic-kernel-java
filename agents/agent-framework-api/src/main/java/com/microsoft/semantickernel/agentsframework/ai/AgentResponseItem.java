// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

public class AgentResponseItem<T> {

    private final T message;
    @Nullable
    private final String conversationId;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AgentResponseItem(T message,
        @Nullable
        String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    /**
     * Gets the agent response message.
     *
     * @return The message.
     */
    public T getMessage() {
        return message;
    }

    @Nullable
    public String getConversationId() {
        return conversationId;
    }
}
