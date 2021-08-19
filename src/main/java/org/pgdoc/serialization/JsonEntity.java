/*
 * Copyright 2016 Flavien Charlon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pgdoc.serialization;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.pgdoc.Document;

import java.lang.reflect.Type;

/**
 * The <code>JsonEntity</code> class represents a document comprised of a unique ID, a deserialized JSON body and a
 * version number.
 *
 * @param <T> the type used to deserialize the JSON body of the document
 */
public class JsonEntity<T> {

    @Setter
    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        Converters.registerAll(builder);
        builder.registerTypeAdapter(EntityId.class, new EntityIdSerializer());
        setGson(builder.create());
    }

    /**
     * Gets the unique identifier of the document.
     */
    @Getter
    private final EntityId id;

    /**
     * Gets the body of the document deserialized into an object, or null if the document does not exist.
     */
    @Getter
    private final T entity;

    /**
     * Gets the current version of the document.
     */
    @Getter
    private final long version;

    public JsonEntity(@NonNull EntityId id, T entity, long version) {
        this.id = id;
        this.entity = entity;
        this.version = version;
    }

    /**
     * Converts a <code>Document</code> object to a <code>JsonEntity</code> by deserializing its JSON body.
     */
    public static <T> JsonEntity<T> fromDocument(Class<T> type, Document document) {
        return new JsonEntity<T>(
            new EntityId(document.getId()),
            document.getBody() != null
                ? gson.fromJson(document.getBody(), type)
                : null,
            document.getVersion());
    }

    /**
     * Converts this <code>JsonEntity</code> object to a <code>Document</code> by serializing its body to JSON.
     */
    public Document toDocument() {
        return new Document(
            this.getId().getValue(),
            this.getEntity() != null
                ? gson.toJson(this.getEntity())
                : null,
            this.getVersion());
    }

    /**
     * Returns a copy of this <code>JsonEntity</code> object with the same ID and version, but replaces the
     * body with a new one.
     */
    public JsonEntity<T> modify(T newValue) {
        return new JsonEntity<T>(getId(), newValue, getVersion());
    }

    /**
     * Creates a new <code>JsonEntity</code> object with a new random ID and a version set to zero.
     */
    public static <T> JsonEntity<T> create(T value) {
        return new JsonEntity<T>(
            EntityId.newRandom(EntityId.getEntityType(value.getClass())),
            value,
            0);
    }

    private static class EntityIdSerializer implements JsonSerializer<EntityId>, JsonDeserializer<EntityId> {
        @Override
        public EntityId deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

            String jsonString = json.getAsString();
            return EntityId.fromString(jsonString);
        }

        @Override
        public JsonElement serialize(EntityId entityId, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(entityId.toString());
        }
    }
}
