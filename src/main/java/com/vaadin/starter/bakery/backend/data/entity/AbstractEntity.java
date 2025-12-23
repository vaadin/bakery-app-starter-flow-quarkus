package com.vaadin.starter.bakery.backend.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.util.Objects;

@MappedSuperclass
public abstract class AbstractEntity extends PanacheEntity {

	@Version
	public int version;

	public Long getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, version);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AbstractEntity that = (AbstractEntity) o;
		return version == that.version &&
				Objects.equals(id, that.id);
	}
}
