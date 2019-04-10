package com.gnu.multijob;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Apps {
	ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
	
	// simulate a delayed job
	public int get() throws InterruptedException {
		Random r = new Random(System.nanoTime());
		int result = r.nextInt(1000);
		System.out.println(result);
		Thread.sleep(1000);
		return result;
	}
	
	public CompletableFuture<Integer> get2() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return 0;
		});
	}
	
	public Callable<Integer> get3(){
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return get();
			}
			
		};
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Apps app = new Apps();
		long start = 0L;
		long end = 0L;
		
		
		// Synchronous job
		start = System.currentTimeMillis();
		int result1 = app.get()+app.get()+app.get();
		end = System.currentTimeMillis();
		System.out.printf("sum : %d\n", result1);
		System.out.printf("time : %d\n", (end-start));
		System.out.println("-----------------");
		
		
		// Using CompletableFuture, it can be available above Java8
		start = System.currentTimeMillis();
		int result2 = Arrays.asList(app.get2(), app.get2(), app.get2()).stream().mapToInt(x->{
			try {
				return x.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return -1;
		}).reduce((x,y)->(x+y)).getAsInt();
		end = System.currentTimeMillis();
		System.out.printf("sum : %d\n", result2);
		System.out.printf("time : %d\n", (end-start));
		System.out.println("-----------------");
		
		
		// Using ExecutorService
		start = System.currentTimeMillis();
		ExecutorService es = Executors.newFixedThreadPool(3);
		int result3 = 0;
		for(Future<Integer> task : es.invokeAll(Arrays.asList(app.get3(), app.get3(), app.get3()))) {
			result3 += task.get();
		}
		end = System.currentTimeMillis();
		System.out.printf("sum : %d\n", result3);
		System.out.printf("time : %d\n", (end-start));
		System.out.println("-----------------");
		
		
		// Using ExecutorCompletionService
		start = System.currentTimeMillis();
		ExecutorService es2 = Executors.newFixedThreadPool(3);
		ExecutorCompletionService<Integer> ecs = new ExecutorCompletionService<>(es2);
		int size = 3;
		int i = 0;
		for(i = 0; i < size; i++) {
			ecs.submit(app.get3());
		}
		int result4 = 0;
		for(i = 0; i < size; i++) {
			result4 += ecs.take().get();
		}
		end = System.currentTimeMillis();
		System.out.printf("sum : %d\n", result4);
		System.out.printf("time : %d\n", (end-start));
		System.out.println("-----------------");
	}
}
