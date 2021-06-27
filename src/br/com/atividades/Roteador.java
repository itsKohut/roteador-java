package br.com.atividades;

import br.com.atividades.dto.Mensagem;
import br.com.atividades.dto.Roteamento;
import br.com.atividades.printer.Impressora;
import br.com.atividades.tarefas.atualizar.AtualizarTarefa;
import br.com.atividades.tarefas.keepalive.KeepAliveTarefa;
import br.com.atividades.tarefas.listener.ListenerTarefa;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.atividades.image.Image.encodedImageToBase64;
import static br.com.atividades.printer.Impressora.imprime;
import static br.com.atividades.printer.Impressora.menu;

public class Roteador {

    public static String LOCALHOST = "localhost";
    public static InetAddress inetAddress;
    public static Integer quantidadePortaLocal; // quantidade de portas locais (1 ou 2)

    //controle
    public static List<Roteamento> tabelaRoteamento = Collections.synchronizedList(new ArrayList());
    public static Map<Integer, Integer> vizinhos = new HashMap<>(); // porta local (chave) e porta vizinho (valor)

    //tarefas
    public static Map<Integer, AtualizarTarefa> atualizarTarefas = new HashMap<>(); // porta (chave) e atualizar tarefa da porta (valor)
    public static Map<Integer, DatagramSocket> datagramSocketSenders = new HashMap<>(); // porta (chave) e socket da porta (valor)
    public static Map<Integer, KeepAliveTarefa> keepAliveTarefas = new HashMap<>(); // porta (chave) e keepalive da porta (valor)
    public static Map<Integer, ListenerTarefa> listenerTasks = new HashMap<>(); // porta (chave) e thread de listening (valor)

    public static boolean roteadorJaFoiLigado = false;

    public static void main(String[] args) throws IOException {

        System.out.println(">>> Router iniciado <<<");

        inetAddress = InetAddress.getByName(LOCALHOST); // definindo o ip padrão para todas as rotas (localhost)

        Scanner input = new Scanner(System.in);

        /** Bloco inicial de configuração do roteador*/
        definirQuantidadePortasLocais(input);
        definirPortasLocais(input);
        definirVizinhos(input);

        /** Loop principal da aplicação em execução */
        comando(input);
    }

    private static void definirQuantidadePortasLocais(Scanner input) {
        do {
            System.out.println("# Definir a quantidade de portas locais (1 ou 2)");

            // configuração de ambiente
            if (System.getenv("QTD_PORTAS") != null) {
                quantidadePortaLocal = Integer.valueOf(System.getenv("QTD_PORTAS"));
            } else {
                quantidadePortaLocal = input.nextInt();
            }

        } while (quantidadePortaLocal <= 0 || quantidadePortaLocal > 2 || quantidadePortaLocal == null);
    }

    private static void definirPortasLocais(Scanner input) throws SocketException {

        for (int i = 0; i < quantidadePortaLocal; i++) {
            System.out.format("# Definir porta local %d (%d/%d) - ex: 6060\n", i + 1, i + 1, quantidadePortaLocal);

            Integer porta;

            // configuração de ambiente
            if (System.getenv("QTD_PORTAS") != null) {
                porta = Integer.valueOf(System.getenv("PORTA_LOCAL" + (i + 1)));
            } else {
                porta = input.nextInt();
            }

            // adiciona o mapeamento da rota local desta porta
            Roteamento roteamento = new Roteamento(porta, 0, 0, 0); // 0 igual a local
            tabelaRoteamento.add(roteamento);

            //adiciona a porta na collection de controle dos sockets
            DatagramSocket datagramSocket = null;
            datagramSocket = new DatagramSocket(porta);
            datagramSocketSenders.put(porta, datagramSocket);
        }
    }

    private static void definirVizinhos(Scanner input) throws SocketException {

        // pega toddas as portas locais
        List<Integer> portas = tabelaRoteamento.stream()
                .map(tr -> tr.getPortaDestino())
                .collect(Collectors.toList());

        int incrementaPorta = 0;
        for (Integer porta : portas) {

            System.out.format("# Associar porta %d com uma porta vizinha de outro roteador - ex: 6061\n", porta);

            // adiciona a porta e a sua vizinha no collection de vizinhos
            Integer portaVizinho;

            // configuração de ambiente
            if (System.getenv("QTD_PORTAS") != null) {
                portaVizinho = Integer.valueOf(System.getenv("PORTA_VIZINHA" + (++incrementaPorta)));
            } else {
                portaVizinho = input.nextInt();
            }

            vizinhos.put(porta, portaVizinho);

            // adiciona o mapeamento da rota para o vizinho
            Roteamento roteamento = new Roteamento(portaVizinho, 1, portaVizinho, porta); // 0 igual a local // 1 igual a externo
            tabelaRoteamento.add(roteamento);
        }
    }

