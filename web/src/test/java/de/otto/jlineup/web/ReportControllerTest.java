package de.otto.jlineup.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.otto.jlineup.config.JobConfig;
import de.otto.jlineup.config.UrlConfig;
import de.otto.jlineup.service.JLineupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static de.otto.jlineup.config.JobConfig.DEFAULT_WARMUP_BROWSER_CACHE_TIME;
import static de.otto.jlineup.config.JobConfig.configBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JLineupWebApplication.class)
@WebAppConfiguration
public class ReportControllerTest {

    @MockBean
    private JLineupService jLineupService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc(){

        initMocks(this);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .defaultRequest(get("/")
                        .contextPath("/jlineup-ctxpath"))
                .build();
    }

    @Test
    public void shouldGetReportsPageWithOrderedReports() throws Exception {

        //given
        Instant now = Instant.now();

        JLineupRunStatus runStatus1 = JLineupRunStatus.runStatusBuilder()
                .withStartTime(now.minus(5, ChronoUnit.HOURS))
                .withEndTime(now.plus(1, ChronoUnit.HOURS))
                .withState(State.FINISHED_WITHOUT_DIFFERENCES)
                .withId("someOldId")
                .withReports(JLineupRunStatus.Reports.reportsBuilder().withHtmlUrl("/reportHtmlUrl").build())
                .withJobConfig(createJobConfigWithUrl("www.sample0.de"))
                .build();

        JLineupRunStatus runStatus2 = JLineupRunStatus.runStatusBuilder()
                .withStartTime(now)
                .withEndTime(now.plus(1, ChronoUnit.HOURS))
                .withState(State.FINISHED_WITHOUT_DIFFERENCES)
                .withId("someId")
                .withReports(JLineupRunStatus.Reports.reportsBuilder().withHtmlUrl("/reportHtmlUrl").build())
                .withJobConfig(createJobConfigWithUrl("www.sample1.de"))
                .build();

        JLineupRunStatus runStatus3 = JLineupRunStatus.runStatusBuilder()
                .withStartTime(now.minus(1, ChronoUnit.HOURS))
                .withEndTime(now.plus(1, ChronoUnit.HOURS))
                .withState(State.AFTER_RUNNING)
                .withId("someOtherId")
                .withJobConfig(createJobConfigWithUrl("www.other1.de"))
                .build();

        when(jLineupService.getRunStatus()).thenReturn(ImmutableList.of(
                runStatus1,
                runStatus2,
                runStatus3
        ));



        //when
        ModelAndView modelAndView = mockMvc.perform(get("/jlineup-ctxpath/internal/reports").contextPath("/jlineup-ctxpath").accept(MediaType.TEXT_HTML))

        //then
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attributeExists("reportList"))
                .andExpect(model().attribute("reportList", hasSize(3)))
                .andExpect(model().attribute("reportList", is(
                        ImmutableList.of(
                                new ReportController.Report(runStatus2),
                                new ReportController.Report(runStatus3),
                                new ReportController.Report(runStatus1)
                        )
                )))
                .andExpect(content().string(containsString("AFTER_RUNNING")))
                .andExpect(content().string(containsString("FINISHED_WITHOUT_DIFFERENCES")))
                .andExpect(content().string(containsString("someOldId")))
                .andExpect(content().string(containsString("someId")))
                .andExpect(content().string(containsString("someOtherId")))
                .andReturn().getModelAndView();

        @SuppressWarnings("unchecked")
        List<ReportController.Report> reportList = (List<ReportController.Report>)modelAndView.getModelMap().get("reportList");

        assertThat(reportList.get(0).getId(), is("someId"));
        assertThat(reportList.get(0).getDuration(), is("01:00:00.000"));
        assertThat(reportList.get(1).getId(), is("someOtherId"));
        assertThat(reportList.get(1).getDuration(), is("02:00:00.000"));
        assertThat(reportList.get(2).getId(), is("someOldId"));
        assertThat(reportList.get(2).getDuration(), is("06:00:00.000"));

    }

    private JobConfig createJobConfigWithUrl(String url) {
        return configBuilder()
                .withUrls(ImmutableMap.of(url,
                        new UrlConfig(
                                ImmutableList.of("/"),
                                0,
                                ImmutableList.of(),
                                ImmutableMap.of(),
                                ImmutableMap.of(),
                                ImmutableMap.of(),
                                ImmutableList.of(600),
                                100000,
                                0,
                                0,
                                0,
                                DEFAULT_WARMUP_BROWSER_CACHE_TIME,
                                null,
                                0
                        )))
                .build();

    }
}