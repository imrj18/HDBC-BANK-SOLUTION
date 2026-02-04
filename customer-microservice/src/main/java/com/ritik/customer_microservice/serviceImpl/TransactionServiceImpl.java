package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
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
import com.ritik.customer_microservice.service.OtpService;
import com.ritik.customer_microservice.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final CacheManager cacheManager;

    private final ApplicationEventPublisher eventPublisher;

    private void evictBalanceCache(String email, Long accountNum) {
        Cache checkBalanceCache = cacheManager.getCache("checkBalance");
        if (checkBalanceCache != null) {
            checkBalanceCache.evict(List.of(email, accountNum));
        }

        Cache bankCustomersCache = cacheManager.getCache("bankCustomers");
        if (bankCustomersCache != null) {
            bankCustomersCache.clear();
        }
    }


    private Transaction toEntityForDeposit(DepositRequestDTO dto, Account account) {

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAccountNum(account.getAccountNum());
        transaction.setBankId(account.getBankId());

        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setTransactionType(TransactionType.CREDIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(account.getAmount());
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(null);
        transaction.setCounterpartyAccountNum(null);

        return transaction;
    }


    private Transaction toEntityForWithdraw(WithdrawRequestDTO dto, Account account) {

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAccountNum(account.getAccountNum());
        transaction.setBankId(account.getBankId());

        transaction.setOperationType(OperationType.WITHDRAW);
        transaction.setTransactionType(TransactionType.DEBIT);

        transaction.setAmount(dto.getAmount());
        transaction.setTransactionStatus(TransactionStatus.PENDING);

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

        Transaction transaction = new Transaction();
        transaction.setAccount(sender);
        transaction.setAccountNum(sender.getAccountNum());
        transaction.setBankId(sender.getBankId());

        transaction.setOperationType(OperationType.TRANSFER);
        transaction.setTransactionType(TransactionType.DEBIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(null);
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        transaction.setTransactionReferenceId(transactionRefId);
        transaction.setCounterpartyAccountNum(dto.getToAccountNum());

        return transaction;
    }

    private Transaction toEntityForTransferCredit(TransferRequestDTO dto, Account receiver, UUID transactionRefId) {

        Transaction transaction = new Transaction();
        transaction.setAccount(receiver);
        transaction.setAccountNum(receiver.getAccountNum());
        transaction.setBankId(receiver.getBankId());

        transaction.setOperationType(OperationType.TRANSFER);
        transaction.setTransactionType(TransactionType.CREDIT);

        transaction.setAmount(dto.getAmount());
        transaction.setClosingBalance(receiver.getAmount());
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        transaction.setTransactionReferenceId(transactionRefId);
        transaction.setCounterpartyAccountNum(dto.getFromAccountNum());

        return transaction;
    }

    private Customer checkCustomer(String email){
        return customerRepository.findByEmail(email).orElseThrow(()->
                new CustomerNotFoundException("Customer not found"));
    }

    private Account checkAccount(Long accountNum, UUID customerId){
        return accountRepository.findByAccountNumAndCustomer_CustomerId(
                accountNum, customerId).orElseThrow(()->new AccountNotFoundException("Account not found"));
    }

    @Override
    @Transactional
    public TransactionResponseDTO depositMoney(String email, DepositRequestDTO depositRequestDTO){
        if (depositRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Customer customer = checkCustomer(email);
        Account account = accountRepository.lockAccount(
                depositRequestDTO.getAccountNum(),
                customer.getCustomerId()
        ).orElseThrow(()->new AccountNotFoundException("Account not found."));

        if (account == null) {
            throw new AccountNotFoundException("Account not found");
        }

        account.setAmount(account.getAmount().add(depositRequestDTO.getAmount()));
        Transaction transaction = toEntityForDeposit(depositRequestDTO,account);
        transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent(
                transaction.getTransactionId(),
                email,
                depositRequestDTO.getAmount(),
                OperationType.DEPOSIT,
                TransactionType.CREDIT,
                TransactionStatus.SUCCESS,
                null);

        eventPublisher.publishEvent(event);

        evictBalanceCache(email, depositRequestDTO.getAccountNum());
        return toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionResponseDTO withdrawMoney(String email, WithdrawRequestDTO withdrawRequestDTO){
        if (withdrawRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdraw amount must be greater than zero");
        }

        Customer customer = checkCustomer(email);

        Account account = checkAccount(withdrawRequestDTO.getAccountNum(), customer.getCustomerId());

        if(!passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash())){
            throw new WrongPinException("Wrong pin");
        }

        if (account.getAmount().compareTo(withdrawRequestDTO.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Transaction transaction = toEntityForWithdraw(withdrawRequestDTO,account);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);
        otpService.sendOtp(email, transaction.getTransactionId());

        return toDto(transaction);
    }

    @Override
    @Cacheable(
            value = "transactionHistory",
            key = "{#email, #accountNum, #page, #size}",
            unless = "#result==null"
    )
    public PageResponse<TransactionHistoryDTO> transactionHistory(String email, Long accountNum, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Customer customer = checkCustomer(email);

        Page<Transaction> transactions;

        if (accountNum != null) {

            Account account = checkAccount(accountNum, customer.getCustomerId());

            transactions = transactionRepository.findByAccount_AccountId(account.getAccountId(), pageable);

        }else{
            List<Account> accounts = accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

            if (accounts.isEmpty()) {
                throw new AccountNotFoundException("No accounts found for customer");
            }

            List<UUID> accountIds = accounts.stream().map(Account::getAccountId).toList();

            transactions = transactionRepository.findByAccount_AccountIdIn(accountIds, pageable);

        }
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Transactions not found");
        }

        List<TransactionHistoryDTO> response = transactions
                .getContent()
                .stream()
                .map(this::toTransactionHistoryDto)
                .toList();

        return new PageResponse<>(
                response,
                transactions.getNumber(),
                transactions.getTotalPages(),
                transactions.getSize(),
                transactions.isLast()
        );
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

        Customer customer = checkCustomer(email);

        accountRepository.findByAccountNum(transferRequestDTO.getToAccountNum())
                .orElseThrow(()->new AccountNotFoundException("Receiver account not found"));

        Account senderAccount = checkAccount(transferRequestDTO.getFromAccountNum(), customer.getCustomerId());


        if (!passwordEncoder.matches(transferRequestDTO.getPin(), senderAccount.getPinHash())) {
            throw new WrongPinException("Wrong PIN");
        }

        if (senderAccount.getAmount().compareTo(transferRequestDTO.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        UUID transactionRefId = UUID.randomUUID();


        Transaction debitTxn = toEntityForTransferDebit(transferRequestDTO,senderAccount, transactionRefId);
        transactionRepository.save(debitTxn);
        otpService.sendOtp(email, debitTxn.getTransactionId());


        return TransferResponseDTO.builder()
                .transactionId(debitTxn.getTransactionId())
                .status(TransactionStatus.PENDING)
                .message("Transfer completed successfully")
                .transactionReferenceId(transactionRefId)
                .fromAccountNum(transferRequestDTO.getFromAccountNum())
                .toAccountNum(transferRequestDTO.getToAccountNum())
                .amount(transferRequestDTO.getAmount())
                .senderClosingBalance(senderAccount.getAmount())
                .timestamp(LocalDateTime.now())
                .build();

    }

    @Override
    @Transactional
    public TransactionResponseDTO transactionConfirm(String email, ConfirmRequestDTO dto) {

        Transaction debitTx = transactionRepository
                .lockByTransactionId(dto.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (debitTx.getTransactionStatus() == TransactionStatus.SUCCESS) {
            throw new TransactionAlreadyProcessedException("Transaction already processed");
        }

        if (debitTx.getTransactionStatus() == TransactionStatus.FAILED) {
            throw new TransactionFailedException("Transaction failed!!! Create new transaction.");
        }

        if (!debitTx.getAccount().getCustomer().getEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }

        otpService.verifyOtp(email, dto.getTransactionId(), dto.getOTP());

        Account sender = accountRepository.lockAccountById(debitTx.getAccount().getAccountId());

        if (sender.getAmount().compareTo(debitTx.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance at confirmation");
        }

        sender.setAmount(sender.getAmount().subtract(debitTx.getAmount()));

        debitTx.setTransactionStatus(TransactionStatus.SUCCESS);
        debitTx.setClosingBalance(sender.getAmount());

        accountRepository.save(sender);
        transactionRepository.save(debitTx);

        evictBalanceCache(sender.getCustomer().getEmail(), sender.getAccountNum());

        if (debitTx.getOperationType() == OperationType.TRANSFER) {

            Account receiver = accountRepository.lockByAccountNum(debitTx.getCounterpartyAccountNum());
            receiver.setAmount(receiver.getAmount().add(debitTx.getAmount()));

            Transaction creditTx = toEntityForTransferCredit(
                    new TransferRequestDTO(
                            debitTx.getCounterpartyAccountNum(),
                            debitTx.getAccountNum(),
                            debitTx.getAmount()
                    ),
                    receiver,
                    debitTx.getTransactionReferenceId()
            );

            accountRepository.save(receiver);
            transactionRepository.save(creditTx);

            eventPublisher.publishEvent(new TransactionEvent(
                    dto.getTransactionId(),
                    email,
                    debitTx.getAmount(),
                    OperationType.TRANSFER,
                    TransactionType.DEBIT,
                    TransactionStatus.SUCCESS,
                    null
            ));

            eventPublisher.publishEvent(new TransactionEvent(
                    creditTx.getTransactionId(),
                    receiver.getCustomer().getEmail(),
                    creditTx.getAmount(),
                    OperationType.TRANSFER,
                    TransactionType.CREDIT,
                    TransactionStatus.SUCCESS,
                    null
            ));

            evictBalanceCache(receiver.getCustomer().getEmail(), receiver.getAccountNum());

        } else if (debitTx.getOperationType() == OperationType.WITHDRAW) {

            eventPublisher.publishEvent(new TransactionEvent(
                    dto.getTransactionId(),
                    email,
                    debitTx.getAmount(),
                    OperationType.WITHDRAW,
                    TransactionType.DEBIT,
                    TransactionStatus.SUCCESS,
                    null
            ));
        }


        return toDto(debitTx);
    }

}
