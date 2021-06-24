package br.com.atividades.dto;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private Integer portaOrigem;
    private Integer portaDestino;
    private String texto;

    public Mensagem(Integer portaOrigem, Integer portaDestino, String texto) {
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        this.texto = texto;
    }

    public Integer getPortaOrigem() {
        return portaOrigem;
    }

    public Integer getPortaDestino() {
        return portaDestino;
    }

    public String getTexto() {
        return texto;
    }
}
