// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

public class ChatCompletionAgent implements ChatClient {

    private final ChatCompletionService chatCompletionService;

    public ChatCompletionAgent(ChatCompletionService chatCompletionService) {
        this.chatCompletionService = chatCompletionService;
    }

    /**
     * Invoke the agent with the given chat history.
     *
     * @param messages The chat history to process
     * @param thread   The agent thread to use
     * @param options  The options for invoking the agent
     * @return A Mono containing the agent response
     */
    @Override
    public Mono<ChatHistory> getResponseAsync(
        ChatHistory messages,
        @Nullable AgentInvokeOptions options) {

        if (options == null) {
            options = new AgentInvokeOptions();
        }

        final String additionalInstructions = options.getAdditionalInstructions();

        // Add agent additional instructions
        if (additionalInstructions != null) {
            messages.addMessage(new ChatMessageContent<>(
                AuthorRole.SYSTEM,
                additionalInstructions));
        }

        return chatCompletionService.getChatMessageContentsAsync(messages, null, null)
            .map(response -> {
                return new ChatHistory(response);
            });

    }
}
