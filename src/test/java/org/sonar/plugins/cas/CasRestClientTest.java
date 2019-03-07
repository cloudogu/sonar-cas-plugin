package org.sonar.plugins.cas;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CasRestClientTest {

    @Test
    public void credentialsShouldBeWrittenIntoConnection() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        ByteArrayOutputStream connectionStream = new ByteArrayOutputStream();
        when(connection.getOutputStream()).thenReturn(connectionStream);
        CasAuthenticatorTest.EasyTicketTestCasRestClient sut = new CasAuthenticatorTest.EasyTicketTestCasRestClient();

        sut.appendCredentials(connection, "user", "secret");

        verify(connection).getOutputStream();
        String firstZeroByte = "\u0000";
        String expectedBytes = ("username=user&password=secret" + firstZeroByte);
        assertThat(connectionStream.toString("UTF-8").contains(expectedBytes));
    }

    @Test
    public void createGrantingTicketShouldDoItsThing() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        ByteArrayOutputStream connectionStream = new ByteArrayOutputStream();
        when(connection.getOutputStream()).thenReturn(connectionStream);
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_CREATED);
        when(connection.getHeaderField("Location")).thenReturn("server.url.com/");
        EasyConnectionTestCasRestClient sut = new EasyConnectionTestCasRestClient(connection);

        sut.createGrantingTicket("sonar.server", "user", "secret");

        verify(connection).getOutputStream();
        String firstZeroByte = "\u0000";
        String expectedBytes = ("username=user&password=secret" + firstZeroByte);
        assertThat(connectionStream.toString("UTF-8").contains(expectedBytes));
    }

    @Test
    public void createServiceTicketShouldReadFromConnection() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        OutputStream outputStream = mock(OutputStream.class);
        when(connection.getOutputStream()).thenReturn(outputStream);
        InputStream inputStream = new ByteArrayInputStream("ST-12-3456789".getBytes());
        when(connection.getInputStream()).thenReturn(inputStream);
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        EasyConnectionTestCasRestClient sut = new EasyConnectionTestCasRestClient(connection);

        String actual = sut.createServiceTicket("TGT=importantTGT");

        assertThat(actual).isEqualTo("ST-12-3456789");
    }

    private class EasyConnectionTestCasRestClient extends CasRestClient {
        private final HttpURLConnection mockedConnection;

        EasyConnectionTestCasRestClient(HttpURLConnection mockedConnection) {
            super("https://sonar.server.com", "https://cas.server.com");
            this.mockedConnection = mockedConnection;
        }

        @Override
        HttpURLConnection open(String url) {
            return mockedConnection;
        }
    }
}
