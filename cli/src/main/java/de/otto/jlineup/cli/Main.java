package de.otto.jlineup.cli;

import com.beust.jcommander.JCommander;
import de.otto.jlineup.JLineupRunner;
import de.otto.jlineup.RunStepConfig;
import de.otto.jlineup.Utils;
import de.otto.jlineup.browser.BrowserUtils;
import de.otto.jlineup.config.JobConfig;
import de.otto.jlineup.exceptions.ConfigValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

import static de.otto.jlineup.cli.Utils.convertCommandLineParametersToRunConfiguration;
import static de.otto.jlineup.cli.Utils.readConfig;
import static java.lang.invoke.MethodHandles.lookup;

public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

    public static void main(String[] args) {

        final CommandLineParameters parameters = new CommandLineParameters();
        final JCommander jCommander = new JCommander(parameters);
        jCommander.parse(args);
        jCommander.setProgramName("JLineup");
        if (parameters.isHelp()) {
            jCommander.usage();
            LOG.info("Version: {}\n", Utils.getVersion());
            return;
        }

        if (parameters.isVersion()) {
            LOG.info("JLineup version {}", Utils.getVersion());
            return;
        }

        if (parameters.isDebug()) {
            Utils.setLogLevelToDebug();
        }

        JobConfig jobConfig = null;
        try {
            jobConfig = buildConfig(parameters);
        } catch(FileNotFoundException e) {
            System.exit(1);
        }

        if (parameters.isPrintConfig()) {
            System.out.println(JobConfig.prettyPrint(jobConfig));
            System.exit(0);
        }

        if (jobConfig.debug) {
            Utils.setLogLevelToDebug();
        }

        if (jobConfig.logToFile || parameters.isLogToFile()) {
            Utils.logToFile(parameters.getWorkingDirectory());
        }

        LOG.info("Running JLineup [{}] with step '{}'.\n\n", Utils.getVersion(), parameters.getStep());

        RunStepConfig runStepConfig = convertCommandLineParametersToRunConfiguration(parameters);

        JLineupRunner jLineupRunner = null;
        try {
            jLineupRunner = new JLineupRunner(jobConfig, runStepConfig);
        } catch (ConfigValidationException e) {
            LOG.error(e.getMessage());
            System.exit(1);
        }

        try {
            boolean runSucceeded = jLineupRunner.run();
            if (!runSucceeded) {
                System.exit(1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            System.exit(1);
        }

    }

    private static JobConfig buildConfig(CommandLineParameters parameters) throws FileNotFoundException {
        JobConfig jobConfig;
        if (parameters.getUrl() != null) {
            String url = BrowserUtils.prependHTTPIfNotThereAndToLowerCase(parameters.getUrl());
            jobConfig = JobConfig.defaultConfig(url);
            if (!parameters.isPrintConfig()) {
                LOG.info("You specified an explicit URL parameter ({}), any given jobConfig file is ignored! This should only be done for testing purpose.", url);
                LOG.info("Using generated jobConfig:\n {}", JobConfig.prettyPrint(jobConfig));
                LOG.info("You can take this generated jobConfig as base and save it as a text file named 'lineup.json'.");
                LOG.info("Just add --print-jobConfig parameter to let JLineup print an example jobConfig");
            }
        } else {
            try {
                jobConfig = readConfig(parameters);
            } catch (FileNotFoundException e) {
                if (!parameters.isPrintConfig()) {
                    LOG.error(e.getMessage());
                    LOG.error("Use --help to see the JLineup quick help.");
                    throw e;
                } else {
                    return JobConfig.exampleConfig();
                }
            }
        }
        return jobConfig;


    }

}
