import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Java client for the Mids Quest Game API.
 *
 * This class has been modified to print all server responses to the
 * console and return the HTTP status code as an integer.
 *
 * It handles:
 * - Creating a user (/user)
 * - Logging in and storing the session token (/login)
 * - Sending authenticated requests for game actions (/move, /look, /doing, /use)
 *
 * WARNING: This class uses manual string building and regex for JSON.
 * This is fragile and not recommended for production. A proper JSON
 * library (like Jackson or Gson) is far more robust.
 */
public class MidsQuest {

    private final HttpClient httpClient;
    private final String apiBaseUrl;

    /**
     * The session token is stored internally after a successful login.
     */
    private String sessionToken;

    public String getToken()
    {
	    return sessionToken;
    }

    /**
     * Constructs a new MidsQuest.
     *
     * @param apiBaseUrl The base URL of the Mids Quest API (e.g., "http://lnx1073302govt:8000")
     */
    public MidsQuest(String apiBaseUrl, HttpClient h) {
        this.apiBaseUrl = apiBaseUrl;
        this.httpClient = h;
    }

    // --- 1. Public API Methods (Modified) ---

    /**
     * Creates a new user. (POST /user)
     * Prints the full server response and returns the HTTP status code.
     *
     * @param username The desired username.
     * @param password The desired password.
     * @return The HTTP status code from the server (e.g., 200, 400).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int createUser(String username, String password) throws IOException, InterruptedException {
        // Manually build JSON: {"username": "...", "password": "..."}
        String requestBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}",
                escapeJsonString(username), escapeJsonString(password));

        HttpResponse<String> response;
        try {
            response = sendPostRequest("/user", requestBody, false);
        } catch (ApiException e) {
            // This should not be reachable on a non-auth endpoint
            System.err.println(e.getMessage());
            return e.getStatusCode();
        }

        System.out.println(response.body()); // Print response to screen
        return response.statusCode(); // Return code
    }

    /**
     * Logs in and stores the session token for future requests. (POST /login)
     * Prints the full server response and returns the HTTP status code.
     *
     * @param username The user's username.
     * @param password The user's password.
     * @return The HTTP status code from the server (e.g., 200, 400).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int login(String username, String password) throws IOException, InterruptedException {
        // Manually build JSON: {"username": "...", "password": "..."}
        String requestBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}",
                escapeJsonString(username), escapeJsonString(password));

        HttpResponse<String> response;
        try {
            response = sendPostRequest("/login", requestBody, false);
        } catch (ApiException e) {
            // This should not be reachable on a non-auth endpoint
            System.err.println(e.getMessage());
            return e.getStatusCode();
        }

        System.out.println(response.body()); // Print response to screen

        // If login was successful, parse and store the token
        if (response.statusCode() == 200) {
            String token = parseSimpleJsonValue(response.body(), "session_token");
            if (token.equals("Could not parse JSON")) {
                System.err.println("Warning: Login successful but could not parse session_token.");
            }
            // Store the token internally for other methods
            this.sessionToken = token;
        }

        return response.statusCode(); // Return code
    }

    /**
     * Moves the player in a given direction. (POST /move)
     * Requires a prior successful login.
     * Prints the full server response and returns the HTTP status code.
     *
     * @param direction The direction to move (e.g., "north").
     * @return The HTTP status code from the server (e.g., 200, 401, 400).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int move(String direction) throws IOException, InterruptedException {
        // Manually build JSON: {"direction": "..."}
        String requestBody = String.format("{\"direction\": \"%s\"}", escapeJsonString(direction));

        HttpResponse<String> response;
        try {
            response = sendPostRequest("/move", requestBody, true);
        } catch (ApiException e) {
            // This *can* happen if not logged in
            System.err.println(e.getMessage());
            return e.getStatusCode(); // Returns 0
        }

        System.out.println(response.body()); // Print response to screen
        return response.statusCode(); // Return code
    }

    /**
     * Gets a description of the current room. (GET /look)
     * Requires a prior successful login.
     * Prints the full server response and returns the HTTP status code.
     *
     * @return The HTTP status code from the server (e.g., 200, 401).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int look() throws IOException, InterruptedException {
        HttpResponse<String> response;
        try {
            response = sendGetRequest("/look", true);
        } catch (ApiException e) {
            // This *can* happen if not logged in
            System.err.println(e.getMessage());
            return e.getStatusCode(); // Returns 0
        }

        System.out.println(response.body()); // Print response to screen
        return response.statusCode(); // Return code
    }

    /**
     * Sets the player's current action. (POST /doing)
     * Requires a prior successful login.
     * Prints the full server response and returns the HTTP status code.
     *
     * @param action The action to set (e.g., "reading a book").
     * @return The HTTP status code from the server (e.g., 200, 401).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int setDoing(String action) throws IOException, InterruptedException {
        // Manually build JSON: {"action": "..."}
        String requestBody = String.format("{\"action\": \"%s\"}", escapeJsonString(action));

        HttpResponse<String> response;
        try {
            response = sendPostRequest("/doing", requestBody, true);
        } catch (ApiException e) {
            // This *can* happen if not logged in
            System.err.println(e.getMessage());
            return e.getStatusCode(); // Returns 0
        }

        System.out.println(response.body()); // Print response to screen
        return response.statusCode(); // Return code
    }

    /**
     * Uses an item in the current room. (POST /use)
     * Requires a prior successful login.
     * Prints the full server response and returns the HTTP status code.
     *
     * @param item The name of the item to use.
     * @return The HTTP status code from the server (e.g., 200, 401, 400).
     * @throws IOException          If a network error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    public int useItem(String item) throws IOException, InterruptedException {
        // Manually build JSON: {"item": "..."}
        String requestBody = String.format("{\"item\": \"%s\"}", escapeJsonString(item));

        HttpResponse<String> response;
        try {
            response = sendPostRequest("/use", requestBody, true);
        } catch (ApiException e) {
            // This *can* happen if not logged in
            System.err.println(e.getMessage());
            return e.getStatusCode(); // Returns 0
        }

        System.out.println(response.body()); // Print response to screen
        return response.statusCode(); // Return code
    }

    // --- 2. Private Helper Methods ---

    /**
     * Sends a POST request.
     *
     * @param endpoint    The API endpoint (e.g., "/login").
     * @param requestBody The JSON string payload.
     * @param requireAuth Whether to send the session-token header.
     * @return The server's HttpResponse.
     * @throws IOException, InterruptedException
     * @throws ApiException if authentication is required but not available.
     */
    private HttpResponse<String> sendPostRequest(String endpoint, String requestBody, boolean requireAuth)
            throws IOException, InterruptedException, ApiException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (requireAuth) {
            if (this.sessionToken == null || this.sessionToken.isEmpty()) {
                // This exception is for *before* the request is sent
                throw new ApiException("Cannot make authenticated request: Not logged in.", 0);
            }
            // Add the "session-token" (with hyphen) header
            requestBuilder.header("session-token", this.sessionToken);
        }

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a GET request.
     *
     * @param endpoint    The API endpoint (e.g., "/look").
     * @param requireAuth Whether to send the session-token header.
     * @return The server's HttpResponse.
     * @throws IOException, InterruptedException
     * @throws ApiException if authentication is required but not available.
     */
    private HttpResponse<String> sendGetRequest(String endpoint, boolean requireAuth)
            throws IOException, InterruptedException, ApiException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + endpoint))
                .header("accept", "application/json")
                .GET();

        if (requireAuth) {
            if (this.sessionToken == null || this.sessionToken.isEmpty()) {
                // This exception is for *before* the request is sent
                throw new ApiException("Cannot make authenticated request: Not logged in.", 0);
            }
            // Add the "session-token" (with hyphen) header
            requestBuilder.header("session-token", this.sessionToken);
        }

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * A very simple, fragile JSON parser using regex.
     * It finds a key and returns its value (as a String).
     *
     * @param json The JSON string to parse.
     * @param key  The key to find (e.g., "message", "session_token", "detail").
     * @return The value as a string, or "Could not parse JSON" if not found.
     */
    private String parseSimpleJsonValue(String json, String key) {
        // This regex looks for the key, followed by a colon,
        // and then captures either a quoted string ("...") or an array ([...]).
        Pattern pattern = Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(\".*?\"|\\[.*?\\]|\\{.*?\\})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String value = matcher.group(1);

            // If the value is a string, strip the surrounding quotes
            if (value.startsWith("\"") && value.endsWith("\"")) {
                // Also un-escape quotes within the string
                return value.substring(1, value.length() - 1).replace("\\\"", "\"");
            }
            // Otherwise, return the value as-is (e.g., an array as a string)
            return value;
        }

        return "Could not parse JSON";
    }

    /**
     * Escapes special characters for a JSON string.
     */
    private String escapeJsonString(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // --- 3. Custom Exception Class ---

    /**
     * Custom exception for handling API-specific errors.
     * This is only used for *internal* client errors (e.g., 'not logged in').
     */
    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    // --- 4. Example Usage (main method - MODIFIED) ---

    public static void main(String[] args) {
        // Use the API URL from the documentation
	HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        MidsQuest client = new MidsQuest("http://lnx1073302govt:8000", httpClient);

        // Generate a unique username to avoid 400 errors
        String username = "player_" + System.currentTimeMillis();
        String password = "password123";

        try {
            // 1. Create User
            System.out.println("Attempting to create user: " + username);
            // Methods now print the response and return a code
            int createCode = client.createUser(username, password);
            System.out.println("-> Status code: " + createCode);
            // Check the code before proceeding
            if (createCode != 200) {
                System.err.println("User creation failed. Stopping.");
                return;
            }

            // 2. Login
            System.out.println("\nAttempting to log in as: " + username);
            int loginCode = client.login(username, password);
            System.out.println("-> Status code: " + loginCode);
            if (loginCode != 200) {
                System.err.println("Login failed. Stopping.");
                return;
            }

            // 3. Look (Authenticated)
            System.out.println("\nAttempting to 'look'...");
            int lookCode = client.look();
            System.out.println("-> Status code: " + lookCode);
            if (lookCode != 200) {
                 System.err.println("Look failed. Stopping.");
                 return;
            }

            // 4. Move (Authenticated)
            System.out.println("\nAttempting to 'move north'...");
            int moveCode = client.move("north");
            System.out.println("-> Status code: " + moveCode);
             if (moveCode != 200) {
                 System.err.println("Move failed. Stopping.");
                 return;
            }

            // 5. Set Doing (Authenticated)
            System.out.println("\nAttempting to set action 'looking around'...");
            int doingCode = client.setDoing("looking around");
            System.out.println("-> Status code: " + doingCode);
             if (doingCode != 200) {
                 System.err.println("SetDoing failed. Stopping.");
                 return;
            }

            // 6. Use Item (Authenticated)
            System.out.println("\nAttempting to 'use torch'...");
            int useCode = client.useItem("torch");
            System.out.println("-> Status code: " + useCode);
            if (useCode != 200) {
                 System.err.println("UseItem failed.");
            }

            System.out.println("\n--- Full sequence complete! ---");

        } catch (IOException | InterruptedException e) {
            // This catch block is for network or thread errors
            System.err.println("--- Network or System Error ---");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
