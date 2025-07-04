package com.microsoft.semantickernel.agentsframework.chatcompletion;

import com.microsoft.semantickernel.agentsframework.ChatClientAgent;
import com.microsoft.semantickernel.agentsframework.ChatClientAgentOptions;
import com.microsoft.semantickernel.agentsframework.ai.ChatClient;

public class ChatCompletionChatClientAgent extends ChatClientAgent {

    public ChatCompletionChatClientAgent(ChatClient chatClient) {
        super(chatClient);
    }

    public ChatCompletionChatClientAgent(ChatClient chatClient, ChatClientAgentOptions options) {
        super(chatClient, options);
    }
}
