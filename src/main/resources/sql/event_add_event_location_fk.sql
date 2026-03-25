ALTER TABLE event
    ADD COLUMN event_location_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_event_event_location
        FOREIGN KEY (event_location_id) REFERENCES event_location(id);

