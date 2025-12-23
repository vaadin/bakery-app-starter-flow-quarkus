package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.DashboardData;
import com.vaadin.starter.bakery.backend.data.DeliveryStats;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderSummary;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.OrderRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@ApplicationScoped
@Transactional
public class OrderService implements CrudService<Order> {

    private final OrderRepository orderRepository;

    @Inject
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private static final Set<OrderState> notAvailableStates = Collections.unmodifiableSet(
            EnumSet.complementOf(EnumSet.of(OrderState.DELIVERED, OrderState.READY, OrderState.CANCELLED)));

    @Transactional(rollbackOn = Exception.class)
    public Order saveOrder(User currentUser, Long id, BiConsumer<User, Order> orderFiller) {
        Order order;
        if (id == null) {
            order = new Order(currentUser);
        } else {
            order = load(id);
        }
        orderFiller.accept(currentUser, order);
        return save(currentUser, order);
    }

    @Transactional(rollbackOn = Exception.class)
    public Order saveOrder(Order order) {
        return save(null, order);
    }

    @Transactional(rollbackOn = Exception.class)
    public Order addComment(User currentUser, Order order, String comment) {
        order.addHistoryItem(currentUser, comment);
        return save(currentUser, order);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Order> findAnyMatchingAfterDueDate(Optional<String> optionalFilter,
            Optional<LocalDate> optionalFilterDate, Page page) {
        if (optionalFilter.isPresent() && !optionalFilter.get().isEmpty()) {
            if (optionalFilterDate.isPresent()) {
                return orderRepository.findByCustomer_FullNameContainingIgnoreCaseAndDueDateAfter(
                        optionalFilter.get(), optionalFilterDate.get(), page);
            } else {
                return orderRepository.findByCustomer_FullNameContainingIgnoreCase(optionalFilter.get(), page);
            }
        } else {
            if (optionalFilterDate.isPresent()) {
                return orderRepository.findByDueDateAfter(optionalFilterDate.get(), page);
            } else {
                return orderRepository.findAllBrief(page);
            }
        }
    }

    @Transactional
    public List<OrderSummary> findAnyMatchingStartingToday() {
        return orderRepository.findByDueDateGreaterThanEqual(LocalDate.now());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public long countAnyMatchingAfterDueDate(Optional<String> optionalFilter, Optional<LocalDate> optionalFilterDate) {
        if (optionalFilter.isPresent() && optionalFilterDate.isPresent()) {
            return orderRepository.countByCustomer_FullNameContainingAndDueDateAfterAllIgnoreCase(optionalFilter.get(),
                    optionalFilterDate.get());
        } else if (optionalFilter.isPresent()) {
            return orderRepository.countByCustomer_FullNameContainingIgnoreCase(optionalFilter.get());
        } else if (optionalFilterDate.isPresent()) {
            return orderRepository.countByDueDateAfter(optionalFilterDate.get());
        } else {
            return orderRepository.count();
        }
    }

    private DeliveryStats getDeliveryStats() {
        DeliveryStats stats = new DeliveryStats();
        LocalDate today = LocalDate.now();
        stats.setDueToday((int) orderRepository.countByDueDate(today));
        stats.setDueTomorrow((int) orderRepository.countByDueDate(today.plusDays(1)));
        stats.setDeliveredToday((int) orderRepository.countByDueDateAndStateIn(today,
                Collections.singleton(OrderState.DELIVERED)));

        stats.setNotAvailableToday((int) orderRepository.countByDueDateAndStateIn(today, notAvailableStates));
        stats.setNewOrders((int) orderRepository.countByState(OrderState.NEW));

        return stats;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public DashboardData getDashboardData(int month, int year) {
        DashboardData data = new DashboardData();
        data.setDeliveryStats(getDeliveryStats());
        data.setDeliveriesThisMonth(getDeliveriesPerDay(month, year));
        data.setDeliveriesThisYear(getDeliveriesPerMonth(year));

        Number[][] salesPerMonth = new Number[3][12];
        data.setSalesPerMonth(salesPerMonth);
        List<Object[]> sales = orderRepository.sumPerMonthLastThreeYears(OrderState.DELIVERED, year);

        for (Object[] salesData : sales) {
            // year, month, deliveries
            int y = year - (int) salesData[0];
            int m = (int) salesData[1] - 1;
            if (y == 0 && m == month - 1) {
                // skip current month as it contains incomplete data
                continue;
            }
            long count = (long) salesData[2];
            salesPerMonth[y][m] = count;
        }

        LinkedHashMap<Product, Integer> productDeliveries = new LinkedHashMap<>();
        data.setProductDeliveries(productDeliveries);
        for (Object[] result : orderRepository.countPerProduct(OrderState.DELIVERED, year, month)) {
            int sum = ((Long) result[0]).intValue();
            Product p = (Product) result[1];
            productDeliveries.put(p, sum);
        }

        return data;
    }

    private List<Number> getDeliveriesPerDay(int month, int year) {
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        return flattenAndReplaceMissingWithNull(daysInMonth,
                orderRepository.countPerDay(OrderState.DELIVERED, year, month));
    }

    private List<Number> getDeliveriesPerMonth(int year) {
        return flattenAndReplaceMissingWithNull(12, orderRepository.countPerMonth(OrderState.DELIVERED, year));
    }

    private List<Number> flattenAndReplaceMissingWithNull(int length, List<Object[]> list) {
        List<Number> counts = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            counts.add(null);
        }

        for (Object[] result : list) {
            counts.set((Integer) result[0] - 1, (Number) result[1]);
        }
        return counts;
    }

    @Override
    public OrderRepository getRepository() {
        return orderRepository;
    }


    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Order load(long id) {
        // Use full entity graph for loading orders
        Order entity = orderRepository.findByIdFull(id).orElse(null);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

    @Override
    public Order createNew(User currentUser) {
        Order order = new Order(currentUser);
        order.setDueTime(LocalTime.of(16, 0));
        order.setDueDate(LocalDate.now());
        return order;
    }
}
