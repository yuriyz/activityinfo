package org.activityinfo.service.tasks;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;

public class UserTask implements IsRecord {

    private String id;
    private String description;
    private double timeStarted;
    private UserTaskStatus status;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(double timeStarted) {
        this.timeStarted = timeStarted;
    }

    public UserTaskStatus getStatus() {
        return status;
    }

    public void setStatus(UserTaskStatus status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Record asRecord() {
        return new Record()
            .set("id", id)
            .set("description", description)
            .set("timeStarted", timeStarted)
            .set("status", status.name());
    }

    public static UserTask fromRecord(Record record) {
        UserTask task = new UserTask();
        task.setId(record.getString("id"));
        task.setTimeStarted(record.getDouble("timeStarted"));
        task.setDescription(record.getString("description"));
        task.setStatus(UserTaskStatus.valueOf(record.getString("status")));
        return task;
    }
}