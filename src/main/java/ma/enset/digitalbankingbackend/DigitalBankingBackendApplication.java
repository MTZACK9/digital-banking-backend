package ma.enset.digitalbankingbackend;

import ma.enset.digitalbankingbackend.dtos.BankAccountDTO;
import ma.enset.digitalbankingbackend.dtos.CurrentBankAccountDTO;
import ma.enset.digitalbankingbackend.dtos.CustomerDTO;
import ma.enset.digitalbankingbackend.dtos.SavingBankAccountDTO;
import ma.enset.digitalbankingbackend.entities.*;
import ma.enset.digitalbankingbackend.enums.AccountStatus;
import ma.enset.digitalbankingbackend.enums.OperationType;
import ma.enset.digitalbankingbackend.exceptions.BalanceNotSufficentException;
import ma.enset.digitalbankingbackend.exceptions.BankAccountNotFoundException;
import ma.enset.digitalbankingbackend.exceptions.CustomerNotFoundException;
import ma.enset.digitalbankingbackend.repositories.AccountOperationRepository;
import ma.enset.digitalbankingbackend.repositories.BankAccountRepository;
import ma.enset.digitalbankingbackend.repositories.CustomerRepository;
import ma.enset.digitalbankingbackend.services.BankAccountService;
import ma.enset.digitalbankingbackend.services.BankService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class DigitalBankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankingBackendApplication.class, args);
    }

     @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
        return args -> {
            Stream.of("Hassan", "Yassine", "Aicha")
                    .forEach(name -> {
                        CustomerDTO customer = new CustomerDTO();
                        customer.setName(name);
                        customer.setEmail(name+"@gmail.com");
                        bankAccountService.saveCustomer(customer);
                    });
            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random() * 9000, 9000, customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random() * 91000, 5.5, customer.getId());

                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });
            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for(BankAccountDTO bankAccount : bankAccounts){
                for (int i = 0; i < 10; i++) {
                    String accountId;
                    if (bankAccount instanceof SavingBankAccountDTO)
                    {
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    }else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000+Math.random()*120000, "Credit");
                    bankAccountService.debit(accountId, 10000+Math.random()*9000, "Debit");
                }
            }
        };
    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository, BankAccountRepository bankAccountRepository, AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("Hassan", "Yassine", "Aicha").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 1000);
                currentAccount.setCustomer(customer);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setOverDraft(1000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 1000);
                savingAccount.setCustomer(customer);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });

            bankAccountRepository.findAll().forEach(bankAccount -> {
                for (int i = 0; i <5; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 1000);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT: OperationType.CREDIT);
                    accountOperation.setBankAccount(bankAccount);
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }
}
