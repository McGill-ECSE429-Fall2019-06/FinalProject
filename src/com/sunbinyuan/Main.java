package com.sunbinyuan;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main
{

	public final static String[] TOKENS = { "+", "-", "*", "/" };
	public final static String[] EXCEPTION_TOKENS = { "++", "--", "->" };

	public static void main(String[] args) throws IOException
	{
		if (args.length > 0)
		{
			for (String arg : args)
			{
				File f = new File(arg);

				Reader r = new FileReader(f);
				run(r, f);
			}
		}
		else
		{
			System.out.printf("no input files");
		}

	}

	public static List<Mutation> mutate(String str)
	{
		StringBuilder strb = new StringBuilder(str);
		ArrayList<Mutation> mutations = null;

		for (String token : EXCEPTION_TOKENS)
		{
			int index = -1;
			while ((index = strb.indexOf(token, index)) != -1)
			{
				for (int i = index; i < index + token.length(); i++)
				{
					strb.setCharAt(i, '\0');
				}

			}
		}
		for (String token : TOKENS)
		{
			int index = -1;
			while ((index = strb.indexOf(token, index)) != -1)
			{
				if (mutations == null)
				{
					mutations = new ArrayList<>();
				}
				Mutation m = new Mutation(token, index, getCurrentMutations(token));
				mutations.add(m);
				for (int i = index; i < index + token.length(); i++)
				{
					strb.setCharAt(i, '\0');
				}

			}
		}
		return mutations;
	}

	public static String[] getCurrentMutations(String token)
	{
		String[] mutations = new String[TOKENS.length - 1];
		int i = 0;
		for (String m : TOKENS)
		{
			if (!token.equals(m))
			{
				mutations[i++] = m;
			}
		}
		return mutations;
	}

	public static void run(Reader input, File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(input);

		ByteArrayOutputStream content = new ByteArrayOutputStream();
		LinkedList<FileOutputStream> filesList = new LinkedList<>();
		long line = 1;

		String str;
		while ((str = reader.readLine()) != null)
		{
			for (FileOutputStream fow : filesList)
			{
				fow.write(str.getBytes());
				fow.write(System.lineSeparator().getBytes());
			}

			List<Mutation> mutations = mutate(str);
			if (mutations != null)
			{
				System.out.printf("%5d  %s\n", line, str);
				for (Mutation m : mutations)
				{
					System.out.printf("\t%3d:%d  %s: %s\n", line, m.index, m.mutation, Arrays.toString(m.mutations));
					// create new file for each mutations
					for (int i = 0; i < m.mutations.length; i++)
					{
						String mutation = m.mutations[i];
						System.out.println(file.getName());
						String[] path = file.getName().split("\\.");
						if (path.length > 1)
						{
							path[path.length - 2] = path[path.length - 2]
									+ String.format("_l%d_i%d_m%d", line, m.index, i);
						}

						File dir = new File(file.getParent() + "/FaultList/");
						dir.mkdirs();

						FileOutputStream writer = new FileOutputStream(
								new File(file.getParent() + "/FaultList/", String.join(".", path)));
						// write to file until current point
						writer.write(content.toByteArray());

						// write line with mutation
						writer.write((str.substring(0, m.index) + mutation + str.substring(m.index + mutation.length()))
								.getBytes());
						writer.write(System.lineSeparator().getBytes());
						filesList.add(writer);
					}

				}
			}
			// write current line to the stream
			content.write(str.getBytes());
			content.write(System.lineSeparator().getBytes()); // newline
			line++;
		}
	}
}

class Mutation
{
	public int index;
	public String[] mutations;
	public String mutation;

	public Mutation(String mutation, int index, String[] mutations)
	{
		this.mutation = mutation;
		this.index = index;
		this.mutations = mutations;
	}

	@Override
	public String toString()
	{
		return mutation + ':' + index + " " + Arrays.toString(mutations);
	}
}