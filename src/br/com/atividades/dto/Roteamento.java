package br.com.atividades.dto;

import java.io.Serializable;

public class Roteamento implements Serializable {

    private Integer portaDestino;
    private Integer metrica;
    private Integer portaSaida;
    private Integer portaLocalSaida;

    public Roteamento(Integer portaDestino, Integer metrica, Integer portaSaida, Integer portalLocalSaida) {
        this.portaDestino = portaDestino;
        this.metrica = metrica;
        this.portaLocalSaida = portalLocalSaida;
        this.portaSaida = portaSaida;
    }

    public Integer getPortaDestino() {
        return portaDestino;
    }

    public Integer getMetrica() {
        return metrica;
    }

    public Integer getPortaSaida() {
        return portaSaida;
    }

    public Integer getPortaLocalSaida() {
        return portaLocalSaida;
    }

    @Override
    public String toString() {
        return "Roteamento{" +
                "portaDestino=" + portaDestino +
                ", metrica=" + metrica +
                ", portaSaida=" + portaSaida +
                ", portaLocalSaida=" + portaLocalSaida +
                '}';
    }

}
