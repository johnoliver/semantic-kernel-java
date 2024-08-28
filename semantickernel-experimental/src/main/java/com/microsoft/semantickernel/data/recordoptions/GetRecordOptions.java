// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

/**
 * Options for getting a record.
 */
public class GetRecordOptions {

    private final boolean includeVectors;

    private final boolean wildcardKeyMatching;

    private GetRecordOptions(
        boolean includeVectors,
        boolean wildcardKeyMatching) {
        this.includeVectors = includeVectors;
        this.wildcardKeyMatching = wildcardKeyMatching;
    }

    public boolean isWildcardKeyMatching() {
        return wildcardKeyMatching;
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<GetRecordOptions> {

        private boolean includeVectors;
        private boolean wildcardKeyMatching = false;

        /**
         * Sets whether to include vectors.
         *
         * @param includeVectors whether to include vectors
         * @return GetRecordOptions.Builder
         */
        public Builder includeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        /**
         * Sets whether to use wildcard key matching. Default is false. Wildcard key matching allows
         * for matching multiple ids, for instance using "LIKE 'a%'" on a SQL query.
         *
         * @param wildcardKeyMatching whether to use wildcard key matching
         * @return GetRecordOptions.Builder
         */
        public Builder setWildcardKeyMatching(boolean wildcardKeyMatching) {
            this.wildcardKeyMatching = wildcardKeyMatching;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return GetRecordOptions
         */
        @Override
        public GetRecordOptions build() {
            return new GetRecordOptions(includeVectors, wildcardKeyMatching);
        }
    }

    /**
     * Gets whether to include vectors.
     *
     * @return whether to include vectors
     */
    public boolean includeVectors() {
        return includeVectors;
    }
}
