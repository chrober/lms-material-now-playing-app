package com.chrober.lmsmaterial.nowplaying;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lyrion's eIPAD UDP discovery protocol, based on the approach in
 * CDrummond/lms-material-app ServerDiscovery.java (MIT, Craig Drummond).
 */
final class LyrionDiscovery {
    interface Callback { void completed(List<Server> servers); }

    static final class Server {
        final String name;
        final String ip;
        final int port;
        Server(String name, String ip, int port) {
            this.name = name;
            this.ip = ip;
            this.port = port;
        }
        String url() { return "http://" + ip + ":" + port; }
        @Override public String toString() {
            return (name.isEmpty() ? "Lyrion server" : name)
                    + " (" + ip + ":" + port + ")";
        }
    }

    private static final byte[] QUERY = {
        'e', 'I', 'P', 'A', 'D', 0, 'N', 'A', 'M', 'E', 0, 'J', 'S', 'O', 'N', 0
    };

    static void discover(Context context, Callback callback) {
        Context application = context.getApplicationContext();
        new Thread(() -> {
            Map<String, Server> found = new LinkedHashMap<>();
            WifiManager.MulticastLock lock = null;
            try {
                WifiManager wifi = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
                if (wifi != null) {
                    lock = wifi.createMulticastLock("lyrion-server-discovery");
                    lock.setReferenceCounted(false);
                    lock.acquire();
                }
                List<InetAddress> addresses = new ArrayList<>();
                addresses.add(InetAddress.getByName("255.255.255.255"));
                Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
                if (networks != null) {
                    for (NetworkInterface network : Collections.list(networks)) {
                        if (!network.isUp() || network.isLoopback()) continue;
                        for (InterfaceAddress addr : network.getInterfaceAddresses()) {
                            if (addr.getAddress() instanceof Inet4Address
                                    && addr.getBroadcast() != null)
                                addresses.add(addr.getBroadcast());
                        }
                    }
                }
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setBroadcast(true);
                    socket.setSoTimeout(250);
                    for (InetAddress address : addresses) {
                        try {
                            socket.send(new DatagramPacket(QUERY, QUERY.length, address, 3483));
                        } catch (Exception ignored) { }
                    }
                    long end = SystemClock.elapsedRealtime() + 2200;
                    while (SystemClock.elapsedRealtime() < end) {
                        byte[] bytes = new byte[512];
                        DatagramPacket reply = new DatagramPacket(bytes, bytes.length);
                        try { socket.receive(reply); }
                        catch (java.net.SocketTimeoutException ignored) { continue; }
                        if (reply.getLength() < 2 || bytes[0] != (byte) 'E') continue;
                        String name = "";
                        int port = 9000;
                        int pos = 1;
                        while (pos + 5 <= reply.getLength()) {
                            String key = new String(bytes, pos, 4, StandardCharsets.US_ASCII);
                            pos += 4;
                            int length = bytes[pos++] & 0xff;
                            if (pos + length > reply.getLength()) break;
                            String value = new String(bytes, pos, length, StandardCharsets.UTF_8);
                            if ("NAME".equals(key)) name = value;
                            if ("JSON".equals(key)) {
                                try { port = Integer.parseInt(value); }
                                catch (NumberFormatException ignored) { }
                            }
                            pos += length;
                        }
                        if (port < 1 || port > 65535) port = 9000;
                        String ip = reply.getAddress().getHostAddress();
                        found.put(ip + ":" + port, new Server(name, ip, port));
                    }
                }
            } catch (Exception ignored) {
                // Manual server entry remains available when the network blocks broadcasts.
            } finally {
                if (lock != null && lock.isHeld()) lock.release();
            }
            List<Server> result = new ArrayList<>(found.values());
            new Handler(Looper.getMainLooper()).post(() -> callback.completed(result));
        }, "lyrion-server-discovery").start();
    }

    private LyrionDiscovery() { }
}
