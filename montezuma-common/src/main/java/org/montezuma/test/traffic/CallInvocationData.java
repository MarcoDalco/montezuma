package org.montezuma.test.traffic;

import java.util.Date;

public class CallInvocationData extends InvocationData {
	private static final long	serialVersionUID	= -7140867167446198896L;
	public final Class<?>			declaringType;
	public final Class<?>			targetClazz;
	public final int					id;
	public final int					modifiers;
	public final Class<?>			invokingClass;

	public CallInvocationData(Class<?> declaringType, Object target, Date invocationDate, String signature, byte[][] serialisedArgs, int[] argIDs, int modifiers, Class<?> invokingClass) {
		super(invocationDate, signature, serialisedArgs, argIDs);
		this.declaringType = declaringType;
		this.targetClazz = (target == null ? null : target.getClass());
		this.id = System.identityHashCode(target);
		this.modifiers = modifiers;
		this.invokingClass = invokingClass;
	}
}
