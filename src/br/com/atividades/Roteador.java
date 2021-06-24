package br.com.atividades;

import br.com.atividades.dto.Mensagem;
import br.com.atividades.dto.Roteamento;
import br.com.atividades.listener.ListenerTarefa;
import br.com.atividades.printer.Impressora;

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

public class Roteador {

    public static String LOCALHOST = "localhost";
    public static InetAddress inetAddress;
    public static Integer quantidadePortaLocal; // quantidade de portas locais (1 ou 2)
    public static List<Roteamento> tabelaRoteamento = Collections.synchronizedList(new ArrayList());
    public static Map<Integer, Integer> vizinhos = new HashMap<>(); // porta local (chave) e porta vizinho (valor)
    public static Map<Integer, ListenerTarefa> listenerTasks = new HashMap<>(); // porta (chave) e thread de listening (valor)
    public static Map<Integer, DatagramSocket> datagramSocketSenders = new HashMap<>(); // porta (chave) e socket da porta (valor)

    public static void main(String[] args) throws IOException {
        System.out.println(">>> Router iniciado <<<");

        inetAddress = InetAddress.getByName(LOCALHOST); // definindo o ip padrão para todas as rotas (localhost)

        Scanner input = new Scanner(System.in);

        /** Bloco inicial de configuração do roteador*/
        //TODO não sei se é necessário poder em runtime vincular uma porta a outro roteador (Acredito que não)
        definirQuantidadePortasLocais(input);
        definirPortasLocais(input);
        definirVizinhos(input);

        /** Loop principal da aplicação em execução */
        //TODO criar dentro do comando a opção para ativar o envio da tabela de roteamento para as tabelas vizinhas,
        // deste modo apos o usuário configurar toda a rede dos roteadores, ativa-la para começar a atualização e evitar
        // cenários de ao tentar atualizar um roteador que ainda não foi criado.
        comando(input);
    }

    public static void comando(Scanner input) throws IOException {

        /**
         * 1 - Enviar mensagem ");
         * 2 - Imprimir as portas e seu respectivo vizinho");
         * 3 - Imprimir tabela de roteamento");
         * 4 - Remover uma porta do roteador ");
         * 5 - Desligar roteador");
         * */

        Impressora.menu();

        switch (input.nextInt()) {
            case 1:
                enviarMensagem(input);
                break;
            case 2:
                Impressora.imprime(vizinhos);
                break;
            case 3:
                Impressora.imprime(tabelaRoteamento);
                break;
            case 4:
                Impressora.imprime(vizinhos.keySet());
                System.out.println("Digite o numero da porta que quer fechar: ");
                int porta = input.nextInt();
                fecharPorta(porta);
                break;
            case 5:
                desligarRoteador();
                break;
            case 6:
                //TODO chamada do metodo que cria a thread de atualização do roteamento (ver de exxemplo as classes no package listener)
                break;
            default:
                System.out.println("Comando inválido");
        }
        comando(input);
    }

    public static Integer proximoRoteador(int portaOrigem, int portaDestino) {

        Impressora.imprime(tabelaRoteamento);

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
                .filter(tr -> tr.getPortaLocalSaida() == portaOrigem)
                .filter(tr -> tr.getPortaDestino() == portaDestino)
                .map(tb -> tb.getPortaSaida())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não é possível enviar esta mensagem"));

    }

    private static void definirQuantidadePortasLocais(Scanner input) {
        do {
            System.out.println("# Definir a quantidade de portas locais (1 ou 2)");
            quantidadePortaLocal = input.nextInt();
        } while (quantidadePortaLocal <= 0 || quantidadePortaLocal > 2 || quantidadePortaLocal == null);
    }

