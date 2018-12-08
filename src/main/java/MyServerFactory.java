import java.net.InetSocketAddress;
import java.util.Collections;

import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import spark.embeddedserver.jetty.JettyHandler;
import spark.embeddedserver.jetty.JettyServerFactory;

public class MyServerFactory implements JettyServerFactory {
	public static class Svr extends Server {
		private final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
		private SessionHandler sessionHandler;
		private final LoginService loginService = new HashLoginService("MyRealm", "realm.properties");
		{
			Constraint constraint = new Constraint();
			constraint.setName(Constraint.__FORM_AUTH);
			constraint.setAuthenticate(true);
			constraint.setRoles(new String[]{"user", "admin"});

			ConstraintMapping mapping = new ConstraintMapping();

			mapping.setPathSpec("/auth/*");
			mapping.setConstraint(constraint);

			security.setConstraintMappings(Collections.singletonList(mapping));
			security.setAuthenticator(new BasicAuthenticator());
			security.setLoginService(loginService);
			security.setRealmName("myrealm");

			addBean(loginService);
		}

		private Svr() {
			super();
		}
		private Svr(InetSocketAddress addr) {
			super(addr);
		}
		private Svr(int port) {
			super(port);
		}
		private Svr(ThreadPool pool) {
			super(pool);
		}		
		@Override
		public void setHandler(Handler handler) {
			sessionHandler = (JettyHandler) handler;
			security.setHandler(handler);
			security.setServer(getServer());
			
			sessionHandler.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
			super.setHandler(security);
		}
	}
	@Override
	public Svr create(int maxThreads, int minThreads, int threadTimeoutMillis) {
		Svr Svr;

		if (maxThreads > 0) {
			int max = maxThreads;
			int min = (minThreads > 0) ? minThreads : 8;
			int idleTimeout = (threadTimeoutMillis > 0) ? threadTimeoutMillis : 60000;

			Svr = new Svr(new QueuedThreadPool(max, min, idleTimeout));
		} else {
			Svr = new Svr();
		}

		return Svr;
	}

	@Override
	public Svr create(ThreadPool threadPool) {
		return threadPool != null ? new Svr(threadPool) : new Svr();
	}

}
