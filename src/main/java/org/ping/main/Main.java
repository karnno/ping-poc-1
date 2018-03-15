package org.ping.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.ping.services.PingICMP;
import org.ping.services.PingTCP;
import org.ping.services.PingTRACER;
import org.ping.services.report.Report;
import org.ping.services.report.ReportObserver;


/**
 * 
 * The idea is to have only one Report pojo.
 * 
 * Shared by all the threads, any of them 
 * can "synchronously" update the report with the latest result.
 * 
 * If an issue occurs in one the threads, the report will 
 * be updated with latest results (and reveal the issue origin) 
 * and will transmit the json-Report to its observers.
 * 
 * The observers will display the json-Report 
 * 
 * Threads stop when issue happens (unknow host, timeout, etc...)
 * 
 * @author karnno
 *
 */
public class Main {

	public static void main(String[] args) {

		System.out.println("Run the main !");
		
		loadLoggingConfiguration();
		
		Properties properties = new Properties();
		String[] hosts = null;
		int timeOutInMs;		
		int delay;
		String urlServer;
		boolean serverReportingActive;
		
		try {
//			properties.load(Main.class.getResourceAsStream("configuration.properties"));
			properties.load(new java.io.FileInputStream("configuration.properties"));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		if (properties.get("hosts") != null 
				&& properties.getProperty("delay") != null 
				&& properties.getProperty("urlServer") != null
				&& properties.getProperty("timeOutInMs") != null
				&& properties.getProperty("serverReportingActive") !=null
				) {
			hosts = properties.getProperty("hosts").split(",");
			delay = Integer.valueOf(properties.getProperty("delay"));
			timeOutInMs = Integer.valueOf(properties.getProperty("timeOutInMs"));
			urlServer = properties.getProperty("urlServer");
			serverReportingActive = Boolean.valueOf(properties.getProperty("serverReportingActive"));
		} else {
			System.out.println(
					"Please revise config file.. hosts and/or delay parameter(s), urlServer are missing, corrupted or incorrectly formatted"
							+ "\n(Consider formatting hosts=\"host1, host2..\" and delay=seconds and urlServer=http://...... and timeOutInMs=milliseconds and serverReportingActive=true/false)");
			return;
		}

		//////////////////////////////////////////////////////////////////
		// All preliminary setup is correct and let's get to actual logic >
		
		String host1 = hosts[0], host2 = hosts[1];
		
		Report report1 = new Report(host1);
		report1.addObserver(new ReportObserver(host1, urlServer, serverReportingActive));
		
		Report report2 = new Report(host2);
		report2.addObserver(new ReportObserver(host2, urlServer, serverReportingActive));
		
		
		Callable<String> icmp1 		= new PingICMP(report1, host1 , delay);
		Callable<String> tcp1 		= new PingTCP(report1, host1, delay, timeOutInMs);
		Callable<String> tracer1 	= new PingTRACER(report1, host1, delay);
		
		Callable<String> icmp2 		= new PingICMP(report2, host2 , delay);
		Callable<String> tcp2 		= new PingTCP(report2, host2, delay, timeOutInMs);
		Callable<String> tracer2 	= new PingTRACER(report2, host2, delay);
		
		//run the ping threads
		ExecutorService executor = Executors.newWorkStealingPool();
		List<Callable<String>> callables = Arrays.asList(
				icmp1,
				tcp1, 
				tracer1, 
				icmp2, 
				tcp2,
				tracer2
		);
		
		try {
			List<Future<String>> futures =executor.invokeAll(callables);
			for(Future<String> future : futures){
                try{
                    System.out.println("future.isDone = " + future.isDone());
                    System.out.println("future: call ="+future.get());
                }
                catch (CancellationException ce) {
                    ce.printStackTrace();
                } catch (ExecutionException ee) {
                    ee.printStackTrace();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }
            }
			
		} catch (Exception excp) {
			excp.printStackTrace();
			 
		}
		// shutdown l'executor
		stop(executor);
	}
	
	static void stop(ExecutorService executor) {
        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("termination interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }
    }
	
	static void loadLoggingConfiguration() {
		try {
			LogManager.getLogManager().readConfiguration(new java.io.FileInputStream("loggingconfig.properties"));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
