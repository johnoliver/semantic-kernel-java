// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import reactor.core.publisher.Mono;

/**
 * Interface for chat completion clients.
 */
public interface ChatClient {

    /**
     * Gets a response from the chat client.
     *
     * @param messages    The messages to send.
     * @param chatOptions Optional parameters for the chat completion.
     * @return A CompletableFuture that will complete with the chat response.
     */
    Mono<ChatHistory> getResponseAsync(
        ChatHistory messages,
        AgentInvokeOptions chatOptions);

}
