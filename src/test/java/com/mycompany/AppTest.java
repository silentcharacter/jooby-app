package com.mycompany;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.New;
import com.mycompany.domain.User;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jooby.test.Client;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jooby generator
 */
public class AppTest extends BaseTest {

    private static String[] lastNames = {"Иванов", "Петров", "Сидоров", "Путин", "Медведев", "Лавров", "Ходарковский"};
    private static String[] firstNames = {"Иван", "Петр", "Сидр", "Владимир", "Дмитрий", "Сергей", "Михаил"};

//    @Rule
    public MyClient client = new MyClient("https://localhost:8443");
    public NameValuePair[] credentials = new NameValuePair[]{
            new BasicNameValuePair("username", "igolnikovilya@gmail.com"),
            new BasicNameValuePair("password", "123123")
    };

    public void testMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String str = "{\"createdOn\": \"2001-12-28T21:00:00.000Z\"}";
        New _new = mapper.readValue(str, New.class);
        System.out.println(_new.createdOn);
    }

    public void updateUsers() throws Exception {
        client.start();
        client.post("/auth?client_name=FormClient").bodyForm(credentials).execute();
        MyClient.Response response = client.get("/api/users?_page=1&_perPage=100").execute();
        HttpEntity entity = response.getRsp().getEntity();
        String strResponse = EntityUtils.toString(entity);
        ObjectMapper mapper = new ObjectMapper();
        List<User> users = mapper.readValue(strResponse, new TypeReference<List<User>>(){});
        for (User user : users) {
            System.out.println(user.getFullText());
        }
    }

    public void createUsers() throws Exception {
        client.start();
        client.post("/auth?client_name=FormClient").bodyForm(credentials).execute();
        ObjectMapper mapper = new ObjectMapper();
        User user = new User();
        for (int i = 0; i < 30; i++) {
            int n = Double.valueOf(Math.random() * firstNames.length).intValue();
            user.firstName = firstNames[n];
            n = Double.valueOf(Math.random() * lastNames.length).intValue();
            user.lastName = lastNames[n];
            n = Double.valueOf(Math.random() * 100).intValue();
            user.email = "test" + n + "@test.com";
            n = Double.valueOf(Math.random() * 10000).intValue();
            user.password = "" + n;
            //Object to JSON in file
            mapper.writeValue(new File("user.json"), user);
            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(user);
            client.post("/api/users").body(jsonInString, "application/json").execute();
        }
    }

//    @Test
    public void index() throws Exception {
        server.get("/")
                .expect(200)
                .header("Content-Type", "text/html;charset=UTF-8");
    }
}
