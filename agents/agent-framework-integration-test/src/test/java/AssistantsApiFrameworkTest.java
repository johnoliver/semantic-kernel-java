import com.azure.ai.openai.assistants.AssistantsAsyncClient;
import com.azure.ai.openai.assistants.AssistantsClientBuilder;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.agentsframework.ChatClientAgent;
import com.microsoft.semantickernel.agentsframework.ai.AgentResponseItem;
import com.microsoft.semantickernel.agentsframework.ai.AgentThread;
import com.microsoft.semantickernel.agentsframework.ai.ChatClient;
import com.microsoft.semantickernel.agentsframework.openai.AssistantChatClient;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

public class AssistantsApiFrameworkTest {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-35-turbo");


    public static void main(String[] args) {
        AssistantsAsyncClient client;

        if (AZURE_CLIENT_KEY != null) {
            client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new AssistantsClientBuilder()
                .credential(new KeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();
        }

        Assistant assistant = client.createAssistant(
            new AssistantCreationOptions(MODEL_ID)
                .setName("Joker")
                .setInstructions("You are good at telling jokes.")
        ).block();

        ChatClient chatClient = new AssistantChatClient(assistant, client);

        ChatClientAgent agent = new com.microsoft.semantickernel.agentsframework.openai.OpenAiChatClientAgent(
            client, chatClient);
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
