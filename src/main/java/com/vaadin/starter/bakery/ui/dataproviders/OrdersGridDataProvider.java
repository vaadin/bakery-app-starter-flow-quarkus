package com.vaadin.starter.bakery.ui.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.service.OrderService;
import com.vaadin.starter.bakery.ui.utils.BakeryConst;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A pageable order data provider.
 */
@Dependent
public class OrdersGridDataProvider extends FilterablePageableDataProvider<Order, OrdersGridDataProvider.OrderFilter> {

	public static class OrderFilter implements Serializable {
		private String filter;
		private boolean showPrevious;

		public String getFilter() {
			return filter;
		}

		public boolean isShowPrevious() {
			return showPrevious;
		}

		public OrderFilter(String filter, boolean showPrevious) {
			this.filter = filter;
			this.showPrevious = showPrevious;
		}

		public static OrderFilter getEmptyFilter() {
			return new OrderFilter("", false);
		}
	}

	private final OrderService orderService;
	private List<QuerySortOrder> defaultSortOrders;
	private Consumer<List<Order>> pageObserver;

	@Inject
	public OrdersGridDataProvider(OrderService orderService) {
		this.orderService = orderService;
		setSortOrders(BakeryConst.DEFAULT_SORT_DIRECTION, BakeryConst.ORDER_SORT_FIELDS);
	}

	private void setSortOrders(SortDirection direction, String[] properties) {
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		for (String property : properties) {
			if (direction == SortDirection.ASCENDING) {
				builder.thenAsc(property);
			} else {
				builder.thenDesc(property);
			}
		}
		defaultSortOrders = builder.build();
	}

	@Override
	protected List<Order> fetchFromBackEnd(Query<Order, OrderFilter> query, Page page, Sort sort) {
		OrderFilter filter = query.getFilter().orElse(OrderFilter.getEmptyFilter());
		List<Order> orders = orderService.findAnyMatchingAfterDueDate(
				Optional.ofNullable(filter.getFilter()),
				getFilterDate(filter.isShowPrevious()),
				page);
		if (pageObserver != null) {
			pageObserver.accept(orders);
		}
		return orders;
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		return defaultSortOrders;
	}

	@Override
	protected int sizeInBackEndFiltered(Query<Order, OrderFilter> query) {
		OrderFilter filter = query.getFilter().orElse(OrderFilter.getEmptyFilter());
		return (int) orderService
				.countAnyMatchingAfterDueDate(Optional.ofNullable(filter.getFilter()), getFilterDate(filter.isShowPrevious()));
	}

	private Optional<LocalDate> getFilterDate(boolean showPrevious) {
		if (showPrevious) {
			return Optional.empty();
		}

		return Optional.of(LocalDate.now().minusDays(1));
	}

	public void setPageObserver(Consumer<List<Order>> pageObserver) {
		this.pageObserver = pageObserver;
	}

	@Override
	public Object getId(Order item) {
		return item.getId();
	}
}
