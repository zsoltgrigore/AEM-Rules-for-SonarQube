package com.example;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.jcr.Session;
import org.apache.sling.api.resource.ResourceResolver;

import javax.servlet.ServletException;
import java.io.IOException;

@SlingServlet(resourceTypes = "sling/servlet/default", selectors = "someSelector", extensions = "json", methods = "GET")
@Properties({
		@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Some description") })
public class ServletDoGet extends SlingSafeMethodsServlet {

	private ResourceResolver resourceResolver;

	private Session session;

	@Override
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		methodThree();
	}

	private void methodOne() {
		resourceResolver.commit(); // Noncompliant {{Get method of servlet should not modify repository.}}
	}

	private void methodTwo() {
		session.save(); // Noncompliant {{Get method of servlet should not modify repository.}}
	}

	private void methodThree() {
		session.save();
		resourceResolver.commit();
	}

	private void methodFour() {
		session.save(); // Noncompliant {{Get method of servlet should not modify repository.}}
		resourceResolver.commit(); // Noncompliant {{Get method of servlet should not modify repository.}}
	}

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		methodOne();
		methodTwo();
		methodFour();
		session.save(); // Noncompliant {{Get method of servlet should not modify repository.}}
	}
}
