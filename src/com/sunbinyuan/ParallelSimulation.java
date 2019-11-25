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
	static volatile List<String> undetectedMutants = new ArrayList<String>();

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

		MyClassLoader original = new MyClassLoader(null);
		int expected = original.simulate(original, "File.java", args[1], Integer.parseInt(args[2]),
				Integer.parseInt(args[3]));
		System.out.println("\nOriginal code expected output: " + expected);
		System.out.println("Creating Executor Service with a thread pool of size 3");
		ExecutorService executorService = Executors.newFixedThreadPool(3);

		Runnable task1 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(0))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
				String[] path = s.split("/");
				if (out != expected)
				{
					System.out.println("mutant: " + path[path.length - 1] + " killed in task 1!");
					killed++;
				}
				else
				{
					undetectedMutants.add(path[path.length - 1]);
				}
			}
		};

		Runnable task2 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(1))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
				String[] path = s.split("/");
				if (out != expected)
				{
					System.out.println("mutant: " + path[path.length - 1] + " killed in task 2!");
					killed++;
				}
				else
				{
					undetectedMutants.add(path[path.length - 1]);
				}
			}
		};

		Runnable task3 = () -> {
			System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
			for (String s : partitions.get(2))
			{
				MyClassLoader cl = new MyClassLoader(null);
				int out = original.simulate(cl, s, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
				String[] path = s.split("/");
				if (out != expected)
				{
					System.out.println("mutant: " + path[path.length - 1] + " killed in task 3!");
					killed++;
				}
				else
				{
					undetectedMutants.add(path[path.length - 1]);
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
			System.out.println("Total mutants killed: " + killed);
			System.out.println("Mutant coverage: " + (killed / filepaths.size()) * 100);
			System.out.println("Undetected mutants: " + undetectedMutants);
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
