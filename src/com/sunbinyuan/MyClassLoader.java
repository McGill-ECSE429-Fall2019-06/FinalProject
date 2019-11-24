package com.sunbinyuan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MyClassLoader extends ClassLoader
{

	private static final String CLASSPATH = System.getProperty("java.class.path");

	public static void main(String args[])
	{
		MyClassLoader cl = new MyClassLoader(null);

		try
		{
			List<ClassFile> list = cl.compile("File.java", "File");

			Class<?> clazz = cl.loadClass(list.get(0).outputStream.toByteArray(), list.get(0).getName());

			Method m = clazz.getMethod("add", new Class[] { int.class, int.class });

			Object instance = clazz.newInstance();
			System.out.println((int) m.invoke(instance, 1, 2));

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int simulate(MyClassLoader cl, String filepath, String functionName, int arg1, int arg2)
	{
		try
		{
			List<ClassFile> list = cl.compile(filepath, "File");
			Class<?> clazz = cl.loadClass(list.get(0).outputStream.toByteArray(), list.get(0).getName());
			Method m = clazz.getMethod(functionName, new Class[] { int.class, int.class });
			Object instance = clazz.newInstance();
			return (int) m.invoke(instance, arg1, arg2);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -999;
	}

	public MyClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	public List<ClassFile> compile(String path, String className) throws FileNotFoundException
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		Scanner scanner = new Scanner(new File(path));
		String text = scanner.useDelimiter("\\A").next();
		scanner.close(); // Put this call in a finally block

		JavaFileObject java = new JavaSourceFromString(className, text);
		StandardJavaFileManager standardJavaFileManager = compiler.getStandardFileManager(null, null, null);

		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(java);
		FileManager fileManager = new FileManager(standardJavaFileManager);

		CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
				Arrays.asList("-g", "-proc:none", "-classpath", CLASSPATH), null, compilationUnits);

		boolean success = task.call();

		if (success)
		{
			return fileManager.output;
		}
		return null;

	}

	public Class<?> loadClass(byte[] classData, String className) throws ClassNotFoundException
	{

		return defineClass(className, classData, 0, classData.length);

	}

	/** A compiled class file. It's content is stored in a ByteArrayOutputStream. */
	private static class ClassFile extends SimpleJavaFileObject
	{
		private ByteArrayOutputStream outputStream;

		private ClassFile(URI uri)
		{
			super(uri, Kind.CLASS);
		}

		@Override
		public String getName()
		{
			return uri.getRawSchemeSpecificPart();
		}

		@Override
		public OutputStream openOutputStream() throws IOException
		{
			return outputStream = new ByteArrayOutputStream();
		}
	}

	/** A simple file manager that collects written files in memory */
	private static class FileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
	{

		private List<ClassFile> output = new ArrayList<>();

		FileManager(StandardJavaFileManager target)
		{
			super(target);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
		{
			final ClassFile file = new ClassFile(toUri(className));
			output.add(file);
			return file;
		}
	}

	private static URI toUri(String path)
	{
		try
		{
			return new URI(null, null, path, null);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException("exception parsing uri", e);
		}
	}
}

class JavaSourceFromString extends SimpleJavaFileObject
{
	final String code;

	JavaSourceFromString(String name, String code)
	{
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
	{
		return code;
	}
}