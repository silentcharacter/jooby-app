package com.mycompany;

import com.mongodb.*;
import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controller.News;
import com.mycompany.controller.Roles;
import com.mycompany.controller.Todos;
import com.mycompany.controller.Users;
import com.mycompany.domain.ScriptLog;
import com.mycompany.hbs.EqualHelper;
import com.mycompany.service.AuthenticationService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.hbs.Hbs;
import org.jooby.json.Jackson;
import org.jooby.mongodb.Jongoby;
import org.jooby.mongodb.Mongodb;
import org.jooby.pac4j.Auth;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.VkClient;

import java.io.*;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App extends Jooby {

    final static Logger logger = LoggerFactory.getLogger(App.class);

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs().doWith((hbs, config) -> {
            hbs.registerHelper("equal", new EqualHelper());
        }));
        use(new Jackson());
//        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");

        get("/", req -> Results.html("angular").put("profile", AuthenticationService.getUserProfile(req)));

        use(new ShopApp());

        //public rest resources
        use(new Todos());
        use(new News());

        //auth routes
        get("/userProfile", req -> AuthenticationService.getProfilePageData(req));
        get("/login", ((request, response) -> response.redirect("/#/login")));
        post("/register", AuthenticationService.registrationHandler);

        use(new Auth()
                        .form("/admin/**", MyUsernamePasswordAuthenticator.class)
                        .form("/api/**", MyUsernamePasswordAuthenticator.class)
                        .authorizer("admin", "/admin/**", AuthenticationService.authorizerHandler)
                        .client("/google/**", conf -> new Google2Client(conf.getString("google.key"), conf.getString("google.secret")))
                        .client("/vk/**", conf -> new VkClient(conf.getString("vk.key"), conf.getString("vk.secret")))
                        .client("/facebook/**", conf -> new FacebookClient(conf.getString("facebook.key"), conf.getString("facebook.secret")))
                        .client("/twitter/**", conf -> new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret")))
        );

        get("/facebook", AuthenticationService.socialLoginHandler);
        get("/twitter", AuthenticationService.socialLoginHandler);
        get("/google", AuthenticationService.socialLoginHandler);
        get("/vk", AuthenticationService.socialLoginHandler);

        //secure routes
        get("/admin", req -> Results.html("admin"));

        //secure rest resources
        use(new Users());
        use(new Roles());
    }

    @Override
    public void start(final String[] args) throws Exception
    {
        super.start(args);
        runUpdateScripts();
    }

    private void runUpdateScripts() throws IOException
    {
        logger.info("Running update scripts");
        Config conf = ConfigFactory.defaultApplication();
        String dbName = StringUtils.substringAfterLast(conf.getString("db"), "/");
        DB db = new MongoClient().getDB(dbName);
        Jongo jongo = new Jongo(db);
        MongoCollection scriptlogs = jongo.getCollection("scriptlogs");

        File scriptFolder = new File(System.getProperty("user.dir") + "/public/db-update");
        for (final File script : scriptFolder.listFiles()) {
            String scriptName = script.getName();
            String scriptPath = script.getAbsolutePath();
            logger.info(scriptName);

            ScriptLog log = scriptlogs.findOne(String.format("{version: '%s'}", scriptName)).as(ScriptLog.class);
            if (log == null) {
                try {
                    String command = String.format("mongo %s %s", dbName, scriptPath);
                    Process p = Runtime.getRuntime().exec(command);
                    String line;
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
                    while ((line = in.readLine()) != null) {
                        logger.info(line);
                    }
                    in.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                log = new ScriptLog();
                log.version = scriptName;
                log.date = new Date();
                scriptlogs.insert(log);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}
