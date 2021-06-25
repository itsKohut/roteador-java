package br.com.atividades.tarefas.keepalive;

import br.com.atividades.dto.Mensagem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

import static br.com.atividades.Roteador.inetAddress;

class KeepAliveExecutador extends TimerTask {

    private DatagramSocket datagramSocket;
    private Integer portaVizinho;
    private Mensagem mensagem;

    public KeepAliveExecutador(Integer portaVizinho, DatagramSocket socket) {
        this.portaVizinho = portaVizinho;
        this.datagramSocket = socket;
        this.mensagem = new Mensagem(null, null, "#KEEP-ALIVE");
    }

    public void run() {

        try {

            // vai sempre mandar a mensagem com keep-alive como codigo, na tarefa de listener vai caputar essa mensagem sem printar nada, apenas para que a porta do vizinho nesta porta do nosso roteador
            // ao seja desvinculada.
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bStream);
            oo.writeObject(this.mensagem);
            oo.close();

            byte[] serializedMessage = bStream.toByteArray();

            DatagramPacket newPacket = new DatagramPacket(serializedMessage, 0, serializedMessage.length, inetAddress, portaVizinho);

            datagramSocket.send(newPacket);

        } catch (IOException e) {
        }
    }
}


