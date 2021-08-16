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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.pgdoc.Document;

public class JsonEntity<T> {

    @Setter
    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        Converters.registerAll(builder);
        gson = builder.create();
    }

    @Getter
    private final EntityId id;

    @Getter
    private final T entity;

    @Getter
    private final long version;

    public JsonEntity(@NonNull EntityId id, T entity, long version) {
        this.id = id;
        this.entity = entity;
        this.version = version;
    }

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

    public static <T> JsonEntity<T> create(T value) {
        return new JsonEntity<T>(
            EntityId.newRandom(EntityId.getEntityType(value.getClass())),
            value,
            0);
    }
}
