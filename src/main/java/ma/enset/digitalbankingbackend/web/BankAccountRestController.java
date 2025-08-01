package ma.enset.digitalbankingbackend.web;

import ma.enset.digitalbankingbackend.dtos.*;
import ma.enset.digitalbankingbackend.exceptions.BalanceNotSufficentException;
import ma.enset.digitalbankingbackend.exceptions.BankAccountNotFoundException;
import ma.enset.digitalbankingbackend.services.BankAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BankAccountRestController {
    private final BankAccountService bankAccountService;

    public BankAccountRestController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts")
    public List<BankAccountDTO> listAccounts() {
        return bankAccountService.bankAccountList();
    }

    @GetMapping("accounts/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId) {
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(@PathVariable String accountId,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficentException {
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    @PostMapping("/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException, BalanceNotSufficentException {
        this.bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/accounts/transfer")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficentException {
        this.bankAccountService.transfer(transferRequestDTO.getAccountSource(), transferRequestDTO.getAccountDestination(), transferRequestDTO.getAmount());
    }
}
