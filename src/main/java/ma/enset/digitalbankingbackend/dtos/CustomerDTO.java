package ma.enset.digitalbankingbackend.dtos;


import lombok.Data;


@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
}
