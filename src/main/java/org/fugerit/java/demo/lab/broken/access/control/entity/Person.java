package org.fugerit.java.demo.lab.broken.access.control.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.fugerit.java.demo.lab.broken.access.control.dto.PersonResponseDTO;

import java.time.LocalDateTime;

/**
 * Entity che mappa la tabella PEOPLE
 */
@Entity
@Table(name = "PEOPLE", schema = "LAB_BAC")
@Getter
@Setter
@ToString
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FIRST_NAME", length = 512)
    private String firstName;

    @Column(name = "LAST_NAME", length = 512)
    private String lastName;

    @Column(name = "TITLE", length = 512)
    private String title;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "MIN_ROLE", length = 512)
    private String minRole;

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
    }

    public PersonResponseDTO toDTO() {
        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(this.id);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        dto.setTitle(this.title);
        dto.setCreationDate(this.creationDate);
        dto.setMinRole(this.minRole);
        return dto;
    }

}
