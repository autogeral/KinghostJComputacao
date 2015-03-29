package br.com.jcomputacao.kinghost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

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
    private boolean mobile;

    public CepWs(String cep) {
        this.host      = System.getProperty("kinghost.host", "webservice.kinghost.net");
        this.protocolo = System.getProperty("kinghost.protocol", "http");
        this.auth      = System.getProperty("kinghost.auth");
        this.cep       = cep;
    }
    
    public static void main(String a[]) {
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
        return (mobile ? executeMobile() : executeCommon());
    }
        
    private ResultadoCep executeMobile() throws IOException {
        //http://www.buscacep.correios.com.br/servicos/dnec/consultaEnderecoAction.do
        String aurl = "http://m.correios.com.br/movel/buscaCepConfirma.do?cepEntrada=";
        aurl += cep;
        aurl += "&tipoCep=,&cepTemp=,&metodo=buscarCep";
        System.out.println(aurl);
        
        boolean usaAutenticacaoProxy = Boolean.parseBoolean(System.getProperty("usa.autenticacao.proxy", "false"));
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
        return new ResultadoCep();
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

    private ResultadoCep executeCommon() throws IOException {
        ResultadoCep result = new ResultadoCep();

        HttpClient client = new HttpClient();
        String aurl = "http://www.buscacep.correios.com.br";
        aurl += "/servicos/dnec/consultaEnderecoAction.do";
        PostMethod post = new PostMethod(aurl);

//            if (usaAutenticacaoProxy) {
//                String proxy = System.getProperty("http.proxyHost");
//                int port = Integer.parseInt(System.getProperty("http.proxyPorta"));
//                String user = System.getProperty("http.proxyUser");
//                String password = System.getProperty("http.proxyPassword");
//
//                Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
//
//                post.getProxyAuthState().setAuthScheme(new BasicScheme());
//                AuthState proxyAuthState = post.getProxyAuthState();
//                AuthScheme authScheme = proxyAuthState.getAuthScheme();
//                String authstring = authScheme.authenticate(defaultcreds, post);
//                post.addRequestHeader(new Header(PROXY_AUTH_RESP, authstring, true));
//                post.setDoAuthentication(true);
//                client.getState().setProxyCredentials(new AuthScope(proxy, port, authScheme.getRealm(), authScheme.getSchemeName()), defaultcreds);
//                client.getHostConfiguration().setProxy(proxy, port);
//                client.getHostConfiguration().setHost(aurl);
//
//            }

        post.setParameter("relaxation", cep);
        post.setParameter("TipoCep", "ALL");
        post.setParameter("semelhante", "N");

        post.setParameter("cfm", "1");
        post.setParameter("Metodo", "listaLogradouro");
        post.setParameter("TipoConsulta", "relaxation");
        post.setParameter("StartRow", "1");
        post.setParameter("EndRow", "10");
                                
        int codigo = client.executeMethod(post);
        InputStream is = post.getResponseBodyAsStream();
        InputStreamReader isr = new InputStreamReader(is, "ISO-8859-1");
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String buf = null;
        while ((buf = br.readLine()) != null) {
            sb.append(buf);
            sb.append("\n");
        }
        br.close();
        isr.close();
        is.close();
        post.releaseConnection();

        String res = sb.toString();
//        System.out.println(res);
//        System.out.flush();
        
        /**
         * <tr bgcolor="#ECF3F6" onclick="javascript:detalharCep('1','2');" style="cursor: pointer;">
         * <td width="268" style="padding: 2px">Rua Marechal Deodoro</td>
         * <td width="140" style="padding: 2px">Centro</td>
         * <td width="140" style="padding: 2px">Itu</td>
         * <td width="25" style="padding: 2px">SP</td>
         * <td width="65" style="padding: 2px">13300-110</td>
         * </tr>
         */
        String constante = "<tr bgcolor=\"#ECF3F6\" onclick=\"javascript:detalharCep('1','2');\" style=\"cursor: pointer;\">";
               constante = "<tr bgcolor=\"#ECF3F6\" onclick=\"javascript:detalharCep('1','5');\" style=\"cursor: pointer;\">";
               constante = "onclick=\"javascript:detalharCep";
        int idx1 = res.indexOf(constante);
        idx1 = res.indexOf("style=\"cursor: pointer;\">", idx1) + "style=\"cursor: pointer;\">".length();
        int idx2 = res.indexOf("</tr>", idx1);
        if (idx1 >= 0 && idx2 > idx1) {
            result.setResultado("1");
            String filtrar = res.substring(idx1, idx2);
            filtrar = filtrar.replace(constante, "");
            filtrar = semEspacoDuploSemLinhaDupla(filtrar);
            
            String[] frases = filtrar.split("</td>");
            if (frases != null) {
                int i = 1;
                for (String frase : frases) {
                    idx1 = frase.indexOf(">");
                    String conteudo = frase.substring(idx1 + 1);
                    System.out.println(conteudo);
                    switch (i) {
                        case 1: {
                            idx1 = conteudo.indexOf(" ");
                            String tipo = conteudo.substring(0, idx1);
                            String logradouro = conteudo.substring(idx1 + 1);
                            result.setTipo_logradouro(tipo);
                            result.setLogradouro(logradouro);
                        }
                        break;
                        case 2: {
                            result.setBairro(conteudo);
                        }
                        break;
                        case 3: {
                            result.setCidade(conteudo);
                        }
                        break;
                        case 4: {
                            result.setUf(conteudo);
                        }
                        break;
                        case 5: {
                            result.setCep(conteudo);
                        }
                        break;
                    }
                    i++;
                }
            }
        }
        return result;
    }
}
