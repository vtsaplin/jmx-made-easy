package com.tsaplin.util.jmx;

/**
 * Represents a managed exception.
 */
public class JMXBeanException extends Exception {
	private static final long serialVersionUID = 8844399147000419181L;

	public JMXBeanException() {
		super();
	}

	public JMXBeanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JMXBeanException(String message, Throwable cause) {
		super(message, cause);
	}

	public JMXBeanException(String message) {
		super(message);
	}

	public JMXBeanException(Throwable cause) {
		super(cause);
	}

}
