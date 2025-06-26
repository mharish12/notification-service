package com.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@SpringBootTest
class NotificationServiceApplicationTests {

    // @Test
    // void contextLoads() {
    // // This test verifies that the Spring application context loads successfully
    // }

    @Test
    void contextLoads() throws IOException {
        try (DatagramSocket socket = new DatagramSocket(8888)) {
            byte[] buffer = new byte[1024];

            System.out.println("Listening for broadcast messages...");


            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + msg);

        }

    }
}