package br.com.atividades.tarefas.atualizar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

import static br.com.atividades.Roteador.*;

class AtualizarExecutador extends TimerTask {

    private DatagramSocket datagramSocket;

    public AtualizarExecutador(DatagramSocket socket) {
        this.datagramSocket = socket;
    }

    public synchronized void run() {

        vizinhos.forEach((portalLocal, portaVizinho) -> {

            if (portaVizinho != null) {
                try {

                    // Serialize to a byte array
                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    ObjectOutput oo = new ObjectOutputStream(bStream);
                    oo.writeObject(tabelaRoteamento);
                    oo.close();

                    byte[] serializedMessage = bStream.toByteArray();

                    DatagramSocket socket = datagramSocketSenders.get(portalLocal);

                    DatagramPacket newPacket = new DatagramPacket(serializedMessage, 0, serializedMessage.length, inetAddress, portaVizinho);

                    socket.send(newPacket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


