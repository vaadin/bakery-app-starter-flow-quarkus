package com.vaadin.starter.bakery.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.starter.bakery.ui.views.orderedit.HistoryItemDisplayData;
import com.vaadin.starter.bakery.ui.views.orderedit.OrderDisplayData;
import com.vaadin.starter.bakery.ui.views.orderedit.OrderItemDisplayData;
import com.vaadin.starter.bakery.ui.views.orderedit.ProductDisplayData;
import com.vaadin.starter.bakery.ui.views.storefront.OrderCard;
import com.vaadin.starter.bakery.ui.views.storefront.beans.OrderCardHeader;
import com.vaadin.starter.bakery.ui.views.storefront.converters.StorefrontDate;
import io.quarkus.runtime.annotations.RegisterForReflection;

import static com.vaadin.starter.bakery.ui.utils.BakeryConst.VIEWPORT;

@Viewport(VIEWPORT)
@Theme("bakery")
@PWA(name = "Bakery App Starter", shortName = "###Bakery###",
		startPath = "login",
		backgroundColor = "#227aef", themeColor = "#227aef",
		offlinePath = "offline-page.html",
		offlineResources = {"images/offline-login-banner.jpg"})
@RegisterForReflection(targets = {
		StorefrontDate.class, OrderCardHeader.class, OrderCard.class,
		OrderDisplayData.class, OrderItemDisplayData.class, ProductDisplayData.class,
		HistoryItemDisplayData.class
}, classNames = {
		"com.vaadin.starter.bakery.backend.data.entity.Order",
		"com.vaadin.starter.bakery.backend.data.entity.OrderItem",
		"com.vaadin.starter.bakery.backend.data.OrderState"
})
public class AppShell implements AppShellConfigurator {
}