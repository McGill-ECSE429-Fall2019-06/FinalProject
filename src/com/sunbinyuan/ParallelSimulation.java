package com.sunbinyuan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelSimulation
{
	static volatile int killed = 0;

	public static void main(String args[])
	{
		File folder = new File(args[0]);
		System.out.println(args[0]);
		File[] listOfFiles = folder.listFiles();
		List<String> filepaths = new ArrayList<String>();

		for (int i = 0; i < listOfFiles.length; i++)
		{
			if (listOfFiles[i].isFile())
			{
				filepaths.add(listOfFiles[i].getAbsolutePath());
			}
		}

		List<List<String>> partitions = arraySplitter(3, filepaths);
		System.out.println(partitions.get(0));
		System.out.println(partitions.get(1));
		System.out.println(partitions.get(2));

		int arg1 = 1;
		int arg2 = 2;
		String funcName = "add";

		MyClassLoader original = new MyClassLoader(null);
		int expected = original.simulate(original, "File.java", funcName, arg1, arg2);
		System.out.println("\nOriginal code expected output: " + expected);
		System.out.println("Creating Executor Service with a thread pool of size 3");
		ExecutorService executorService = Executors.newFixedThreadPool(3);

		Runnable task1 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(0))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, funcName, arg1, arg2);
				if (out != expected)
				{
					System.out.println("mutant killed in task 1!");
					killed++;
				}
			}
		};

		Runnable task2 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(1))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, funcName, arg1, arg2);
				if (out != expected)
				{
					System.out.println("mutant killed in task 2!");
					killed++;
				}
			}
		};

		Runnable task3 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(2))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, funcName, arg1, arg2);
				if (out != expected)
				{
					System.out.println("mutant killed in task 3!");
					killed++;
				}
			}
		};

		System.out.println("Submitting tasks for execution...");
		executorService.submit(task1);
		executorService.submit(task2);
		executorService.submit(task3);
		executorService.shutdown();
		try
		{
			executorService.awaitTermination(5, TimeUnit.MINUTES);
			System.out.println(killed);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static List<List<String>> arraySplitter(int num, List<String> list)
	{
		List<List<String>> result = new ArrayList<List<String>>();

		int index1 = list.size() / 3;
		int index2 = list.size() * 2 / 3;

		result.add(list.subList(0, index1));
		result.add(list.subList(index1, index2));
		result.add(list.subList(index2, list.size()));

		return result;
	}
}
