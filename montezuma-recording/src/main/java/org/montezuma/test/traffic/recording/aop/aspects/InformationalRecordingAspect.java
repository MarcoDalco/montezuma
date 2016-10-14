package org.montezuma.test.traffic.recording.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.InitializerSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.montezuma.test.traffic.Common;

import java.io.File;
import java.io.IOException;

@Aspect
public class InformationalRecordingAspect {
	private static final String	WITHIN_REGEX																								= "analysethis..*";

	private static final String	CALL_INVOCATION_FILTER																			= "((call(* *(..))) || (call(*.new(..)))) && within(" + WITHIN_REGEX + ")";
	@Pointcut(CALL_INVOCATION_FILTER)
	public void callPoint() {}

	private static final String	CALLNEW_INVOCATION_FILTER																		= "(call(*.new(..))) && within(analysethis..*)";
	@Pointcut(CALLNEW_INVOCATION_FILTER)
	public void callNewPoint() {}

	private static final String	EXECUTION_INVOCATION_FILTER																	= "(execution(* *(..))) && within(analysethis..*)";
	@Pointcut(EXECUTION_INVOCATION_FILTER)
	public void executionPoint() {}

	private static final String	EXECUTION_NEW_INVOCATION_FILTER															= "(execution(*.new(..))) && within(analysethis..*)";
	@Pointcut(EXECUTION_NEW_INVOCATION_FILTER)
	public void executionNewPoint() {}

	private static final String	PREINITIALIZATION_NEW_INVOCATION_FILTER											= "(preinitialization(*.new(..))) && within(analysethis..*)";
	@Pointcut(PREINITIALIZATION_NEW_INVOCATION_FILTER)
	public void preinitialisationPoint() {}

	private static final String	INITIALIZATION_NEW_INVOCATION_FILTER												= "(initialization(*.new(..))) && within(analysethis..*)";
	@Pointcut(INITIALIZATION_NEW_INVOCATION_FILTER)
	public void initialisationPoint() {}
	// (staticinitialization(*)))?;

	private static final String	PREINITIALIZATION_INVOCATION_FILTER_AFTER_PREINITIALISATION	= "(preinitialization(*.new(..))) && within(analysethis..*)";
	@Pointcut(PREINITIALIZATION_INVOCATION_FILTER_AFTER_PREINITIALISATION)
	public void preinitialisationAfterPreinitialisationPoint() {}

	private static final String	INITIALIZATION_INVOCATION_FILTER_AFTER_INITIALISATION				= "(initialization(*.new(..))) && within(analysethis..*)";
	@Pointcut(INITIALIZATION_INVOCATION_FILTER_AFTER_INITIALISATION)
	public void initialisationAfterInitialisationPoint() {}

	private static final String	EXECUTION_INVOCATION_FILTER_AFTER_CONSTRUCTOR								= "(execution(*.new(..))) && within(analysethis..*)";
	@Pointcut(EXECUTION_INVOCATION_FILTER_AFTER_CONSTRUCTOR)
	public void executionAfterConstructorPoint() {}

	public final static String	RECORDING_PATH																							= "recordings";
	private final static File		recordingPath																								= new File(RECORDING_PATH);
	{
		recordingPath.mkdirs();
	}
	public static boolean				stop																												= false;

	@Before("executionPoint()")
	public void logBeforeExecution(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printInfo(joinPoint, "EXEC", null);
	}

	@Before("executionNewPoint()")
	public void logBeforeExecutioNew(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printInfo(joinPoint, "EXEC-NEW", null);
	}

	@Before("preinitialisationPoint()")
	public void logBeforePreinitializationNew(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printInfo(joinPoint, "PREINITIALISATION", null);
	}

	@Before("initialisationPoint()")
	public void logBeforeInitializationNew(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printInfo(joinPoint, "INITIALISATION", null);
	}

	private void printInfo(JoinPoint joinPoint, String operation, Object thiz) {
		final Signature signature = joinPoint.getSignature();
		final String methodSignatureString;
		if (signature instanceof MethodSignature) {
			methodSignatureString = getMethodSignatureString((MethodSignature) signature);
		} else if (signature instanceof ConstructorSignature) {
			methodSignatureString = getConstructorSignatureString((ConstructorSignature) signature);
		} else if (signature instanceof InitializerSignature) {
			methodSignatureString = signature.getName(); // <clinit>
		} else
			throw new IllegalStateException("Unexpected signature type: " + signature.getClass());
		System.out.println("\nBEFORE " + operation + " on " + joinPoint.getSignature().getDeclaringType().getName() + ", type: " + joinPoint.getStaticPart().getKind() + ", method " + methodSignatureString
				+ ", this: " + (thiz == null ? "null" : thiz.getClass()) + ", n. args: " + joinPoint.getArgs().length);
	}

