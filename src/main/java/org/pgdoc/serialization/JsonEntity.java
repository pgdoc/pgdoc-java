package org.pgdoc.serialization;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import org.pgdoc.Document;

public class JsonEntity<T> {

    private static final Gson gson = new Gson();

    public JsonEntity(@NonNull EntityId id, T entity, long version) {
        this.id = id;
        this.entity = entity;
        this.version = version;
    }

    @Getter
    private final EntityId id;

    @Getter
    private final T entity;

    @Getter
    private final long version;

    public static <T> JsonEntity<T> fromDocument(Class<T> type, Document document) {

        return new JsonEntity<T>(
            new EntityId(document.getId()),
            document.getBody() != null
                ? gson.fromJson(document.getBody(), type)
                : null,
            document.getVersion());
    }

    public Document toDocument() {

        return new Document(
            this.getId().getValue(),
            this.getEntity() != null
                ? gson.toJson(this.getEntity())
                : null,
            this.getVersion());
    }

    public JsonEntity<T> modify(T newValue) {
        return new JsonEntity<T>(getId(), newValue, getVersion());
    }

    public static <T> JsonEntity<T> create(T value, EntityId type) {
        return new JsonEntity<>(type, value, 0);
    }
}
