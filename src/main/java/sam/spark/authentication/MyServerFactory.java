package sam.spark.authentication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServerFactory;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

/**
 * almost a copy of {@link EmbeddedJettyFactory}
 * @author Sameer
 *
 */
public class MyServerFactory implements EmbeddedServerFactory, JettyServerFactory {
	private final Logger LOGGER = Log.getLogger(getClass());
	
	private SessionHandler _sessionHandler;
	private final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
	private final LoginService loginService = new HashLoginService("MyRealm", "realm.properties");
	
	@Override
	public EmbeddedServer create(Routes routeMatcher, StaticFilesConfiguration staticFilesConfiguration, boolean hasMultipleHandler) {
		MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, staticFilesConfiguration, false, hasMultipleHandler);
        matcherFilter.init(null);
        
		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setAuthenticate(true);
		
		String[] roles = Utils.noError(() -> Files.lines(Paths.get("roles"))).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
		if(roles.length == 0)
			 LOGGER.warn("no roles specified in: "+Paths.get("roles").toAbsolutePath(), (Exception)null);
		else if(LOGGER.isDebugEnabled())
			LOGGER.debug("Roles found: "+Arrays.toString(roles), (Exception)null);
			
		constraint.setRoles(roles);
		ConstraintMapping mapping = new ConstraintMapping();

		mapping.setPathSpec("/auth/*");
		mapping.setConstraint(constraint);

		security.setConstraintMappings(Collections.singletonList(mapping));

		security.setLoginService(loginService);
		security.setRealmName("myrealm");

		_sessionHandler = new JettyHandler(matcherFilter);
        security.setHandler(_sessionHandler);
		
		_sessionHandler.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
		
		Authenticators.digest(constraint, security);
		// Authenticators.form(constraint, security, () -> _sessionHandler);
		
        return new EmbeddedJettyServer(this, security);
	}

	@Override
	public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
		Server server;

        if (maxThreads > 0) {
            int max = maxThreads; 
            int min = minThreads > 0 ? minThreads : 8;
            int idleTimeout = threadTimeoutMillis > 0 ? threadTimeoutMillis : 60000;

            server = new Server(new QueuedThreadPool(max, min, idleTimeout));
        } else {
            server = new Server();
        }

        return server;
	}
	@Override
	public Server create(ThreadPool threadPool) {
		return threadPool == null ? new Server() : new Server(threadPool);
	}
}
