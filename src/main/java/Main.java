
import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import java.util.Scanner;

import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory(new MyServerFactory()));
		staticFiles.location("public");
		port(8080);
		
		before("*", (req, res) -> {
			String s = req.uri();
			if(s.length() > 1 && s.charAt(s.length() - 1) == '/')
				res.redirect(s.substring(0, s.length() - 1));
		});
		
		get("auth/hello", (req, res) -> "<h1>Hello authorized Person</h1>");
		path("hello", () -> {
			get("", (req, res) -> "Hello Stranger!");
			get("/:name", (req, res) -> "Hello "+req.params("name"));
		});
		
		awaitInitialization();
		Thread.sleep(200);
		waitForExit();
		stop();
	}
	
	private static void waitForExit() {
		System.out.println("\n\ntype --exit to exit....");
		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		while(true) {
			if(sc.nextLine().trim().equalsIgnoreCase("--exit"))
				break;
		}
	}

}
