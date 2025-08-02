/*
 *  created by iorihuang 20-9-21 下午10:32
 */

package iorihuang.bankaccountmanager.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NetworkUtils {
    public static String IP_V4 = getLocalIpV4();

    public static final String getLocalIpV4() {
        List<String> addresses = getLocalHostAddresses();
        for (int i = 0; i < addresses.size(); i++) {
            if (!addresses.get(i).contains(".") || "127.0.0.1".equals(addresses.get(i))) {
                continue;
            }
            String ip = addresses.get(i);
            if (!ip.isEmpty()) {
                return ip;
            }
        }
        return null;
    }

    public static final List<String> getLocalHostAddresses() {
        List<NetworkInterface> nis;
        try {
            nis = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            return Collections.EMPTY_LIST;
        }

        List<String> addresses = new ArrayList<>();

        for (NetworkInterface ni : nis) {
            try {
                if (ni.isUp()) {
                    addresses.addAll(
                            Collections.list(ni.getInetAddresses())
                                    .stream().map(inet -> inet.getHostAddress())
                                    .collect(Collectors.toList())
                    );
                }
            } catch (SocketException e) {
            }
        }

        return addresses;
    }

    public static long ipV4ToLong(String strIp) {
        long[] ip = new long[4];
        //先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf('.');
        int position2 = strIp.indexOf('.', position1 + 1);
        int position3 = strIp.indexOf('.', position2 + 1);
        //将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }
}