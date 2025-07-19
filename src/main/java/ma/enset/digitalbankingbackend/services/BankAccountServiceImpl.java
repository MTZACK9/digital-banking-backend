package ma.enset.digitalbankingbackend.services;

import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbankingbackend.dtos.*;
import ma.enset.digitalbankingbackend.entities.*;
import ma.enset.digitalbankingbackend.enums.OperationType;
import ma.enset.digitalbankingbackend.exceptions.BalanceNotSufficentException;
import ma.enset.digitalbankingbackend.exceptions.BankAccountNotFoundException;
import ma.enset.digitalbankingbackend.exceptions.CustomerNotFoundException;
import ma.enset.digitalbankingbackend.mappers.BankAccountMapperImpl;
import ma.enset.digitalbankingbackend.repositories.AccountOperationRepository;
import ma.enset.digitalbankingbackend.repositories.BankAccountRepository;
import ma.enset.digitalbankingbackend.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl  bankAccountMapper;

    public BankAccountServiceImpl(CustomerRepository customerRepository, BankAccountRepository bankAccountRepository, AccountOperationRepository accountOperationRepository, BankAccountMapperImpl bankAccountMapper) {
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.accountOperationRepository = accountOperationRepository;
        this.bankAccountMapper = bankAccountMapper;
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customer) {
        log.info("Saving customer {}", customer);
        Customer customer1 = bankAccountMapper.fromCustomerDTO(customer);
        Customer savedCustomer = this.customerRepository.save(customer1);
        return bankAccountMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = this.customerRepository.findById(customerId).orElse(null);
        if(customer == null) {
            throw new CustomerNotFoundException("Customer with id " + customerId + " not found");
        }
        CurrentAccount currentAccount = new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setCustomer(customer);
        currentAccount.setOverDraft(overDraft);
        CurrentAccount savedCurrentAccount = this.bankAccountRepository.save(currentAccount);
        return bankAccountMapper.fromCurrentBankAccount(savedCurrentAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = this.customerRepository.findById(customerId).orElse(null);
        if(customer == null) {
            throw new CustomerNotFoundException("Customer with id " + customerId + " not found");
        }
        SavingAccount savingAccount = new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(interestRate);
        SavingAccount savedSavingAccount = bankAccountRepository.save(savingAccount);
        return bankAccountMapper.fromSavingBankAccount(savedSavingAccount);
    }


    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(customer -> bankAccountMapper.fromCustomer(customer)).collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = this.bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Bank Account Not Found"));
        if(bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return bankAccountMapper.fromSavingBankAccount(savingAccount);
        }
        else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return bankAccountMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficentException {
        BankAccount bankAccount = this.bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Bank Account Not Found"));
        if(bankAccount.getBalance() < amount) {
            throw new BalanceNotSufficentException("Balance not sufficient");
        }
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = this.bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Bank Account Not Found"));

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficentException {
        debit(accountIdSource, amount, "Transfer");
        credit(accountIdDestination, amount, "Transfer");
    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof SavingAccount) {
                return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        }).toList();
    }
    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = this.customerRepository.findById(customerId).orElse(null);
        if(customer == null) {
            throw new CustomerNotFoundException("Customer with id " + customerId + " not found");
        }
        return bankAccountMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customer) {
        log.info("Update customer {}", customer);
        Customer customer1 = bankAccountMapper.fromCustomerDTO(customer);
        Customer savedCustomer = this.customerRepository.save(customer1);
        return bankAccountMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op -> bankAccountMapper.fromAccountOperation(op)).toList();
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount == null) {
            throw new BankAccountNotFoundException("Account Not found");
        }
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(op -> bankAccountMapper.fromAccountOperation(op)).toList();
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.findByNameContains(keyword);
        return customers.stream().map(customer -> bankAccountMapper.fromCustomer(customer)).toList();
    }
}
