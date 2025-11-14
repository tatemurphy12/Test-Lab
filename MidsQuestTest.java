import org.junit.jupiter.api.*;
import org.mockito.*;
import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MidsQuestTest {

	//The following mocks allow us to run Unit Tests
    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockResponse;
	
    @InjectMocks
    private MidsQuest midsQuest;

    private String baseUrl = "http://lnx1073302govt:8000";

	//This fixture creates a new testing environment before each unit test
    @BeforeEach
    public void setUp() {
		//This scans the testing class and replaces all objects marked as mocks with mocks at runtime
        MockitoAnnotations.initMocks(this);
        midsQuest = new MidsQuest(baseUrl, mockHttpClient);
    }

    @Test
    @Tag("unit")
    public void testCreateUser() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"message\": \"User created\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        int result = midsQuest.createUser("testUser", "pass123");
        assertEquals(200, result);
    }

    @Test
    @Tag("unit")
    public void testLogin() throws Exception {
        String mockJson = "{\"session_token\": \"abc123\"}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
		
		//Check login code is 200
        int result = midsQuest.login("testUser", "pass123");
        assertEquals(200, result);

        //Check sesssion token
        String token = midsQuest.getToken();
        assertEquals("abc123", token);
    }

    @Test
    @Tag("unit")
    public void testMove() throws Exception {
		int result = midsQuest.move("north");
		assertEquals(0, result, "move() should return 0 when there is no session token");
    }

    @Test
    @Tag("it")
    public void integrationTestFullSequence() throws IOException, InterruptedException {

		HttpClient realClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        MidsQuest real = new MidsQuest(baseUrl, realClient);

        String username = "player_" + System.currentTimeMillis();
        String password = "pw123";

        int createCode = real.createUser(username, password);
        assertEquals(200, createCode);

        int loginCode = real.login(username, password);
        assertEquals(200, loginCode);

        int lookCode = real.look();
        assertEquals(200, lookCode);
    }
}

