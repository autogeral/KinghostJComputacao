package br.com.jcomputacao.kinghost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
    private final boolean mobile = Boolean.parseBoolean(System.getProperty("buscaCep.correio.mobile", "true"));
    //link sobre o Via Cep -  https://viacep.com.br/
    private final boolean wsViaCep = Boolean.parseBoolean(System.getProperty("buscaCep.webService.viaCep", "true"));;

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
        ResultadoCep result = new ResultadoCep();
        String aurl = "http://m.correios.com.br/movel/buscaCepConfirma.do";
        boolean usaBibliotecaJsoup = Boolean.parseBoolean(System.getProperty("usa.biblioteca.jsoup.buscaCep", "true"));
        if (usaBibliotecaJsoup) {
            Map<String, String> query = new HashMap<String, String>();
            query.put("cepEntrada", cep);
            query.put("tipoCep", "");
            query.put("cepTemp", "");
            query.put("metodo", "buscarCep");
            Document doc = Jsoup.connect(aurl)
                    .data(query)
                    .post();
            Elements elementos = doc.select(".respostadestaque");
            if (elementos.size() > 0) {
                if (elementos.size() >= 4) {
                    if (elementos.get(0) != null && elementos.get(0).text() != null && !elementos.get(0).text().isEmpty()) {
                        System.out.println("Logradouro: " + elementos.get(0).text());
                        result.setLogradouro(elementos.get(0).text());
                    }
                    if (elementos.get(1) != null && elementos.get(1).text() != null && !elementos.get(1).text().isEmpty()) {
                        System.out.println("Bairro: " + elementos.get(1).text());
                        result.setBairro(elementos.get(1).text());
                    }
                    if (elementos.get(2) != null && elementos.get(2).text() != null && !elementos.get(2).text().isEmpty()) {
                        System.out.println("Localidade/UF: " + elementos.get(2).text());
                        String cidade = StringUtil.noDeadKeysToUpperCase(elementos.get(2).text().substring(0, elementos.get(2).text().indexOf("/")));
                        String uf = elementos.get(2).text().substring(elementos.get(2).text().indexOf("/") + 1, elementos.get(2).text().length());
                        result.setCidade(cidade.toUpperCase().trim());
                        result.setUf(uf);
                    }
                    if (elementos.get(3) != null && elementos.get(3).text() != null && !elementos.get(3).text().isEmpty()) {
                        System.out.println("CEP: " + elementos.get(3).text());
                        result.setCep(elementos.get(3).text());
                    }
                } else if (elementos.size() == 2 && elementos.get(0).text().contains("/")) {
                    if (elementos.get(0) != null && elementos.get(0).text() != null && !elementos.get(0).text().isEmpty()) {
                        System.out.println("Localidade/UF: " + elementos.get(0).text());
                        String cidade = elementos.get(0).text().substring(0, elementos.get(0).text().indexOf("/"));
                        String uf = elementos.get(0).text().substring(elementos.get(0).text().indexOf("/") + 1, elementos.get(0).text().length());
                        result.setCidade(cidade.toUpperCase().trim());
                        result.setUf(uf);
                    }
                    if (elementos.get(1) != null && elementos.get(1).text() != null && !elementos.get(1).text().isEmpty()) {
                        System.out.println("CEP: " + elementos.get(1).text());
                        result.setCep(elementos.get(1).text());
                    }
                }
            } else {
                System.out.println("CEP não encontrado");
            }
        } else {

            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(aurl);

            post.setParameter("cepEntrada", cep);
            post.setParameter("metodo", "buscarCep");
            post.setParameter("tipoCep", "");
            post.setParameter("cepTemp", "");

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
            System.out.println(res);
            int index1 = res.indexOf("<div class=\"caixacampobranco\">");
            int index2 = res.indexOf("<span class=\"resposta\">CEP: </span><span class=\"respostadestaque\">"+cep+"</span><br/>");

            if (index1 >= 0 && index2 > index1) {
                result.setResultado("1");
                String filtrar = res.substring(index1, index2);
                filtrar = filtrar.replace("<span class=\"resposta\">", "");
                filtrar = semEspacoDuploSemLinhaDupla(filtrar);
                filtrar = filtrar.replace("<span class=\"respostadestaque\">", "");
                filtrar = filtrar.replace("<div class=\"caixacampobranco\">", "");
                filtrar = filtrar.replace("</span>", "");
                filtrar = filtrar.replace("<br/>", "");

                int inicio = 0;
                int fim = 0;
                if (filtrar.contains("Logradouro:")) {
                    inicio = filtrar.indexOf("Logradouro:");
                    fim = filtrar.indexOf("Bairro:");
                    String logradouro = filtrar.substring(inicio, fim);
                    result.setLogradouro(logradouro.replace("Logradouro:", ""));
                } else if(filtrar.contains("Endereço:")) {
                    inicio = filtrar.indexOf("Endereço:");
                    fim = filtrar.indexOf("Bairro:");
                    String logradouro = filtrar.substring(inicio, fim);
                    result.setLogradouro(logradouro.replace("Endereço:", ""));
                }

                if (filtrar.contains("Localidade/UF:")) {
                    inicio = fim;
                    fim = filtrar.indexOf("Localidade/UF:");
                }

                if (filtrar.contains("Bairro:")) {
                    String bairro = filtrar.substring(inicio, fim);
                    result.setBairro(bairro.replace("Bairro:", ""));
                }

                inicio = fim;
                fim = filtrar.length();

                String cidade = filtrar.substring(inicio, fim);
                cidade = cidade.replace("Localidade/UF:", "");
                String aux = cidade.substring(0, cidade.indexOf("/")).toUpperCase().trim();
                result.setCidade(aux);

                String uf = cidade.substring(cidade.indexOf("/"), cidade.length()).replace("/", "").toUpperCase().trim();
                result.setUf(uf);

                result.setCep(cep);
            }
        }
        return result;

        //http://www.buscacep.correios.com.br/servicos/dnec/consultaEnderecoAction.do
