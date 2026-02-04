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
import com.ritik.customer_microservice.wrapper.PageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        return customerRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Customer not found | email={}", email);
            return new CustomerNotFoundException("Customer not found");
        });
    }

    private Account checkAccount(Long accountNum, UUID customerId){
        return accountRepository.findByAccountNumAndCustomer_CustomerId(
                accountNum, customerId).orElseThrow(()-> {
                    log.warn("Account not found | accountNum={} | customerId={}", accountNum, customerId);
                    return new AccountNotFoundException("Account not found");
                });
    }

    @Override
    @Transactional
    public TransactionResponseDTO depositMoney(String email, DepositRequestDTO depositRequestDTO){
        log.info(
                "Deposit request received | email={} | accountNum={} | amount={}",
                email,
                depositRequestDTO.getAccountNum(),
                depositRequestDTO.getAmount()
        );
        if (depositRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(
                    "Deposit failed | reason=INVALID_AMOUNT | email={} | accountNum={} | amount={}",
                    email,
                    depositRequestDTO.getAccountNum(),
                    depositRequestDTO.getAmount()
            );
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Customer customer = checkCustomer(email);
        log.debug(
                "Attempting to acquire account lock | accountNum={} | customerId={}",
                depositRequestDTO.getAccountNum(),
                customer.getCustomerId()
        );
        Account account = accountRepository.lockAccount(
                depositRequestDTO.getAccountNum(),
                customer.getCustomerId()
        ).orElseThrow(() -> {
            log.warn(
                    "Deposit failed | reason=ACCOUNT_NOT_FOUND | email={} | accountNum={}",
                    email,
                    depositRequestDTO.getAccountNum()
            );
            return new AccountNotFoundException("Account not found.");
        });

        BigDecimal oldBalance = account.getAmount();
        BigDecimal newBalance = oldBalance.add(depositRequestDTO.getAmount());

        account.setAmount(newBalance);

        Transaction transaction = toEntityForDeposit(depositRequestDTO, account);
        transactionRepository.save(transaction);

        log.info(
                "Deposit successful | txnId={} | accountNum={} | oldBalance={} | newBalance={}",
                transaction.getTransactionId(),
                account.getAccountNum(),
                oldBalance,
                newBalance
        );

        TransactionEvent event = new TransactionEvent(
                transaction.getTransactionId(),
                email,
                depositRequestDTO.getAmount(),
                OperationType.DEPOSIT,
                TransactionType.CREDIT,
                TransactionStatus.SUCCESS,
                null);

        log.debug("Transaction event published | txnId={} | type=DEPOSIT", transaction.getTransactionId());

        eventPublisher.publishEvent(event);

        evictBalanceCache(email, depositRequestDTO.getAccountNum());

        log.debug("Balance cache evicted | email={} | accountNum={}", email, depositRequestDTO.getAccountNum());
        return toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionResponseDTO withdrawMoney(String email, WithdrawRequestDTO withdrawRequestDTO){

        log.info(
                "Withdraw request received | email={} | accountNum={} | amount={}",
                email,
                withdrawRequestDTO.getAccountNum(),
                withdrawRequestDTO.getAmount()
        );

        if (withdrawRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(
                    "Withdraw failed | reason=INVALID_AMOUNT | email={} | accountNum={} | amount={}",
                    email,
                    withdrawRequestDTO.getAccountNum(),
                    withdrawRequestDTO.getAmount()
            );
            throw new InvalidAmountException("Withdraw amount must be greater than zero");
        }

        Customer customer = checkCustomer(email);

        Account account = checkAccount(withdrawRequestDTO.getAccountNum(), customer.getCustomerId());

        if(!passwordEncoder.matches(withdrawRequestDTO.getPin(), account.getPinHash())){
            log.warn(
                    "Withdraw failed | reason=WRONG_PIN | email={} | accountNum={}",
                    email,
                    withdrawRequestDTO.getAccountNum()
            );
            throw new WrongPinException("Wrong pin");
        }

        if (account.getAmount().compareTo(withdrawRequestDTO.getAmount()) < 0) {
            log.warn(
                    "Withdraw failed | reason=INSUFFICIENT_BALANCE | email={} " +
                            "| accountNum={} | balance={} | requestedAmount={}",
                    email,
                    withdrawRequestDTO.getAccountNum(),
                    account.getAmount(),
                    withdrawRequestDTO.getAmount()
            );
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Transaction transaction = toEntityForWithdraw(withdrawRequestDTO,account);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        log.info(
                "Withdraw transaction created | txnId={} | status=PENDING | accountNum={}",
                transaction.getTransactionId(),
                account.getAccountNum()
        );

        otpService.sendOtp(email, transaction.getTransactionId());

        log.debug("OTP sent for withdraw transaction | txnId={} | email={}", transaction.getTransactionId(), email);

        return toDto(transaction);
    }

    @Override
    @Cacheable(
            value = "transactionHistory",
            key = "{#email, #accountNum, #page, #size}",
            unless = "#result==null"
    )
    public PageResponse<TransactionHistoryDTO> transactionHistory(
            String email,
            Long accountNum,
            int page,
            int size
    ) {

        log.info(
                "Transaction history request | email={} | accountNum={} | page={} | size={}",
                email,
                accountNum,
                page,
                size
        );
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Customer customer = checkCustomer(email);

        Page<Transaction> transactions;

        if (accountNum != null) {
            log.debug("Fetching transaction history for specific account | accountNum={}", accountNum);

            Account account = checkAccount(accountNum, customer.getCustomerId());

            transactions = transactionRepository.findByAccount_AccountId(account.getAccountId(), pageable);

        }else{
            log.debug("Fetching transaction history for all customer accounts | email={}", email);
            List<Account> accounts = accountRepository.findByCustomer_CustomerId(customer.getCustomerId());

            if (accounts.isEmpty()) {
                log.warn("Transaction history failed | reason=NO_ACCOUNTS | email={}", email);
                throw new AccountNotFoundException("No accounts found for customer");
            }

            List<UUID> accountIds = accounts.stream().map(Account::getAccountId).toList();

            transactions = transactionRepository.findByAccount_AccountIdIn(accountIds, pageable);

        }
        if (transactions.isEmpty()) {
            log.warn("No transactions found | email={} | accountNum={}", email, accountNum);
            throw new TransactionNotFoundException("Transactions not found");
        }

        log.info(
                "Transaction history fetched | email={} | returnedCount={} | page={} | totalPages={}",
                email,
                transactions.getNumberOfElements(),
                transactions.getNumber(),
                transactions.getTotalPages()
        );


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
        log.info(
                "Transfer request received | email={} | fromAccount={} | toAccount={} | amount={}",
                email,
                transferRequestDTO.getFromAccountNum(),
                transferRequestDTO.getToAccountNum(),
                transferRequestDTO.getAmount()
        );
        if (transferRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(
                    "Transfer failed | reason=INVALID_AMOUNT | email={} | fromAccount={} | amount={}",
                    email,
                    transferRequestDTO.getFromAccountNum(),
                    transferRequestDTO.getAmount()
            );
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        if (transferRequestDTO.getFromAccountNum().equals(transferRequestDTO.getToAccountNum())) {
            log.warn(
                    "Transfer failed | reason=SAME_ACCOUNT | email={} | accountNum={}",
                    email,
                    transferRequestDTO.getFromAccountNum()
            );
            throw new IllegalArgumentException("Sender and receiver accounts cannot be same");
        }

        Customer customer = checkCustomer(email);

        accountRepository.findByAccountNum(transferRequestDTO.getToAccountNum())
                .orElseThrow(() -> {
                    log.warn(
                            "Transfer failed | reason=RECEIVER_ACCOUNT_NOT_FOUND | toAccount={}",
                            transferRequestDTO.getToAccountNum()
                    );
                    return new AccountNotFoundException("Receiver account not found");
                });
        Account senderAccount = checkAccount(transferRequestDTO.getFromAccountNum(), customer.getCustomerId());


        if (!passwordEncoder.matches(transferRequestDTO.getPin(), senderAccount.getPinHash())) {
            log.warn(
                    "Transfer failed | reason=WRONG_PIN | email={} | fromAccount={}",
                    email,
                    transferRequestDTO.getFromAccountNum()
            );
            throw new WrongPinException("Wrong PIN");
        }

        if (senderAccount.getAmount().compareTo(transferRequestDTO.getAmount()) < 0) {
            log.warn(
                    "Transfer failed | reason=INSUFFICIENT_BALANCE | email={} | fromAccount={} | balance={} | requestedAmount={}",
                    email,
                    transferRequestDTO.getFromAccountNum(),
                    senderAccount.getAmount(),
                    transferRequestDTO.getAmount()
            );
            throw new InsufficientBalanceException("Insufficient balance");
        }

        UUID transactionRefId = UUID.randomUUID();


        Transaction debitTxn = toEntityForTransferDebit(transferRequestDTO,senderAccount, transactionRefId);
        transactionRepository.save(debitTxn);

        log.info(
                "Debit transaction created | txnId={} | refId={} | fromAccount={} | amount={} | status=PENDING",
                debitTxn.getTransactionId(),
                transactionRefId,
                transferRequestDTO.getFromAccountNum(),
                transferRequestDTO.getAmount()
        );
        otpService.sendOtp(email, debitTxn.getTransactionId());

        log.debug("OTP sent for transfer | txnId={} | email={}", debitTxn.getTransactionId(), email);

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
        log.info("Transaction confirmation request | email={} | txnId={}", email, dto.getTransactionId());

        Transaction debitTx = transactionRepository
                .lockByTransactionId(dto.getTransactionId())
                .orElseThrow(() -> {
                    log.warn(
                            "Transaction confirmation failed | reason=TXN_NOT_FOUND | txnId={}",
                            dto.getTransactionId()
                    );
                    return new TransactionNotFoundException("Transaction not found");
                });

        if (debitTx.getTransactionStatus() == TransactionStatus.SUCCESS) {
            log.warn("Transaction already processed | txnId={}", debitTx.getTransactionId());
            throw new TransactionAlreadyProcessedException("Transaction already processed");
        }

        if (debitTx.getTransactionStatus() == TransactionStatus.FAILED) {
            log.warn("Transaction failed | txnId={}", debitTx.getTransactionId());
            throw new TransactionFailedException("Transaction failed!!! Create new transaction.");
        }

        if (!debitTx.getAccount().getCustomer().getEmail().equals(email)) {
            log.warn(
                    "Unauthorized transaction confirmation attempt | txnId={} | email={}",
                    debitTx.getTransactionId(),
                    email
            );
            throw new UnauthorizedException("Unauthorized");
        }
        log.debug("Verifying OTP | txnId={} | email={}", debitTx.getTransactionId(), email);

        otpService.verifyOtp(email, dto.getTransactionId(), dto.getOTP());

        Account sender = accountRepository.lockAccountById(debitTx.getAccount().getAccountId());

        if (sender.getAmount().compareTo(debitTx.getAmount()) < 0) {
            log.error(
                    "Insufficient balance at confirmation | txnId={} | accountNum={} | balance={} | required={}",
                    debitTx.getTransactionId(),
                    sender.getAccountNum(),
                    sender.getAmount(),
                    debitTx.getAmount()
            );
            throw new InsufficientBalanceException("Insufficient balance at confirmation");
        }

        BigDecimal oldSenderBalance = sender.getAmount();
        BigDecimal newSenderBalance = oldSenderBalance.subtract(debitTx.getAmount());

        sender.setAmount(newSenderBalance);

        debitTx.setTransactionStatus(TransactionStatus.SUCCESS);
        debitTx.setClosingBalance(newSenderBalance);

        accountRepository.save(sender);
        transactionRepository.save(debitTx);

        log.info(
                "Debit transaction confirmed | txnId={} | accountNum={} | oldBalance={} | newBalance={}",
                debitTx.getTransactionId(),
                sender.getAccountNum(),
                oldSenderBalance,
                newSenderBalance
        );

        evictBalanceCache(sender.getCustomer().getEmail(), sender.getAccountNum());

        if (debitTx.getOperationType() == OperationType.TRANSFER) {

            log.debug("Processing transfer credit leg | refId={}", debitTx.getTransactionReferenceId());

            Account receiver = accountRepository.lockByAccountNum(debitTx.getCounterpartyAccountNum());

            BigDecimal oldReceiverBalance = receiver.getAmount();
            BigDecimal newReceiverBalance = oldReceiverBalance.add(debitTx.getAmount());

            receiver.setAmount(newReceiverBalance);

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

            log.info(
                    "Credit transaction completed | creditTxnId={} | accountNum={} | oldBalance={} | newBalance={}",
                    creditTx.getTransactionId(),
                    receiver.getAccountNum(),
                    oldReceiverBalance,
                    newReceiverBalance
            );

            eventPublisher.publishEvent(new TransactionEvent(
                    debitTx.getTransactionId(),
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

            log.info(
                    "Withdraw transaction confirmed | txnId={} | accountNum={}",
                    debitTx.getTransactionId(),
                    sender.getAccountNum()
            );

            eventPublisher.publishEvent(new TransactionEvent(
                    debitTx.getTransactionId(),
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
