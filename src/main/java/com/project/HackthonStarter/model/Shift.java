
package com.project.HackthonStarter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    private String id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String requiredCertification;

    private String assignedTo;

    public Shift() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRequiredCertification() {
        return requiredCertification;
    }

    public void setRequiredCertification(String requiredCertification) {
        this.requiredCertification = requiredCertification;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
