# PgDoc

PgDoc is a library for using PostgreSQL as a JSON document store.

## Setup

Run the [SQL script in `src/main/resources/pgdoc_core.sql`](src/main/resources/pgdoc_core.sql) to create the required table and functions in the database.

## Document structure

Documents are stored in a single table with three columns:

* `body`: The JSON data itself. It can include nested objects and nested arrays. This is a `jsonb` column, and it is possible to query and index any nested field. This column is `NULL` if the document has been deleted.
* `id`: The unique identifier of the document, of type `uuid`.
* `version`: The current version of the document. Any update to a document must specify the version being updated. This ensures documents can be read by the application, and later updated in a safe fashion.

In Java, documents are represented by the `Document` class:

```java
public class Document {

    /**
     * Gets the unique identifier of the document.
     */
    public UUID getId();

    /**
     * Gets the JSON body of the document as a string, or null if the document does not exist.
     */
    public String getBody();

    /**
     * Gets the current version of the document.
     */
    public long getVersion();
}
```

## Initialization

Start by creating an instance of the `SqlDocumentStore` class.

```java
Connection connection = DriverManager.getConnection(connectionString, props);
DocumentStore documentStore = new SqlDocumentStore(connection);
```

## Retrieving a document

Use the `getDocuments` method (or `getDocument`) to retrieve one or more documents by ID.

```java
Document document = documentStore.getDocument(documentId);
```

Attempting to retrieve a document that doesn't exist will return a `Document` object with a `body` set to null. This can be either because the document has not been created yet, or because it has been deleted.

## Updating

Updating a document is done in three steps:

1. Retrieve the current document.
2. Update the document in the application.
3. Call `updateDocuments` to store the updated document in the database.

PgDoc relies on optimistic concurrency to guarantee consistency. When a document is updated, the version of the document being updated must be supplied. If it doesn't match the current version of the document, the update will fail and an `UpdateConflictException` will be thrown.

After a successful update, the `version` of all the updated documents is incremented by one. 

```java
// Retrieve the document to update
Document document = documentStore.getDocument(documentId);

// Create the new version of the document with an updated body
Document updatedDocument = new Document(
    document.Id,
    "{'key':'updated_value'}",
    document.Version
);

documentStore.updateDocuments(updatedDocument);
```

It is also possible to atomically update several documents at once by passing multiple documents to `updateDocuments`. If any of the documents fails the version check, none of the documents will be updated.

## Deleting and creating documents

PgDoc has no concept of inserting or deleting. They are both treated as an update.

Creating a new document is equivalent to updating a document from a null `body` to a non-null `body`. The initial `version` of a document that has never been created is always zero.

```java
// Generate a random ID for the new document
UUID documentId = UUID.randomUUID();

// Create the new document
Document newDocument = new Document(
    documentId,
    "{'key':'inital_value'}",
    0
);

documentStore.updateDocuments(updatedDocument);
```

Deleting a document is equivalent to updating a document from a non-null `body` to a null `body`.

```java
// Retrieve the document to delete
Document document = documentStore.getDocument(documentId);

// Create the new version of the document with a null body
Document deletedDocument = new Document(
    document.Id,
    null,
    document.Version
);

documentStore.updateDocuments(deletedDocument);
```

## License

Copyright 2016 Flavien Charlon

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
