package com.microsoft.semantickernel.agentsframework.openai;

import com.azure.ai.openai.assistants.AssistantsAsyncClient;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.microsoft.semantickernel.agentsframework.ChatClientAgent;
import com.microsoft.semantickernel.agentsframework.ai.AgentInvokeOptions;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.agentsframework.ai.ChatClient;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.Locale;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OpenAiChatClientAgent extends ChatClientAgent {

    private final AssistantsAsyncClient client;

    public OpenAiChatClientAgent(
        AssistantsAsyncClient client,
        ChatClient chatClient
    ) {
        super(chatClient);
        this.client = client;
    }

    @Override
    public Mono<AgentThread> getNewThread() {
        return client.createThread(
                new AssistantThreadCreationOptions()
            )
            .map(thread -> {
                return new OpenAiAgentThread(thread, client);
            });
    }

    @Override
    protected Mono<ChatHistory> executeThread(
        AgentThread agentThread,
        ChatHistory threadMessages,
        AgentInvokeOptions chatOptions) {

        return ((AssistantChatClient) getChatClient())
            .getResponseAsync(agentThread)
            .then(Mono.<ChatHistory>defer(() -> {
                return agentThread.getMessages()
                    .map(m -> new ChatHistory(m));
            }));
    }

}
