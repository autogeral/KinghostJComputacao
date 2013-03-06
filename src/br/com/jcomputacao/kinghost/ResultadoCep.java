package br.com.jcomputacao.kinghost;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 15/10/2011 15:15:23
 * @author Murilo
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "webservicecep")
@XmlRootElement(name="webservicecep")
public class ResultadoCep {
    private String uf;
    private String cidade;
    private String resultado;
    private String resultado_txt;
    private String bairro;
    private String tipo_logradouro;
    private String logradouro;

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getResultado_txt() {
        return resultado_txt;
    }

    public void setResultado_txt(String resultado_txt) {
        this.resultado_txt = resultado_txt;
    }

    public String getTipo_logradouro() {
        return tipo_logradouro;
    }

    public void setTipo_logradouro(String tipo_logradouro) {
        this.tipo_logradouro = tipo_logradouro;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}