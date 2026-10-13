package ro.umfst.newsapplication.model;

import java.sql.Timestamp;

/**
 * @author hunorszilvasi
 * An event is an article with a start time and a location,
 * so it inherits every News field from Article.
 */
public class Event extends Article {

    private Timestamp eventDateTime; // day and start time in one field
    private String location;

    public Event() {
        super();
    }

    public Event(int id) {
        super();
        setId(id); // Article keeps its own id field
    }

    public Timestamp getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Timestamp eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Events are identified by title + start time + location
    @Override
    public String businessKey() {
        return getTitle() + "|" + eventDateTime + "|" + location;
    }

    @Override
    public String getEntityType() {
        return "Event";
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + getId() + ", title=" + getTitle()
                + ", eventDateTime=" + eventDateTime
                + ", location=" + location + "}";
    }
}