	private String getMethodSignatureString(MethodSignature signature) {
		final Class<?>[] parameterTypes = signature.getParameterTypes();
		final String name = signature.getName();
		return getSignatureString(parameterTypes, name);
	}

	private String getConstructorSignatureString(ConstructorSignature signature) {
		final Class<?>[] parameterTypes = signature.getParameterTypes();
		final String name = signature.getName();
		return getSignatureString(parameterTypes, name);
	}

	private String getSignatureString(final Class<?>[] parameterTypes, final String name) {
		final StringBuilder argTypes = new StringBuilder();
		if (parameterTypes.length > 0) {
			for (Class<?> paramType : parameterTypes) {
				argTypes.append(paramType.getName());
				argTypes.append(Common.ARGS_SEPARATOR);
			}
			argTypes.setLength(argTypes.length() - Common.ARGS_SEPARATOR.length());
		}
		return name + Common.METHOD_NAME_TO_ARGS_SEPARATOR + argTypes;
	}

	@AfterReturning(pointcut = "preinitialisationAfterPreinitialisationPoint()")
	public void logAfterPreinitialisationReturning(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printAfterInfo(joinPoint, "PREINITIALISATION", joinPoint.getThis());
	}

	@AfterReturning(value = "initialisationAfterInitialisationPoint()")
	public void logAfterInitialisationReturning(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printAfterInfo(joinPoint, "INITIALISATION", joinPoint.getThis());
	}

	@AfterReturning(value = "executionAfterConstructorPoint()")
	public void logAfterConstructorReturning(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printAfterInfo(joinPoint, "EXEC-NEW", joinPoint.getThis());
	}

	@AfterReturning(value = "executionPoint()", returning = "result")
	public void logAfterExecutionReturning(JoinPoint joinPoint, Object result) throws IOException {
		if (stop)
			return;
		printAfterInfo(joinPoint, "EXEC", result);
	}

	@AfterThrowing(value = "executionPoint()", throwing = "throwable")
	public void logAfterExecutionThrowing(JoinPoint joinPoint, Throwable throwable) throws IOException {
		if (stop)
			return;
		printAfterInfo(joinPoint, "THROWING", throwable);
	}

	private void printAfterInfo(JoinPoint joinPoint, String operation, Object result) {
		System.out.println("\nAFTER " + operation + " on " + joinPoint.getSignature().getDeclaringType().getName() + ", type: " + joinPoint.getStaticPart().getKind() + ", result class:"
				+ (result == null ? "null" : result.getClass()) + ", method " + joinPoint.getSignature().toString() + ", n. args: " + joinPoint.getArgs().length);
	}

	@Before("callPoint()")
	public void logBeforeCall(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "BEFORE", "CALL", null);
	}

	@Before("callNewPoint()")
	public void logBeforeCallNew(JoinPoint joinPoint) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "BEFORE", "CALL", null);
	}

	private void printCallInfo(JoinPoint joinPoint, String when, String what, Object result) {
		final Signature signature = joinPoint.getSignature();
		final String methodSignatureString;
		if (signature instanceof MethodSignature) {
			methodSignatureString = getMethodSignatureString((MethodSignature) signature);
		} else if (signature instanceof ConstructorSignature) {
			methodSignatureString = getConstructorSignatureString((ConstructorSignature) signature);
		} else if (signature instanceof InitializerSignature) {
			methodSignatureString = signature.getName(); // <clinit>
		} else
			methodSignatureString = "UNEXP";// throw new IllegalStateException("Unexpected signature type: " +
																			// signature.getClass());
		System.out.println("\n" + when + " " + what + " on " + joinPoint.getSignature().getDeclaringType().getName() + ", type: " + joinPoint.getStaticPart().getKind() + ", to signature type: "
				+ joinPoint.getSignature() + ", to target: " + (joinPoint.getTarget() == null ? null : joinPoint.getTarget().getClass().getName()) + ", method " + methodSignatureString + ", n. args: "
				+ joinPoint.getArgs().length + ", result class: " + (result == null ? "null" : result.getClass()));
	}

	@AfterReturning(value = "callPoint()", returning = "result")
	public void logAfterCallReturning(JoinPoint joinPoint, Object result) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "AFTER", "CALL", result);
	}

	@AfterReturning(value = "callNewPoint()", returning = "result")
	public void logAfterCallNewReturning(JoinPoint joinPoint, Object result) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "AFTER", "CALL", result);
	}

	@AfterThrowing(value = "callPoint()", throwing = "throwable")
	public void logAfterCallThrowing(JoinPoint joinPoint, Throwable throwable) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "AFTER", "THROWING", throwable);
	}

	@AfterThrowing(value = "callNewPoint()", throwing = "throwable")
	public void logAfterCallNewThrowing(JoinPoint joinPoint, Throwable throwable) throws IOException {
		if (stop)
			return;
		printCallInfo(joinPoint, "AFTER", "THROWING", throwable);
	}
}
