package com.cognifide.aemrules.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.checks.methods.MethodMatcher;
import org.sonar.java.checks.methods.TypeCriteria;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(
		key = ServletGetMethodDonNotModifyRepository.RULE_KEY,
		name = ServletGetMethodDonNotModifyRepository.RULE_MESSAGE,
		priority = Priority.MINOR)
public class ServletGetMethodDonNotModifyRepository extends BaseTreeVisitor implements JavaFileScanner {

	public static final String RULE_KEY = "AEM-15";

	public static final String RULE_MESSAGE = "Get method of servlet should not modify repository.";

	private MethodMatcher DO_GET_METHOD_DECLARATION = MethodMatcher.create().name("doGet").typeDefinition(
			TypeCriteria.subtypeOf("org.apache.sling.api.servlets.SlingSafeMethodsServlet")).addParameter(
			TypeCriteria.is("org.apache.sling.api.SlingHttpServletRequest")).addParameter(
			TypeCriteria.is("org.apache.sling.api.SlingHttpServletResponse"));

	private MethodMatcher RESOURCE_RESOULVER_COMMIT_MATCHER = MethodMatcher.create().typeDefinition(
			"org.apache.sling.api.resource.ResourceResolver").name("commit");

	private MethodMatcher SESSION_SAVE_MATCHER = MethodMatcher.create().typeDefinition("javax.jcr.Session")
			.name("save");

	private JavaFileScannerContext context;

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitMethod(MethodTree tree) {
		if (DO_GET_METHOD_DECLARATION.matches(tree)) {
			InvocationVisitor visitator = new InvocationVisitor(this);
			tree.accept(visitator);
		}
		super.visitMethod(tree);
	}

	private class InvocationVisitor extends BaseTreeVisitor {

		private ServletGetMethodDonNotModifyRepository parent;

		public InvocationVisitor(ServletGetMethodDonNotModifyRepository parent) {
			this.parent = parent;
		}

		@Override
		public void visitMethodInvocation(MethodInvocationTree tree) {
			if (SESSION_SAVE_MATCHER.matches(tree) || RESOURCE_RESOULVER_COMMIT_MATCHER.matches(tree)) {
				context.addIssue(tree, parent, RULE_MESSAGE);
			} else {
				InvocationVisitor visitor = new InvocationVisitor(parent);
				MethodTree methodDeclaration = getMethodTree(tree);
				if (methodDeclaration != null) {
					methodDeclaration.accept(visitor);
				}
			}
			super.visitMethodInvocation(tree);
		}
	}

	private static MethodTree getMethodTree(MethodInvocationTree methodInvocation) {
		MethodTree methodTree = null;
		if (methodInvocation.methodSelect() instanceof IdentifierTree) {
			IdentifierTree method = (IdentifierTree) methodInvocation.methodSelect();
			methodTree = (MethodTree) getDeclaration(method);
		}
		return methodTree;
	}

	private static Tree getDeclaration(IdentifierTree variable) {
		return variable.symbol().declaration();
	}
}
