// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import javax.annotation.Nullable;

/**
 * Options for invoking an agent.
 */
public class AgentInvokeOptions {

    @Nullable
    private final String additionalInstructions;

    /**
     * Default constructor for AgentInvokeOptions.
     */
    public AgentInvokeOptions() {
        this(null);
    }

    /**
     * Constructor for AgentInvokeOptions.
     *
     * @param kernelArguments        The arguments for the kernel function.
     * @param kernel                 The kernel to use.
     * @param additionalInstructions Additional instructions for the agent.
     * @param invocationContext      The invocation context.
     */
    public AgentInvokeOptions(
        @Nullable String additionalInstructions
    ) {
        this.additionalInstructions = additionalInstructions;
    }

    /**
     * Get additional instructions.
     *
     * @return The additional instructions.
     */
    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    /**
     * Builder for AgentInvokeOptions.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<AgentInvokeOptions> {

        private String additionalInstructions;

        /**
         * Set additional instructions.
         *
         * @param additionalInstructions The additional instructions.
         * @return The builder.
         */
        public Builder withAdditionalInstructions(String additionalInstructions) {
            this.additionalInstructions = additionalInstructions;
            return this;
        }

        /**
         * Build the object.
         *
         * @return a constructed object.
         */
        @Override
        public AgentInvokeOptions build() {
            return new AgentInvokeOptions(additionalInstructions);
        }
    }
}