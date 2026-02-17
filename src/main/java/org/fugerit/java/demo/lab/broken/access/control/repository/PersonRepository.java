package org.fugerit.java.demo.lab.broken.access.control.repository;

import org.fugerit.java.demo.lab.broken.access.control.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository per la entity Person
 *
 * Equivalente Spring Data JPA del PersonRepository Panache di Quarkus
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Restituisce l'elenco delle persone ordinate per cognome e nome,
     * filtrate per MIN_ROLE (NULL oppure presente nella collection di ruoli fornita)
     *
     * @param roles Collection dei ruoli dell'utente
     * @return Lista di Person filtrate e ordinate
     */
    @Query("SELECT p FROM Person p ORDER BY p.lastName, p.firstName")
    List<Person> findByRolesOrderedByName(@Param("roles") Collection<String> roles);

    /**
     * Trova una persona per ID
     */
    Optional<Person> findById(Long id);

}
