package br.com.atividades.listener;

import java.net.DatagramSocket;
import java.util.Timer;

public class ListenerTarefa {

    private Timer timer;

    public ListenerTarefa(int seconds, DatagramSocket socket) {
        timer = new Timer();
        timer.schedule(new ListenerExecutador(socket), 0, seconds * 1000);
    }

    public Timer getTimer() {
        return timer;
    }
}