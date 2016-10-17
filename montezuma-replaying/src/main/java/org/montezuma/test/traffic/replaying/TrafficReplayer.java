package org.montezuma.test.traffic.replaying;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.montezuma.test.traffic.Common;
import org.montezuma.test.traffic.InvocationData;
import org.montezuma.test.traffic.MustMock;
import org.montezuma.test.traffic.TrafficReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficReplayer extends TrafficReader {
	private final static boolean					log							= true;

	private static Map<String, Class<?>>	primitiveTypes	= new HashMap<>();
	static {
		primitiveTypes.put("boolean", boolean.class);
		primitiveTypes.put("byte", byte.class);
		primitiveTypes.put("char", char.class);
		primitiveTypes.put("short", short.class);
		primitiveTypes.put("int", int.class);
		primitiveTypes.put("long", long.class);
		primitiveTypes.put("float", float.class);
		primitiveTypes.put("double", double.class);
	};

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParseException {
		Option classnameOption = new Option("c", "classname", true, "Name of the class to replay the recordings of");
		Option recordingspathOption = new Option("r", "recordingspath", true, "Path of the directory with the recordings data");

		classnameOption.setRequired(true);
		recordingspathOption.setRequired(true);

		Options options = new Options();
		options.addOption(classnameOption);
		options.addOption(recordingspathOption);

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);

			final String className = line.getOptionValue(classnameOption.getLongOpt());
			final String pathname = line.getOptionValue(recordingspathOption.getLongOpt());

			replay(Class.forName(className), new File(pathname));
		}
		catch(ParseException pe) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp(TrafficReplayer.class.getSimpleName(), options, true);
		}
	}

	public static void replay(final Class<?> clazz, File recordingDir) throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Map<Integer, List<InvocationData>> invocationDataLists = loadInvocationDataForClass(clazz, recordingDir);
		for (List<InvocationData> invocationDataList : invocationDataLists.values()) {
			printInvocationDataSizes(invocationDataList);
		}

		Object cut = null;
		for (List<InvocationData> invocationDataList : invocationDataLists.values()) {
			for (InvocationData invocationData : invocationDataList) {
				String methodSignature = invocationData.signature;
				String methodName = methodSignature.substring(0, methodSignature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR));
				if (log)
					System.out.println("*£%$&%£$*% METHOD NAME: " + methodName);
				Object[] methodArgs = (Object[]) TrafficReader.getDeserialisedArgs(invocationData.serialisedArgs);
				for (Object arg : methodArgs)
					if (arg instanceof MustMock)
						throw new UnsupportedOperationException(
								"MustMock classes not yet supported: the original traffic recording and policy resulted in the use of a non-recordable object, so only a generic information class-and-instance information was stored instead of full serialisation (MustMock class). That is not yet supported in replaying. Invocation of method: "
										+ methodSignature + " on class: " + clazz + ", argument type: " + ((MustMock) arg).clazz);
				// TODO - get the parameter types from the actual invocation, as overloaded methods/constructors more specific
				// than the originally invoked one can be found by the existing algorithm
				Class<?>[] parameterTypes = getParameterTypes(methodArgs);
				if (methodName.equals("<init>")) {
					Constructor<? extends Object> constructor = clazz.getConstructor(parameterTypes);
					final Object newInstance = constructor.newInstance(methodArgs);
					cut = newInstance;
				} else {
					Method method = findMethod(invocationData.signature, clazz);
					final Object replayResult = method.invoke(cut, methodArgs);

					if (log)
						System.out.println(replayResult);
				}
			}
		}
	}

	private static Class<?>[] getParameterTypes(Object[] methodArgs) {
		Class<?>[] parameterTypes = new Class<?>[methodArgs.length];

		for (int i = 0; i < methodArgs.length; i++) {
			parameterTypes[i] = methodArgs[i].getClass();
		}

		return parameterTypes;
	}

	private static Method findMethod(String signature, Class<? extends Object> class1) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		int methodNameToArgsSeparatorIndex = signature.indexOf(Common.METHOD_NAME_TO_ARGS_SEPARATOR);
		final String methodName = signature.substring(0, methodNameToArgsSeparatorIndex);
		final String argTypeNames = signature.substring(methodNameToArgsSeparatorIndex + 1);
		final String[] parameterTypeNames = (argTypeNames.length() == 0 ? new String[] {} : argTypeNames.split(Common.ARGS_SEPARATOR));
		final Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			final String className = parameterTypeNames[i];
			Class<?> parameterType = primitiveTypes.get(className);
			if (parameterType == null)
				parameterType = Class.forName(className);
			parameterTypes[i] = parameterType;
		}

		if (log) {
			System.out.println("LOOKING for method: " + methodName);
			System.out.println("PARAMS: " + parameterTypeNames);
		}
		return class1.getMethod(methodName, parameterTypes);
	}
}
