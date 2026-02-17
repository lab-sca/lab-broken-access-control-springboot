package org.fugerit.java.demo.lab.broken.access.control.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Dati di una persona")
public class PersonResponseDTO {
    
    @Schema(description = "ID della persona")
    private Long id;
    
    @Schema(description = "Nome")
    private String firstName;
    
    @Schema(description = "Cognome")
    private String lastName;
    
    @Schema(description = "Titolo")
    private String title;
    
    @Schema(description = "Data creazione")
    private LocalDateTime creationDate;
    
    @Schema(description = "Ruolo minimo")
    private String minRole;
    
}
