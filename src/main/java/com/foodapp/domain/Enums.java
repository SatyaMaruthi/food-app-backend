package com.foodapp.domain;

public class Enums {
    public enum Role { USER, SELLER, ADMIN }
    public enum SellerStatus { PENDING, VERIFIED, REJECTED }
    public enum MenuType { DAILY, WEEKLY }
    public enum PlanStatus { ACTIVE, INACTIVE }
    public enum SubscriptionStatus { ACTIVE, PAUSED, CANCELLED, COMPLETED }
    public enum DeliveryStatus { PENDING, SKIPPED, DELIVERED, MISSED }
    public enum PaymentStatus { INITIATED, SUCCESS, FAILED, REFUNDED, PARTIAL_REFUNDED }
    public enum PaymentProvider { STRIPE, RAZORPAY }
}
