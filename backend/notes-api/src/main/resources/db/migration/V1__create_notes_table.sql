CREATE TABLE notes (
    id UUID PRIMARY KEY,
    site VARCHAR(150) NOT NULL,
    equipment VARCHAR(100) NOT NULL,
    variable VARCHAR(100) NOT NULL,
    note_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    author VARCHAR(150) NOT NULL,
    message TEXT NOT NULL
);

CREATE INDEX idx_notes_site ON notes (site);
CREATE INDEX idx_notes_equipment ON notes (equipment);
CREATE INDEX idx_notes_timestamp ON notes (note_timestamp);
