package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.User;
import org.junit.Test;

import java.io.File;

/**
 * @author jooby generator
 */
public class AppTest extends BaseTest {

  private static String[] lastNames = {"Иванов", "Петров", "Сидоров", "Путин", "Медведев", "Лавров", "Ходарковский"};
  private static String[] firstNames = {"Иван", "Петр", "Сидр", "Владимир", "Дмитрий", "Сергей", "Михаил"};

  @Test
  public void index() throws Exception {
    server.get("/")
        .expect(200)
        .header("Content-Type", "text/html;charset=UTF-8");
  }

  @Test
  public void createUsers() throws Exception {
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
      server.post("/api/users").body(jsonInString, "application/json")
              .expect(200);
    }
  }

}
