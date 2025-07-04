package com.microsoft.semantickernel.agentsframework.openai;

import com.azure.ai.openai.assistants.AssistantsAsyncClient;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.microsoft.semantickernel.agentsframework.ai.AgentInvokeOptions;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.agentsframework.ai.ChatClient;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import reactor.core.publisher.Mono;

public class AssistantChatClient implements ChatClient {

    private final Assistant assistant;
    private final AssistantsAsyncClient client;

    public AssistantChatClient(Assistant assistant, AssistantsAsyncClient client) {
        this.assistant = assistant;
        this.client = client;
    }

    @Override
    public Mono<ChatHistory> getResponseAsync(
        ChatHistory messages,
        AgentInvokeOptions chatOptions) {
        return Mono.error(new UnsupportedOperationException());
    }

    public Mono<Void> getResponseAsync(AgentThread thread) {
        return client.createRun(thread.getId(), new CreateRunOptions(assistant.getId()))
            .flatMap(run -> {
                return checkRunUntilCompletion(thread, run.getId());
            });
    }

    private Mono<Void> checkRunUntilCompletion(AgentThread thread, String runId) {
        return client.getRun(thread.getId(), runId)
            .flatMap(run -> {
                if (run.getStatus() == RunStatus.COMPLETED) {
                    return Mono.empty();
                } else {
                    // Still in progress, poll again after delay
                    return Mono.delay(java.time.Duration.ofSeconds(1))
                        .then(checkRunUntilCompletion(thread, runId));
                }
            });
    }
}
