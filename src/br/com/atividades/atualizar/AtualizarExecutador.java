package br.com.atividades.atualizar;

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

    public void run() {

        vizinhos.forEach((portalLocal, portaVizinho) -> {
            try {

                //TODO aqui tem um problema, a gente atualiza as nossas colections de controle quando retira um cara, quando chegar aqui
                // o vizinho ja vai ter sido removido do nosso lado e não vai mandar a tabela para o antigo vizinho atualizar a sua tabela;
                // solução: disparar uma mensagem unica quando fazer o evento de remover da tabela local
                // ou
                // solução: criar uma outra collection que ao tirar o uma porta vizinha, adiciona em um collection de controle nova, e quando executar esse fluxo
                // remove dela (sem controle pra saber se houve a atualização correta etc)
                // metodo que deveria ser alterador (fecharPorta na classe Roteador)
                // pegaria a porta assim Integer portaVizinhoAssociada = vizinhos.remove(porta); e salvaria
                // em uma collection para disparo do evento na proxima execução da tarefa de atualização

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
        });
    }
}


