package org.ping.main;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
		String host = "hostMain";
		String urlServer = "urlServer";
		boolean serverReportingActive = false;
		
		Report oneReport = new Report(host);
		oneReport.addObserver(new ReportObserver(host, urlServer, serverReportingActive));
		
		int delay = 1;
		
		
		PingICMP icmp1 = new PingICMP(oneReport, host, delay);
		PingTCP tcp1 = new PingTCP(oneReport, host, delay);
		PingTRACER tracer1 = new PingTRACER(oneReport, host, delay);
//		PingTRACER tracer2 = new PingTRACER(oneReport, host, delay);
		
		//run the ping threads
		ExecutorService executor = Executors.newWorkStealingPool();
		List<Callable<String>> callables = Arrays.asList(icmp1, tcp1, tracer1 /*, tracer2*/);
		
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
		// eteindre l'executor
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
	
}
