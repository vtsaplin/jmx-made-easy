package com.tsaplin.util.jmx;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

/**
 * Exposes an annotated POJO as a managed bean. 
 */
class JMXBeanWrapper implements DynamicMBean {

	private final Object bean;

	private MBeanInfo mBeanInfo;

	private final Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
	private final Map<String, MethodDescriptor> methods = new HashMap<String, MethodDescriptor>();
	
	public JMXBeanWrapper(Object bean) throws IntrospectionException {

		if (bean == null) {
			throw new IllegalArgumentException();
		}
		
		this.bean = bean;
		
		createMBeanInfo();
	}

	@Override
	public synchronized Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		PropertyDescriptor property = properties.get(Introspector.decapitalize(attribute));
		if (property == null) {
			throw new AttributeNotFoundException(attribute);
		}
		try {
			return property.getReadMethod().invoke(bean, new Object[0]);
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	@Override
	public synchronized void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		PropertyDescriptor property = properties.get(Introspector.decapitalize(attribute.getName()));
		if (property == null) {
			throw new AttributeNotFoundException(attribute.getName());
		}
		try {
			property.getWriteMethod().invoke(bean, new Object[] { attribute.getValue() } );
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	@Override
	public synchronized AttributeList getAttributes(String[] attributes) {
		AttributeList attributeList = new AttributeList();
		for (String attributeName : attributes) {
			attributeList.add(new Attribute(properties.get(attributeName).getName(), properties.get(attributeName).getValue(attributeName)));
		}
		return attributeList;
	}

	@Override
	public synchronized AttributeList setAttributes(AttributeList attributes) {
		for (Attribute attribute : attributes.asList()) {
			properties.get(attribute.getName()).setValue(attribute.getName(), attribute.getValue());
		}
		return attributes;
	}

	@Override
	public synchronized Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		MethodDescriptor method = methods.get(actionName);
		if (method != null) {
			try {
				return method.getMethod().invoke(bean, params);
			} catch (Exception e) {
				throw new ReflectionException(e);
			}
		}
		return null; 
	}

	@Override
    public synchronized MBeanInfo getMBeanInfo() {
		return mBeanInfo;
    }		
	
	private void createMBeanInfo() throws IntrospectionException {

		BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());

		List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
		List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
		
		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			
			JMXProperty managedProperty = null;
			
			if (property.getReadMethod() != null) {
				managedProperty = property.getReadMethod().getAnnotation(JMXProperty.class);
			} else if (property.getWriteMethod() != null) {
				managedProperty = property.getWriteMethod().getAnnotation(JMXProperty.class);
			}
			
			if (managedProperty != null) {
				
				attributes.add(new MBeanAttributeInfo(
						property.getName(),
		                property.getPropertyType().getName(),
		                managedProperty.description().isEmpty() ? null : managedProperty.description(),
		                property.getReadMethod() != null,
		                property.getWriteMethod() != null,
		                property.getReadMethod() != null && property.getReadMethod().getName().startsWith("is")
		                ));
				
				properties.put(property.getName(), property);
			}
		}

		for (MethodDescriptor method : beanInfo.getMethodDescriptors()) {
			
			JMXMethod managedMethod = method.getMethod().getAnnotation(JMXMethod.class);
			if (managedMethod != null) {
				
				List<MBeanParameterInfo> parameters = new ArrayList<MBeanParameterInfo>();
				
				for (Parameter parameter : method.getMethod().getParameters()) {
					JMXParameter managedParameter = parameter.getAnnotation(JMXParameter.class);
					String name = managedParameter != null && !managedParameter.name().isEmpty() ?
							managedParameter.name() : parameter.getName();
					String description = managedParameter != null && !managedParameter.description().isEmpty() ?
							managedParameter.description() : null;
					parameters.add(new MBeanParameterInfo(name, parameter.getType().getName(), description));
				}
				
				operations.add(new MBeanOperationInfo(
						method.getName(),
						managedMethod.description().isEmpty() ? null : managedMethod.description(),
						parameters.toArray(new MBeanParameterInfo[0]),
		                method.getMethod().getReturnType().getName(),
		                MBeanOperationInfo.ACTION_INFO
		                ));
				
				methods.put(method.getName(), method);
			}
		}
		
		JMXBean managedBean = bean.getClass().getAnnotation(JMXBean.class);
		
		String description = (managedBean != null && !managedBean.description().isEmpty()) ? 
				managedBean.description() : null;
		
        mBeanInfo = new MBeanInfo(
        		bean.getClass().getSimpleName(), 
        		description,
        		attributes.toArray(new MBeanAttributeInfo[0]), 
        		null, 
        		operations.toArray(new MBeanOperationInfo[0]),
        		null
        		);
	}
	
}
