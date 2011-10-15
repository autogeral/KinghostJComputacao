package br.com.jcomputacao.kinghost;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 15/10/2011 14:20:34
 * @author Murilo
 */
public class CepWs {
    private final String host;
    private final String protocolo;
    private final String auth;
    private final String cep;

    public CepWs(String cep) {
        this.host      = System.getProperty("kinghost.host","webservice.kinghost.net");
        this.protocolo = System.getProperty("kinghost.protocol","http");
        this.auth      = System.getProperty("kinghost.auth");
        this.cep       = cep;

    }

    public void execute() {
        try {
            String aurl = protocolo+"://webservice.kinghost.net/web_cep.php?auth=";
            aurl += auth;
            aurl += "&formato=xml&cep="+cep;
            URL url = new URL(aurl);
            InputStream is = url.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int byt = -1;
            while ((byt = is.read()) != -1) {
                baos.write(byt);
            }
            is.close();

            String string = new String(baos.toByteArray());
            System.out.println(string);
        } catch (IOException ex) {
            Logger.getLogger(CepWs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
