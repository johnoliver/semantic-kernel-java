package com.microsoft.semantickernel.agentsframework.openai;

import com.azure.ai.openai.assistants.AssistantsAsyncClient;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;
import java.util.Locale;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OpenAiAgentThread implements AgentThread {

    private final AssistantThread thread;
    private final AssistantsAsyncClient client;

    public OpenAiAgentThread(AssistantThread thread, AssistantsAsyncClient client) {
        this.thread = thread;
        this.client = client;
    }

    @Override
    public String getId() {
        return thread.getId();
    }

    @Override
    public Mono<List<ChatMessageContent<?>>> onNewMessageAsync(ChatHistory newMessage) {
        return Flux.fromIterable(newMessage)
            .concatMap(message -> {
                if (message.getId().isPresent()) {
                    return Mono.<ChatMessageContent<?>>just(message);
                }

                return client
                    .createMessage(
                        thread.getId(),
                        new ThreadMessageOptions(
                            MessageRole.fromString(
                                message.getAuthorRole().name().toLowerCase(Locale.ROOT)),
                            message.getContent()
                        )
                    )
                    .<ChatMessageContent<?>>flatMapMany(msg -> {
                        return Flux.fromIterable(msg.getContent())
                            .map(it ->
                                new ChatMessageContent<>(
                                    msg.getId(),
                                    AuthorRole.valueOf(
                                        msg.getRole().toString().toUpperCase(Locale.ROOT)),
                                    ((MessageTextContent) it).getText().getValue()
                                )
                            );
                    });
            })
            .collectList();
    }

    @Override
    public Mono<List<ChatMessageContent<?>>> getMessages() {
        return client.listMessages(thread.getId(), 100, ListSortOrder.ASCENDING, null, null)
            .flatMapMany(message -> {
                return Flux.fromIterable(message.getData());
            })
            .<ChatMessageContent<?>>concatMap(message -> {
                    return Flux.fromIterable(message.getContent())
                        .map(it ->
                            new ChatMessageContent<>(
                                message.getId(),
                                AuthorRole.valueOf(
                                    message.getRole().toString().toUpperCase(Locale.ROOT)),
                                ((MessageTextContent) it).getText().getValue()
                            )
                        );
                }
            )
            .collectList();
    }

    @Override
    public Mono<ChatMessageContent<?>> createMessage(AuthorRole authorRole, String content) {
        return client
            .createMessage(
                thread.getId(),
                new ThreadMessageOptions(
                    MessageRole.fromString(
                        authorRole.name().toLowerCase(Locale.ROOT)),
                    content
                )
            )
            .<ChatMessageContent<?>>flatMapMany(message -> {
                    return Flux.fromIterable(message.getContent())
                        .map(it ->
                            new ChatMessageContent<>(
                                message.getId(),
                                AuthorRole.valueOf(
                                    message.getRole().toString().toUpperCase(Locale.ROOT)),
                                ((MessageTextContent) it).getText().getValue()
                            )
                        );
                }
            )
            .single();
    }

}
