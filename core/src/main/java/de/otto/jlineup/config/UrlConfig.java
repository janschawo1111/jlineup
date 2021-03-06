package de.otto.jlineup.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.of;
import static de.otto.jlineup.config.JobConfig.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlConfig {

    public final List<String> paths;

    @SerializedName("max-diff")
    @JsonProperty("max-diff")
    public final float maxDiff;

    public final List<Cookie> cookies;

    @SerializedName("env-mapping")
    @JsonProperty("env-mapping")
    public final Map<String, String> envMapping;

    @SerializedName("local-storage")
    @JsonProperty("local-storage")
    public final Map<String, String> localStorage;

    @SerializedName("session-storage")
    @JsonProperty("session-storage")
    public final Map<String, String> sessionStorage;

    @SerializedName(value = "window-widths", alternate = {"resolutions","widths"})
    @JsonProperty(value = "window-widths")
    @JsonAlias({"resolutions","widths"})
    public final List<Integer> windowWidths;

    @SerializedName("max-scroll-height")
    @JsonProperty("max-scroll-height")
    public final int maxScrollHeight;

    @SerializedName("wait-after-page-load")
    @JsonProperty("wait-after-page-load")
    public final int waitAfterPageLoad;

    @SerializedName("wait-after-scroll")
    @JsonProperty("wait-after-scroll")
    public final int waitAfterScroll;

    @SerializedName("wait-for-no-animation-after-scroll")
    @JsonProperty("wait-for-no-animation-after-scroll")
    public final float waitForNoAnimationAfterScroll;

    @SerializedName("warmup-browser-cache-time")
    @JsonProperty("warmup-browser-cache-time")
    public final int warmupBrowserCacheTime;

    @SerializedName("wait-for-fonts-time")
    @JsonProperty("wait-for-fonts-time")
    public final int waitForFontsTime;

    @SerializedName("javascript")
    @JsonProperty("javascript")
    public final String javaScript;

    @SerializedName("hide-images")
    @JsonProperty("hide-images")
    public final boolean hideImages;

    @SerializedName("http-check")
    @JsonProperty("http-check")
    public final HttpCheckConfig httpCheck;

    @SerializedName("max-color-diff-per-pixel")
    @JsonProperty("max-color-diff-per-pixel")
    public final int maxColorDiffPerPixel;

    //Default constructor for GSON
    public UrlConfig() {
        this.paths = of(DEFAULT_PATH);
        this.windowWidths = of(DEFAULT_WINDOW_WIDTH);
        this.maxDiff = DEFAULT_MAX_DIFF;
        this.cookies = null;
        this.localStorage = null;
        this.sessionStorage = null;
        this.maxScrollHeight = DEFAULT_MAX_SCROLL_HEIGHT;
        this.waitAfterPageLoad = DEFAULT_WAIT_AFTER_PAGE_LOAD;
        this.waitAfterScroll = DEFAULT_WAIT_AFTER_SCROLL;
        this.waitForNoAnimationAfterScroll = DEFAULT_WAIT_FOR_NO_ANIMATION_AFTER_SCROLL;
        this.envMapping = null;
        this.warmupBrowserCacheTime = DEFAULT_WARMUP_BROWSER_CACHE_TIME;
        this.javaScript = null;
        this.waitForFontsTime = DEFAULT_WAIT_FOR_FONTS_TIME;
        this.httpCheck = new HttpCheckConfig();
        this.maxColorDiffPerPixel = DEFAULT_MAX_COLOR_DIFF_PER_PIXEL;
        this.hideImages = false;
    }

    public UrlConfig(List<String> paths, float maxDiff, List<Cookie> cookies, Map<String, String> envMapping, Map<String, String> localStorage, Map<String, String> sessionStorage, List<Integer> windowWidths, int maxScrollHeight, int waitAfterPageLoad, int waitAfterScroll, float waitForNoAnimationAfterScroll, int warmupBrowserCacheTime, String javaScript, int waitForFontsTime, HttpCheckConfig httpCheck, int maxColorDiffPerPixel, boolean hideImages) {
        this.paths = paths != null ? paths : of(DEFAULT_PATH);
        this.windowWidths = windowWidths != null ? windowWidths : of(DEFAULT_WINDOW_WIDTH);
        this.maxDiff = maxDiff;
        this.cookies = cookies;
        this.envMapping = envMapping;
        this.localStorage = localStorage;
        this.sessionStorage = sessionStorage;
        this.maxScrollHeight = maxScrollHeight;
        this.waitAfterPageLoad = waitAfterPageLoad;
        this.waitAfterScroll = waitAfterScroll;
        this.waitForNoAnimationAfterScroll = waitForNoAnimationAfterScroll;
        this.warmupBrowserCacheTime = warmupBrowserCacheTime;
        this.javaScript = javaScript;
        this.waitForFontsTime = waitForFontsTime;
        this.httpCheck = httpCheck;
        this.maxColorDiffPerPixel = maxColorDiffPerPixel;
        this.hideImages = hideImages;
    }

    private UrlConfig(Builder builder) {
        paths = builder.paths;
        maxDiff = builder.maxDiff;
        cookies = builder.cookies;
        envMapping = builder.envMapping;
        localStorage = builder.localStorage;
        sessionStorage = builder.sessionStorage;
        windowWidths = builder.windowWidths;
        maxScrollHeight = builder.maxScrollHeight;
        waitAfterPageLoad = builder.waitAfterPageLoad;
        waitAfterScroll = builder.waitAfterScroll;
        waitForNoAnimationAfterScroll = builder.waitForNoAnimationAfterScroll;
        warmupBrowserCacheTime = builder.warmupBrowserCacheTime;
        waitForFontsTime = builder.waitForFontsTime;
        javaScript = builder.javaScript;
        httpCheck = builder.httpCheck;
        maxColorDiffPerPixel = builder.maxColorDiffPerPixel;
        hideImages = builder.hideImages;
    }

    public static Builder urlConfigBuilder() {
        return new Builder().withHttpCheck(new HttpCheckConfig());
    }

    public static Builder newBuilder(UrlConfig copy) {
        Builder builder = new Builder();
        builder.paths = copy.paths;
        builder.maxDiff = copy.maxDiff;
        builder.cookies = copy.cookies;
        builder.envMapping = copy.envMapping;
        builder.localStorage = copy.localStorage;
        builder.sessionStorage = copy.sessionStorage;
        builder.windowWidths = copy.windowWidths;
        builder.maxScrollHeight = copy.maxScrollHeight;
        builder.waitAfterPageLoad = copy.waitAfterPageLoad;
        builder.waitAfterScroll = copy.waitAfterScroll;
        builder.waitForNoAnimationAfterScroll = copy.waitForNoAnimationAfterScroll;
        builder.warmupBrowserCacheTime = copy.warmupBrowserCacheTime;
        builder.waitForFontsTime = copy.waitForFontsTime;
        builder.javaScript = copy.javaScript;
        builder.httpCheck = copy.httpCheck;
        builder.maxColorDiffPerPixel = copy.maxColorDiffPerPixel;
        builder.hideImages = copy.hideImages;
        return builder;
    }

    @Override
    public String toString() {
        return "UrlConfig{" +
                "paths=" + paths +
                ", maxDiff=" + maxDiff +
                ", cookies=" + cookies +
                ", envMapping=" + envMapping +
                ", localStorage=" + localStorage +
                ", sessionStorage=" + sessionStorage +
                ", windowWidths=" + windowWidths +
                ", maxScrollHeight=" + maxScrollHeight +
                ", waitAfterPageLoad=" + waitAfterPageLoad +
                ", waitAfterScroll=" + waitAfterScroll +
                ", waitForNoAnimationAfterScroll=" + waitForNoAnimationAfterScroll +
                ", warmupBrowserCacheTime=" + warmupBrowserCacheTime +
                ", waitForFontsTime=" + waitForFontsTime +
                ", javaScript='" + javaScript + '\'' +
                ", httpCheck=" + httpCheck +
                ", hideImages=" + hideImages +
                ", maxColorDiffPerPixel=" + maxColorDiffPerPixel +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlConfig urlConfig = (UrlConfig) o;
        return Float.compare(urlConfig.maxDiff, maxDiff) == 0 &&
                maxScrollHeight == urlConfig.maxScrollHeight &&
                waitAfterPageLoad == urlConfig.waitAfterPageLoad &&
                waitAfterScroll == urlConfig.waitAfterScroll &&
                Float.compare(urlConfig.waitForNoAnimationAfterScroll, waitForNoAnimationAfterScroll) == 0 &&
                warmupBrowserCacheTime == urlConfig.warmupBrowserCacheTime &&
                waitForFontsTime == urlConfig.waitForFontsTime &&
                maxColorDiffPerPixel == urlConfig.maxColorDiffPerPixel &&
                Objects.equals(paths, urlConfig.paths) &&
                Objects.equals(cookies, urlConfig.cookies) &&
                Objects.equals(envMapping, urlConfig.envMapping) &&
                Objects.equals(localStorage, urlConfig.localStorage) &&
                Objects.equals(sessionStorage, urlConfig.sessionStorage) &&
                Objects.equals(windowWidths, urlConfig.windowWidths) &&
                Objects.equals(javaScript, urlConfig.javaScript) &&
                Objects.equals(httpCheck, urlConfig.httpCheck) &&
                Objects.equals(hideImages, urlConfig.hideImages);
    }

    @Override
    public int hashCode() {

        return Objects.hash(paths, maxDiff, cookies, envMapping, localStorage, sessionStorage, windowWidths, maxScrollHeight, waitAfterPageLoad, waitAfterScroll, waitForNoAnimationAfterScroll, warmupBrowserCacheTime, waitForFontsTime, javaScript, httpCheck, maxColorDiffPerPixel, hideImages);
    }

    public static final class Builder {

        private List<String> paths = of(DEFAULT_PATH);
        private float maxDiff = DEFAULT_MAX_DIFF;
        private List<Cookie> cookies;
        private Map<String, String> envMapping;
        private Map<String, String> localStorage;
        private Map<String, String> sessionStorage;
        private List<Integer> windowWidths = of(DEFAULT_WINDOW_WIDTH);
        private int maxScrollHeight = DEFAULT_MAX_SCROLL_HEIGHT;
        private int waitAfterPageLoad = DEFAULT_WAIT_AFTER_PAGE_LOAD;
        private int waitAfterScroll = DEFAULT_WAIT_AFTER_SCROLL;
        private float waitForNoAnimationAfterScroll = DEFAULT_WAIT_FOR_NO_ANIMATION_AFTER_SCROLL;
        private int warmupBrowserCacheTime = DEFAULT_WARMUP_BROWSER_CACHE_TIME;
        private int waitForFontsTime = DEFAULT_WAIT_FOR_FONTS_TIME;
        private String javaScript;
        private HttpCheckConfig httpCheck;
        private boolean hideImages;
        private int maxColorDiffPerPixel = DEFAULT_MAX_COLOR_DIFF_PER_PIXEL;

        private Builder() {
        }

        public Builder withPaths(List<String> val) {
            paths = val;
            return this;
        }

        public Builder withMaxDiff(float val) {
            maxDiff = val;
            return this;
        }

        public Builder withCookies(List<Cookie> val) {
            cookies = val;
            return this;
        }

        public Builder withCookie(Cookie val) {
            cookies = Collections.singletonList(val);
            return this;
        }

        public Builder withEnvMapping(Map<String, String> val) {
            envMapping = val;
            return this;
        }

        public Builder withHttpCheck(HttpCheckConfig val) {
            httpCheck = val;
            return this;
        }

        public Builder withLocalStorage(Map<String, String> val) {
            localStorage = val;
            return this;
        }

        public Builder withSessionStorage(Map<String, String> val) {
            sessionStorage = val;
            return this;
        }

        public Builder withWindowWidths(List<Integer> val) {
            windowWidths = val;
            return this;
        }

        public Builder withMaxScrollHeight(int val) {
            maxScrollHeight = val;
            return this;
        }

        public Builder withWaitAfterPageLoad(int val) {
            waitAfterPageLoad = val;
            return this;
        }

        public Builder withWaitAfterScroll(int val) {
            waitAfterScroll = val;
            return this;
        }

        public Builder withWaitForNoAnimationAfterScroll(float val) {
            waitForNoAnimationAfterScroll = val;
            return this;
        }

        public Builder withWarmupBrowserCacheTime(int val) {
            warmupBrowserCacheTime = val;
            return this;
        }

        public Builder withWaitForFontsTime(int val) {
            waitForFontsTime = val;
            return this;
        }

        public Builder withJavaScript(String val) {
            javaScript = val;
            return this;
        }

        public Builder withMaxColorDiffPerPixel(int val) {
            maxColorDiffPerPixel = val;
            return this;
        }

        public Builder withHideImages(boolean val) {
            hideImages = val;
            return this;
        }

        public UrlConfig build() {
            return new UrlConfig(this);
        }
    }
}
