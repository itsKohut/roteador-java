package br.com.atividades.tarefas.listener;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;

public class ListenerTarefa {

    private Timer timer;

    public ListenerTarefa(int vizinho, int seconds, DatagramSocket socket)  {
        timer = new Timer();
        timer.schedule(new ListenerExecutador(vizinho, socket), 0, seconds * 1000);
        try {
            socket.setSoTimeout(30 * 1000); // 30 segundos
        } catch (SocketException e) {
            // do nothing
        }
    }

    public Timer getTimer() {
        return timer;
    }
}