package br.com.atividades.tarefas.keepalive;

import java.net.DatagramSocket;
import java.util.Timer;

public class KeepAliveTarefa {

    private Timer timer;

    public KeepAliveTarefa(int vizinho, int seconds, DatagramSocket socket) {
        timer = new Timer();
        timer.schedule(new KeepAliveExecutador(vizinho, socket), 0, seconds * 1000);
    }

    public Timer getTimer() {
        return timer;
    }
}