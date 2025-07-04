// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework;

import com.microsoft.semantickernel.agentsframework.ai.BaseAgentThread;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Represents a thread for a chat client agent.
 */
public class ChatClientAgentThread extends BaseAgentThread {

    private final ChatHistory messages = new ChatHistory();
    private final ChatClientAgentThreadType threadType;

    public ChatClientAgentThread(
        String id,
        ChatClientAgentThreadType threadType,
        List<ChatMessageContent<?>> initialMessages
    ) {
        super(id);
        this.threadType = threadType;
        if (initialMessages != null) {
            this.messages.addAll(initialMessages);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<ChatMessageContent<?>>> onNewMessageAsync(ChatHistory newMessage) {
        if (newMessage != null) {
            newMessage
                .getMessages()
                .stream()
                .map(it -> it.withId(UUID.randomUUID()::toString))
                .forEach(messages::addMessage);
        }

        // TODO evaluate completion of the thread
        return Mono.empty();
    }

    /**
     * Gets an unmodifiable view of the messages in the thread.
     *
     * @return An unmodifiable list of the messages in the thread.
     */
    public Mono<List<ChatMessageContent<?>>> getMessages() {
        return Mono.just(messages.getMessages());
    }

    @Override
    public Mono<ChatMessageContent<?>> createMessage(AuthorRole authorRole,
        String content) {
        return Mono.just(new ChatMessageContent(UUID.randomUUID().toString(), authorRole, content));
    }

    /**
     * Gets the type of thread.
     *
     * @return The thread type.
     */
    public ChatClientAgentThreadType getThreadType() {
        return threadType;
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Collection<ChatMessageContent<?>>> getMessagesAsync() {
        return Mono.just(this.messages.getMessages());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating ChatClientAgentThread instances.
     */
    public static class Builder implements SemanticKernelBuilder<ChatClientAgentThread> {

        private String id = UUID.randomUUID().toString();
        private ChatClientAgentThreadType threadType;
        private List<ChatMessageContent<?>> initialMessages;

        public void setId(String id) {
            this.id = id;
        }

        /**
         * Sets the thread type.
         *
         * @param threadType The thread type to set.
         * @return This builder instance.
         */
        public Builder withThreadType(ChatClientAgentThreadType threadType) {
            this.threadType = threadType;
            return this;
        }

        /**
         * Sets the initial messages for the thread.
         *
         * @param messages The initial messages to add.
         * @return This builder instance.
         */
        public Builder withInitialMessages(List<ChatMessageContent<?>> messages) {
            this.initialMessages = messages;
            return this;
        }

        /**
         * Builds a new ChatClientAgentThread instance.
         *
         * @return A new ChatClientAgentThread instance.
         */
        public ChatClientAgentThread build() {
            return new ChatClientAgentThread(id, threadType, initialMessages);
        }
    }
}
