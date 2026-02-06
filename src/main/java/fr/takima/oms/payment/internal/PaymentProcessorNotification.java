package fr.takima.oms.payment.internal;

public record PaymentProcessorNotification(PaymentDetails paymentDetails) {
    public record PaymentDetails(String rrn) {
    }
}
