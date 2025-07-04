// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

public class ChatHistoryAgentThread extends BaseAgentThread {

    private ChatHistory chatHistory;

    /**
     * Constructor for ChatHistoryAgentThread.
     */
    public ChatHistoryAgentThread() {
        this(UUID.randomUUID().toString(), new ChatHistory());
    }

    /**
     * Constructor for ChatHistoryAgentThread.
     *
     * @param chatHistory The chat history.
     */
    public ChatHistoryAgentThread(@Nullable ChatHistory chatHistory) {
        this(UUID.randomUUID().toString(), chatHistory);
    }

    /**
     * Constructor for ChatHistoryAgentThread.
     *
     * @param id          The ID of the thread.
     * @param chatHistory The chat history.
     */
    public ChatHistoryAgentThread(String id, @Nullable ChatHistory chatHistory) {
        super(id);
        this.chatHistory = chatHistory != null ? chatHistory : new ChatHistory();
    }

    /**
     * Get the chat history.
     *
     * @return The chat history.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public Mono<String> createAsync() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
            chatHistory = new ChatHistory();
        }
        return Mono.just(id);
    }

    public Mono<Void> deleteAsync() {
        return Mono.fromRunnable(chatHistory::clear);
    }

    /**
     * Create a copy of the thread.
     *
     * @return A new instance of the thread.
     */
    public ChatHistoryAgentThread copy() {
        return new ChatHistoryAgentThread(this.id, new ChatHistory(chatHistory.getMessages()));
    }

    public Mono<Void> onNewMessageAsync(ChatMessageContent<?> newMessage) {
        return Mono.fromRunnable(() -> {
            chatHistory.addMessage(newMessage);
        });
    }

    public Mono<List<ChatMessageContent<?>>> getMessages() {
        return Mono.just(chatHistory.getMessages());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Mono<List<ChatMessageContent<?>>> onNewMessageAsync(ChatHistory newMessage) {
        return Mono.fromRunnable(() -> {
            chatHistory.addAll(newMessage);
        });
    }

    public static class Builder implements SemanticKernelBuilder<ChatHistoryAgentThread> {

        private String id;
        private ChatHistory chatHistory;

        /**
         * Set the ID of the thread.
         *
         * @param id The ID of the thread.
         * @return The builder instance.
         */
        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the chat history.
         *
         * @param chatHistory The chat history.
         * @return The builder instance.
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withChatHistory(ChatHistory chatHistory) {
            this.chatHistory = chatHistory;
            return this;
        }

        @Override
        public ChatHistoryAgentThread build() {
            return new ChatHistoryAgentThread(id, chatHistory);
        }
    }
}
