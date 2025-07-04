import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.agentsframework.ChatClientAgent;
import com.microsoft.semantickernel.agentsframework.ChatClientAgentOptions;
import com.microsoft.semantickernel.agentsframework.ai.AgentResponseItem;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.agentsframework.ai.ChatCompletionAgent;
import com.microsoft.semantickernel.agentsframework.chatcompletion.ChatCompletionChatClientAgent;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

public class AgentFrameworkTest {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-35-turbo");

    public static void main(String[] args) {
        OpenAIAsyncClient client;

        if (AZURE_CLIENT_KEY != null) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        ChatCompletionService chatGPT = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        ChatCompletionAgent chatCompletionAgent = new ChatCompletionAgent(chatGPT);
        ChatClientAgent agent = new ChatCompletionChatClientAgent(
            chatCompletionAgent,
            ChatClientAgentOptions.builder()
                .withName("Joker")
                .withInstructions("You are good at telling jokes.")
                .build()
        );

        AgentThread thread = agent.getNewThread().block();

        AgentResponseItem<ChatHistory> response = agent.invokeAsync(
            new ChatMessageContent<>(AuthorRole.USER, "Tell me a joke about a pirate."),
            thread
        ).block();

        response.getMessage().getMessages().forEach(System.out::println);

        response = agent.invokeAsync(
            new ChatMessageContent<>(AuthorRole.USER, "Now add some emojis to the joke."),
            thread
        ).block();

        response.getMessage().getMessages().forEach(System.out::println);

    }


}
