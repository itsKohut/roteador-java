package br.com.atividades.tarefas.atualizar;

import java.net.DatagramSocket;
import java.util.Timer;

public class AtualizarTarefa {

    private Timer timer;

    public AtualizarTarefa(int seconds, DatagramSocket socket) {
        timer = new Timer();
        timer.schedule(new AtualizarExecutador(socket), 0, seconds * 1000);
    }

    public Timer getTimer() {
        return timer;
    }
}