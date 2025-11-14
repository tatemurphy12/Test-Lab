import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;

public class ServerModel
{
  //private Player;
  private String sessionToken;
  private String homeURL = "http://lnx1073302govt:8000";
  private HttpClient client = HttpClient.newHttpClient();
  
  public void init()
  {
    HttpRequest req = HttpRequest.newBuilder()
     .uri(URI.create(homeURL))
     .header("Accept", "appplication/json")
     .GET()
     .build();
    try {
      HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
      System.out.println("Status Code: " + response.statusCode());
      System.out.println("Response Body: ");
      System.out.println(response.body());
    } catch (IOException | InterruptedException e)
    {
      System.out.println("Burgers");
    }

  }
  public void signUp(String username, String password)
  {
   String jsonBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
   HttpRequest req = HttpRequest.newBuilder()
     .uri(URI.create(homeURL + "/user"))
     .header("Content-Type", "application/json")
     .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
     .build();

   try {
            // .send() is synchronous (waits for the response)
            // .BodyHandlers.ofString() means we expect the response body as a String
            HttpResponse<String> response = client.send(req, BodyHandlers.ofString());

            // 5. Print the response information
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Headers: " + response.headers().map());
            System.out.println("Response Body: ");
            System.out.println(response.body());

        } catch (Exception e) {
            System.err.println("Error sending POST request: " + e.getMessage());
            e.printStackTrace();
        }
  }

  public void login(String username, String password)
  {
    String jsonBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    HttpRequest req = HttpRequest.newBuilder()
     .uri(URI.create(homeURL + "/login"))
     .header("Content-Type", "application/json")
     .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
     .build();
    String jsonInput = "";

    try {
            // .send() is synchronous (waits for the response)
            // .BodyHandlers.ofString() means we expect the response body as a String
            HttpResponse<String> response = client.send(req, BodyHandlers.ofString());

            // 5. Print the response information
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Headers: " + response.headers().map());
            System.out.println("Response Body: ");
            System.out.println(response.body());
            jsonInput = response.body();

        } catch (Exception e) {
            System.err.println("Error sending POST request: " + e.getMessage());
            e.printStackTrace();
        }

    String content = jsonInput.substring(1, jsonInput.length() - 1);

    // 2. Split the string into two parts at the colon
    // The '2' limit ensures it only splits on the first colon
    String[] parts = content.split(":", 2);

    // 3. Clean up the key and value
    // .trim() removes whitespace
    // .substring(1, length-1) removes the first and last character (the quotes)
    String value = parts[1].trim().substring(1, parts[1].trim().length() - 1);

    // 4. Print the results
    sessionToken = value;
    System.out.println(sessionToken);

  }

  public void move(String dir)
  {
    

  }

  public void look()
  {
  }


  public static void main(String[] args)
  {
    ServerModel m = new ServerModel();
    m.init();
    m.login("TateM", "burger");
  }
}
