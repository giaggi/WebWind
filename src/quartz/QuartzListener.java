package quartz;

import Wind.Core;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


public class QuartzListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(QuartzListener.class.getName());
    public static final String CoreClass = "core";

    Scheduler scheduler = null;

    static Core core;// = new Core();

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {

        core = new Core();
        core.init();

        ServletContext cntxt = servletContext.getServletContext();
        cntxt.setAttribute(CoreClass, core);


        System.out.println("Context Initialized");

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            //pass the servlet context to the job
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("servletContext", servletContext.getServletContext());
            // define the job and tie it to our job's class

            JobDetail meteodataJob = newJob(MeteoDataQuartzJob.class).withIdentity(
                    "MeteoDataQuartzJob", "Group")
                    .usingJobData(jobDataMap)
                    .build();
            // Trigger the job to run now, and then every 60 seconds
            Trigger meteodataTrigger = newTrigger()
                    .withIdentity("SensorTriggerName", "Group")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60) //  60
                            .repeatForever())
                    .build();
            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler.scheduleJob(meteodataJob, meteodataTrigger);

            JobDetail forecastJob = newJob(ForecastQuartzJob.class).withIdentity(
                    "ForecastQuartzJob", "Group")
                    .usingJobData(jobDataMap)
                    .build();
            // Trigger the job to run now, and then every 30 minuti
            Trigger forecastTrigger = newTrigger()
                    .withIdentity("ForecastTriggerName", "Group")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60*30) //  30 mimnuti
                            .repeatForever())
                    .build();
            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler.scheduleJob(forecastJob, forecastTrigger);

            // Setup the Job class and the Job group
            JobDetail dbJob = newJob(DBQuartzJob.class).withIdentity(
                    "DBQuartzJob", "Group")
                    .usingJobData(jobDataMap)
                    .build();
            // Trigger the job to run now, and then every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("DBTriggerName", "Group")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60*30)
                            .repeatForever())
                    .build();
            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler.scheduleJob(dbJob, trigger);

            //Build a trigger for a specific moment in time, with no repeats:
            /*SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startAt(myStartTime) // some Date
                    .forJob("job1", "group1") // identify job with name, group strings
                    .build();*/


            //scheduler2 = new StdSchedulerFactory().getScheduler();
            //scheduler2.start();
            // Setup the Job class and the Job group
            /*JobDetail recoveryJob = newJob(NextProgramQuartzJob.class).withIdentity(
                    "CronRecoveryQuartzJob", "Group2").build();*/
            // Trigger the job to run now, and then every 40 seconds
            /*Trigger recoveryTrigger = newTrigger()
                    .withIdentity("RecoveryTriggerName", "Group2")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3000)
                            .repeatForever())
                    .build();*/
            // Setup the Job and Trigger with Scheduler & schedule jobs
            //scheduler2.scheduleJob(recoveryJob, recoveryTrigger);



        }
        catch (SchedulerException e) {
            LOGGER.info("QuartzListener exception" + e.getStackTrace());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {
        System.out.println("Context Destroyed");
        try
        {
            scheduler.shutdown();
        }
        catch (SchedulerException e)
        {
            LOGGER.info("execute" + e.getStackTrace());
            e.printStackTrace();
        }
    }
}
