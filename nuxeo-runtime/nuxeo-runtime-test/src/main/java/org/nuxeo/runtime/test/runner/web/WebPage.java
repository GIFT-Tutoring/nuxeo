/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.runtime.test.runner.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class WebPage {

    /**
     * Can be used by tests as default timeouts. This way you can change these
     * values to change all the timeout in the tests. DEFAULT_TIMEOUT is used
     * for regular timeouts (loading an ajax page an ajax dialog etc.) while
     * BIG_TIMEOUT should be used for pages that are slower (like the home of a
     * GWT application)
     */
    public static int DEFAULT_TIMEOUT = 5;

    public static int BIG_TIMEOUT = 15;

    private static final Map<Class<?>, WebPage> pages = new HashMap<Class<?>, WebPage>();

    @Inject
    protected Configuration config;

    @Inject
    protected WebDriver driver;

    @Inject
    protected FeaturesRunner runner;

    /**
     * Should be overridden by dynamic page (using ajax) to wait until the page
     * is completely loaded By default nothing is done (page is assumed to be
     * loaded)
     *
     * @return the page itself
     */
    public WebPage ensureLoaded() {
        return this; // do nothing by default
    }

    public Configuration getConfiguration() {
        return config;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public FeaturesRunner getRunner() {
        return runner;
    }

    public void home() {
        driver.get(config.home);
    }

    public void to(String path) {
        if (path.contains("://")) {
            driver.get(path);
        } else {
            try {
                URL url = new URL(new URL(config.home), path);
                driver.navigate().to(url);
            } catch (MalformedURLException e) {
                throw new WebDriverException(e);
            }
        }
    }

    public boolean hasElement(final By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public boolean hasElement(final By by, int timeoutInSeconds) {
        try {
            findElement(by, timeoutInSeconds);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public WebElement findElement(final By by) {
        return driver.findElement(by);
    }

    public List<WebElement> findElements(final By by) {
        return driver.findElements(by);
    }

    public WebElement findElement(final By by, int timeOutInSeconds) {
        return waitUntilElementFound(by, timeOutInSeconds);
    }

    public WebElement waitUntilElementFound(final By by, int timeOutInSeconds) {
        try {
            return findElement(by); // try once first
        } catch (NotFoundException e) {
            return new WebDriverWait(driver, timeOutInSeconds).until(new ExpectedCondition<WebElement>() {
                public WebElement apply(WebDriver arg0) {
                    return driver.findElement(by);
                }
            });
        }
    }

    public void waitUntilElementNotFound(final By by, int timeOutInSeconds) {
        try {
            findElement(by); // try once first
            new WebDriverWait(driver, timeOutInSeconds).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver arg0) {
                    try {
                        driver.findElement(by);
                        return Boolean.FALSE;
                    } catch (NotFoundException e) {
                        return Boolean.TRUE;
                    }
                }
            });
        } catch (NotFoundException e) {
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends WebPage> T getPage(Class<T> type) {
        T page = (T) pages.get(type);
        if (page == null) {
            synchronized (pages) {
                page = (T) pages.get(type);
                if (page != null) {
                    return page;
                }
                page = PageFactory.initElements(driver, type);
                runner.getInjector().injectMembers(page);
                pages.put(type, page);
            }
        }
        return (T) page.ensureLoaded(); // this will block until page is loaded
        // (if implementation needs this)
    }

    public static void flushPageCache() {
        synchronized (pages) {
            pages.clear();
        }
    }

}