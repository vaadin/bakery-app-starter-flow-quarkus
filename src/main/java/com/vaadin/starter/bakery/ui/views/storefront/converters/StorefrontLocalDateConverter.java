package com.vaadin.starter.bakery.ui.views.storefront.converters;

import java.time.LocalDate;

import static com.vaadin.starter.bakery.ui.utils.FormattingUtils.MONTH_AND_DAY_FORMATTER;
import static com.vaadin.starter.bakery.ui.utils.FormattingUtils.WEEKDAY_FULLNAME_FORMATTER;

/**
 * Date converter specific for the way date is displayed in storefront.
 */
public class StorefrontLocalDateConverter {

	public StorefrontDate encode(LocalDate modelValue) {
		StorefrontDate result = null;
		if (modelValue != null) {
			result = new StorefrontDate();
			result.setDay(MONTH_AND_DAY_FORMATTER.format(modelValue));
			result.setWeekday(WEEKDAY_FULLNAME_FORMATTER.format(modelValue));
			result.setDate(modelValue.toString());
		}
		return result;
	}
}
