package madkit.kernel;

class MDKUtils {
	
	static String getClassFromMainStackTrace() {
		StackTraceElement element = null;
		for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
			if (stackTraceElement.getMethodName().equals("main")) {
				element = stackTraceElement;
				break;
			}
		}
		return element.getClassName();
	}
}