    public static void comando(Scanner input) throws IOException {

        /**
         * 1 - Enviar mensagem ";
         * 2 - Imprimir as portas e seu respectivo vizinho";
         * 3 - Imprimir tabela de roteamento";
         * 4 - Desligar porta";
         * 5 - Desligar roteador";
         * 6 - Ligar porta";
         * 7 - Ligar roteador"
         * */

        ligarRoteador();

        menu();
        int porta;

        switch (input.nextInt()) {
            case 1:
                enviarMensagem(input);
                break;
            case 2:
                imprime(vizinhos);
                break;
            case 3:
                imprime(tabelaRoteamento);
                break;
            case 4:
                desligarPorta(input, false);
                break;
            case 5:
                desligarRoteador(true);
                break;
            case 6:
                ligarPorta(input);
                break;
            case 7:
                ligarRoteador();
                break;
            default:
                System.out.println("Comando inválido");
        }

        comando(input);
    }

    public static Integer proximoRoteador(int portaOrigem, int portaDestino) throws IllegalArgumentException {

        imprime(tabelaRoteamento);

        // Roteamento com envio local (mesmo roteador)
        if (portaDestino == 0 || portaDestino == portaOrigem) { // caso seja um envio local pega a porta origem do envio
            return portaOrigem;
        }

        // Roteamento com envio local (o outro roteador local)
        if (vizinhos.containsKey(portaDestino)) {
            return portaDestino;
        }

        // Roteamento com envio externo (outro roteador que não seja o mesmo que envia a mensagem)
        return tabelaRoteamento.stream()
                .filter(tr -> tr.getPortaDestino() == portaDestino)
                .map(tb -> tb.getPortaSaida())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não é possível enviar esta mensagem. " +
                        "Motivos: Porta local inválida ou não há porta destino no roteamento deste roteador"));

    }

    private static void enviarMensagem(Scanner input) throws IOException {

        System.out.println("************************************************");

        System.out.format("# Digite a mensagem a ser enviada: (para envio de mensagem adicionar a mensagem a tag '#image')\n");
        input.nextLine(); // consome o \n
        String texto = input.nextLine();

        System.out.format("# Digite a porta origem:\n");
        Integer portaOrigem = input.nextInt();

        System.out.format("# Digite a porta destino:\n");
        Integer portaDestino = input.nextInt();

        System.out.println("************************************************");

        Mensagem mensagem = new Mensagem(portaOrigem, portaDestino, texto);

        // caso a mensagem contenha a palatra #image vai adicionar o base64 na mensagem
        if (texto.contains("#image")) {
            texto.replaceAll("#imagem", "");
            mensagem.setBase64Image(encodedImageToBase64());
        }

        // Serialize do objeto para um byte array
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(mensagem);
        oo.close();

        byte[] serializedMessage = bStream.toByteArray();

        try {
            // pacote e envio da mensagem
            DatagramSocket socket = datagramSocketSenders.get(portaOrigem);
            Integer proximaPorta = proximoRoteador(portaOrigem, portaDestino);
            DatagramPacket newPacket = new DatagramPacket(serializedMessage, 0, serializedMessage.length, inetAddress, proximaPorta);
            socket.send(newPacket);

        } catch (Exception e) {
            System.out.println("Erro para enviar a mensagem para a porta destino");
        }
    }

    public static void ligarRoteador() {

        if (roteadorJaFoiLigado == false) {

            vizinhos.forEach((porta, vizinho) -> {
                ligarPorta(porta, vizinho);
            });
        }
        roteadorJaFoiLigado = true;
    }

    private static void ligarPorta(Scanner input) {
        imprime(vizinhos.keySet());
        System.out.println("Digite o numero da porta que quer ligar: ");
        int porta = input.nextInt();
        System.out.println("Digite o numero da porta que quer associas com a porta: " + porta);
        int portaVizinho = input.nextInt();
        ligarPorta(porta, portaVizinho);
    }

    private static void ligarPorta(int portaLocal, int portaVizinho) {

        vizinhos.put(portaLocal, portaVizinho);

        DatagramSocket datagramSocket = datagramSocketSenders.get(portaLocal);
        if (datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket(portaLocal);
                datagramSocketSenders.put(portaLocal, datagramSocket);
            } catch (SocketException e) {
                System.out.println("Porta já está sendo utilizada por outro roteador");
            }
        }

        // tarefa que fica escutando a porta
        ListenerTarefa listenerTarefa = new ListenerTarefa(portaVizinho, 1, datagramSocket);
        listenerTasks.put(portaLocal, listenerTarefa);

        // tarefa que fica autliza a tabela de roteamento a porta
        AtualizarTarefa atualizarTarefa = new AtualizarTarefa(10, datagramSocket);
        atualizarTarefas.put(portaLocal, atualizarTarefa);

        // tarefa que fica verificando o keep alive a porta
        KeepAliveTarefa keepAliveTarefa = new KeepAliveTarefa(10, datagramSocket);
        keepAliveTarefas.put(portaLocal, keepAliveTarefa);

        System.out.format("Porta %s ligada do roteador\n", portaLocal);
    }

    private static void desligarRoteador(boolean fecharSocket) {
        List<Integer> portasVinculadasAEsteRoteador = vizinhos.keySet().stream().collect(Collectors.toList());

        for (Integer porta : portasVinculadasAEsteRoteador) {
            desligarPorta(porta, fecharSocket);
        }
        System.out.println("Roteador desligado");
        System.exit(0);
    }

    private static void desligarPorta(Scanner input, boolean fecharSocket) {
        imprime(vizinhos.keySet());
        System.out.println("Digite o numero da porta que quer desligar: ");
        int porta = input.nextInt();
        desligarPorta(porta, fecharSocket);
    }

    private static void desligarPorta(int porta, boolean fecharSocket) {
        DatagramSocket socket = datagramSocketSenders.remove(porta); // fecha o socket da porta
        removeReferenciasDaTabelaDeRoteamento(porta); // remove a porta da tabela

        ListenerTarefa task = listenerTasks.remove(porta);
        task.getTimer().cancel(); // mata a task de agendamento do listener da porta

        KeepAliveTarefa keepAliveTarefa = keepAliveTarefas.remove(porta);
        keepAliveTarefa.getTimer().cancel(); // mata a task de keepalive

        AtualizarTarefa atualizarTarefa = atualizarTarefas.remove(porta);
        atualizarTarefa.getTimer().cancel(); // mata a task de atualização da tabela de roteamento

        if (fecharSocket) {
            socket.close(); // fecha o socket
        }

        imprime(tabelaRoteamento);
        System.out.format("Porta %s desligada do roteador\n", porta);
    }

    public synchronized static void removeReferenciasDaTabelaDeRoteamento(int porta) {

        List<Roteamento> tabelaRoteamentoAtualizada = tabelaRoteamento.stream()
                .filter(tb -> tb.getPortaDestino() != porta) // remove a rota local
                .filter(tb -> tb.getPortaSaida() != porta)
                .collect(Collectors.toList());

        tabelaRoteamento = Collections.synchronizedList(new ArrayList<>(tabelaRoteamentoAtualizada));
        Impressora.imprime(tabelaRoteamento);
    }

    public static synchronized void atualizarTabela(List<Roteamento> tabela, Integer portaVizinho, Integer porta) {

        tabela.stream().forEach(t -> {

            // se não existe inclui
            if (!existePortaDestinoJaNaTabelaIncluir(t.getPortaDestino())) {
                adicionaPortaNaTabelaRoteamento(t, portaVizinho, porta);
            }

            excluiPortaNaTabelaRoteamento(tabela, portaVizinho, porta);

        });
    }

    private static boolean existePortaDestinoJaNaTabelaIncluir(int porta) {
        return tabelaRoteamento.stream()
                .anyMatch(tb -> tb.getPortaDestino() == porta);
    }

    private static void adicionaPortaNaTabelaRoteamento(Roteamento roteamento, Integer portaVizinho, Integer porta) {

        // nao aprender de um rota que foi ele quem ensinou
        if (!(vizinhos.containsKey(roteamento.getPortaSaida()) && roteamento.getMetrica() > 1)) {
            roteamento.incrementaMetrica();
            roteamento.setPortaSaida(portaVizinho);
            roteamento.setPortaLocalSaida(porta);
            tabelaRoteamento.add(roteamento);
            System.out.format("Porta %d adicionado na tabela de roteamento\n", roteamento.getPortaDestino());
        }
    }

    private static void excluiPortaNaTabelaRoteamento(List<Roteamento> tabela, Integer portaVizinho, Integer porta) {

//        System.out.println("portavizinho: " + portaVizinho + "  porta:" + porta);
        List<Integer> recebido = tabela.stream().map(t -> t.getPortaDestino()).collect(Collectors.toList());
        List<Integer> atual = tabelaRoteamento.stream().map(t -> t.getPortaDestino()).collect(Collectors.toList());

        List<Integer> diferenca = atual.stream()
                .filter(element -> !recebido.contains(element))
                .collect(Collectors.toList());

//        System.out.println(recebido);
//        System.out.println(atual);
//        System.out.println(diferenca);

        // o cara que foi ensinado nao pode deletar a porta
        if (!diferenca.isEmpty()) {
            List<Roteamento> deletarRoteamentos = new ArrayList<>();
            for (Integer pd : diferenca) {
                tabelaRoteamento.stream()
                        .filter((tb -> ((tb.getMetrica() > 1 && tb.getPortaDestino().equals(pd) && tb.getPortaSaida() == portaVizinho))
                                || (tb.getMetrica() == 1 && tb.getPortaDestino().equals(pd) && portaVizinho.equals(tb.getPortaSaida()))))
                        .forEach(delete -> {

                            deletarRoteamentos.add(delete);
                            System.out.format("Porta %d deletada na tabela de roteamento\n", delete.getPortaDestino());
                        });
            }

//            imprime(tabela);
//            System.out.println("+++++++++++++++++++++++++++++++++++");
//            imprime(tabelaRoteamento);
            tabelaRoteamento.removeAll(deletarRoteamentos);
        }
    }
}