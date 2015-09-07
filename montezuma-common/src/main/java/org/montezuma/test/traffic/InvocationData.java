package org.montezuma.test.traffic;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

public class InvocationData implements Serializable {
	private static final long										serialVersionUID	= 8277695757784473932L;

	public final Date														invocationDate;
	public final String													signature;
	public final byte[][]												serialisedArgs;
	public final int[]													argIDs;
	public final LinkedList<CallInvocationData>	calls							= new LinkedList<>();
	public byte[]																serialisedReturnValue;
	public byte[]																serialisedThrowable;
	public int																	returnValueID;

	public InvocationData(Date invocationDate, String signature, byte[][] serialisedArgs, int[] argIDs) {
		this.invocationDate = invocationDate;
		this.signature = signature;
		this.serialisedArgs = serialisedArgs;
		this.argIDs = argIDs;
	}

	public void addCall(CallInvocationData callInvocationData) {
		calls.add(callInvocationData);
	}

	public CallInvocationData getLastCall() {
		return calls.getLast();
	}

	public static void printSingleInvocationDataSize(InvocationData invocationData) {
		final byte[] serialisedReturnValue = invocationData.serialisedReturnValue;
		final PrintStream out = System.out;
		out.println("************ INVOCATION DATA SIZE:");
		out.println("* signature:    " + invocationData.signature.length());
		out.println("* args:         " + invocationData.serialisedArgs.length);
		out.println("* calls (size): " + invocationData.calls.size());
		out.println("* return value: " + (serialisedReturnValue == null ? null : serialisedReturnValue.length));
		out.println("* throwable:    " + invocationData.serialisedThrowable);
		out.println("**********************************");
	}

}
