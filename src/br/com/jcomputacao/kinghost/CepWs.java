package br.com.jcomputacao.kinghost;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * 15/10/2011 14:20:34
 *
 * @author Murilo
 */
public class CepWs {

    private final String host;
    private final String protocolo;
    private final String auth;
    private final String cep;

    public CepWs(String cep) {
        this.host      = System.getProperty("kinghost.host", "webservice.kinghost.net");
        this.protocolo = System.getProperty("kinghost.protocol", "http");
        this.auth      = System.getProperty("kinghost.auth");
        this.cep       = cep;
    }

    public ResultadoCep execute() throws IOException, JAXBException {
        String aurl = protocolo + "://" + host + "/web_cep.php?auth=";
        aurl += auth;
        aurl += "&formato=xml&cep=" + cep;
        System.out.println(aurl);
        URL url = new URL(aurl);
        InputStream is = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int byt;
        while ((byt = is.read()) != -1) {
            baos.write(byt);
        }
        is.close();

        String xml = new String(baos.toByteArray());
        System.out.println(xml);
        JAXBContext context = JAXBContext.newInstance("br.com.jcomputacao.kinghost");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        ResultadoCep rc = unmarshaller.unmarshal(new StreamSource(is), ResultadoCep.class).getValue();
        is.close();
        return rc;
    }
}
