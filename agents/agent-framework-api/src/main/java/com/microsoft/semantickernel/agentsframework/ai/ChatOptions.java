// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework.ai;

/**
 * Options for chat completion requests.
 */
public class ChatOptions {

    private Double temperature;
    private Integer maxTokens;
    private Double topP;

    /**
     * Initializes a new instance of the {@link ChatOptions} class.
     */
    public ChatOptions() {
    }

    /**
     * Initializes a new instance of the {@link ChatOptions} class by cloning another instance.
     *
     * @param options The options to clone.
     */
    public ChatOptions(ChatOptions options) {
        if (options != null) {
            this.temperature = options.temperature;
            this.maxTokens = options.maxTokens;
            this.topP = options.topP;
        }
    }

    /**
     * Gets the temperature setting for the chat completion.
     *
     * @return The temperature setting.
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature setting for the chat completion.
     *
     * @param temperature The temperature setting to set.
     * @return This instance for method chaining.
     */
    public ChatOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Gets the maximum number of tokens to generate.
     *
     * @return The maximum number of tokens.
     */
    public Integer getMaxTokens() {
        return maxTokens;
    }

    /**
     * Sets the maximum number of tokens to generate.
     *
     * @param maxTokens The maximum number of tokens to set.
     * @return This instance for method chaining.
     */
    public ChatOptions setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * Gets the top-p setting for the chat completion.
     *
     * @return The top-p setting.
     */
    public Double getTopP() {
        return topP;
    }

    /**
     * Sets the top-p setting for the chat completion.
     *
     * @param topP The top-p setting to set.
     * @return This instance for method chaining.
     */
    public ChatOptions setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    /**
     * Creates a clone of this instance.
     *
     * @return A new instance with the same settings as this one.
     */
    public ChatOptions clone() {
        return new ChatOptions(this);
    }
}
