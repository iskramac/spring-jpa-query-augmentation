// tag::sample[]
package com.jeefix;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ManagedElement {

    @Id
    private Long id;
    private String meName;
    private String meType;

    protected ManagedElement() {}

    public ManagedElement(Long id,String meName, String meType) {
        this.id = id;
        this.meName = meName;
        this.meType = meType;
    }

    @Override
    public String toString() {
        return String.format(
                "ManagedElement[id=%d, meName='%s', meType='%s']",
                id, meName, meType);
    }

// end::sample[]

	public Long getId() {
		return id;
	}

	public String getMeName() {
		return meName;
	}

	public String getMeType() {
		return meType;
	}
}

