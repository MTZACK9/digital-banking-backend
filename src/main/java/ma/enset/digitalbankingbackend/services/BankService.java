package ma.enset.digitalbankingbackend.services;

import ma.enset.digitalbankingbackend.entities.BankAccount;
import ma.enset.digitalbankingbackend.entities.CurrentAccount;
import ma.enset.digitalbankingbackend.entities.SavingAccount;
import ma.enset.digitalbankingbackend.repositories.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankService {
    private final BankAccountRepository bankAccountRepository;
    public BankService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }
    public void consulter(){
        BankAccount bankAccount=
                this.bankAccountRepository.findById("07cd2b08-15e0-48e2-b2c5-afc16d9a6b17").orElse(null);
        if(bankAccount!=null) {
            System.out.println("*****************************");
            System.out.println(bankAccount.getId());
            System.out.println(bankAccount.getBalance());
            System.out.println(bankAccount.getStatus());
            System.out.println(bankAccount.getCreatedAt());
            System.out.println(bankAccount.getCustomer().getName());
            System.out.println(bankAccount.getClass().getSimpleName());
            if (bankAccount instanceof CurrentAccount) {
                System.out.println("Over Draft=>" + ((CurrentAccount) bankAccount).getOverDraft());
            } else if (bankAccount instanceof SavingAccount) {
                System.out.println("Rate=>" + ((SavingAccount) bankAccount).getInterestRate());
            }
            bankAccount.getAccountOperations().forEach(op -> {
                System.out.println(op.getType() + "\t" + op.getOperationDate() + "\t" + op.getAmount());
            });
        }
    }

}
