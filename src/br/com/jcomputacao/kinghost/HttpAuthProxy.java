package br.com.jcomputacao.kinghost;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 15/10/2011 16:11:00
 * @author Murilo
 */
class HttpAuthProxy extends Authenticator {

    public HttpAuthProxy() {
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String user     = System.getProperty("http.proxyUser");
        String password = System.getProperty("http.proxyPassword");
        return new PasswordAuthentication(user, password.toCharArray());
    }
}
