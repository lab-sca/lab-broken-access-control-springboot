package org.fugerit.java.demo.lab.broken.access.control.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Risposta dal servizio di aggiunta persona")
public class AddPersonResponseDTO {
    
    @Schema(description = "UUID della persona creata")
    private String uuid;
    
    @Schema(description = "Data di creazione")
    private LocalDateTime creationDate;
    
}
