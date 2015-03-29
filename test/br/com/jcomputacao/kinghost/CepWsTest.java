package br.com.jcomputacao.kinghost;

import java.io.IOException;
import java.net.Authenticator;
import javax.xml.bind.JAXBException;
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
//        System.setProperty("http.proxyHost", "192.168.4.254");
//        System.setProperty("http.proxyPort", "3128");
//        System.setProperty("http.proxyUser", "murilo");
//        System.setProperty("http.proxyPassword", "");
        System.setProperty("kinghost.host", "webservice.uni5.net");
        System.setProperty("kinghost.protocol", "http");
        System.setProperty("kinghost.auth", "b78dd4aca16bf9904d92b5a238b91a98");
        Authenticator.setDefault(new HttpAuthProxy());
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class CepWs.
     */
    @Test
    public void testExecute() throws IOException, JAXBException {
        CepWs instance = new CepWs("13300110");
//        CepWs instance = new CepWs("13301909");
        ResultadoCep r = instance.execute();
        
        if ("1".equals(r.getResultado())) {
            System.out.println(r.getTipo_logradouro());
            System.out.println(r.getLogradouro());
            System.out.println(r.getBairro());
            System.out.println(r.getCidade());
            System.out.println(r.getUf());
        } else {
            System.out.println(r.getResultado_txt());
        }
    }
}
