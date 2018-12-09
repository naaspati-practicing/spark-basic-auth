import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;

import sam.logging.filter.ANSI;
import spark.embeddedserver.jetty.JettyHandler;

public class Authenticators {

	public static void basic(Constraint constraint, ConstraintSecurityHandler security) {
		constraint.setName(Constraint.__BASIC_AUTH);
		security.setAuthenticator(new BasicAuthenticator() {
			@Override
			public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
				Authentication a = super.validateRequest(req, res, mandatory);
				System.out.println(ANSI.red(a));
				return a;
			}
		});
	}
	public static void digest(Constraint constraint, ConstraintSecurityHandler security) {
		constraint.setName(Constraint.__DIGEST_AUTH);
		security.setAuthenticator(new DigestAuthenticator());
	}
	public static void form(Constraint constraint, ConstraintSecurityHandler security, Supplier<SessionHandler> sessionHandler) {
		constraint.setName(Constraint.__FORM_AUTH);
		security.setAuthenticator(formauth(sessionHandler));
	}
	
	// losing session, on each request
	// FIXME
	private static FormAuthenticator formauth(Supplier<SessionHandler> sessionHandler) {
		return new FormAuthenticator("/login.html", "/login.html?failed=true", false) {
			@Override
			public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory)
					throws ServerAuthException {
				Request r = (Request) req;
				Authentication a = super.validateRequest(r, res, mandatory);
				return a;
			} 
		};
	}

}
