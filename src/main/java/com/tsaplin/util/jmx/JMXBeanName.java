package com.tsaplin.util.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Represents a name of a managed bean.
 */
public final class JMXBeanName {

	private final String name;
	private final String domain;
	
	public JMXBeanName(String name, String domain) {
		this.name = name;
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public String getDomain() {
		return domain;
	}
	
	ObjectName getObjectName() throws MalformedObjectNameException {
		return new ObjectName(domain, "name", name);
	}
	
}
