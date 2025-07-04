// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agentsframework.ai;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import reactor.core.publisher.Mono;

public abstract class BaseAgentThread implements AgentThread {

    protected String id;

    public BaseAgentThread() {
    }

    public BaseAgentThread(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Mono<ChatMessageContent<?>> createMessage(AuthorRole authorRole, String content) {
        return Mono.just(new ChatMessageContent<>(id, authorRole, content));
    }
}
