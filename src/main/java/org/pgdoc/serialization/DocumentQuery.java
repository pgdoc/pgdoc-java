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

import lombok.Cleanup;
import org.pgdoc.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface DocumentQuery {

    /**
     * Executes an SQL query and converts the result into a list of <code>JsonEntity&lt;T></code> objects. The query
     * must return the <code>id</code>, <code>body</code> and <code>version</code> columns.
     */
    static <T> List<JsonEntity<T>> execute(Class<T> type, PreparedStatement statement)
        throws SQLException {

        @Cleanup ResultSet resultSet = statement.executeQuery();

        ArrayList<JsonEntity<T>> result = new ArrayList<>();

        while (resultSet.next()) {
            Document document = new Document(
                resultSet.getObject("id", java.util.UUID.class),
                resultSet.getString("body"),
                resultSet.getLong("version")
            );

            result.add(JsonEntity.fromDocument(type, document));
        }

        return Collections.unmodifiableList(result);
    }
}
