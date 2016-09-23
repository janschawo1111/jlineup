package de.otto.jlineup.browser;

import com.google.gson.annotations.SerializedName;
import de.otto.jlineup.config.Config;
import de.otto.jlineup.config.Cookie;
import de.otto.jlineup.config.Parameters;
import de.otto.jlineup.file.FileService;
import de.otto.jlineup.image.ImageService;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.otto.jlineup.browser.BrowserUtils.buildUrl;
import static de.otto.jlineup.file.FileService.AFTER;
import static de.otto.jlineup.file.FileService.BEFORE;

public class Browser implements AutoCloseable{

    private static final Logger LOG = LoggerFactory.getLogger(Browser.class);
    private static final int MAX_SCROLL_HEIGHT = 100000;

    public enum Type {
        @SerializedName(value = "Firefox", alternate = {"firefox", "FIREFOX"})
        FIREFOX,
        @SerializedName(value = "Chrome", alternate = {"chrome", "CHROME"})
        CHROME,
        @SerializedName(value = "PhantomJS", alternate = {"phantomjs", "PHANTOMJS"})
        PHANTOMJS
    }

    static final String JS_DOCUMENT_HEIGHT_CALL = "return Math.max( document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight );";
    static final String JS_CLIENT_VIEWPORT_HEIGHT_CALL = "return document.documentElement.clientHeight";
    static final String JS_SET_LOCAL_STORAGE_CALL = "localStorage.setItem('%s','%s')";
    static final String JS_SCROLL_CALL = "window.scrollBy(0,%d)";

    final private Parameters parameters;
    final private Config config;
    final private WebDriver driver;
    final private FileService fileService;

    public Browser(Parameters parameters, Config config, WebDriver driver, FileService fileService) {
        this.parameters = parameters;
        this.config = config;
        this.driver = driver;
        this.fileService = fileService;
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    }

    public void takeScreenshots() throws IOException, InterruptedException {
        boolean before = !parameters.isAfter();
        List<ScreenshotContext> screenshotContextList = BrowserUtils.buildScreenshotContextListFromConfigAndState(config, before);
        takeScreenshots(screenshotContextList);
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }

    void takeScreenshots(final List<ScreenshotContext> screenshotContextList) throws IOException, InterruptedException {
        for (final ScreenshotContext screenshotContext : screenshotContextList) {

            driver.manage().window().setPosition(new Point(0, 0));
            driver.manage().window().setSize(new Dimension(screenshotContext.windowWidth, config.getWindowHeight()));

            final String url = buildUrl(screenshotContext.url, screenshotContext.path, screenshotContext.urlConfig.envMapping);
            final String rootUrl = buildUrl(screenshotContext.url, "/", screenshotContext.urlConfig.envMapping);

            //get root page from url to be able to set cookies afterwards
            //if you set cookies before getting the page once, it will fail
            LOG.debug("Getting root url: " + rootUrl);
            driver.get(rootUrl);

            if (config.getBrowser() == Type.PHANTOMJS) {
                //current phantomjs driver has a bug that prevents selenium's normal way of setting cookies
                LOG.debug("Setting cookies for PhantomJS");
                setCookiesPhantomJS(screenshotContext.urlConfig.cookies);
            } else {
                LOG.debug("Setting cookies");
                setCookies(screenshotContext.urlConfig.cookies);
            }
            setLocalStorage(screenshotContext.urlConfig.localStorage);

            LOG.debug("Browsing to " + url);
            //now get the real page
            driver.get(url);

            Long pageHeight = getPageHeight();
            Long viewportHeight = getViewportHeight();

            screenshotContext.urlConfig.getWaitAfterPageLoad().ifPresent(waitTime -> {
                try {
                    LOG.debug("Waiting for " + waitTime + " + seconds (wait-after-page-load");
                    Thread.sleep(waitTime * 1000);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            LOG.debug("Page height before scrolling: {}", pageHeight);
            LOG.debug("Viewport height of browser window: {}", viewportHeight);

            if (config.getAsyncWait() > 0) {
                LOG.debug("Waiting for " + config.getAsyncWait() + " seconds (async-wait)");
                Thread.sleep(Math.round(config.getAsyncWait() * 1000));
            }

            for (int yPosition = 0; yPosition < pageHeight && yPosition <= screenshotContext.urlConfig.getMaxScrollHeight().orElse(MAX_SCROLL_HEIGHT); yPosition += viewportHeight) {
                BufferedImage currentScreenshot = takeScreenshot();
                currentScreenshot = waitForNoAnimation(screenshotContext, currentScreenshot);
                fileService.writeScreenshot(currentScreenshot, screenshotContext.url,
                        screenshotContext.path, screenshotContext.windowWidth, yPosition, screenshotContext.before ? BEFORE : AFTER);
                //PhantomJS (until now) always makes full page screenshots, so no scrolling and multi-screenshooting
                //This is subject to change because W3C standard wants viewport screenshots
                if (config.getBrowser() == Type.PHANTOMJS) {
                    break;
                }
                LOG.debug("topOfViewport: {}, pageHeight: {}", yPosition, pageHeight);
                scrollBy(viewportHeight.intValue());

                //Sleep some milliseconds to give scrolling time before the next screenshot happens
                Thread.sleep(50);

                //Refresh to check if page grows during scrolling
                pageHeight = getPageHeight();
            }
        }
    }

    private BufferedImage takeScreenshot() throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        return ImageIO.read(screenshot);
    }

    private BufferedImage waitForNoAnimation(ScreenshotContext screenshotContext, BufferedImage currentScreenshot) throws IOException {
        File screenshot;
        float waitForNoAnimation = screenshotContext.urlConfig.getWaitForNoAnimationAfterScroll().orElse(0f);
        if (waitForNoAnimation > 0f) {
            final long beginTime = System.currentTimeMillis();
            int sameCounter = 0;
            while (sameCounter < 10 && !timeIsOver(beginTime, waitForNoAnimation)) {
                screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                BufferedImage newScreenshot = ImageIO.read(screenshot);
                if (ImageService.bufferedImagesEqualQuick(newScreenshot, currentScreenshot)) {
                    sameCounter++;
                }
                currentScreenshot = newScreenshot;
            }
        }
        return currentScreenshot;
    }

    private boolean timeIsOver(long beginTime, float waitForNoAnimation) {
        boolean over = beginTime + (long) (waitForNoAnimation * 1000L) < System.currentTimeMillis();
        if (over) LOG.debug("Time is over");
        return over;
    }

    private Long getPageHeight() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        return (Long) (jse.executeScript(JS_DOCUMENT_HEIGHT_CALL));
    }

    private Long getViewportHeight() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        return (Long) (jse.executeScript(JS_CLIENT_VIEWPORT_HEIGHT_CALL));
    }

