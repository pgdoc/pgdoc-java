-- ======================================================================
-- document: Contains all the documents.
-- ======================================================================

CREATE TABLE document
(
    id      uuid PRIMARY KEY,
    body    jsonb,
    version bigint NOT NULL
);

-- ======================================================================
-- update_documents: Updates a series of documents.
-- ======================================================================

CREATE TYPE document_update AS
(
    id uuid,
    body jsonb,
    version bigint,
    check_only boolean
);

CREATE OR REPLACE FUNCTION update_documents(document_updates document_update[])
RETURNS VOID AS $$ #variable_conflict use_variable
DECLARE
    conflict_id uuid;
BEGIN

    -- Insert the new documents

    INSERT INTO document (id, body, version)
    SELECT document_update.id, NULL, 0
    FROM UNNEST(document_updates) AS document_update
    ON CONFLICT (id) DO NOTHING;

    -- This query returns conflicting rows, the result must be empty
    -- "FOR SHARE" ensures existing documents don't get modified before the UPDATE statement

    WITH document_update AS (
      SELECT document.id, document.version AS old_version, document_update.version AS new_version
      FROM document, UNNEST(document_updates) AS document_update
      WHERE document.id = document_update.id
      FOR SHARE OF document
    )
    SELECT id INTO conflict_id
    FROM document_update
    WHERE old_version <> new_version;

    IF conflict_id IS NOT NULL THEN
      RAISE EXCEPTION 'check_violation' USING HINT = 'update_documents_conflict', DETAIL = conflict_id::text;
    END IF;

    -- Update existing documents

    UPDATE document
    SET body = document_update.body,
        version = document_update.version + 1
    FROM UNNEST(document_updates) AS document_update
    WHERE document.id = document_update.id AND NOT document_update.check_only;

END $$ LANGUAGE plpgsql
SECURITY DEFINER;

-- ======================================================================
-- get_documents: Gets a list of documents from their IDs.
-- ======================================================================

CREATE OR REPLACE FUNCTION get_documents(ids uuid[])
RETURNS TABLE (id uuid, body jsonb, version bigint) AS $$
BEGIN

    RETURN QUERY
    SELECT document.id, document.body, document.version
    FROM document
    WHERE document.id = ANY(ids);

END $$ LANGUAGE plpgsql;
