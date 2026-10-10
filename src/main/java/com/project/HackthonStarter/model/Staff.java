

package com.project.HackthonStarter.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    private String id;

    private String name;

    private String role;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> certifications = new ArrayList<>();

    public Staff() {
    }

    public Staff(String id, String name, String role,
                 List<String> certifications) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.certifications = certifications == null
                ? new ArrayList<>()
                : new ArrayList<>(certifications);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications == null
                ? new ArrayList<>()
                : new ArrayList<>(certifications);
    }
}
