// tag::sample[]
package com.jeefix;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ConfigFileSet {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    private ManagedElement managedElement;

    private String name;

    public ConfigFileSet() {
    }

    public ConfigFileSet(ManagedElement managedElement, String name) {
        this.managedElement = managedElement;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ManagedElement getManagedElement() {
        return managedElement;
    }

    public void setManagedElement(ManagedElement managedElement) {
        this.managedElement = managedElement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ConfigFileSet{" +
                "id=" + id +
                ", managedElement=" + managedElement +
                ", name='" + name + '\'' +
                '}';
    }
}

