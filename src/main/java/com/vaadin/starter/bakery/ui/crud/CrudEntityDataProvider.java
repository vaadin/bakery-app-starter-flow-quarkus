package com.vaadin.starter.bakery.ui.crud;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import com.vaadin.starter.bakery.backend.service.FilterableCrudService;
import com.vaadin.starter.bakery.ui.dataproviders.FilterablePageableDataProvider;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

public class CrudEntityDataProvider<T extends AbstractEntity> extends FilterablePageableDataProvider<T, String> {

	private final FilterableCrudService<T> crudService;
	private List<QuerySortOrder> defaultSortOrders;

	public CrudEntityDataProvider(FilterableCrudService<T> crudService) {
		this.crudService = crudService;
		setSortOrders();
	}

	private void setSortOrders() {
		QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
		builder.thenAsc("id");
		defaultSortOrders = builder.build();
	}

	@Override
	protected List<T> fetchFromBackEnd(Query<T, String> query, Page page, Sort sort) {
		return crudService.findAnyMatching(query.getFilter(), page);
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		return defaultSortOrders;
	}

	@Override
	protected int sizeInBackEndFiltered(Query<T, String> query) {
		return (int) crudService.countAnyMatching(query.getFilter());
	}

}