//        String aurl = "http://m.correios.com.br/movel/buscaCepConfirma.do?cepEntrada=";
//        aurl += cep;
//        aurl += "&tipoCep=,&cepTemp=,&metodo=buscarCep";
//        System.out.println(aurl);
//        
//        boolean usaAutenticacaoProxy = Boolean.parseBoolean(System.getProperty("usa.autenticacao.proxy", "false"));
//        InputStream is = null;
//        URLConnection urlConnection = null;
//        URL url = null;
//        InputStreamReader isr = null;
//        BufferedReader br = null;
//        if (usaAutenticacaoProxy) {
//            String proxy = System.getProperty("http.proxyHost");
//            int port = Integer.parseInt(System.getProperty("http.proxyPorta"));
//            java.net.Proxy informacoesProxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(proxy, port));
//
//            urlConnection = new URL(aurl).openConnection(informacoesProxy);
//
//            HttpAuthProxy authProxy = new HttpAuthProxy();
//            urlConnection.setRequestProperty("Proxy-Authorization", "Basic" + authProxy.getPasswordAuthentication());
//            urlConnection.connect();
//            is = urlConnection.getInputStream();
//        } else {
//            url = new URL(aurl);
//            is = url.openStream();
//        }
//        
//        isr = new InputStreamReader(is, "ISO-8859-1");
//        br = new BufferedReader(isr);
//        
//        StringBuilder sb = new StringBuilder();
//        String buf = null;
//        while ((buf = br.readLine()) != null) {
//            sb.append(buf);
//            sb.append("\n");
//        }
//        br.close();
//        isr.close();
//        is.close();
//        
//        String res = sb.toString();
//        
//        
//        System.out.println(res);
//
//        int idx1 = res.indexOf("<div class=\"caixacampobranco\">");
//        int idx2 = res.indexOf("<div class=\"divopcoes\">");
//        if (idx1 >= 0 && idx2 > idx1) {
//            String filtrar = res.substring(idx1, idx2);
//            filtrar = semEspacoDuploSemLinhaDupla(filtrar);
//////            filtrar = semDiv(filtrar);
////            System.out.println("################################");
////            System.out.println("Resultado ate entao");
////            System.out.println("################################");
////            System.out.println(filtrar);
////            System.out.println("################################");
//            return trataSpans(filtrar);
//        } else {
//            System.out.println("Idx1 : " + idx1 + " Idx2 : " + idx2);
//        }
//        return new ResultadoCep();
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
        String aurl = "";
        if (!wsViaCep) {
            aurl = "http://www.buscacep.correios.com.br";
            aurl += "/servicos/dnec/consultaEnderecoAction.do";
        } else {
            aurl = "https://viacep.com.br/ws/";
            aurl += cep;
            aurl += "/xml/";//tipo do retorno desejado
        }
        
        GetMethod get = null;
        PostMethod post = null;

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

        if (!wsViaCep) {
            post = new PostMethod(aurl);
            post.setParameter("relaxation", cep);
            post.setParameter("TipoCep", "ALL");
            post.setParameter("semelhante", "N");

            post.setParameter("cfm", "1");
            post.setParameter("Metodo", "listaLogradouro");
            post.setParameter("TipoConsulta", "relaxation");
            post.setParameter("StartRow", "1");
            post.setParameter("EndRow", "10");
        } else {
            get = new GetMethod(aurl);
            get.addRequestHeader("User-Agent", "Auto Geral - ERPJ");
        }
                                
        int codigo = client.executeMethod(post != null ? post : get);
        InputStream is = (post != null ? post.getResponseBodyAsStream() : get.getResponseBodyAsStream());
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
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
        if(post != null) {
            post.releaseConnection();
        } else if (get != null) {
            get.releaseConnection();
        }

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
        if (!wsViaCep) {
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
        } else {
            String[] tagsXml = res.split("<[^>]*>");
            if (verificaSeTagErroXmlViaCep(tagsXml)) {
                result.setResultado("CEP Inválido, por favor informe um que seja válido!");
            } else {
                String aux = XmlUtil.getTagConteudo(res, "cep", false).get(0);
                result.setCep(aux);
                aux = XmlUtil.getTagConteudo(res, "logradouro", false).get(0);
                result.setLogradouro(aux);
                aux = XmlUtil.getTagConteudo(res, "bairro", false).get(0);
                result.setBairro(aux);
                aux = XmlUtil.getTagConteudo(res, "localidade", false).get(0);
                result.setCidade(StringUtil.noDeadKeysToUpperCase(aux));
                aux = XmlUtil.getTagConteudo(res, "uf", false).get(0);
                result.setUf(aux);
            }
        }
        return result;
    }
    
    private boolean verificaSeTagErroXmlViaCep(String[] tags) {
        for (String tag : tags) {
            if (tag.equals("true"))
                return true;
        }
        return false;
    }
}
