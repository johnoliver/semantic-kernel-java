// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework;

import com.microsoft.semantickernel.agentsframework.ai.Agent;
import com.microsoft.semantickernel.agentsframework.ai.AgentInvokeOptions;
import com.microsoft.semantickernel.agentsframework.ai.AgentResponseItem;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.agentsframework.ai.ChatClient;
import com.microsoft.semantickernel.agentsframework.ai.ChatOptions;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents an agent that can be invoked using a chat client.
 */
public abstract class ChatClientAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientAgent.class.getName());

    private final ChatClientAgentOptions agentOptions;
    private final ChatClient chatClient;

    public ChatClientAgent(ChatClient chatClient) {
        this(chatClient, null);
    }

    /**
     * Initializes a new instance of the {@link ChatClientAgent} class.
     *
     * @param chatClient The chat client to use for invoking the agent.
     * @param options    Optional agent options to configure the agent.
     * @param logger     Logger to use for logging.
     */
    public ChatClientAgent(
        ChatClient chatClient,
        ChatClientAgentOptions options) {
        if (chatClient == null) {
            throw new IllegalArgumentException("ChatClient cannot be null");
        }

        // Options must be cloned since ChatClientAgentOptions is mutable.
        this.agentOptions = options;

        // Use the chat client directly
        this.chatClient = chatClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.agentOptions != null && this.agentOptions.getId() != null ?
            this.agentOptions.getId() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.agentOptions != null ? this.agentOptions.getName() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return this.agentOptions != null ? this.agentOptions.getDescription() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInstructions() {
        return this.agentOptions != null ? this.agentOptions.getInstructions() : null;
    }

    /**
     * Gets the default {@link ChatOptions} used by the agent.
     *
     * @return The default chat options or null if not specified.
     */
    protected AgentInvokeOptions getChatOptions() {
        return this.agentOptions != null ? this.agentOptions.getChatOptions() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AgentThread> getNewThread() {
        return Mono.just(ChatClientAgentThread.builder().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        ChatHistory messages,
        AgentThread thread,
        @Nullable AgentInvokeOptions options) {
        if (messages == null) {
            throw new IllegalArgumentException("Messages cannot be null");
        }

        if (thread == null) {
            throw new IllegalArgumentException("Thread must not be null");
        }

        // Use prepareThreadAndMessages to consolidate the setup
        return prepareThreadAndMessages(thread, messages, options)
            .flatMap(result -> {
                AgentThread chatClientThread = result.thread;
                AgentInvokeOptions chatOptions = result.chatOptions;
                ChatHistory threadMessages = result.threadMessages;

                String agentName = getName() != null ? getName() : getId();

                LOGGER.info("Agent {0} ({1}) is invoking chat client}",
                    new Object[]{this.getId(), agentName});

                return getNewMessages(threadMessages, chatClientThread)
                    .flatMap(newMessages ->
                        notifyThreadOfNewMessages(chatClientThread, new ChatHistory(newMessages)))
                    .then(Mono.defer(() -> {
                        // Get the response from the chat client
                        return executeThread(chatClientThread, threadMessages, chatOptions)
                            .flatMap(chatResponse -> {
                                LOGGER.info(
                                    "Agent {0} ({1}) invoked chat client {2} messages",
                                    new Object[]{this.getId(), agentName,
                                        chatResponse.getMessages().size()});

                                // Notify thread of new messages
                                return notifyThreadOfNewMessages(chatClientThread, chatResponse)
                                    .then(Mono.defer(() -> {
                                            return chatClientThread.getMessages();
                                        })
                                    );
                            })
                            .map(newResponseMessages -> {
                                String id = null;
                                if (thread != null) {
                                    id = thread.getId();
                                }

                                // Create and return the agent response item
                                return new AgentResponseItem<>(new ChatHistory(newResponseMessages),
                                    id);
                            });
                    }));
            });
    }

    private static Mono<List<ChatMessageContent<?>>> getNewMessages(ChatHistory threadMessages,
        AgentThread chatClientThread) {
        return chatClientThread.getMessages()
            .map(messages -> {
                if (!threadMessages.assertHeadMatches(messages)) {
                    throw new IllegalArgumentException("Messages do not match");
                }

                List<ChatMessageContent<?>> newMessages = threadMessages.getMessages()
                    .subList(messages.size(),
                        threadMessages.getMessages().size());
                return newMessages;
            });
    }

    protected Mono<ChatHistory> executeThread(AgentThread agentThread, ChatHistory threadMessages,
        AgentInvokeOptions chatOptions) {
        return this.chatClient.getResponseAsync(threadMessages, chatOptions);
    }

    @Override
    public Mono<Void> notifyThreadOfNewMessageAsync(AgentThread thread,
        ChatMessageContent<?> newMessage) {
        // TODO

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        ChatMessageContent<?> message,
        AgentThread thread,
        AgentInvokeOptions options) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        return invokeAsync(new ChatHistory(message), thread, options);
    }

    public ChatClient getChatClient() {
        return chatClient;
    }
    /**
     * Updates the thread with the conversation ID.
     *
     * @param thread         The thread to update.
     * @param conversationId The conversation ID to set.
    private void updateThreadWithConversationId(
    ChatClientAgentThread thread,
    String conversationId) {
    if (thread != null && conversationId != null) {
    thread.setId(conversationId);
    }
    }
     */

    /**
     * Notifies the thread of new messages.
     *
     * @param thread   The thread to notify.
     * @param messages The new messages.
     * @return A CompletableFuture that completes when notification is complete.
     */
    private Mono<List<ChatMessageContent<?>>> notifyThreadOfNewMessages(AgentThread thread,
        ChatHistory messages) {
        if (thread != null && messages != null && !messages.isEmpty()) {
            return thread.onNewMessageAsync(messages);
        }
        return Mono.empty();
    }

    /**
     * Prepares the thread and messages for agent invocation.
     *
     * @param thread   The thread to use, or null to create a new one.
     * @param messages The messages to include in the invocation.
     * @param options  Additional options for the invocation.
     * @return A CompletableFuture that will complete with the preparation result.
     */
    private Mono<ThreadPreparationResult> prepareThreadAndMessages(
        AgentThread thread,
        ChatHistory messages,
        AgentInvokeOptions options) {

        // Prepare chat options
        AgentInvokeOptions chatOptions = getChatOptions();
        if (chatOptions != null) {
            chatOptions = new AgentInvokeOptions();
        }

        Flux<ChatMessageContent<?>> newMessageMono = Flux.empty();

        // Add instructions as system message if available
        if (getInstructions() != null && !getInstructions().isEmpty()) {
            newMessageMono = newMessageMono.concatWith(
                thread.createMessage(AuthorRole.SYSTEM, getInstructions())
            );
        }

        // Add additional instructions if specified in options
        if (options != null && options.getAdditionalInstructions() != null
            && !options.getAdditionalInstructions().isEmpty()) {
            newMessageMono = newMessageMono.concatWith(
                thread.createMessage(AuthorRole.SYSTEM, options.getAdditionalInstructions())
            );
        }
        AgentInvokeOptions finalChatOptions = chatOptions;

        return newMessageMono
            .collectList()
            .map(ChatHistory::new)
            .flatMap(threadMessages -> {
                return thread.getMessages()
                    .map(tmessages -> {
                        threadMessages.addAllIfMissing(tmessages);

                        // Add the provided messages
                        threadMessages.addAll(messages);

                        return new ThreadPreparationResult(thread, finalChatOptions,
                            threadMessages);

                    });
            });
    }

    /**
     * Helper class to store thread preparation results.
     */
    private static class ThreadPreparationResult {

        public final AgentThread thread;
        public final AgentInvokeOptions chatOptions;
        public final ChatHistory threadMessages;

        public ThreadPreparationResult(AgentThread thread, AgentInvokeOptions chatOptions,
            ChatHistory threadMessages) {
            this.thread = thread;
            this.chatOptions = chatOptions;
            this.threadMessages = threadMessages;
        }
    }
}
