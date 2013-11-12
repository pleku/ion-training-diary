package org.vaadin.ion.diary.domain;

import java.util.Calendar;
import java.util.List;

/**
 * Entity class for representing a workout.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
public class Workout {

    private String sport;
    private Float duration;
    private Calendar time;
    private Float distance;
    private String description;
    private Float rating;
    private String name;
    private List<String> tags;

    public String getSport() {
        return sport;
    }
    public void setSport(String sport) {
        this.sport = sport;
    }
    public Float getDuration() {
        return duration;
    }
    public void setDuration(Float duration) {
        this.duration = duration;
    }
    public Calendar getTime() {
        if (time == null) {
            return null;
        }
        return (Calendar) time.clone();
    }
    public void setTime(Calendar time) {
        if (time == null) {
            this.time = null;
        } else {
            if (this.time == null) {
                this.time = Calendar.getInstance();
            }
            this.time.setTime(time.getTime());
        }
    }
    public Float getDistance() {
        return distance;
    }
    public void setDistance(Float distance) {
        this.distance = distance;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Float getRating() {
        return rating;
    }
    public void setRating(Float rating) {
        this.rating = rating;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }


}
