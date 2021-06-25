package br.com.atividades.dto;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private Integer portaOrigem;
    private Integer portaDestino;
    private String texto;

    private String base64Image;

    public Mensagem(Integer portaOrigem, Integer portaDestino, String texto) {
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        this.texto = texto;
        this.base64Image = base64Image;
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

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String encodedImageToBase64) {
        this.base64Image = encodedImageToBase64;
    }
}
