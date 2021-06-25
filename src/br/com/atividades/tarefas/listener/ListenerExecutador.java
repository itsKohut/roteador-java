package br.com.atividades.tarefas.listener;

import br.com.atividades.dto.Mensagem;
import br.com.atividades.Roteador;
import br.com.atividades.dto.Roteamento;
import br.com.atividades.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.TimerTask;

import static br.com.atividades.Roteador.inetAddress;

class ListenerExecutador extends TimerTask {

    private DatagramSocket datagramSocket;

    public ListenerExecutador(DatagramSocket socket) {
        this.datagramSocket = socket;
    }

    private static String transformarPorta(Integer porta) {
        return porta.equals(0) ? "Localmente" : ("para a porta " + porta);
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void run() {

        DatagramPacket packet = new DatagramPacket(new byte[1024 * 12], 0, new byte[1024 * 12].length);

        try {

            this.getDatagramSocket().receive(packet);

            byte[] pacoteRecebido = packet.getData();
            ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(pacoteRecebido));
            Object object = iStream.readObject();

            Mensagem mensagem = null;
            List<Roteamento> tabela = null;

            /** Esse tipo de objeto sera apenas enviado por input do usuário*/
            if (Mensagem.class.isInstance(object)) { // mensagem comum
                mensagem = (Mensagem) object;

                System.out.format("A mensagem '%s' foi enviada da porta %d %s %s\n",
                        mensagem.getTexto(),
                        mensagem.getPortaOrigem(),
                        transformarPorta(mensagem.getPortaDestino()),
                        mensagem.getBase64Image() == null ? "" : "(mensagem com imagem anexada");
            }

            /** Esse tipo de objeto sera apenas enviado através de uma thread agendadora ou evento de exit de um roteador*/
            if (object instanceof java.util.List) { // mensagem de atualização de tabela de roteamento
                if (((List) object).size() > 0 && (((List) object).get(0) instanceof Roteamento)) {
                    tabela = (List<Roteamento>) object;

                    System.out.println(tabela);
                    // TODO lógica de atualização da tabela de roteamento
                }
            }

            iStream.close();

            if(mensagem != null && mensagem.getTexto().contains("#KEEP-ALIVE")) {
                System.out.println("Mensagem do keep-alive recebida com sucesso"); // comentar este print quando testado
                return; // sai do metodo para nao continuar a execulçao de demais logica abaixo
            }

            /** verifica se é ou não o roteador final*/
            boolean roteadorFinal = verificarRoteadorFinal(mensagem.getPortaDestino(), getDatagramSocket().getLocalPort());

            if (roteadorFinal) {

                if (mensagem != null && mensagem.getBase64Image() != null) {
                    System.out.format("Imagem salva no diretório %s)\n", Image.RESOURCE_PATH);
                    Image.decodeImageToBase64(mensagem);
                }
                System.out.println("Mensagem enviada para o roteador destino com sucesso!");

            /** caso não for repassa a mensagem para o proximo roteador */
            } else {
                System.out.println("Mensagem roteada para a proxima rota");

                Integer proximaPorta = Roteador.proximoRoteador(mensagem.getPortaOrigem(), mensagem.getPortaDestino());
                DatagramPacket newPacket = new DatagramPacket(pacoteRecebido, 0, pacoteRecebido.length, inetAddress, proximaPorta);

                getDatagramSocket().send(newPacket);
            }

        } catch (IOException ioException) {
            //TODO default message error
        } catch (ClassNotFoundException e) {
            //TODO default message error
        }
    }

    private boolean verificarRoteadorFinal(int portaDestino, int portaSocket) {
        return portaDestino == portaSocket || portaDestino == 0;
    }
}


