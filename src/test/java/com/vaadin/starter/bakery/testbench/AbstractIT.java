package com.vaadin.starter.bakery.testbench;

import java.net.URL;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import com.vaadin.starter.bakery.testbench.elements.ui.LoginViewElement;
import com.vaadin.starter.bakery.ui.utils.BakeryConst;
import com.vaadin.testbench.BrowserTestBase;
import com.vaadin.testbench.TestBenchDriverProxy;
import com.vaadin.testbench.TestBenchElement;

@QuarkusIntegrationTest
public abstract class AbstractIT<E extends TestBenchElement> extends BrowserTestBase {

	@TestHTTPResource("/")
	URL appUrl;

	static {
		// Let notifications persist longer during tests
		BakeryConst.NOTIFICATION_DURATION = 10000;
	}

	@BeforeEach
	void setup() throws Exception {
		getDriver().manage().window().setSize(new Dimension(1024, 800));
	}

	@Override
	public TestBenchDriverProxy getDriver() {
		return (TestBenchDriverProxy) super.getDriver();
	}

	protected String getAppUrl() {
		return appUrl.toString();
	}

	protected LoginViewElement openLoginView() {
		return openLoginView(getDriver(), getAppUrl());
	}

	protected LoginViewElement openLoginView(WebDriver driver, String url) {
		driver.get(url);
		return $(LoginViewElement.class).waitForFirst();
	}

	protected abstract E openView();

}
