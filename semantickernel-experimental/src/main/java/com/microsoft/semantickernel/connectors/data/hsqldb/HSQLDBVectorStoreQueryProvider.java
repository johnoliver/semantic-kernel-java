// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.hsqldb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreDefaultQueryProvider;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class HSQLDBVectorStoreQueryProvider extends
    JDBCVectorStoreDefaultQueryProvider implements JDBCVectorStoreQueryProvider {

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private HSQLDBVectorStoreQueryProvider(
        DataSource dataSource,
        String collectionsTable,
        String prefixForCollectionTables,
        int defaultVarCharLength) {
        super(
            dataSource,
            collectionsTable,
            prefixForCollectionTables,
            buildSupportedKeyTypes(defaultVarCharLength),
            buildSupportedDataTypes(defaultVarCharLength),
            buildSupportedVectorTypes(defaultVarCharLength));
    }

    private static Map<Class<?>, String> buildSupportedVectorTypes(int defaultVarCharLength) {
        HashMap<Class<?>, String> supportedVectorTypes = new HashMap<>();
        supportedVectorTypes.put(String.class, "VARCHAR(%d)");
        supportedVectorTypes.put(List.class, "VARCHAR(%d)");
        supportedVectorTypes.put(Collection.class, "VARCHAR(%d)");
        return supportedVectorTypes;
    }

    private static Map<Class<?>, String> buildSupportedDataTypes(int defaultVarCharLength) {
        HashMap<Class<?>, String> supportedDataTypes = new HashMap<>();
        supportedDataTypes.put(String.class, "VARCHAR(" + defaultVarCharLength + ")");
        supportedDataTypes.put(Integer.class, "INTEGER");
        supportedDataTypes.put(int.class, "INTEGER");
        supportedDataTypes.put(Long.class, "BIGINT");
        supportedDataTypes.put(long.class, "BIGINT");
        supportedDataTypes.put(Float.class, "REAL");
        supportedDataTypes.put(float.class, "REAL");
        supportedDataTypes.put(Double.class, "DOUBLE");
        supportedDataTypes.put(double.class, "DOUBLE");
        supportedDataTypes.put(Boolean.class, "BOOLEAN");
        supportedDataTypes.put(boolean.class, "BOOLEAN");
        supportedDataTypes.put(OffsetDateTime.class, "TIMESTAMPTZ");
        return supportedDataTypes;
    }

    private static HashMap<Class<?>, String> buildSupportedKeyTypes(int defaultVarCharLength) {
        HashMap<Class<?>, String> supportedKeyTypes = new HashMap<>();
        supportedKeyTypes.put(String.class, "VARCHAR(" + defaultVarCharLength + ")");
        return supportedKeyTypes;
    }

    private void setStatementValues(PreparedStatement statement, Object record,
        List<VectorStoreRecordField> fields) {
        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            try {
                Field recordField = record.getClass().getDeclaredField(field.getName());
                recordField.setAccessible(true);
                Object value = recordField.get(record);

                if (field instanceof VectorStoreRecordKeyField) {
                    statement.setObject(i + 1, (String) value);
                } else if (field instanceof VectorStoreRecordVectorField) {
                    Class<?> vectorType = record.getClass().getDeclaredField(field.getName())
                        .getType();

                    // If the vector field is other than String, serialize it to JSON
                    if (vectorType.equals(String.class)) {
                        statement.setObject(i + 1, value);
                    } else {
                        // Serialize the vector to JSON
                        statement.setObject(i + 1, new ObjectMapper().writeValueAsString(value));
                    }
                } else {
                    statement.setObject(i + 1, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException | SQLException e) {
                throw new SKException("Failed to set statement values", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Upserts records into the collection.
     *
     * @param collectionName   the collection name
     * @param records          the records to upsert
     * @param recordDefinition the record definition
     * @param options          the upsert options
     * @throws SKException if the upsert fails
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    // SQL query is generated dynamically with valid identifiers
    public void upsertRecords(
        String collectionName,
        List<?> records,
        VectorStoreRecordDefinition recordDefinition,
        UpsertRecordOptions options) {
        validateSQLidentifier(getCollectionTableName(collectionName));

        List<VectorStoreRecordField> fields = recordDefinition.getAllFields();

        String keyName = recordDefinition.getKeyField().getName();

        String updater = fields
            .stream()
            .map(VectorStoreRecordField::getName)
            .map(it -> "t." + it + "=vals." + it)
            .collect(Collectors.joining(","));

        String setter = fields
            .stream()
            .map(VectorStoreRecordField::getName)
            .map(it -> "vals." + it)
            .collect(Collectors.joining(","));

        String query = "MERGE INTO " + getCollectionTableName(collectionName)
            + " AS t USING (VALUES (" + getWildcardString(fields.size()) + "))"
            + " AS vals(" + getQueryColumnsFromFields(fields) + ")"
            + " ON t." + keyName + "=vals." + keyName
            + " WHEN MATCHED THEN UPDATE SET " + updater
            + " WHEN NOT MATCHED THEN INSERT VALUES " + setter;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            for (Object record : records) {
                setStatementValues(statement, record, recordDefinition.getAllFields());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new SKException("Failed to upsert records", e);
        }
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
        extends JDBCVectorStoreDefaultQueryProvider.Builder {

        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;
        private int defaultVarCharLength = 255;

        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the collections table name.
         *
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public Builder withCollectionsTable(String collectionsTable) {
            this.collectionsTable = validateSQLidentifier(collectionsTable);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         *
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder withPrefixForCollectionTables(String prefixForCollectionTables) {
            this.prefixForCollectionTables = validateSQLidentifier(prefixForCollectionTables);
            return this;
        }

        public Builder setDefaultVarCharLength(int defaultVarCharLength) {
            this.defaultVarCharLength = defaultVarCharLength;
            return this;
        }

        public HSQLDBVectorStoreQueryProvider build() {
            if (dataSource == null) {
                throw new SKException("DataSource is required");
            }

            return new HSQLDBVectorStoreQueryProvider(
                dataSource,
                collectionsTable,
                prefixForCollectionTables,
                defaultVarCharLength);
        }

    }
}
