package guru.springframework.spring6restmvc.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerDTO {

    private UUID id;
    private Integer version;
    private String customerName;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

}
