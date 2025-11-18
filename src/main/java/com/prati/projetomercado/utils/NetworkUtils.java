package com.prati.projetomercado.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Optional;

public class NetworkUtils {

    public static String getLocalIP(String intranetType) {
        try {
            String ifaceName1;
            String ifaceName2;
            if (intranetType.equals("ethernet")) {
                ifaceName1 = "enp3s0";
                ifaceName2 = "eth0";
            } else {
                ifaceName1 = "wlan0";
                ifaceName2 = "wlp3s0";
            }
            return accessInetAddress(ifaceName1, Optional.of(ifaceName2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "127.0.0.1";
    }

    private static String accessInetAddress(String iface1, Optional<String> iface2) throws SocketException {
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByName(iface1);
        } catch (NullPointerException e) {
            networkInterface = NetworkInterface.getByName(iface2.get());
        }

        var address = networkInterface.getInetAddresses();
        while (address.hasMoreElements()) {
            var addr = address.nextElement();

            if (addr.isSiteLocalAddress() && !addr.isLoopbackAddress() && !addr.getHostAddress().contains(":")) {
                return addr.getHostAddress();
            }
        }
        throw new SocketException("No IP address found");
    }
}
