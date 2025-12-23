package com.vaadin.starter.bakery.ui.dataproviders;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.stream.Stream;

/**
 * Base class for filterable, pageable data providers.
 * Replaces org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider
 * for Quarkus compatibility.
 *
 * @param <T> the data type
 * @param <F> the filter type
 */
public abstract class FilterablePageableDataProvider<T, F> extends AbstractBackEndDataProvider<T, F> {

	private F currentFilter;

	/**
	 * Sets the filter and refreshes the data.
	 *
	 * @param filter the filter to apply
	 */
	public void setFilter(F filter) {
		this.currentFilter = filter;
		refreshAll();
	}

	/**
	 * Returns the current filter.
	 */
	protected F getCurrentFilter() {
		return currentFilter;
	}

	/**
	 * Returns the default sort orders to use when the query doesn't specify any.
	 */
	protected abstract List<QuerySortOrder> getDefaultSortOrders();

	/**
	 * Fetches data from the back end using the given page and sort configuration.
	 *
	 * @param query the query containing filter and pagination info
	 * @param page the Quarkus Page object for pagination
	 * @param sort the Quarkus Sort object for sorting
	 * @return a list of items
	 */
	protected abstract List<T> fetchFromBackEnd(Query<T, F> query, Page page, Sort sort);

	/**
	 * Counts the total number of items matching the filter.
	 * Subclasses should implement this to count items with the given filter.
	 */
	protected abstract int sizeInBackEndFiltered(Query<T, F> query);

	@Override
	protected int sizeInBackEnd(Query<T, F> query) {
		// Use current filter if query has no filter
		Query<T, F> effectiveQuery = query.getFilter().isPresent() ? query : createQueryWithFilter(query);
		return sizeInBackEndFiltered(effectiveQuery);
	}

	@Override
	protected Stream<T> fetchFromBackEnd(Query<T, F> query) {
		Page page = Page.of(query.getPage(), query.getPageSize());
		Sort sort = createSort(query);
		// Use current filter if query has no filter
		Query<T, F> effectiveQuery = query.getFilter().isPresent() ? query : createQueryWithFilter(query);
		return fetchFromBackEnd(effectiveQuery, page, sort).stream();
	}

	/**
	 * Creates a new query with the current filter applied.
	 */
	private Query<T, F> createQueryWithFilter(Query<T, F> originalQuery) {
		return new Query<>(
				originalQuery.getOffset(),
				originalQuery.getLimit(),
				originalQuery.getSortOrders(),
				originalQuery.getInMemorySorting(),
				currentFilter
		);
	}

	/**
	 * Creates a Quarkus Sort object from the query's sort orders.
	 */
	protected Sort createSort(Query<T, F> query) {
		List<QuerySortOrder> sortOrders = query.getSortOrders();
		if (sortOrders.isEmpty()) {
			sortOrders = getDefaultSortOrders();
		}

		if (sortOrders.isEmpty()) {
			return Sort.empty();
		}

		Sort sort = null;
		for (QuerySortOrder order : sortOrders) {
			Sort.Direction direction = order.getDirection() == SortDirection.ASCENDING
					? Sort.Direction.Ascending
					: Sort.Direction.Descending;
			if (sort == null) {
				sort = Sort.by(order.getSorted(), direction);
			} else {
				sort = sort.and(order.getSorted(), direction);
			}
		}
		return sort != null ? sort : Sort.empty();
	}
}