    void scrollBy(int viewportHeight) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript(String.format(JS_SCROLL_CALL, viewportHeight));
    }

    void setLocalStorage(Map<String, String> localStorage) {
        if (localStorage == null) return;

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        for (Map.Entry<String, String> localStorageEntry : localStorage.entrySet()) {

            final String entry = localStorageEntry.getValue().replace("'", "\"");

            String jsCall = String.format(JS_SET_LOCAL_STORAGE_CALL, localStorageEntry.getKey(), entry);
            jse.executeScript(jsCall);
        }
    }

    void setCookies(List<Cookie> cookies) {
        if (cookies == null) return;
        for (Cookie cookie : cookies) {
            org.openqa.selenium.Cookie.Builder cookieBuilder = new org.openqa.selenium.Cookie.Builder(cookie.name, cookie.value);
            if (cookie.domain != null) cookieBuilder.domain(cookie.domain);
            if (cookie.path != null) cookieBuilder.path(cookie.path);
            cookieBuilder.isSecure(cookie.secure);
            driver.manage().addCookie(cookieBuilder.build());
        }
    }

    void setCookiesPhantomJS(List<Cookie> cookies) {
        if (cookies == null) return;
        for(Cookie cookie : cookies) {
            StringBuilder cookieCallBuilder = new StringBuilder(String.format("document.cookie = '%s=%s;", cookie.name, cookie.value));
            if (cookie.path != null) {
                cookieCallBuilder.append("path=");
                cookieCallBuilder.append(cookie.path);
                cookieCallBuilder.append(";");
            }
            if (cookie.domain != null) {
                cookieCallBuilder.append("domain=");
                cookieCallBuilder.append(cookie.domain);
                cookieCallBuilder.append(";");
            }
            if (cookie.secure) {
                cookieCallBuilder.append("secure;");
            }
            if (cookie.expiry != null) {
                cookieCallBuilder.append("expires=");

                SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String asGmt = df.format(cookie.expiry.getTime()) + " GMT";
                cookieCallBuilder.append(asGmt);
                cookieCallBuilder.append(";");
            }
            cookieCallBuilder.append("'");

            ((JavascriptExecutor) driver).executeScript(cookieCallBuilder.toString());
        }
    }



}
