package com.illay.spring.springboot.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class GithubActivity2Application {

    public static void main(String[] args) {

        SpringApplication.run(GithubActivity2Application.class, args);

        if (args[0] == null) {
            System.out.println("Не спраюцював нікнейм");
            System.out.println(args[0]);
        }

        HttpClient client = HttpClient.newHttpClient();
        try {
            String username = args[0];
            HttpRequest request =
                    HttpRequest.newBuilder()
                    .uri(new URL("https://api.github.com/users/" + username + "/events/public").toURI())
                    .header("Accept", "application/vnd.github+json").GET().build();
            HttpResponse<String> response =client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonParser parser = new JsonParser();
                JsonArray events = parser.parse(response.body()).getAsJsonArray();
                getinfoAboutCurentUser(events);
                System.out.println(response.body());
            }
            else {
                System.out.println("Error: " + response.statusCode());
                System.out.println("Respons body" + response.body());
            }

        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void getinfoAboutCurentUser(JsonArray array) {
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            String type = element.getAsJsonObject().get("type").getAsString();
            String action = "";
            switch (type) {
                case "PushEvent":
                    JsonObject payloadArray = object.getAsJsonObject("payload");
                    int commitsCount = 0;

                    if (payloadArray != null && payloadArray.has("commits")) {
                        commitsCount = payloadArray.getAsJsonArray("commits").size();

                    }
                    action = "Pushed "+ commitsCount +" commit(s) to "
                            + element.getAsJsonObject()
                            .get("repo")
                            .getAsJsonObject()
                            .get("name")
                            .getAsString();

                    break;
                case "PullRequestEvent":
                    action = "Pull request" + element
                                    .getAsJsonObject()
                                    .get("repo").
                                    getAsJsonObject().
                                    get("name").getAsString();
                    break;
                case "DeleteEvent":
                    action = "Delete " + element.getAsJsonObject()
                            .get("repo")
                            .getAsJsonObject()
                            .get("name")
                            .getAsString();
                    break;
                case "CreateEvent":
                    action = "Created " + element.getAsJsonObject()
                            .get("payload")
                            .getAsJsonObject().get("ref_type").getAsString() + " in " + element.getAsJsonObject()
                            .get("repo").getAsJsonObject()
                            .get("name").getAsString();
                    break;
                case "WatchEvent":
                    action = "Starred " + element.getAsJsonObject().get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "ForkEvent":
                    action = "Forked " + element.getAsJsonObject().get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                default:
                    action = element
                            .getAsJsonObject()
                            .get("type")
                            .getAsString().replace("Event", "") + " in " +
                            element.getAsJsonObject()
                            .get("repo").
                            getAsJsonObject().get("name").getAsString();
            }
            System.out.println(action);
        }

    }

}
