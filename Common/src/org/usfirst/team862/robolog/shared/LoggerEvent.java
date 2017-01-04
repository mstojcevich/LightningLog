package org.usfirst.team862.robolog.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class LoggerEvent implements Serializable {

    private final LoggerEventType type;
    private final String title, description;
    private final RioData rioData;
    private final Date time;
    private final Map<String, String> customProperties;
    private final String stackTrace;

    public LoggerEvent(LoggerEventType type, String title, String description, Date time, RioData rioData, Map<String, String> customProperties, String stackTrace) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.rioData = rioData;
        this.time = time;
        this.customProperties = customProperties;
        this.stackTrace = stackTrace;
    }

    public LoggerEventType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RioData getRioData() {
        return rioData;
    }

    public Date getTime() {
        return time;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public String getStackTrace() {
        return stackTrace;
    }

}
