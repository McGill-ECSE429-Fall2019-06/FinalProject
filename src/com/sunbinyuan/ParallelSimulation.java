package com.sunbinyuan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelSimulation
{
	static volatile Map<String, List<String>> killed = new HashMap<String, List<String>>();

	public static void main(String args[])
	{
		File folder = new File(args[0]);
		System.out.println(args[0]);
		System.out.println(args[1]);

		List<String> simulationLines = simulationFileReader(args[1]);
		String funcName = getFunctionName(simulationLines);
		System.out.println(funcName);

		List<String> vectors = getVectors(simulationLines);
		System.out.println(vectors);

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

		for (String vector : vectors)
		{
			String[] vectorArgs = vector.split(",");

			MyClassLoader original = new MyClassLoader(null);
			int expected = original.simulate(original, "File.java", funcName, Integer.parseInt(vectorArgs[0]),
					Integer.parseInt(vectorArgs[1]));
			System.out.println("\nOriginal code expected output: " + expected);
			System.out.println("Creating Executor Service with a thread pool of size 3");
			ExecutorService executorService = Executors.newFixedThreadPool(3);

			Runnable task1 = () -> {
				System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
				for (String s : partitions.get(0))
				{
					MyClassLoader cl = new MyClassLoader(null);
					int out = original.simulate(cl, s, funcName, Integer.parseInt(vectorArgs[0]),
							Integer.parseInt(vectorArgs[1]));
					String[] path = s.split("/");
					if (out != expected)
					{
						// System.out.println("mutant: " + path[path.length - 1] + " killed in task 1!");
						if (!killed.containsKey(path[path.length - 1]))
						{
							List<String> killerVectors = new ArrayList<String>();
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
						else
						{
							List<String> killerVectors = killed.get(path[path.length - 1]);
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
					}
				}
			};

			Runnable task2 = () -> {
				System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
				for (String s : partitions.get(1))
				{
					MyClassLoader cl = new MyClassLoader(null);
					int out = original.simulate(cl, s, funcName, Integer.parseInt(vectorArgs[0]),
							Integer.parseInt(vectorArgs[1]));
					String[] path = s.split("/");
					if (out != expected)
					{
						// System.out.println("mutant: " + path[path.length - 1] + " killed in task 2!");
						if (!killed.containsKey(path[path.length - 1]))
						{
							List<String> killerVectors = new ArrayList<String>();
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
						else
						{
							List<String> killerVectors = killed.get(path[path.length - 1]);
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
					}
				}
			};

			Runnable task3 = () -> {
				System.out.println("Excuting task1 inside: " + Thread.currentThread().getName());
				for (String s : partitions.get(2))
				{
					MyClassLoader cl = new MyClassLoader(null);
					int out = original.simulate(cl, s, funcName, Integer.parseInt(vectorArgs[0]),
							Integer.parseInt(vectorArgs[1]));
					String[] path = s.split("/");
					if (out != expected)
					{
						// System.out.println("mutant: " + path[path.length - 1] + " killed in task 3!");
						if (!killed.containsKey(path[path.length - 1]))
						{
							List<String> killerVectors = new ArrayList<String>();
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
						else
						{
							List<String> killerVectors = killed.get(path[path.length - 1]);
							killerVectors.add("(" + vectorArgs[0] + ", " + vectorArgs[1] + ")");
							killed.put(path[path.length - 1], killerVectors);
						}
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
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		System.out.println("");
		for (String filepath : filepaths)
		{
			String[] path = filepath.split("/");
			String filename = path[path.length - 1];
			String vectorsList = "";
			if (killed.containsKey(filename))
			{
				vectorsList = killed.get(filename).toString();
			}
			System.out.println(filename + " is killed by vectors: " + vectorsList);
		}

		System.out.println("Total mutants killed: " + killed.size());
		System.out.println("Total number of files: " + filepaths.size());
		System.out.println("Mutant coverage: " + ((float) killed.size() / filepaths.size()) * 100 + "%");
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

	public static List<String> simulationFileReader(String simulationFilePath)
	{
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(simulationFilePath)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines;
	}

	public static String getFunctionName(List<String> simulationFileLines)
	{
		return simulationFileLines.get(0);
	}

	public static List<String> getVectors(List<String> simulationFileLines)
	{
		return simulationFileLines.subList(1, simulationFileLines.size());
	}
}
