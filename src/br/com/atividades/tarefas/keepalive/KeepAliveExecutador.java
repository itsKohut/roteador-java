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
import static br.com.atividades.Roteador.vizinhos;

class KeepAliveExecutador extends TimerTask {

    private DatagramSocket datagramSocket;
    private Mensagem mensagem;

    public KeepAliveExecutador(DatagramSocket socket) {
        this.datagramSocket = socket;
        this.mensagem = new Mensagem(null, null, "#KEEP-ALIVE");
    }

    public void run() {

        Integer portaLocal = datagramSocket.getLocalPort();
        Integer portaVizinho = vizinhos.get(portaLocal);

        if (portaVizinho != null) {

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
                //System.out.println("Erro ao enviar o keep alive para a porta do vizinho");
            }
        } else {
//            System.out.println("Nenhum vizinho vinculado a esta porta");
        }
    }
}


