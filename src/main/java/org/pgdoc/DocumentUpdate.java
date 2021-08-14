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

package org.pgdoc;

import com.impossibl.postgres.api.jdbc.PGAnyType;
import com.impossibl.postgres.api.jdbc.PGType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class DocumentUpdate implements SQLData {
    private static final String TYPE_NAME = "document_update";

    public static final PGAnyType TYPE = new PGAnyType() {
        @Override
        public String getName() {
            return DocumentUpdate.TYPE_NAME;
        }

        @Override
        public String getVendor() {
            return "";
        }

        @Override
        public Integer getVendorTypeNumber() {
            return null;
        }

        @Override
        public Class getJavaType() {
            return DocumentUpdate.class;
        }
    };

    @Getter
    private UUID id;

    @Getter
    private String body;

    @Getter
    private long version;

    @Getter
    private Boolean checkOnly;

    @Override
    public String getSQLTypeName() throws SQLException {
        return TYPE_NAME;
    }

    @Override
    public void readSQL(SQLInput in, String typeName) throws SQLException {
        this.id = in.readObject(UUID.class);
        this.body = in.readString();
        this.version = in.readLong();
        this.checkOnly = in.readObject(Boolean.class);
    }

    @Override
    public void writeSQL(SQLOutput out) throws SQLException {
        out.writeObject(this.id, JDBCType.STRUCT);
        out.writeObject(this.body, PGType.JSONB);
        out.writeObject(this.version, PGType.INT8);
        out.writeObject(this.checkOnly, JDBCType.BOOLEAN);
    }
}
