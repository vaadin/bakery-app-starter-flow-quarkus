package com.vaadin.starter.bakery.ui.utils.converters;

import java.time.LocalTime;

import static com.vaadin.starter.bakery.ui.dataproviders.DataProviderUtil.convertIfNotNull;
import static com.vaadin.starter.bakery.ui.utils.FormattingUtils.HOUR_FORMATTER;

public class LocalTimeConverter {

	public String encode(LocalTime modelValue) {
		return convertIfNotNull(modelValue, HOUR_FORMATTER::format);
	}
}
