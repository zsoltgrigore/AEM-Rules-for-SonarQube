package com.cognifide.aemrules.checks;

import org.junit.Test;

public class ServletGetMethodDonNotModifyRepositoryTest extends AbstractBaseTest {

	@Test
	public void servletGetMethodDontModifyRepository() {
		check = new ServletGetMethodDonNotModifyRepository();
		filename = "src/test/files/checks/ServletDoGet.java";
		verify();
	}

}
