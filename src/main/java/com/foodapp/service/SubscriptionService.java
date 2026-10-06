package com.foodapp.service;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.*;
import com.foodapp.dto.SubscriptionDtos;
import com.foodapp.repository.*;
import org.springframework.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDeliveryRepository deliveryRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final @Nullable NotificationService notificationService;
    private final @Nullable RewardService rewardService;
    private final List<String> activePartners = List.of("Ravi Kumar", "Aisha Khan", "Sandeep Naik", "Priya Das");
    private final AtomicInteger partnerCursor = new AtomicInteger(0);
    private final DeliverySkipRepository deliverySkipRepository;

    @Transactional
    public SubscriptionDtos.SubscriptionResponse create(Long userId, SubscriptionDtos.CreateSubscriptionRequest request) {
        if (subscriptionRepository.existsByUserIdAndPlanIdAndStatusIn(userId, request.planId(),
                List.of(Enums.SubscriptionStatus.ACTIVE, Enums.SubscriptionStatus.PAUSED))) {
            throw new IllegalStateException("You already have this plan active");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Seller seller = sellerRepository.findById(request.sellerId()).orElseThrow();
        SubscriptionPlan plan = planRepository.findById(request.planId()).orElseThrow();

        // Initialize reward for user if not exists
        if (rewardService != null) {
            rewardService.initializeReward(userId);
        }

        log.info("Creating subscription for user {} with plan {} from seller {}", user.getFullName(), plan.getName(), seller.getBrandName());
        Subscription s = new Subscription();
        s.setUser(user);
        s.setSeller(seller);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(request.startDate().plusDays(plan.getDurationDays() - 1L));
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
        log.info("Subscription details: startDate={}, endDate={}", s.getStartDate(), s.getEndDate());
        subscriptionRepository.save(s);

        LocalDate current = s.getStartDate();
        while (!current.isAfter(s.getEndDate())) {
            SubscriptionDelivery d = new SubscriptionDelivery();
            d.setSubscription(s);
            d.setDeliveryDate(current);
            d.setStatus(Enums.DeliveryStatus.PENDING);
            d.setTimeZone("Asia/Kolkata");
            d.setMobileNumber(user.getPhone() == null ? "9999999999" : user.getPhone());
            d.setDeliveryBy(pickActivePartner());
            d.setLiveTrackingUrl("https://tracking.foodapp.local/order/" + s.getId() + "/date/" + current);
            deliveryRepository.save(d);
            current = current.plusDays(1);
            log.info("Scheduled delivery for date {}", d.getDeliveryDate());
        }

        List<LocalDate> dates = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId())
                .stream().map(SubscriptionDelivery::getDeliveryDate).toList();
        log.info("Created subscription with ID {} and delivery dates: {}", s.getId(), dates);

        if (notificationService != null) {
            notificationService.notifyOrderCreated(
                    user.getEmail(),
                    seller.getUser().getEmail(),
                    "delivery.partner@foodapp.local",
                    s.getId()
            );
        }

        return toResponse(s, deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId()));
    }

    @Transactional
    public void pause(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Pausing subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
        s.setStatus(Enums.SubscriptionStatus.PAUSED);
    }

    @Transactional
    public void resume(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Resuming subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
    }

    @Transactional
    public void cancel(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Cancelling subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
        s.setStatus(Enums.SubscriptionStatus.CANCELLED);
    }

    @Transactional
    public void skipDay(Long subscriptionId, LocalDate date) {
        SubscriptionDelivery d = deliveryRepository.findBySubscriptionIdAndDeliveryDate(subscriptionId, date).orElseThrow();
        d.setStatus(Enums.DeliveryStatus.SKIPPED);
    }

    @Transactional
    public void editActiveDelivery(Long subscriptionId, SubscriptionDtos.EditDeliveryRequest request) {
        if (request == null || request.date() == null || request.date().isBlank()) {
            throw new IllegalArgumentException("Missing or empty date. Use yyyy-MM-dd");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(request.date()); // expects yyyy-MM-dd
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        }

        SubscriptionDelivery d = deliveryRepository.findBySubscriptionIdAndDeliveryDate(subscriptionId, date)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for date: " + date));

        // Optional: ownership check if you require X-User-Id
        // if (!d.getSubscription().getUser().getId().equals(userIdFromHeader)) throw new AccessDeniedException("Not allowed");

        if (request.timeZone() != null) d.setTimeZone(request.timeZone());
        if (request.mobileNumber() != null) d.setMobileNumber(request.mobileNumber());
        deliveryRepository.save(d);
    }

    @Transactional
    public void markDelivered(Long subscriptionId, SubscriptionDtos.DeliveryActionRequest request) {
        SubscriptionDelivery d = deliveryRepository.findBySubscriptionIdAndDeliveryDate(subscriptionId, request.date()).orElseThrow();
        d.setStatus(Enums.DeliveryStatus.DELIVERED);
        d.setDeliveredBy(d.getDeliveryBy());
        d.setDeliveredOn(LocalDateTime.now());
        d.setRating(request.rating());
        d.setFeedback(request.feedback());

        // Award points to user
        if (rewardService != null) {
            Subscription subscription = d.getSubscription();
            rewardService.addPoints(subscription.getUser().getId(), 50);  // 50 points per delivery
        }
    }

    public List<SubscriptionDtos.SubscriptionResponse> activeOrders(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getStatus() == Enums.SubscriptionStatus.ACTIVE)
                .map(s -> {
                    List<SubscriptionDelivery> deliveries = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId());
                    List<SubscriptionDelivery> activeDeliveries = deliveries.stream()
                            .filter(d -> d.getStatus() == Enums.DeliveryStatus.PENDING || d.getStatus() == Enums.DeliveryStatus.SKIPPED)
                            .toList();
                    return toResponse(s, activeDeliveries);
                })
                .toList();
    }

    public List<SubscriptionDtos.SubscriptionResponse> pastOrders(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(s -> {
                    List<SubscriptionDelivery> deliveries = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId()).stream()
                            .filter(d -> d.getStatus() == Enums.DeliveryStatus.DELIVERED || d.getStatus() == Enums.DeliveryStatus.MISSED)
                            .toList();
                    return toResponse(s, deliveries);
                })
                .filter(r -> !r.deliveries().isEmpty())
                .toList();
    }

    public List<SubscriptionDtos.SubscriptionResponse> byUser(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream().map(s -> {
            List<SubscriptionDelivery> deliveries = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId());
            return toResponse(s, deliveries);
        }).toList();
    }

    private SubscriptionDtos.SubscriptionResponse toResponse(Subscription s, List<SubscriptionDelivery> deliveries) {
        List<SubscriptionDtos.DeliverySummary> summaries = deliveries.stream()
                .map(d -> new SubscriptionDtos.DeliverySummary(
                        d.getDeliveryDate(),
                        d.getStatus().name(),
                        d.getDeliveredBy(),
                        d.getDeliveredOn(),
                        d.getRating(),
                        d.getFeedback(),
                        d.getDeliveryBy(),
                        d.getTimeZone(),
                        d.getMobileNumber(),
                        d.getLiveTrackingUrl()
                ))
                .toList();

        String weeklyAmount = s.getPlan().getPrice()
                .multiply(java.math.BigDecimal.valueOf(7))
                .divide(java.math.BigDecimal.valueOf(s.getPlan().getDurationDays()), 2, RoundingMode.HALF_UP)
                .toPlainString();

        // Get weekly menu
        List<Menu> weeklyMenus = menuRepository.findBySellerIdAndMenuType(s.getSeller().getId(), Enums.MenuType.WEEKLY);
        SubscriptionDtos.WeeklyMenu weeklyMenu = null;
        if (!weeklyMenus.isEmpty()) {
            Menu menu = weeklyMenus.get(0);  // Get first available weekly menu
            List<com.foodapp.domain.entity.MenuItem> menuItems = menuItemRepository.findByMenuId(menu.getId());
            List<SubscriptionDtos.MenuItem> items = menuItems.stream()
                    .map(item -> new SubscriptionDtos.MenuItem(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getPrice().toPlainString()
                    ))
                    .toList();
            weeklyMenu = new SubscriptionDtos.WeeklyMenu(menu.getId(), menu.getTitle(), items);
        } else {
            // Create a placeholder menu
            weeklyMenu = new SubscriptionDtos.WeeklyMenu(0L, "Weekly Menu", List.of());
        }

        return new SubscriptionDtos.SubscriptionResponse(
                s.getId(),
                s.getStatus().name(),
                s.getSeller().getBrandName(),
                s.getPlan().getName(),
                weeklyMenu,
                weeklyAmount,
                summaries
        );
    }

    private String pickActivePartner() {
        int index = Math.floorMod(partnerCursor.getAndIncrement(), activePartners.size());
        return activePartners.get(index);
    }

    @Transactional
    public void skipDelivery(Long userId, Long subscriptionId, Long deliveryId, String reason) {
        SubscriptionDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        // Validate subscription id matches
        if (!delivery.getSubscription().getId().equals(subscriptionId)) {
            throw new IllegalArgumentException("Delivery does not belong to subscription");
        }

        // Authorization: only owner can skip
        if (!delivery.getSubscription().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not allowed");
        }

        // Business rule: cutoff time (example: 24 hours)
        Instant cutoff = delivery.getDeliveryDate().atStartOfDay(ZoneId.of(delivery.getTimeZone() == null ? "Asia/Kolkata" : delivery.getTimeZone()))
                .toInstant().minus(Duration.ofHours(24));
        if (Instant.now().isAfter(cutoff)) {
            throw new IllegalStateException("Cannot skip delivery after cutoff time");
        }

        // Idempotent: if already skipped, return
        if (delivery.getStatus() == Enums.DeliveryStatus.SKIPPED) {
            return;
        }

        // Optional: create skip record for audit/credits
        DeliverySkip skip = new DeliverySkip();
        skip.setScheduledDelivery(delivery);
        skip.setSubscription(delivery.getSubscription());
        skip.setUserId(userId);
        skip.setReason(reason);
        skip.setCreatedAt(Instant.now());
        // save skip via skipRepo (inject it)
        if (deliverySkipRepository != null) deliverySkipRepository.save(skip);

        // Update delivery status
        delivery.setStatus(Enums.DeliveryStatus.SKIPPED);
        deliveryRepository.save(delivery);

        // Optional: notify kitchen/delivery partner and create credit
        if (notificationService != null) {
            notificationService.notifyDeliverySkipped(delivery.getSubscription().getUser().getEmail(), delivery.getSubscription().getId(), delivery.getDeliveryDate());
        }
    }

    @Transactional
    public void unskipDelivery(Long userId, Long subscriptionId, Long deliveryId) {
        SubscriptionDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        if (!delivery.getSubscription().getId().equals(subscriptionId)) {
            throw new IllegalArgumentException("Delivery does not belong to subscription");
        }

        if (!delivery.getSubscription().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not allowed");
        }

        // Only allow undo if within allowed window (e.g., before cutoff)
        Instant cutoff = delivery.getDeliveryDate().atStartOfDay(ZoneId.of(delivery.getTimeZone() == null ? "Asia/Kolkata" : delivery.getTimeZone()))
                .toInstant().minus(Duration.ofHours(24));
        if (Instant.now().isAfter(cutoff)) {
            throw new IllegalStateException("Cannot undo skip after cutoff time");
        }

        if (delivery.getStatus() != Enums.DeliveryStatus.SKIPPED) {
            return;
        }

        // Remove skip record if exists (skipRepo.deleteByScheduledDeliveryId(...))
        if (deliverySkipRepository != null) deliverySkipRepository.deleteByScheduledDeliveryId(delivery.getId());

        delivery.setStatus(Enums.DeliveryStatus.PENDING);
        deliveryRepository.save(delivery);

        if (notificationService != null) {
            notificationService.notifyDeliveryUnskipped(delivery.getSubscription().getUser().getEmail(), delivery.getSubscription().getId(), delivery.getDeliveryDate());
        }
    }
}
