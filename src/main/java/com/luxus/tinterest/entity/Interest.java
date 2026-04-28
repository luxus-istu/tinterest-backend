package com.luxus.tinterest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "name"})
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Interest other)) {
            return false;
        }
        if (id != null && other.id != null) {
            return Objects.equals(id, other.id);
        }
        return name != null && other.name != null && name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return name == null ? 0 : name.toLowerCase(Locale.ROOT).hashCode();
    }
}
