package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderSummary;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    // Entity graph queries using JOIN FETCH
    private static final String BRIEF_FETCH = "SELECT o FROM OrderInfo o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.pickupLocation";
    private static final String FULL_FETCH = "SELECT DISTINCT o FROM OrderInfo o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.pickupLocation LEFT JOIN FETCH o.items LEFT JOIN FETCH o.history";

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<Order> findByDueDateAfter(LocalDate filterDate, Page page) {
        return find(BRIEF_FETCH + " WHERE o.dueDate > ?1", filterDate).page(page).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<Order> findByCustomer_FullNameContainingIgnoreCase(String searchQuery, Page page) {
        return find(BRIEF_FETCH + " WHERE upper(o.customer.fullName) LIKE upper(?1)", "%" + searchQuery + "%").page(page).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<Order> findByCustomer_FullNameContainingIgnoreCaseAndDueDateAfter(String searchQuery, LocalDate dueDate, Page page) {
        return find(BRIEF_FETCH + " WHERE upper(o.customer.fullName) LIKE upper(?1) AND o.dueDate > ?2", "%" + searchQuery + "%", dueDate).page(page).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<Order> findAllBrief() {
        return find(BRIEF_FETCH).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<Order> findAllBrief(Page page) {
        return find(BRIEF_FETCH).page(page).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_BRIEF, type = EntityGraphType.LOAD)
    public List<OrderSummary> findByDueDateGreaterThanEqual(LocalDate dueDate) {
        return find(BRIEF_FETCH + " WHERE o.dueDate >= ?1", dueDate).project(OrderSummary.class).list();
    }

    // @EntityGraph(value = Order.ENTITY_GRAPTH_FULL, type = EntityGraphType.LOAD)
    public Optional<Order> findByIdFull(Long id) {
        return find(FULL_FETCH + " WHERE o.id = ?1", id).singleResultOptional();
    }

    public long countByDueDateAfter(LocalDate dueDate) {
        return count("dueDate > ?1", dueDate);
    }

    public long countByCustomer_FullNameContainingIgnoreCase(String searchQuery) {
        return count("upper(customer.fullName) LIKE upper(?1)", "%" + searchQuery + "%");
    }

    // should be countByCustomer_FullNameContainingIgnoreCaseAndDueDateAfter but gives the error
    // Unable to ignore case of com.vaadin.starter.bakery.backend.data.entity.Customer types, the property 'customer' must reference a String
    // May be a bug in spring.
    public long countByCustomer_FullNameContainingAndDueDateAfterAllIgnoreCase(String searchQuery, LocalDate dueDate) {
        return count("upper(customer.fullName) LIKE upper(?1) AND dueDate > ?2", "%" + searchQuery + "%", dueDate);
    }

    public long countByDueDate(LocalDate dueDate) {
        return count("dueDate = ?1", dueDate);
    }

    public long countByDueDateAndStateIn(LocalDate dueDate, Collection<OrderState> states) {
        return count("dueDate = ?1 AND state IN ?2", dueDate, states);
    }

    public long countByState(OrderState state) {
        return count("state = ?1", state);
    }

    // @Query("SELECT month(dueDate) as month, count(*) as deliveries FROM OrderInfo o where o.state=?1 and year(dueDate)=?2 group by month(dueDate)")
    public List<Object[]> countPerMonth(OrderState orderState, int year) {
        return getEntityManager()
                .createQuery("SELECT month(o.dueDate) as month, count(*) as deliveries FROM OrderInfo o WHERE o.state = ?1 AND year(o.dueDate) = ?2 GROUP BY month(o.dueDate)", Object[].class)
                .setParameter(1, orderState)
                .setParameter(2, year)
                .getResultList();
    }

    // @Query("SELECT year(o.dueDate) as y, month(o.dueDate) as m, sum(oi.quantity*p.price) as deliveries FROM OrderInfo o JOIN o.items oi JOIN oi.product p where o.state=?1 and year(o.dueDate)<=?2 AND year(o.dueDate)>=(?2-3) group by year(o.dueDate), month(o.dueDate) order by y desc, month(o.dueDate)")
    public List<Object[]> sumPerMonthLastThreeYears(OrderState orderState, int year) {
        return getEntityManager()
                .createQuery("SELECT year(o.dueDate) as y, month(o.dueDate) as m, sum(oi.quantity * p.price) as deliveries FROM OrderInfo o JOIN o.items oi JOIN oi.product p WHERE o.state = ?1 AND year(o.dueDate) <= ?2 AND year(o.dueDate) >= (?2 - 3) GROUP BY year(o.dueDate), month(o.dueDate) ORDER BY y DESC, month(o.dueDate)", Object[].class)
                .setParameter(1, orderState)
                .setParameter(2, year)
                .getResultList();
    }

    // @Query("SELECT day(dueDate) as day, count(*) as deliveries FROM OrderInfo o where o.state=?1 and year(dueDate)=?2 and month(dueDate)=?3 group by day(dueDate)")
    public List<Object[]> countPerDay(OrderState orderState, int year, int month) {
        return getEntityManager()
                .createQuery("SELECT day(o.dueDate) as day, count(*) as deliveries FROM OrderInfo o WHERE o.state = ?1 AND year(o.dueDate) = ?2 AND month(o.dueDate) = ?3 GROUP BY day(o.dueDate)", Object[].class)
                .setParameter(1, orderState)
                .setParameter(2, year)
                .setParameter(3, month)
                .getResultList();
    }

    // @Query("SELECT sum(oi.quantity), p FROM OrderInfo o JOIN o.items oi JOIN oi.product p WHERE o.state=?1 AND year(o.dueDate)=?2 AND month(o.dueDate)=?3 GROUP BY p.id ORDER BY p.id")
    public List<Object[]> countPerProduct(OrderState orderState, int year, int month) {
        return getEntityManager()
                .createQuery("SELECT sum(oi.quantity), p FROM OrderInfo o JOIN o.items oi JOIN oi.product p WHERE o.state = ?1 AND year(o.dueDate) = ?2 AND month(o.dueDate) = ?3 GROUP BY p.id ORDER BY p.id", Object[].class)
                .setParameter(1, orderState)
                .setParameter(2, year)
                .setParameter(3, month)
                .getResultList();
    }
}
