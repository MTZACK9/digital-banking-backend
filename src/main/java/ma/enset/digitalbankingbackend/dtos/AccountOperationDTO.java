package ma.enset.digitalbankingbackend.dtos;

import lombok.Data;
import ma.enset.digitalbankingbackend.entities.BankAccount;
import ma.enset.digitalbankingbackend.enums.OperationType;
import java.util.Date;


@Data

public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}
