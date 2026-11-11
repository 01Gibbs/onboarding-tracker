package com.example.notifications;

public class NotificationsApp {

    public static void main(String[] args) {
        System.out.println(new NotificationService().paymentReceived("grace", 5000));
    }
}
