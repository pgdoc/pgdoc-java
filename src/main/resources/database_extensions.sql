-- ======================================================================
-- get_document_type: Get the type of an entity.
-- ======================================================================

CREATE OR REPLACE FUNCTION get_document_type(id uuid)
    RETURNS int
AS $$ DECLARE
    bytes bytea;
BEGIN
    bytes = decode(substring(id::text, 1, 8), 'hex');
    RETURN (get_byte(bytes, 0)::int << 24)
        | (get_byte(bytes, 1)::int << 16)
        | (get_byte(bytes, 2)::int << 8)
        | (get_byte(bytes, 3)::int);
END $$ LANGUAGE plpgsql IMMUTABLE;
