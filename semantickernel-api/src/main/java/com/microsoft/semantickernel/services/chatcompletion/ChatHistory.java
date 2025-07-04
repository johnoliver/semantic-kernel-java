// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageTextContent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Provides a history of messages between the User, Assistant and System
 */
public class ChatHistory implements Iterable<ChatMessageContent<?>> {

    // Holds messages in FIFO order
    private final List<ChatMessageContent<?>> chatMessageContents;
    // Object used for synchronization
    private final Object syncLock = new Object();

    /**
     * The default constructor
     */
    public ChatHistory() {
        this((String) null);
    }

    /**
     * Constructor that adds the given system instructions to the chat history.
     *
     * @param instructions The instructions to add to the chat history
     */
    public ChatHistory(@Nullable String instructions) {
        this.chatMessageContents = Collections.synchronizedList(new ArrayList<>());
        if (instructions != null) {
            this.chatMessageContents.add(ChatMessageTextContent.systemMessage(instructions));
        }
    }

    private static <T extends ChatMessageContent<?>> SortedSet<T> createSet(
        List<T> chatMessageContents) {

        TreeSet<T> set = new TreeSet<>(
            (o1, o2) -> o1.getId().get().compareTo(o2.getId().get())
        );

        set.addAll(chatMessageContents);
        return set;
    }

    /**
     * Constructor that adds the given chat message contents to the chat history.
     *
     * @param chatMessageContents The chat message contents to add to the chat history
     */
    public ChatHistory(List<? extends ChatMessageContent<?>> chatMessageContents) {
        assertIdsAreUnique(chatMessageContents);
        this.chatMessageContents = Collections
            .synchronizedList(new ArrayList<>(chatMessageContents));
    }

    private boolean containsId(Optional<String> id) {
        if (!id.isPresent()) {
            return false;
        }
        return chatMessageContents
            .stream()
            .anyMatch(message -> message.getId().equals(id));
    }

