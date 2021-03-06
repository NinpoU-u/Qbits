package com.b_knabe.packet_capture.core.nat;

import com.b_knabe.packet_capture.core.util.common.FileManager;
import com.b_knabe.packet_capture.core.util.common.ACache;
import com.b_knabe.packet_capture.core.util.common.TimeFormatter;
import com.b_knabe.packet_capture.core.util.net_utils.tcp.TcpDataSaver;
import com.b_knabe.packet_capture.core.util.process_parse.PortSessionInfoService;
import com.b_knabe.packet_capture.core.vpn.VpnProxyServer;
import java.io.File;
import java.util.*;

public class NatSessionListHelper {
    public static Collection<NatSession> getAllSessions() {
        File file = new File(TcpDataSaver.CONFIG_DIR
                + TimeFormatter.formatToYYMMDDHHMMSS(VpnProxyServer.getStartTime()));
        ACache aCache = ACache.get(file);
        String[] list = file.list();
        ArrayList<NatSession> baseNetSessions = new ArrayList<>();
        if (list != null) {
            for (String fileName : list) {
                NatSession netConnection = (NatSession) aCache.getAsObject(fileName);
                baseNetSessions.add(netConnection);
            }
        }
        PortSessionInfoService portSessionInfoService = PortSessionInfoService.getInstance();
        if (portSessionInfoService != null) {
            Collection<NatSession> aliveConnInfo = portSessionInfoService.getAndRefreshSessionInfo();
            if (aliveConnInfo != null) {
                baseNetSessions.addAll(aliveConnInfo);
            }
        }
        Collections.sort(baseNetSessions, new Comparator<NatSession>() {
            @Override
            public int compare(NatSession o1, NatSession o2) {
                return Long.compare(o2.lastRefreshTime, o1.lastRefreshTime);
            }
        });
        return baseNetSessions;
    }

    public static void clearCache() {
        String data = TcpDataSaver.DATA_DIR;
        String config = TcpDataSaver.CONFIG_DIR;
        File dataDir = new File(data);
        File configDir = new File(config);
        FileManager.deleteUnder(dataDir);
        FileManager.deleteUnder(configDir);
        NatSessionManager.clearAllSession();
    }
}
