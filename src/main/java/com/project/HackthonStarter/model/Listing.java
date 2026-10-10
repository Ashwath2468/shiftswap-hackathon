
package com.project.HackthonStarter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shift_listings")
public class Listing {

    @Id
    private String id;

    private String shiftId;

    private String posterId;

    private String claimerId;

    private String status;

    public Listing() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getClaimerId() {
        return claimerId;
    }

    public void setClaimerId(String claimerId) {
        this.claimerId = claimerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