    private static void definirPortasLocais(Scanner input) {
        tabelaRoteamento = new ArrayList<>();

        for (int i = 0; i < quantidadePortaLocal; i++) {
            System.out.format("# Definir porta local %d (%d/%d) - ex: 6060\n", i + 1, i + 1, quantidadePortaLocal);

            int porta = input.nextInt();
            Roteamento roteamento = new Roteamento(porta, 0, 0, 0); // 0 igual a local

            DatagramSocket datagramSocket = null;

            try {
                datagramSocket = new DatagramSocket(porta);
            } catch (SocketException e) {
                //TODO default exception message
            }

            ListenerTarefa listenerTarefa = new ListenerTarefa(1, datagramSocket);

            // Collections de controle
            tabelaRoteamento.add(roteamento);
            listenerTasks.put(porta, listenerTarefa);
            datagramSocketSenders.put(porta, datagramSocket);
        }
    }

    private static void definirVizinhos(Scanner input) {

        List<Integer> portas = tabelaRoteamento.stream()
                .map(tr -> tr.getPortaDestino())
                .collect(Collectors.toList());

        for (Integer porta : portas) {
            System.out.format("# Associar porta %d com uma porta vizinha de outro roteador - ex: 6061\n", porta);
            int portaVizinho = input.nextInt();

            Roteamento roteamento = new Roteamento(portaVizinho, 1, portaVizinho, porta); // 0 igual a local // 1 igual a externo

            tabelaRoteamento.add(roteamento);
            vizinhos.put(porta, portaVizinho);
        }
    }

    private static void enviarMensagem(Scanner input) throws IOException {

        System.out.println("************************************************");

        System.out.format("# Digite a mensagem a ser enviada:\n");
        input.nextLine(); // consome o \n
        String texto = input.nextLine();

        System.out.format("# Digite a porta origem:\n");
        Integer portaOrigem = input.nextInt();

        System.out.format("# Digite a porta destino:\n");
        Integer portaDestino = input.nextInt();

        System.out.println("************************************************");

        Mensagem mensagem = new Mensagem(portaOrigem, portaDestino, texto);

        // Serialize to a byte array
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(mensagem);
        oo.close();

        byte[] serializedMessage = bStream.toByteArray();

        DatagramSocket socket = datagramSocketSenders.get(portaOrigem);

        Integer proximaPorta = proximoRoteador(portaOrigem, portaDestino);

        DatagramPacket newPacket = new DatagramPacket(serializedMessage, 0, serializedMessage.length, inetAddress, proximaPorta);

        try {
            socket.send(newPacket);
        } catch (IOException e) {
            //TODO default exception message
        }
    }

    private static void desligarRoteador() {

        List<Integer> portasVinculadasAEsteRoteador = vizinhos.keySet().stream().collect(Collectors.toList());

        for (Integer porta : portasVinculadasAEsteRoteador) {
            fecharPorta(porta);
        }

        System.out.println("Roteador desligado");
        System.exit(0);
    }

    private static void fecharPorta(int porta) {

        DatagramSocket socket = datagramSocketSenders.remove(porta); // fecha o socket da porta
        removeReferenciasDaTabelaDeRoteamento(porta); // remove a porta da tabela

        Integer portaVizinhoAssociada = vizinhos.remove(porta);

        ListenerTarefa task = listenerTasks.remove(porta);
        task.getTimer().cancel(); // mata a task de agendamento do listenerda porta

        socket.close();

        Impressora.imprime(tabelaRoteamento);
        System.out.format("Porta %s deletada do roteador\n", porta);
    }

    //TODO testar este metodo quando tiver mais threads alterando essa lista ao mesmo tempo
    private synchronized static void removeReferenciasDaTabelaDeRoteamento(int porta) {

        List<Roteamento> tabelaRoteamentoAtualizada = tabelaRoteamento.stream()
                .filter(tb -> tb.getPortaLocalSaida() != porta) // remove as rotas que saem da porta removida
                .filter(tb -> tb.getPortaDestino() != porta) // remove a rota local
                .collect(Collectors.toList());

        tabelaRoteamento = Collections.synchronizedList(new ArrayList<>(tabelaRoteamentoAtualizada));
    }
}