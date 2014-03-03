package br.com.jcomputacao.kinghost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;

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
    
    public static void main(String a[]) {
        System.setProperty("http.proxyHost", "192.168.1.254");
        System.setProperty("http.proxyPorta", "3128");
        System.setProperty("http.proxyUser", "informatica");
        System.setProperty("http.proxyPassword", "informatica");
        try {
            CepWs cw = new CepWs("13301909");
            ResultadoCep r = cw.execute();
            if (r != null) {
                System.out.println("Endereço : " + r.getLogradouro());
                System.out.println("Bairro   : " + r.getBairro());
                System.out.println("Cidade   : " + r.getCidade());
                System.out.println("Estado   : " + r.getUf());
                System.out.println("Cliente  : " + r.getCliente());

            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public ResultadoCep execute() throws IOException {
        String aurl = "http://m.correios.com.br/movel/buscaCepConfirma.do?cepEntrada=";
        aurl += cep;
        aurl += "&tipoCep=,&cepTemp=,&metodo=buscarCep";
        System.out.println(aurl);
        
        boolean usaAutenticacaoProxy = Boolean.parseBoolean(System.getProperty("usa.autenticacao.proxy", "true"));
        InputStream is = null;
        URLConnection urlConnection = null;
        URL url = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        if (usaAutenticacaoProxy) {
            String proxy = System.getProperty("http.proxyHost");
            int port = Integer.parseInt(System.getProperty("http.proxyPorta"));
            java.net.Proxy informacoesProxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(proxy, port));

            urlConnection = new URL(aurl).openConnection(informacoesProxy);

            HttpAuthProxy authProxy = new HttpAuthProxy();
            urlConnection.setRequestProperty("Proxy-Authorization", "Basic" + authProxy.getPasswordAuthentication());
            urlConnection.connect();
            is = urlConnection.getInputStream();
        } else {
            url = new URL(aurl);
            is = url.openStream();
        }
        
        isr = new InputStreamReader(is, "ISO-8859-1");
        br = new BufferedReader(isr);
        
        StringBuilder sb = new StringBuilder();
        String buf = null;
        while ((buf = br.readLine()) != null) {
            sb.append(buf);
            sb.append("\n");
        }
        br.close();
        isr.close();
        is.close();
        
        String res = sb.toString();
        
        
        System.out.println(res);

        int idx1 = res.indexOf("<div class=\"caixacampobranco\">");
        int idx2 = res.indexOf("<div class=\"divopcoes\">");
        if (idx1 >= 0 && idx2 > idx1) {
            String filtrar = res.substring(idx1, idx2);
            filtrar = semEspacoDuploSemLinhaDupla(filtrar);
////            filtrar = semDiv(filtrar);
//            System.out.println("################################");
//            System.out.println("Resultado ate entao");
//            System.out.println("################################");
//            System.out.println(filtrar);
//            System.out.println("################################");
            return trataSpans(filtrar);
        } else {
            System.out.println("Idx1 : " + idx1 + " Idx2 : " + idx2);
        }
        return null;
    }

    private String semEspacoDuploSemLinhaDupla(String filtrar) {
        filtrar = replace(filtrar, "\t", "");
        filtrar = replace(filtrar, "</br>", "");
        filtrar = replace(filtrar, "<br/>", "");
        filtrar = replace(filtrar, "\n", "");
        filtrar = filtrar.replaceAll("<span class=\"resposta\">", "\n<span class=\"resposta\">");
        filtrar = replace(filtrar, "  ", " ");
        filtrar = replace(filtrar, "> ", ">");
        filtrar = replace(filtrar, " >", ">");
        filtrar = replace(filtrar, "< ", "<");
        filtrar = replace(filtrar, " <", "<");
        filtrar = replace(filtrar, "/ ", "/");
        filtrar = replace(filtrar, " /", "/");
        return filtrar;
    }

    private String replace(String filtrar, String string, String string0) {
        while (filtrar.contains(string)) {
            filtrar = filtrar.replaceAll(string, string0);
        }
        return filtrar;
    }

    private ResultadoCep trataSpans(String filtrar) {
        ResultadoCep cr = new ResultadoCep();
        
        String[] frases = filtrar.split("\n");
        for(String frase:frases) {
            String aux = soConteudo(frase, "<span class=\"resposta\">Endereço:</span>");
            if (aux == null) {
                aux = soConteudo(frase, "<span class=\"resposta\">Logradouro:</span>");
            }
            if (aux != null) {
                cr.setLogradouro(aux);
            }
            aux = soConteudo(frase, "<span class=\"resposta\">Bairro:</span>");
            if (aux != null) {
                cr.setBairro(aux);
            }
            aux = soConteudo(frase, "<span class=\"resposta\">Localidade/UF:</span>");
            if (aux != null) {
                if(aux.contains("/")) {
                    String ce[] = aux.replace(" ", "").split("/");
                    cr.setCidade(ce[0]);
                    cr.setUf(ce[1]);
                } else {
                    cr.setCidade(aux);
                }
            }
            aux = soConteudo(frase, "<span class=\"resposta\">CEP:</span>");
            if (aux != null) {
                cr.setCep(aux);
            }
            aux = soConteudo(frase, "<span class=\"resposta\">Cliente:</span>");
            if (aux != null) {
                cr.setCliente(aux);
            }
        }
        return cr;
    }

    private String soConteudo(String frase, String span) {
        if(frase.contains(span)) {
            String segpart = "<span class=\"respostadestaque\">";
            int idx1 = span.length() + segpart.length();
            int idx2 = frase.indexOf("</span>", idx1);
            if (idx2 > idx1) {
                String conteudo = frase.substring(idx1, idx2);
                return conteudo;
            }
        }
        return null;
    }
}
