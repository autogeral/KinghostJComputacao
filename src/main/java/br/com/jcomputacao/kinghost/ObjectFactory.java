package br.com.jcomputacao.kinghost;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * 15/10/2011 16:28:50
 * @author Murilo
 */
@XmlRegistry
public class ObjectFactory {

    public ResultadoCep createResultadoCep() {
        return new ResultadoCep();
    }

    public ResultadoFrete createResultadoFrete() {
        return new ResultadoFrete();
    }
}
