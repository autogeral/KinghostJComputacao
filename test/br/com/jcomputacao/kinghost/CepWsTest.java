package br.com.jcomputacao.kinghost;

import java.net.Authenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Murilo
 */
public class CepWsTest {

    public CepWsTest() {
    }

    @Before
    public void setUp() {
        System.setProperty("http.proxyHost", "192.168.4.254");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("http.proxyUser", "murilo");
        System.setProperty("http.proxyPassword", "");
        System.setProperty("kinghost.auth", "");
        Authenticator.setDefault(new HttpAuthProxy());
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class CepWs.
     */
    @Test
    public void testExecute() {
        CepWs instance = new CepWs("01300-110");
        instance.execute();
    }
}
