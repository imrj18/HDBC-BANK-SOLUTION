package com.example.notification_microservice.service;

import com.example.notification_microservice.enums.TransactionStatus;


import com.hdbc.dto.event.TransactionEvent;
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

            case "WITHDRAW":
                return withdrawMessage(event);

            case "DEPOSIT":
                return depositMessage(event);

            case "TRANSFER":
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

        if (event.getStatus().equals(TransactionStatus.SUCCESS.toString())) {
            return "Dear Customer,\n\n"
                    + "We would like to inform you that a debit transaction of ₹"
                    + event.getAmount() + " has been successfully processed from your account.\n\n"
                    + "Transaction Details:\n"
                    + "Transaction ID: " + event.getTransactionId() + "\n"
                    + "Status: Successful\n\n"
                    + "If you did not authorize this transaction or have any concerns, "
                    + "please contact our support team immediately.\n\n"
                    + "Thank you for banking with us.\n\n"
                    + "Warm regards,\n"
                    + "Customer Support Team";
        }
        return "Dear Customer,\n\n"
                + "We regret to inform you that your recent withdrawal attempt was unsuccessful.\n\n"
                + "Transaction Details:\n"
                + "Transaction ID: " + event.getTransactionId() + "\n"
                + "Status: Failed\n\n"
                + "Please try again after some time. If the issue persists, "
                + "feel free to reach out to our support team for assistance.\n\n"
                + "Thank you for your patience.\n\n"
                + "Warm regards,\n"
                + "Customer Support Team";
    }

    private String depositMessage(TransactionEvent event) {

        log.debug("Creating deposit email body | transactionId={}", event.getTransactionId());

        return "Dear Customer,\n\n"
                + "We are pleased to inform you that an amount of ₹"
                + event.getAmount()
                + " has been successfully credited to your account.\n\n"
                + "Transaction Details:\n"
                + "Transaction ID: " + event.getTransactionId() + "\n"
                + "Status: Successful\n\n"
                + "If you did not authorize this transaction, please contact our support team immediately.\n\n"
                + "Thank you for banking with us.\n\n"
                + "Warm regards,\n"
                + "Customer Support Team";
    }

    private String transferMessage(TransactionEvent event) {

        log.debug(
                "Creating transfer email body | transactionId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );

        if (event.getStatus().equals(TransactionStatus.SUCCESS.toString())) {
            return "Dear Customer,\n\n"
                    + "Your transfer of ₹" + event.getAmount()
                    + " has been successfully processed.\n\n"
                    + "Transaction Details:\n"
                    + "Transaction ID: " + event.getTransactionId() + "\n"
                    + "Status: Successful\n\n"
                    + "If you did not authorize this transaction, please contact our support team immediately.\n\n"
                    + "Thank you for using our services.\n\n"
                    + "Warm regards,\n"
                    + "Customer Support Team";
        }
        return "Dear Customer,\n\n"
                + "We regret to inform you that your recent transfer attempt was unsuccessful.\n\n"
                + "Transaction Details:\n"
                + "Transaction ID: " + event.getTransactionId() + "\n"
                + "Status: Failed\n\n"
                + "Please try again later. If the issue persists, contact our support team for assistance.\n\n"
                + "Thank you for your patience.\n\n"
                + "Warm regards,\n"
                + "Customer Support Team";
    }

    public String buildFailureEmail(TransactionEvent event) {

        log.debug(
                "Building failure email template | transactionId={} | reason={}",
                event.getTransactionId(),
                event.getMessage()
        );

        return """
            Dear Customer,

            We regret to inform you that your transaction has failed.

            Transaction Details:
            ---------------------
            Transaction ID : %s
            Amount         : ₹%s
            Operation      : %s
            Status         : %s
            Reason         : %s

            If you did not initiate this transaction or need assistance,
            please contact customer support immediately.

            Regards,
            HDBC Bank
            """.formatted(
                event.getTransactionId(),
                event.getAmount(),
                event.getOperationType(),
                event.getStatus(),
                event.getMessage() != null ? event.getMessage() : "Unknown"
        );
    }

}
