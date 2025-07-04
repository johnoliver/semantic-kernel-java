// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a message in a chat conversation.
 */
public class ChatMessage {

    private final AuthorRole role;
    private final String content;
    private final Map<String, Object> metadata;
    private String authorName;

    /**
     * Initializes a new instance of the {@link ChatMessage} class.
     *
     * @param role    The role of the message sender.
     * @param content The content of the message.
     */
    public ChatMessage(AuthorRole role, String content) {
        this(role, content, null, null);
    }

    /**
     * Initializes a new instance of the {@link ChatMessage} class.
     *
     * @param role       The role of the message sender.
     * @param content    The content of the message.
     * @param authorName The name of the author.
     */
    public ChatMessage(AuthorRole role, String content, String authorName) {
        this(role, content, authorName, null);
    }

    /**
     * Initializes a new instance of the {@link ChatMessage} class.
     *
     * @param role       The role of the message sender.
     * @param content    The content of the message.
     * @param authorName The name of the author.
     * @param metadata   Additional metadata associated with the message.
     */
    public ChatMessage(AuthorRole role, String content, String authorName,
        Map<String, Object> metadata) {
        this.role = role;
        this.content = content;
        this.authorName = authorName;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    /**
     * Gets the role of the message sender.
     *
     * @return The role of the message sender.
     */
    public AuthorRole getRole() {
        return role;
    }

    /**
     * Gets the content of the message.
     *
     * @return The content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the name of the author.
     *
     * @return The name of the author.
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Sets the name of the author.
     *
     * @param authorName The name of the author to set.
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Gets the metadata associated with the message.
     *
     * @return An unmodifiable view of the metadata associated with the message.
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Adds metadata to the message.
     *
     * @param key   The metadata key.
     * @param value The metadata value.
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}
