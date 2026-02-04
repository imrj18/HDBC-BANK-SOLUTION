package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TransactionEmailTemplateService {

    public String buildEmailBody(TransactionEvent event) {

        log.debug(
                "Building transaction email template | transactionId={} | operation={} | status={}",
                event.getTransactionId(),
                event.getOperationType(),
                event.getStatus()
        );

        switch (event.getOperationType()) {

            case WITHDRAW:
                return withdrawMessage(event);

            case DEPOSIT:
                return depositMessage(event);

            case TRANSFER:
                return transferMessage(event);

            default:
                log.warn(
                        "Unknown transaction operation type | transactionId={} | operation={}",
                        event.getTransactionId(),
                        event.getOperationType()
                );
                return "Transaction update for ID: " + event.getTransactionId();
        }
    }

    private String withdrawMessage(TransactionEvent event) {

        log.debug(
                "Creating withdraw email body | transactionId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );

        if (event.getStatus() == TransactionStatus.SUCCESS) {
            return "₹" + event.getAmount()
                    + " has been debited successfully.\n"
                    + "Transaction ID: " + event.getTransactionId();
        }
        return "Withdrawal failed.\nTransaction ID: " + event.getTransactionId();
    }

    private String depositMessage(TransactionEvent event) {

        log.debug("Creating deposit email body | transactionId={}", event.getTransactionId());

        return "₹" + event.getAmount()
                + " has been credited successfully.\n"
                + "Transaction ID: " + event.getTransactionId();
    }

    private String transferMessage(TransactionEvent event) {

        log.debug(
                "Creating transfer email body | transactionId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );

        if (event.getStatus() == TransactionStatus.SUCCESS) {
            return "₹" + event.getAmount()
                    + " transferred successfully.\n"
                    + "Transaction ID: " + event.getTransactionId();
        }
        return "Transfer failed.\nTransaction ID: " + event.getTransactionId();
    }
}
