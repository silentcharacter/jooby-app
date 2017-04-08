package com.mycompany;

import com.google.common.base.Throwables;
import org.apache.http.*;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;


public class MyClient {

    private Executor executor;
    private CloseableHttpClient client;
    private BasicCookieStore cookieStore;
    private String host;
    private MyClient.Request req;
    private HttpClientBuilder builder;
    private UsernamePasswordCredentials creds;

    public MyClient(String host) {
        this.host = host;
    }

    public MyClient() {
        this("http://localhost:8080");
    }

    public void start() {
        this.cookieStore = new BasicCookieStore();
        this.builder = HttpClientBuilder.create().setMaxConnTotal(1).setRetryHandler(new StandardHttpRequestRetryHandler(0, false)).setMaxConnPerRoute(1).setDefaultCookieStore(this.cookieStore);
    }

    public MyClient resetCookies() {
        this.cookieStore.clear();
        return this;
    }

    public MyClient dontFollowRedirect() {
        this.builder.setRedirectStrategy(new RedirectStrategy() {
            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                return false;
            }

            public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                return null;
            }
        });
        return this;
    }

    public MyClient.Request get(String path) {
        this.req = new MyClient.Request(this, this.executor(), org.apache.http.client.fluent.Request.Get(this.host + path));
        return this.req;
    }

    public Executor executor() {
        if(this.executor == null) {
            if(this.host.startsWith("https://")) {
                try {
                    SSLContext ex = SSLContexts.custom().loadTrustMaterial((KeyStore)null, (chain, authType) -> {
                        return true;
                    }).useTLS().build();
                    this.builder.setSslcontext(ex);
                    this.builder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                } catch (Exception var2) {
                    Throwables.propagate(var2);
                }
            }

            this.client = this.builder.build();
            this.executor = Executor.newInstance(this.client);
            if(this.creds != null) {
                this.executor.auth(this.creds);
            }
        }

        return this.executor;
    }

    public MyClient.Request post(String path) {
        this.req = new MyClient.Request(this, this.executor(), org.apache.http.client.fluent.Request.Post(this.host + path));
        return this.req;
    }

    public MyClient.Request put(String path) {
        this.req = new MyClient.Request(this, this.executor(), org.apache.http.client.fluent.Request.Put(this.host + path));
        return this.req;
    }

    public MyClient.Request delete(String path) {
        this.req = new MyClient.Request(this, this.executor(), org.apache.http.client.fluent.Request.Delete(this.host + path));
        return this.req;
    }

    private org.apache.http.client.fluent.Request pathHack(String string) {
        try {
            Class ex = this.getClass().getClassLoader().loadClass("org.apache.http.client.fluent.InternalHttpRequest");
            Constructor constructor = org.apache.http.client.fluent.Request.class.getDeclaredConstructor(new Class[]{ex});
            constructor.setAccessible(true);
            Constructor ireqcons = ex.getDeclaredConstructor(new Class[]{String.class, URI.class});
            ireqcons.setAccessible(true);
            Object ireq = ireqcons.newInstance(new Object[]{"PATCH", URI.create(string)});
            return (org.apache.http.client.fluent.Request)constructor.newInstance(new Object[]{ireq});
        } catch (SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException var6) {
            throw new UnsupportedOperationException(var6);
        }
    }

    public void stop() throws IOException {
        if(this.req != null) {
            try {
                this.req.close();
            } catch (NullPointerException var2) {
                ;
            }
        }

        if(this.client != null) {
            this.client.close();
        }

        this.builder = null;
        this.executor = null;
    }

    public MyClient basic(String username, String password) {
        this.creds = new UsernamePasswordCredentials(username, password);
        return this;
    }

    protected void before() throws Throwable {
        this.start();
    }

    protected void after() {
        try {
            this.stop();
        } catch (IOException var2) {
            throw new IllegalStateException("Unable to stop client", var2);
        }
    }

    public static class Response {
        private MyClient server;
        private HttpResponse rsp;

        public Response(MyClient server, HttpResponse rsp) {
            this.server = server;
            this.rsp = rsp;
        }

        public HttpResponse getRsp() {
            return rsp;
        }

        public MyClient.Response header(String headerName, String headerValue) throws Exception {
            if(headerValue == null) {
                Assert.assertNull(this.rsp.getFirstHeader(headerName));
            } else {
                Header header = this.rsp.getFirstHeader(headerName);
                if(header == null) {
                    Assert.assertEquals(headerValue, header);
                } else {
                    Assert.assertEquals(headerValue.toLowerCase(), header.getValue().toLowerCase());
                }
            }

            return this;
        }

        public MyClient.Response headers(BiConsumer<String, String> headers) throws Exception {
            Header[] var2 = this.rsp.getAllHeaders();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Header header = var2[var4];
                headers.accept(header.getName(), header.getValue());
            }

            return this;
        }

        public MyClient.Response header(String headerName, Object headerValue) throws Exception {
            return headerValue == null?this.header(headerName, (String)null):this.header(headerName, headerValue.toString());
        }

        public MyClient.Response header(String headerName, Optional<Object> headerValue) throws Exception {
            Header header = this.rsp.getFirstHeader(headerName);
            if(header != null) {
                Assert.assertEquals(headerValue.get(), header.getValue());
            }

            return this;
        }

        public MyClient.Response header(String headerName, MyClient.Callback callback) throws Exception {
            callback.execute(this.rsp.getFirstHeader(headerName).getValue());
            return this;
        }

        public MyClient.Response headers(String headerName, MyClient.ArrayCallback callback) throws Exception {
            Header[] headers = this.rsp.getHeaders(headerName);
            String[] values = new String[headers.length];

            for(int i = 0; i < values.length; ++i) {
                values[i] = headers[i].getValue();
            }

            callback.execute(values);
            return this;
        }

        public MyClient.Response empty() throws Exception {
            HttpEntity entity = this.rsp.getEntity();
            if(entity != null) {
                Assert.assertEquals("", EntityUtils.toString(entity));
            }

            return this;
        }

        public void request(MyClient.ServerCallback request) throws Exception {
            request.execute(this.server);
        }

        public void startsWith(String value) throws IOException {
            String rsp = EntityUtils.toString(this.rsp.getEntity());
            if(!rsp.startsWith(value)) {
                Assert.assertEquals(value, rsp);
            }

        }
    }

    public static class Body {
        private MyClient.Request req;
        private MultipartEntityBuilder parts;
        private List<BasicNameValuePair> fields;

        public Body(MultipartEntityBuilder parts, MyClient.Request req) {
            this.parts = parts;
            this.req = req;
        }

        public Body(MyClient.Request req) {
            this.fields = new ArrayList();
            this.req = req;
        }

        public MyClient.Body add(String name, Object value, String type) {
            if(this.parts != null) {
                this.parts.addTextBody(name, value.toString(), ContentType.parse(type));
            } else {
                this.fields.add(new BasicNameValuePair(name, value.toString()));
            }

            return this;
        }

        public MyClient.Body add(String name, Object value) {
            return this.add(name, value, "text/plain");
        }

        public MyClient.Body file(String name, byte[] bytes, String type, String filename) {
            if(this.parts != null) {
                this.parts.addBinaryBody(name, bytes, ContentType.parse(type), filename);
                return this;
            } else {
                throw new IllegalStateException("Not a multipart");
            }
        }
    }

    public static class Request {
        private Executor executor;
        private org.apache.http.client.fluent.Request req;
        private HttpResponse rsp;
        private MyClient server;

        public Request(MyClient server, Executor executor, org.apache.http.client.fluent.Request req) {
            this.server = server;
            this.executor = executor;
            this.req = req;
        }

        public MyClient.Response execute() throws Exception {
            this.rsp = this.executor.execute(this.req).returnResponse();
            return new MyClient.Response(this.server, this.rsp);
        }

        public MyClient.Body multipart() {
            return new MyClient.Body(MultipartEntityBuilder.create(), this);
        }

        public MyClient.Body form() {
            return new MyClient.Body(this);
        }

        public void close() {
            EntityUtils.consumeQuietly(this.rsp.getEntity());
        }

        public MyClient.Request body(String body, String type) {
            this.req.bodyString(body, ContentType.parse(type));
            return this;
        }

        public MyClient.Request bodyForm(NameValuePair... formParams) {
            this.req.bodyForm(formParams);
            return this;
        }
    }

    public interface ServerCallback {
        void execute(MyClient var1) throws Exception;
    }

    public interface ArrayCallback {
        void execute(String[] var1) throws Exception;
    }

    public interface Callback {
        void execute(String var1) throws Exception;
    }
}
