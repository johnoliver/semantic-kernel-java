// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.semantickernel.agentsframework;

/**
 * Defines the type of a chat client agent thread.
 */
public enum ChatClientAgentThreadType {
    /**
     * The thread is a client-side memory thread that maintains messages in memory on the client.
     */
    CLIENT_MEMORY,

    /**
     * The thread is a service-side thread where messages are persisted in a service.
     */
    SERVICE
}
