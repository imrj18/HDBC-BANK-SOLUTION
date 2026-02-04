package com.ritik.customer_microservice.serviceImpl;

import com.ritik.customer_microservice.dto.event.TransactionEvent;
import com.ritik.customer_microservice.enums.TransactionStatus;
import org.springframework.stereotype.Service;


@Service
public class TransactionEmailTemplateService {

    public String buildEmailBody(TransactionEvent event) {

        switch (event.getOperationType()) {

            case WITHDRAW:
                return withdrawMessage(event);

            case DEPOSIT:
                return depositMessage(event);

            case TRANSFER:
                return transferMessage(event);

            default:
                return "Transaction update for ID: " + event.getTransactionId();
        }
    }

    private String withdrawMessage(TransactionEvent event) {
        if (event.getStatus() == TransactionStatus.SUCCESS) {
            return "₹" + event.getAmount()
                    + " has been debited successfully.\n"
                    + "Transaction ID: " + event.getTransactionId();
        }
        return "Withdrawal failed.\nTransaction ID: " + event.getTransactionId();
    }

    private String depositMessage(TransactionEvent event) {
        return "₹" + event.getAmount()
                + " has been credited successfully.\n"
                + "Transaction ID: " + event.getTransactionId();
    }

    private String transferMessage(TransactionEvent event) {
        if (event.getStatus() == TransactionStatus.SUCCESS) {
            return "₹" + event.getAmount()
                    + " transferred successfully.\n"
                    + "Transaction ID: " + event.getTransactionId();
        }
        return "Transfer failed.\nTransaction ID: " + event.getTransactionId();
    }
}
