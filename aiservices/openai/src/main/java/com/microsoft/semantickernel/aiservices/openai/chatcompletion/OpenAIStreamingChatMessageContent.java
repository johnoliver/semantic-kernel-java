// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Represents the content of a chat message.
 *
 * @param <T> The type of the inner content.
 */
public class OpenAIStreamingChatMessageContent<T> extends OpenAIChatMessageContent<T> implements
    StreamingChatContent<T> {

    private final Optional<String> id;

    /**
     * Creates a new instance of the {@link OpenAIChatMessageContent} class.
     *
     * @param id           The id of the message.
     * @param authorRole   The author role that generated the content.
     * @param content      The content.
     * @param modelId      The model id.
     * @param innerContent The inner content.
     * @param encoding     The encoding.
     * @param metadata     The metadata.
     * @param toolCall     The tool call.
     */
    public OpenAIStreamingChatMessageContent(
        @Nullable
        String id,
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable T innerContent,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata,
        @Nullable List<OpenAIFunctionToolCall> toolCall) {
        super(
            id,
            authorRole,
            content,
            modelId,
            innerContent,
            encoding,
            metadata,
            toolCall);

        this.id = Optional.ofNullable(id);
    }

    @Override
    public Optional<String> getId() {
        return id;
    }
}
