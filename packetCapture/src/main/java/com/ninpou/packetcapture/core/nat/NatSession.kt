package com.ninpou.packetcapture.core.nat

import com.ninpou.packetcapture.core.util.android.AppInfo
import com.ninpou.packetcapture.core.util.net.Packets
import java.io.Serializable
import java.util.*
import kotlin.experimental.and

class NatSession : Serializable {
    @JvmField
    var type: String? = null
    var ipAndPort: String? = null
    @JvmField
    var remoteIP = 0
    @JvmField
    var remotePort: Short = 0
    @JvmField
    var remoteHost: String? = null
    @JvmField
    var localPort: Short = 0
    var bytesSent = 0
    var packetSent = 0
    var receiveByteNum: Long = 0
    var receivePacketNum: Long = 0
    var refreshTime: Long = 0
    @JvmField
    var isHttpsSession = false
    @JvmField
    var requestUrl: String? = null
    @JvmField
    var pathUrl: String? = null
    @JvmField
    var method: String? = null
    var appInfo: AppInfo? = null
    var connectionStartTime = System.currentTimeMillis()
    @JvmField
    var vpnStartTime: Long = 0
    @JvmField
    var isHttp = false


    override fun toString(): String {
        return String.format(Locale.getDefault(), "%s/%s:%d packet: %d",
                remoteHost, Packets.ipToString(remoteIP),
                remotePort and 0xFFFF.toShort(), packetSent)
    }

    val uniqueName: String
        get() {
            val uinID = ipAndPort + connectionStartTime
            return uinID.hashCode().toString()
        }

    fun refreshIpAndPort() {
        val remoteIPStr1 = remoteIP and -0x1000000 shr 24 and 0XFF
        val remoteIPStr2 = remoteIP and 0X00FF0000 shr 16
        val remoteIPStr3 = remoteIP and 0X0000FF00 shr 8
        val remoteIPStr4 = remoteIP and 0X000000FF
        val remoteIPStr = "$remoteIPStr1:$remoteIPStr2:$remoteIPStr3:$remoteIPStr4"
        ipAndPort = type + ":" + remoteIPStr + ":" + remotePort + " " + (localPort.toInt() and 0XFFFF)
    }

    companion object {
        const val TCP = "TCP"
        const val UDP = "UDP"
    }
}