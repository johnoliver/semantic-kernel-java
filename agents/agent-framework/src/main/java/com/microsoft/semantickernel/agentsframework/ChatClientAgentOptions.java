// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework;

import com.microsoft.semantickernel.agentsframework.ai.AgentInvokeOptions;

/**
 * Represents metadata for a chat client agent, including its identifier, name, instructions, and
 * description.
 *
 * <p>
 * This class is used to encapsulate information about a chat client agent, such as its unique
 * identifier, display name, operational instructions, and a descriptive summary. It can be used to
 * store and transfer agent-related metadata within a chat application.
 * </p>
 */
public class ChatClientAgentOptions {

    private final String id;
    private final String name;
    private final String instructions;
    private final String description;
    private final AgentInvokeOptions chatOptions;

    public ChatClientAgentOptions(String id, String name, String instructions, String description,
        AgentInvokeOptions chatOptions) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.description = description;
        this.chatOptions = chatOptions;
    }

    /**
     * Gets the agent id.
     *
     * @return The agent id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the agent name.
     *
     * @return The agent name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the agent instructions.
     *
     * @return The agent instructions.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Gets the agent description.
     *
     * @return The agent description.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Gets the default chat options to use.
     *
     * @return The default chat options.
     */
    public AgentInvokeOptions getChatOptions() {
        return chatOptions;
    }


    /**
     * Creates a new builder for {@link ChatClientAgentOptions}.
     *
     * @return A new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ChatClientAgentOptions}.
     */
    public static class Builder {

        private String id;
        private String name;
        private String instructions;
        private String description;
        private AgentInvokeOptions chatOptions;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withChatOptions(AgentInvokeOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }

        public ChatClientAgentOptions build() {
            return new ChatClientAgentOptions(id, name, instructions, description, chatOptions);
        }
    }
}
