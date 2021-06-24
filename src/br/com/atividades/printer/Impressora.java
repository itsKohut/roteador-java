package br.com.atividades.printer;

import br.com.atividades.dto.Roteamento;

import java.util.*;
import java.util.stream.Stream;

public class Impressora {


    public static void menu() {
        System.out.println("********************** MENU *******************");
        System.out.println("1 - Enviar mensagem ");
        System.out.println("2 - Imprimir as portas e seu respectivo vizinho");
        System.out.println("3 - Imprimir tabela de roteamento");
        System.out.println("4 - Remover uma porta do roteador ");
        System.out.println("5 - Desligar roteador");
        System.out.println("***********************************************");
        System.out.println("Escolha uma das opções acima: ");
    }

    public static void imprime(Set<Integer> portasLocais) {
        StringBuilder sb = new StringBuilder();

        portasLocais.forEach((chave) -> {
            sb.append(chave).append(" ");
        });

        System.out.format("O roteador tem as seguintes portas abertas: %s\n", sb);
    }

    public static void imprime(Map<Integer, Integer> vizinhos) {
        System.out.format("O roteador tem %d porta(s) vinculada(s)\n", vizinhos.entrySet().size());

        vizinhos.forEach((chave, valor) -> {
            System.out.format("A porta %d esta associada a porta %d\n", chave, valor);
        });
    }

    public static void imprime(List<Roteamento> tabelaRoteamento) {

        boolean leftJustifiedRows = false;

        String[][] table = new String[tabelaRoteamento.size() + 1][4];

        for (int i = 0; i < 1; i++) {
            int j = 0;
            table[0][j++] = "Porta Destino";
            table[0][j++] = "Metrica";
            table[0][j++] = "Porta Saída";
            table[0][j] = "Portal Local Saída";
        }

        for (int i = 0; i < tabelaRoteamento.size(); i++) {
            int j = 0;
            int indiceMatriz = i + 1;
            table[indiceMatriz][j++] = String.valueOf(tabelaRoteamento.get(i).getPortaDestino());
            table[indiceMatriz][j++] = String.valueOf(tabelaRoteamento.get(i).getMetrica());
            table[indiceMatriz][j++] = transformarPorta(String.valueOf(tabelaRoteamento.get(i).getPortaSaida()));
            table[indiceMatriz][j] = transformarPorta(String.valueOf(tabelaRoteamento.get(i).getPortaLocalSaida()));
        }

        Map<Integer, Integer> columnLengths = new HashMap<>();
        Arrays.stream(table).forEach(a -> Stream.iterate(0, (i -> i < a.length), (i -> ++i)).forEach(i -> {
            if (columnLengths.get(i) == null) {
                columnLengths.put(i, 0);
            }
            if (columnLengths.get(i) < a[i].length()) {
                columnLengths.put(i, a[i].length());
            }
        }));

        final StringBuilder formatString = new StringBuilder("");
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.entrySet().stream().forEach(e -> formatString.append("| %" + flag + e.getValue() + "s "));
        formatString.append("|\n");

        String line = columnLengths.entrySet().stream().reduce("", (ln, b) -> {
            String templn = "+-";
            templn = templn + Stream.iterate(0, (i -> i < b.getValue()), (i -> ++i)).reduce("", (ln1, b1) -> ln1 + "-",
                    (a1, b1) -> a1 + b1);
            templn = templn + "-";
            return ln + templn;
        }, (a, b) -> a + b);
        line = line + "+\n";

        System.out.print(line);
        Arrays.stream(table).limit(1).forEach(a -> System.out.printf(formatString.toString(), a));
        System.out.print(line);

        Stream.iterate(1, (i -> i < table.length), (i -> ++i))
                .forEach(a -> System.out.printf(formatString.toString(), table[a]));

        System.out.print(line);
    }

    private static String transformarPorta(String porta) {
        return porta.equals("0") ? "Local" : porta;
    }
}
