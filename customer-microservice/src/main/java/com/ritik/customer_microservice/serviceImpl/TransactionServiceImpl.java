package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.transactionDTO.*;
import com.ritik.customer_microservice.enums.OperationType;
import com.ritik.customer_microservice.enums.TransactionStatus;
import com.ritik.customer_microservice.enums.TransactionType;
import com.ritik.customer_microservice.exception.*;
import com.ritik.customer_microservice.model.Account;
import com.ritik.customer_microservice.model.Customer;
import com.ritik.customer_microservice.model.Transaction;
import com.ritik.customer_microservice.repository.AccountRepository;
import com.ritik.customer_microservice.repository.CustomerRepository;
import com.ritik.customer_microservice.repository.TransactionRepository;
import com.ritik.customer_microservice.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private Transaction toEntityForDeposit(DepositRequestDTO dto, Account account) {

        BigDecimal newBalance = account.getAmount().add(dto.getAmount());
        account.setAmount(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAccountNum(account.getAccountNum());
        transaction.setBankId(account.getBankId());

        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setTransactionType(TransactionType.CREDIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(newBalance);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(null);
        transaction.setCounterpartyAccountNum(null);

        return transaction;
    }


    private Transaction toEntityForWithdraw(WithdrawRequestDTO dto, Account account) {

        BigDecimal newBalance = account.getAmount().subtract(dto.getAmount());
        account.setAmount(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAccountNum(account.getAccountNum());
        transaction.setBankId(account.getBankId());

        transaction.setOperationType(OperationType.WITHDRAW);
        transaction.setTransactionType(TransactionType.DEBIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(newBalance);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(null);
        transaction.setCounterpartyAccountNum(null);

        return transaction;
    }

    private TransactionResponseDTO toDto(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAccountNum(transaction.getAccountNum());
        dto.setOperationType(transaction.getOperationType());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setClosingBalance(transaction.getClosingBalance());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }

    private TransactionHistoryDTO toTransactionHistoryDto(Transaction transaction) {

        TransactionHistoryDTO dto = new TransactionHistoryDTO();

        dto.setTransactionId(transaction.getTransactionId());
        dto.setOperationType(transaction.getOperationType());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setClosingBalance(transaction.getClosingBalance());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setCreatedAt(transaction.getCreatedAt());

        return dto;
    }

    private Transaction toEntityForTransferDebit(TransferRequestDTO dto, Account sender, UUID transactionRefId) {

        BigDecimal newBalance = sender.getAmount().subtract(dto.getAmount());
        sender.setAmount(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccount(sender);
        transaction.setAccountNum(sender.getAccountNum());
        transaction.setBankId(sender.getBankId());

        transaction.setOperationType(OperationType.TRANSFER);
        transaction.setTransactionType(TransactionType.DEBIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(newBalance);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(transactionRefId);
        transaction.setCounterpartyAccountNum(dto.getToAccountNum());

        return transaction;
    }

    private Transaction toEntityForTransferCredit(TransferRequestDTO dto, Account receiver, UUID transactionRefId) {

        BigDecimal newBalance = receiver.getAmount().add(dto.getAmount());
        receiver.setAmount(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccount(receiver);
        transaction.setAccountNum(receiver.getAccountNum());
        transaction.setBankId(receiver.getBankId());

        transaction.setOperationType(OperationType.TRANSFER);
        transaction.setTransactionType(TransactionType.CREDIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(newBalance);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(transactionRefId);
        transaction.setCounterpartyAccountNum(dto.getFromAccountNum());

        return transaction;
    }

    @Override
    @Transactional
    public TransactionResponseDTO depositMoney(String email, DepositRequestDTO depositRequestDTO){
        if (depositRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));
        Account account = accountRepository.findByAccountNumAndCustomer_CustomerId(
                depositRequestDTO.getAccountNum(),
                customer.getCustomerId()).orElseThrow(()->new AccountNotFoundException("Account not found"));
        //account.setAmount(account.getAmount().add(depositRequestDTO.getAmount()));
        Transaction transaction = toEntityForDeposit(depositRequestDTO,account);
        transactionRepository.save(transaction);

        return toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionResponseDTO withdrawMoney(String email, WithdrawRequestDTO withdrawRequestDTO){
        if (withdrawRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdraw amount must be greater than zero");
        }
        Customer customer = customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));

        Account account = accountRepository.findByAccountNumAndCustomer_CustomerId(
                withdrawRequestDTO.getAccountNum(),
                customer.getCustomerId()).orElseThrow(()->new AccountNotFoundException("Account not found"));

        if(!passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash())){
            throw new WrongPinException("Wrong pin");
        }

        if (account.getAmount().compareTo(withdrawRequestDTO.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        //account.setAmount(account.getAmount().subtract(withdrawRequestDTO.getAmount()));
        Transaction transaction = toEntityForWithdraw(withdrawRequestDTO,account);
        transactionRepository.save(transaction);

        return toDto(transaction);
    }

    //Customer not found
    //account num provided and account not found
    //account num provided and transaction not found
    //account num provided and successfully transaction history
    //account num not provided and account not found
    //account num not provided and transaction not found
    //account num not provided and successfully transaction history

    @Override
    public List<TransactionHistoryDTO> transactionHistory(String email, Long accountNum) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (accountNum != null) {

            Account account = accountRepository
                    .findByAccountNumAndCustomer_CustomerId(accountNum, customer.getCustomerId())
                    .orElseThrow(() ->
                            new AccountNotFoundException("Account not found"));

            List<Transaction> transactions =
                    transactionRepository.findByAccount_AccountId(account.getAccountId());

            if (transactions.isEmpty()) {
                throw new TransactionNotFoundException("Transactions not found");
            }

            List<TransactionHistoryDTO> response = new ArrayList<>();

            for (Transaction transaction : transactions) {
                response.add(toTransactionHistoryDto(transaction));
            }
            return response;
        }

        List<Account> accounts =
                accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No accounts found for customer");
        }

        List<UUID> accountIds = accounts.stream()
                .map(Account::getAccountId)
                .toList();

        List<Transaction> transactions =
                transactionRepository.findByAccount_AccountIdIn(accountIds);

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Transactions not found");
        }

        List<TransactionHistoryDTO> response = new ArrayList<>();

        for (Transaction transaction : transactions) {
            response.add(toTransactionHistoryDto(transaction));
        }

        return response;
    }

    @Override
    @Transactional
    public TransferResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO){
        if (transferRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        if (transferRequestDTO.getFromAccountNum().equals(transferRequestDTO.getToAccountNum())) {
            throw new IllegalArgumentException("Sender and receiver accounts cannot be same");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Account senderAccount = accountRepository.findByAccountNumAndCustomer_CustomerId(
                transferRequestDTO.getFromAccountNum(),
                customer.getCustomerId()).orElseThrow(()->new AccountNotFoundException("Account not found"));

        Account receiverAccount = accountRepository.findByAccountNumAndCustomer_CustomerId(
                transferRequestDTO.getToAccountNum(),
                customer.getCustomerId()).orElseThrow(()->new AccountNotFoundException("Account not found"));

        if (!passwordEncoder.matches(transferRequestDTO.getPin(), senderAccount.getPinHash())) {
            throw new WrongPinException("Wrong PIN");
        }

        if (senderAccount.getAmount().compareTo(transferRequestDTO.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        UUID transactionRefId = UUID.randomUUID();

        Transaction debitTxn =
                toEntityForTransferDebit(transferRequestDTO, senderAccount, transactionRefId);

        Transaction creditTxn =
                toEntityForTransferCredit(transferRequestDTO, receiverAccount, transactionRefId);

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);

        return TransferResponseDTO.builder()
                .status("SUCCESS")
                .message("Transfer completed successfully")
                .transactionReferenceId(transactionRefId)
                .fromAccountNum(transferRequestDTO.getFromAccountNum())
                .toAccountNum(transferRequestDTO.getToAccountNum())
                .amount(transferRequestDTO.getAmount())
                .senderClosingBalance(senderAccount.getAmount())
                .timestamp(LocalDateTime.now())
                .build();

    }
}
