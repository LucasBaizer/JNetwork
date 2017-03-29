package org.jnetwork;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.jnetwork.DataPackage;

abstract class CommonSide implements Serializable {
	private static final long serialVersionUID = 7021053060292818871L;
	private transient HashMap<String, Method> cachedMethods = new HashMap<>();

	protected void handleExecutionPacket(DataPackage in) throws ReflectiveOperationException {
		CommonSide instance = (CommonSide) in.getObjects()[0];
		String methodName = (String) in.getObjects()[1];
		Object[] args1 = (Object[]) in.getObjects()[2];
		Object[] arguments = new Object[args1.length + 1];
		arguments[0] = this;
		System.arraycopy(args1, 0, arguments, 1, args1.length);

		Method cache = cachedMethods.get(methodName);
		if (cache != null) {
			cache.invoke(instance, arguments);
		} else {
			ArrayList<Class<?>> paramTypes = new ArrayList<>();
			for (Object o : arguments) {
				paramTypes.add(o.getClass());
			}

			Method method = in.getObjects().length < 2 ? instance.getClass().getMethod(methodName)
					: instance.getClass().getMethod(methodName, paramTypes.toArray(new Class<?>[paramTypes.size()]));
			method.invoke(instance, arguments);
			cachedMethods.put(methodName, method);
		}
	}
}
