// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.KernelContent;
import com.microsoft.semantickernel.services.KernelContentImpl;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageContentType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Represents the content of a chat message
 * <p>
 * This class defaults to a {@link ChatMessageContentType#TEXT} content type if none is specified.
 * However, if using this for text content, consider using
 * {@link com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageTextContent} and
 * its builders instead.
 *
 * @param <T> the type of the inner content within the messages
 */
public class ChatMessageContent<T> extends KernelContentImpl<T> {

    private final Optional<String> id;

    private final AuthorRole authorRole;
    @Nullable
    private final String content;
    @Nullable
    private final List<KernelContent<T>> items;
    @Nullable
    private final Charset encoding;
    private final ChatMessageContentType contentType;

    /**
     * Creates a new instance of the {@link ChatMessageContent} class. Defaults to
     * {@link ChatMessageContentType#TEXT} content type.
     *
     * @param authorRole the author role that generated the content
     * @param content    the content
     */
    public ChatMessageContent(
        AuthorRole authorRole,
        String content) {
        this(
            null,
            authorRole,
            content,
            null,
            null,
            null,
            null);
    }

    public ChatMessageContent(
        @Nullable
        String id,
        AuthorRole authorRole,
        String content) {
        this(
            id,
            authorRole,
            content,
            null,
            null,
            null,
            null);
    }

    /**
     * Creates a new instance of the {@link ChatMessageContent} class. Defaults to
     * {@link ChatMessageContentType#TEXT} content type.
     *
     * @param authorRole   the author role that generated the content
     * @param content      the content
     * @param modelId      the model id
     * @param innerContent the inner content
     * @param encoding     the encoding
     * @param metadata     the metadata
     */
    public ChatMessageContent(
        @Nullable
        String id,
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable T innerContent,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata) {
        this(id, authorRole, content, modelId, innerContent, encoding, metadata,
            ChatMessageContentType.TEXT);
    }

    /**
     * Creates a new instance of the {@link ChatMessageContent} class.
     *
     * @param authorRole   the author role that generated the content
     * @param content      the content
     * @param modelId      the model id
     * @param innerContent the inner content
     * @param encoding     the encoding
     * @param metadata     the metadata
     * @param contentType  the content type
     */
    public ChatMessageContent(
        @Nullable
        String id,
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable T innerContent,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata,
        ChatMessageContentType contentType) {
        super(innerContent, modelId, metadata);
        this.id = Optional.ofNullable(id);
        this.authorRole = authorRole;
        this.content = content;
        this.encoding = encoding != null ? encoding : StandardCharsets.UTF_8;
        this.items = null;
        this.contentType = contentType;
    }

    /**
     * Creates a new instance of the {@link ChatMessageContent} class.
     *
     * @param authorRole   the author role that generated the content
     * @param items        the items
     * @param modelId      the model id
     * @param innerContent the inner content
     * @param encoding     the encoding
     * @param metadata     the metadata
     * @param contentType  the content type
     */
    public ChatMessageContent(
        @Nullable
        String id,
        AuthorRole authorRole,
        @Nullable List<KernelContent<T>> items,
        String modelId,
        T innerContent,
        Charset encoding,
        FunctionResultMetadata metadata,
        ChatMessageContentType contentType) {
        super(innerContent, modelId, metadata);
        this.id = Optional.ofNullable(id);
        this.content = null;
        this.authorRole = authorRole;
        this.encoding = encoding != null ? encoding : StandardCharsets.UTF_8;
        if (items == null) {
            this.items = null;
        } else {
            this.items = new ArrayList<>(items);
        }
        this.contentType = contentType;
    }

    /**
     * Gets the author role that generated the content
     *
     * @return the author role that generated the content
     */
    public AuthorRole getAuthorRole() {
        return authorRole;
    }

    /**
     * Gets the content
     *
     * @return the content, which may be {@code null}
     */
    @Nullable
    @Override
    public String getContent() {
        return content;
    }

    /**
     * Gets the {@code KernelContent} items that comprise the content.
     *
     * @return the items, which may be {@code null}
     */
    @Nullable
    public List<KernelContent<T>> getItems() {
        if (items == null) {
            return null;
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the encoding of the content
     *
     * @return the encoding, which may be {@code null}
     */
    @Nullable
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * Gets the content type
     *
     * @return the content type
     */
    public ChatMessageContentType getContentType() {
        return contentType;
    }

    public Optional<String> getId() {
        return id;
    }

    @Override
    public String toString() {
        return content != null ? content : "";
    }

    public ChatMessageContent<T> withId(Supplier<String> id) {
        if (this.id.isPresent()) {
            return this;
        }

        return new ChatMessageContent<T>(
            id.get(),
            this.authorRole,
            this.content,
            this.getModelId(),
            this.getInnerContent(),
            this.encoding,
            this.getMetadata(),
            this.contentType
        );
    }
}
