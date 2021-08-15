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
    @Getter
    private final UUID id;

    /**
     * Gets the JSON body of the document as a string, or null if the document does not exist.
     */
    @Getter
    private final String body;

    /**
     * Gets the current version of the document.
     */
    @Getter
    private final long version;

    public Document(
        @NonNull UUID id,
        String body,
        long version)
    {
        this.id = id;
        this.body = body;
        this.version = version;
    }
}
```

## License

Copyright 2016 Flavien Charlon

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
