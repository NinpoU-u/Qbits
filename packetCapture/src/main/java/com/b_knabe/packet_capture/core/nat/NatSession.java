package com.b_knabe.packet_capture.core.nat;


import com.b_knabe.packet_capture.core.util.net_utils.Packets;
import com.b_knabe.packet_capture.core.util.process_parse.ApplicationInfo;

import java.io.Serializable;
import java.util.Locale;

public class NatSession implements Serializable {
    public static final String TCP = "TCP";
    public static final String UDP = "UDP";
    public String type;
    public String ipAndPort;
    public int remoteIP;
    // The port number of the address to be accessed
    public short remotePort;
    public String remoteHost;
    public short localPort;
    // How much data has been uploaded (IP datagram or TCP datagram header is not included)
    public int bytesSent;
    /**
     * Record how many network packets have been sent in the current network session
     */
    public int packetSent;
    public long receiveByteNum;
    public long receivePacketNum;
    public long lastRefreshTime;
    public boolean isHttpsSession;
    /// The url that the user intends to visit,
    // but does not include the port number,
    // for example: the user is visiting http://192.168.100.103:901/?a=2 then here will
    // Becomes http://192.168.100.103/?a=2
    public String requestUrl;
    public String pathUrl;
    public String method;
    public ApplicationInfo applicationInfo;
    public long connectionStartTime = System.currentTimeMillis();
    public long vpnStartTime;
    public boolean isHttp;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s/%s:%d packet: %d",
                remoteHost, Packets.ipToString(remoteIP),
                remotePort & 0xFFFF, packetSent);
    }

    public String getUniqueName() {
        String uinID = ipAndPort + connectionStartTime;
        return String.valueOf(uinID.hashCode());
    }

    public void refreshIpAndPort() {
        int remoteIPStr1 = (remoteIP & 0XFF000000) >> 24 & 0XFF;
        int remoteIPStr2 = (remoteIP & 0X00FF0000) >> 16;
        int remoteIPStr3 = (remoteIP & 0X0000FF00) >> 8;
        int remoteIPStr4 = remoteIP & 0X000000FF;
        String remoteIPStr = "" + remoteIPStr1 + ":" + remoteIPStr2 + ":" + remoteIPStr3 + ":" + remoteIPStr4;
        ipAndPort = type + ":" + remoteIPStr + ":" + remotePort + " " + ((int) localPort & 0XFFFF);
    }

    public String getType() {
        return type;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public int getRemoteIP() {
        return remoteIP;
    }

    public short getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public short getLocalPort() {
        return localPort;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getPacketSent() {
        return packetSent;
    }

    public long getReceiveByteNum() {
        return receiveByteNum;
    }

    public long getReceivePacketNum() {
        return receivePacketNum;
    }

    public long getRefreshTime() {
        return lastRefreshTime;
    }

    public boolean isHttpsSession() {
        return isHttpsSession;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public String getMethod() {
        return method;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public long getConnectionStartTime() {
        return connectionStartTime;
    }

    public long getVpnStartTime() {
        return vpnStartTime;
    }
}
