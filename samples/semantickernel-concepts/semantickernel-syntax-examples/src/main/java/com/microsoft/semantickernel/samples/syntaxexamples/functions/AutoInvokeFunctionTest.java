// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.functions;


import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AutoInvokeFunctionTest {

    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");


    public static class SamplePlugin {

        @DefineKernelFunction(
            name = "Foo",
            description = "Applies the Foo operator to the input.",
            returnType = "String"
        )
        public String foo(
            @KernelFunctionParameter(
                name = "input",
                description = "The input to apply the Foo operator to.",
                type = String.class
            )
            String input) {
            return input.toUpperCase(Locale.ROOT).replaceAll("[AEIOU]", "*");
        }
    }

    public static void main(String[] args) {
        KernelPlugin plugin = KernelPluginFactory.createFromObject(new SamplePlugin(),
            "SamplePlugin");

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
            .endpoint(CLIENT_ENDPOINT)
            .buildAsyncClient();

        OpenAIChatCompletion chatClient = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(client)
            .withModelId("gpt-4o")
            .build();

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chatClient)
            .withPlugin(plugin)
            .build();

        KernelFunction<String> function = KernelFunction.<String>createFromPrompt(
                """
                    Calculate:
                    
                    Foo("{{$input}}")
                    
                    and
                    
                    Foo("{{$metadata}}")
                    """
                    .stripIndent()
            )
            .build();

        List<KernelFunction<?>> functions = kernel.getPlugins()
            .stream()
            .map(KernelPlugin::getFunctions)
            .map(Map::values)
            .flatMap(Collection::stream)
            .toList();

        ContextVariable<String> metadata = ContextVariable.of("foobar");

        FunctionResult<String> preResult = kernel
            .invokeAsync(function)
            .withToolCallBehavior(ToolCallBehavior.allowOnlyKernelFunctions(true, functions))
            .withArguments(
                KernelFunctionArguments.builder()
                    .withInput("hello there")
                    .withVariable("metadata", metadata)
                    .build())
            .withResultType(ContextVariableTypes.getGlobalVariableTypeForClass(String.class))
            .block();    // occur exception here, for invoke a self-make function

        System.out.println(preResult.getResult());
    }
}
