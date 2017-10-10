package com.mycompany;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controller.News;
import com.mycompany.controller.Roles;
import com.mycompany.controller.Todos;
import com.mycompany.controller.Users;
import com.mycompany.controller.shop.*;
import com.mycompany.hbs.EqualHelper;
import com.mycompany.hbs.FormatDateHelper;
import com.mycompany.hbs.IncHelper;
import com.mycompany.hbs.MapHelper;
import com.mycompany.service.AuthenticationService;
import com.mycompany.service.MigrationService;
import org.jooby.Jooby;
import org.jooby.RequestLogger;
import org.jooby.Results;
import org.jooby.assets.Assets;
import org.jooby.hbs.Hbs;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.mongodb.Jongoby;
import org.jooby.mongodb.MongoSessionStore;
import org.jooby.mongodb.Mongodb;
import org.jooby.pac4j.Auth;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.VkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;


public class App extends Jooby {

    final static Logger logger = LoggerFactory.getLogger(App.class);

    private static AuthenticationService authenticationService;

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs().doWith((hbs, config) -> {
            hbs.registerHelper("equal", new EqualHelper());
            hbs.registerHelper("formatDate", new FormatDateHelper());
            hbs.registerHelper("inc", new IncHelper());
            hbs.registerHelper("map", new MapHelper());
        }));
        use(new Jackson().doWith(mapper -> mapper.setTimeZone(TimeZone.getTimeZone("UTC"))));

        use(new Metrics()
              .request()
              .threadDump()
              .ping()
//              .healthCheck("db", new DatabaseHealthCheck())
              .metric("memory", new MemoryUsageGaugeSet())
              .metric("threads", new ThreadStatesGaugeSet())
              .metric("gc", new GarbageCollectorMetricSet())
              .metric("fs", new FileDescriptorRatioGauge())
              .reporter(registry -> {
                  JmxReporter reporter = JmxReporter.forRegistry(registry).build();
                  reporter.start();
                  return reporter;
              })
        );
        use("*", new RequestLogger().latency().extended());
        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");
        //send pulse
        assets("/sp-push-manifest.json", "/assets/js/sp-push-manifest.json");
        assets("/sp-push-worker.js", "/assets/js/sp-push-worker.js");

        use(new Assets());

        use(new ShopApp());

        onStart(registry -> {
            authenticationService = registry.require(AuthenticationService.class);
            MigrationService.runUpdateScripts(registry);
        });

        //public rest resources
        use(new Todos());
        use(new News());

        //auth routes
        get("/userProfile", (req) -> authenticationService.getProfilePageData(req));
        get("/login", req -> Results.html("login"));
        post("/register", (req, res) -> authenticationService.handleRegistration(req, res));
        Set<String> retainSecureUrls = new HashSet<>(Arrays.asList("admin", "todo"));
        get("**", (req, rsp, chain) -> {
            Optional<String> profileId = req.session().get(Auth.ID).toOptional();
            if (!profileId.isPresent() && retainSecureUrls.stream().anyMatch(path -> req.path().contains(path))) {
                req.session().set("redirectUrl", req.path());
            }
            chain.next(req, rsp);
        });

        use(new Auth()
                        .form("/admin/**", MyUsernamePasswordAuthenticator.class)
                        .form("/todo/**", MyUsernamePasswordAuthenticator.class)
                        .form("/_log/**", MyUsernamePasswordAuthenticator.class)
                        .form("/api/**", MyUsernamePasswordAuthenticator.class)
                        .authorizer("admin", "/admin/**", (ctx, profiles) -> authenticationService.isAuthorized(ctx, profiles))
                        .client("/google/**", conf -> new Google2Client(conf.getString("google.key"), conf.getString("google.secret")))
                        .client("/vk/**", conf -> new VkClient(conf.getString("vk.key"), conf.getString("vk.secret")))
                        .client("/facebook/**", conf -> new FacebookClient(conf.getString("facebook.key"), conf.getString("facebook.secret")))
                        .client("/twitter/**", conf -> new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret")))
        );

        get("/facebook", (req, rsp) -> authenticationService.handleSocLogin(req, rsp));
        get("/twitter", (req, rsp) -> authenticationService.handleSocLogin(req, rsp));
        get("/google", (req, rsp) -> authenticationService.handleSocLogin(req, rsp));
        get("/vk", (req, rsp) -> authenticationService.handleSocLogin(req, rsp));

        get("/todo", req -> Results.html("angular").put("profile", AuthenticationService.getUserProfile(req)));

        //secure routes
        get("/admin/**", req -> {
            if (req.path().length() > 7) {
                return Results.redirect(req.path().replace("/admin/", "/admin/#"));
            }
            return Results.html("admin");
        });

        get("/_log", (req, rsp) -> rsp.type("text/plain").send(new File(System.getProperty("user.dir") + "/jooby-app.log")));

        use(new Users());
        use(new Roles());

        use(new Orders());
        use(new PaymentTypes());
        use(new DeliveryTypes());
        use(new Products());
        use(new Colors());
        use(new Sauces());
        use(new GlobalConfigs());
        use(new Customers());
        use(new Districts());
        use(new Categories());
        use(new Tags());
        use(new Units());
        use(new Menus());
        use(new Medias());
        use(new CategoryPromotions());
        use(new Reviews());
        use(new CmsPages());


    }

    public static void main(final String[] args) throws Exception {
        run(App::new, args);
    }

}
