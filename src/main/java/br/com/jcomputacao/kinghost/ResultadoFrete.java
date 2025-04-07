package br.com.jcomputacao.kinghost;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 15/10/2011 15:15:36
 * @author Murilo
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "webservice")
@XmlRootElement(name="webservice")
public class ResultadoFrete {
    private String resultado;
    private String resultado_txt;
    private String valor;
    private String cidade_origem;
    private String cidade_destino;

    public String getCidade_destino() {
        return cidade_destino;
    }

    public void setCidade_destino(String cidade_destino) {
        this.cidade_destino = cidade_destino;
    }

    public String getCidade_origem() {
        return cidade_origem;
    }

    public void setCidade_origem(String cidade_origem) {
        this.cidade_origem = cidade_origem;
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

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
