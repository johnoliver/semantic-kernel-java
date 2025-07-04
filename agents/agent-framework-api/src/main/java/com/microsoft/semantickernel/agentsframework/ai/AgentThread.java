// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Interface for an agent thread.
 */
public interface AgentThread {

    /**
     * Get the thread ID.
     *
     * @return The thread ID.
     */
    String getId();

    /**
     * Handle a new message in the thread.
     *
     * @param newMessage The new message to handle.
     * @return A Mono indicating completion.
     */
    Mono<List<ChatMessageContent<?>>> onNewMessageAsync(ChatHistory newMessage);

    Mono<List<ChatMessageContent<?>>> getMessages();

    Mono<ChatMessageContent<?>> createMessage(AuthorRole authorRole, String content);
}