    private void assertIdsAreUnique(List<? extends ChatMessageContent<?>> chatMessageContents) {
        chatMessageContents
            .stream()
            .map(ChatMessageContent::getId)
            .map(id -> Tuples.of(id, 1))
            .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2, Integer::sum))
            .forEach((id, count) -> {
                if (count > 1) {
                    throw new IllegalArgumentException(
                        "ChatMessageContent IDs must be unique. Found duplicate ID: " + id);
                }
            });
    }

    public ChatHistory(ChatMessageContent<?> message) {
        this.chatMessageContents = Collections.singletonList(message);
    }

    /**
     * Get the chat history
     *
     * @return List of messages in the chat
     */
    public List<ChatMessageContent<?>> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(chatMessageContents));
    }

    /**
     * Get last message
     *
     * @return The most recent message in chat
     */
    public Optional<ChatMessageContent<?>> getLastMessage() {
        if (chatMessageContents.isEmpty()) {
            return Optional.empty();
        }
        return Optional
            .of(chatMessageContents.get(chatMessageContents.size() - 1));
    }


    /**
     * Create an {@code Iterator} from the chat history.
     *
     * @return An {@code Iterator} from the chat history.
     */
    @Override
    public Iterator<ChatMessageContent<?>> iterator() {
        return chatMessageContents.iterator();
    }

    /**
     * Perform the given action for each message in the chat history
     *
     * @param action The action to perform for each message in the chat history
     */
    @Override
    public void forEach(Consumer<? super ChatMessageContent<?>> action) {
        chatMessageContents.forEach(action);
    }

    /**
     * Create a {@code Spliterator} from the chat history
     *
     * @return A {@code Spliterator} from the chat history
     */
    @Override
    public Spliterator<ChatMessageContent<?>> spliterator() {
        return chatMessageContents.spliterator();
    }

    /**
     * Add a message to the chat history
     *
     * @param authorRole The role of the author of the message
     * @param content    The content of the message
     * @param encoding   The encoding of the message
     * @param metadata   The metadata of the message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addMessage(AuthorRole authorRole, String content, Charset encoding,
        FunctionResultMetadata<?> metadata) {
        synchronized (syncLock) {
            chatMessageContents.add(
                ChatMessageTextContent.builder()
                    .withAuthorRole(authorRole)
                    .withContent(content)
                    .withEncoding(encoding)
                    .withMetadata(metadata)
                    .build());
            return this;
        }
    }

    /**
     * Add a message to the chat history
     *
     * @param authorRole The role of the author of the message
     * @param content    The content of the message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addMessage(AuthorRole authorRole, String content) {
        synchronized (syncLock) {
            chatMessageContents.add(
                ChatMessageTextContent.builder()
                    .withAuthorRole(authorRole)
                    .withContent(content)
                    .build());
            return this;
        }
    }

    /**
     * Add a message to the chat history
     *
     * @param content The content of the message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addMessage(ChatMessageContent<?> content) {
        synchronized (syncLock) {
            if (containsId(content.getId())) {
                throw new IllegalArgumentException(
                    "ChatMessageContent with ID " + content.getId()
                        + " already exists in chat history");
            }
            chatMessageContents.add(content);
            return this;
        }
    }

    /**
     * Add a user message to the chat history
     *
     * @param content The content of the user message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addUserMessage(String content) {
        return addMessage(AuthorRole.USER, content);
    }

    /**
     * Add an assistant message to the chat history
     *
     * @param content The content of the assistant message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addAssistantMessage(String content) {
        return addMessage(AuthorRole.ASSISTANT, content);
    }

    /**
     * Add an system message to the chat history
     *
     * @param content The content of the system message
     * @return {@code this} ChatHistory
     */
    public ChatHistory addSystemMessage(String content) {
        return addMessage(AuthorRole.SYSTEM, content);
    }

    /**
     * Clear the chat history
     */
    public void clear() {
        synchronized (syncLock) {
            chatMessageContents.clear();
        }
    }

    /**
     * Add all messages to the chat history
     *
     * @param messages The messages to add to the chat history
     * @return {@code this} ChatHistory
     */
    public ChatHistory addAll(List<ChatMessageContent<?>> messages) {
        if (messages == null || messages.isEmpty()) {
            return this;
        }

        assertIdsAreUnique(messages);

        synchronized (syncLock) {
            messages
                .stream()
                .filter(message -> !containsId(message.getId()))
                .forEach(chatMessageContents::add);

            return this;
        }
    }

    public boolean isEmpty() {
        return chatMessageContents.isEmpty();
    }

    public void add(ChatMessageContent chatMessageContent) {
        synchronized (syncLock) {
            chatMessageContents.add(chatMessageContent);
        }
    }

    /**
     * Add all messages from the given chat history to this chat history
     *
     * @param value The chat history to add to this chat history
     */
    public void addAll(ChatHistory value) {
        addAll(value.getMessages());
    }

    /**
     * Add all messages that are not already present in the chat history
     *
     * @param messages The messages to add to the chat history if not already present
     */
    public void addAllIfMissing(List<ChatMessageContent<?>> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        synchronized (syncLock) {
            for (ChatMessageContent<?> message : messages) {
                if (!containsMessage(message)) {
                    chatMessageContents.add(message);
                }
            }
        }
    }

    /**
     * Checks if the chat history contains a message with the same content and author or message id
     *
     * @param message The message to check
     * @return true if a message with the same content and author exists, false otherwise
     */
    public boolean containsMessage(ChatMessageContent<?> message) {
        if (message == null) {
            return false;
        }

        synchronized (syncLock) {
            return chatMessageContents
                .stream()
                .anyMatch(existingMessage ->
                    existingMessage.getId() == message.getId() ||
                        existingMessage.getAuthorRole() == message.getAuthorRole() &&
                            existingMessage.getContent().equals(message.getContent())
                );
        }
    }

    /**
     * Asserts that the provided messages match the first n messages in the chat history.
     *
     * @param messages
     * @return
     */
    public boolean assertHeadMatches(List<ChatMessageContent<?>> messages) {
        if (messages == null || messages.isEmpty()) {
            return true;
        }

        synchronized (syncLock) {
            if (messages.size() > chatMessageContents.size()) {
                return false;
            }

            for (int i = 0; i < messages.size(); i++) {
                ChatMessageContent<?> expected = messages.get(i);
                ChatMessageContent<?> actual = chatMessageContents.get(i);

                if (expected.getAuthorRole() != actual.getAuthorRole() ||
                    !expected.getContent().equals(actual.getContent())) {
                    return false;
                }
            }
            return true;
        }
    }

    public void addIfMissing(ChatMessageContent<?> message) {
        synchronized (syncLock) {
            if (!containsMessage(message)) {
                chatMessageContents.add(message);
            }
        }
    }
}
