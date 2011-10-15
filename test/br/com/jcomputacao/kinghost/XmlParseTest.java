package br.com.jcomputacao.kinghost;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;

/**
 *
 * @author Murilo
 */
public class XmlParseTest {

    @Test
    public void testCep() {
        String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                + "<webservicecep>\n"
                + "<resultado>1</resultado>\n"
                + "<resultado_txt>sucesso - cep completo</resultado_txt>\n"
                + "<uf>SP</uf>\n"
                + "<cidade>Itu</cidade>\n"
                + "<bairro>Centro</bairro>\n"
                + "<tipo_logradouro>Rua</tipo_logradouro>\n"
                + "<logradouro>Marechal Deodoro</logradouro>\n"
                + "\n"
                + "</webservicecep>\n";
        try {
            JAXBContext context = JAXBContext.newInstance("br.com.jcomputacao.kinghost");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            ResultadoCep rc = unmarshaller.unmarshal(new StreamSource(is), ResultadoCep.class).getValue();
            is.close();

            System.out.println(rc.getTipo_logradouro() + " " + rc.getLogradouro());
            System.out.println(rc.getBairro());
            System.out.println(rc.getCidade());
            System.out.println(rc.getUf());

        } catch (JAXBException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testSedex() {
        String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                + "<webservice>\n"
                + "<resultado>1</resultado>\n"
                + "<resultado_txt>Valor do sedex de ITU/SP para SÃO PAULO/SP com peso de 1,00 kg é de R$ 14,70.</resultado_txt>\n"
                + "<valor>14.7</valor>\n"
                + "<valor_rs>R$ 14,70</valor_rs>\n"
                + "<cidade_origem>ITU/SP</cidade_origem>\n"
                + "<cidade_destino>SÃO PAULO/SP</cidade_destino>\n"
                + "\n"
                + "</webservice>";
        try {
            JAXBContext context = JAXBContext.newInstance("br.com.jcomputacao.kinghost");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            ResultadoFrete rc = unmarshaller.unmarshal(new StreamSource(is), ResultadoFrete.class).getValue();
            is.close();

            System.out.println(rc.getResultado());
            System.out.println(rc.getResultado_txt());
            
            System.out.println("Cidade origem : " + rc.getCidade_origem());
            System.out.println("Cidade destino: " + rc.getCidade_destino());
            System.out.println("Valor         : " + rc.getValor());
        } catch (JAXBException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testCartaRegistrada() {
        String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                + "<webservice>\n"
                + "<resultado>1</resultado>\n"
                + "<resultado_txt>Carta com peso de 486 gramas com valor total de R$ 5,60.</resultado_txt>\n"
                + "<valor>5.6</valor>\n"
                + "<valor_rs>R$ 5,60</valor_rs>\n"
                + "<cidade_origem>/</cidade_origem>\n"
                + "<cidade_destino>/</cidade_destino>\n"
                + "\n"
                + "</webservice>";
        try {
            JAXBContext context = JAXBContext.newInstance("br.com.jcomputacao.kinghost");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            ResultadoFrete rc = unmarshaller.unmarshal(new StreamSource(is), ResultadoFrete.class).getValue();
            is.close();

            System.out.println(rc.getResultado());
            System.out.println(rc.getResultado_txt());
            
            System.out.println("Cidade origem : " + rc.getCidade_origem());
            System.out.println("Cidade destino: " + rc.getCidade_destino());
            System.out.println("Valor         : " + rc.getValor());
        } catch (JAXBException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testPac() {
        String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                + "<webservice>\n"
                + "<resultado>1</resultado>\n"
                + "<resultado_txt>Valor do pac de ITU/SP para SÃO PAULO/SP com peso de 1,00 kg é de R$ 11,00.</resultado_txt>\n"
                + "<valor>11</valor>\n"
                + "<valor_rs>R$ 11,00</valor_rs>\n"
                + "<cidade_origem>ITU/SP</cidade_origem>\n"
                + "<cidade_destino>SÃO PAULO/SP</cidade_destino>\n"
                + "\n"
                + "</webservice>";

        try {
            JAXBContext context = JAXBContext.newInstance("br.com.jcomputacao.kinghost");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
            ResultadoFrete rc = unmarshaller.unmarshal(new StreamSource(is), ResultadoFrete.class).getValue();
            is.close();

            System.out.println(rc.getResultado());
            System.out.println(rc.getResultado_txt());
            
            System.out.println("Cidade origem : " + rc.getCidade_origem());
            System.out.println("Cidade destino: " + rc.getCidade_destino());
            System.out.println("Valor         : " + rc.getValor());
        } catch (JAXBException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
