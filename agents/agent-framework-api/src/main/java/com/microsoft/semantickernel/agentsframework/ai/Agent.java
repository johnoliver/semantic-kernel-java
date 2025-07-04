// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 * Interface for a semantic kernel agent.
 */
public interface Agent {

    /**
     * Gets the agent's ID.
     *
     * @return The agent's ID
     */
    String getId();

    /**
     * Gets the agent's name.
     *
     * @return The agent's name
     */
    String getName();

    /**
     * Gets the agent's description.
     *
     * @return The agent's description
     */
    String getDescription();

    /**
     * Gets the instructions for the agent (optional).
     *
     * @return The instructions for the agent.
     */
    String getInstructions();

    /**
     * Get a new {@link AgentThread} instance that is compatible with the agent.
     *
     * @return A new {@link AgentThread} instance.
     *
     * <p>
     * If an agent supports multiple thread types, this method should return the default thread type
     * for the agent or whatever the agent was configured to use.
     * </p>
     * <p>
     * If the thread needs to be created via a service call it would be created on first use.
     * </p>
     */
    public abstract Mono<AgentThread> getNewThread();


    /**
     * Run the agent with no message assuming that all required instructions are already provided to
     * the agent or on the thread.
     *
     * @param thread  The conversation thread to continue with this invocation. If not provided,
     *                creates a new thread. The thread will be mutated with the provided messages
     *                and agent response.
     * @param options Optional parameters for agent invocation.
     * @return A {@link CompletableFuture} containing the {@link ChatResponse} with the agent's
     * response.
     */
    default Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        AgentThread thread,
        AgentInvokeOptions options
    ) {
        return invokeAsync(
            (ChatHistory) null,
            thread,
            options
        );
    }

    /**
     * Run the agent with the provided message and arguments.
     *
     * @param message The message to pass to the agent.
     * @param thread  The conversation thread to continue with this invocation. If not provided,
     *                creates a new thread. The thread will be mutated with the provided messages
     *                and agent response.
     * @param options Optional parameters for agent invocation.
     * @return A {@link CompletableFuture} containing the {@link ChatResponse} with the agent's
     * response.
     */
    default Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        String message,
        AgentThread thread,
        AgentInvokeOptions options) {
        return invokeAsync(
            new ChatMessageContent<>(AuthorRole.USER, message),
            thread,
            options
        );
    }


    /**
     * Invokes the agent with the given message and thread.
     *
     * @param message The message to process
     * @param thread  The agent thread to use
     * @return A Mono containing the agent response
     */
    default Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        @Nullable ChatMessageContent<?> message,
        AgentThread thread) {
        return invokeAsync(
            message,
            thread,
            null
        );
    }

    /**
     * Invokes the agent with the given message, thread, and options.
     *
     * @param message The message to process
     * @param thread  The agent thread to use
     * @param options The options for invoking the agent
     * @return A Mono containing the agent response
     */
    default Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        @Nullable ChatMessageContent<?> message,
        @Nullable AgentThread thread,
        @Nullable AgentInvokeOptions options) {
        if (message == null) {
            return invokeAsync(
                new ChatHistory(),
                thread,
                options
            );
        } else {
            return invokeAsync(
                new ChatHistory(message),
                thread,
                options
            );
        }
    }

    /**
     * Invoke the agent with the given chat history.
     *
     * @param messages The chat history to process
     * @param thread   The agent thread to use
     * @param options  The options for invoking the agent
     * @return A Mono containing the agent response
     */
    Mono<AgentResponseItem<ChatHistory>> invokeAsync(
        ChatHistory messages,
        @Nullable AgentThread thread,
        @Nullable AgentInvokeOptions options);

    /**
     * Notifies the agent of a new message.
     *
     * @param thread The agent thread to use
     */
    Mono<Void> notifyThreadOfNewMessageAsync(AgentThread thread, ChatMessageContent<?> newMessage);
}