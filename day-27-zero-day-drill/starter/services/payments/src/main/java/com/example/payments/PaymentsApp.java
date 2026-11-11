package com.example.payments;

public class PaymentsApp {

    public static void main(String[] args) {
        new PaymentService().pay("ada", "acc-1", "acc-2", 5000);
        System.out.println("payments: one payment made");
    }
}
