package com.nomoresat.dpitunnelcli.domain.entities

enum class DesyncZeroAttack {
    DESYNC_ZERO_FAKE,
    DESYNC_ZERO_RST,
    DESYNC_ZERO_RSTACK
}

enum class DesyncFirstAttack {
    DESYNC_FIRST_DISORDER,
    DESYNC_FIRST_DISORDER_FAKE,
    DESYNC_FIRST_SPLIT,
    DESYNC_FIRST_SPLIT_FAKE
}

data class Profile(
    var id: Long?,
    var enabled: Boolean,
    var name: String,
    var title: String?,
    var bufferSize: Int?,
    var splitPosition: Int?,
    var splitAtSni: Boolean,
    var wrongSeq: Boolean,
    var autoTtl: Boolean,
    var fakePacketsTtl: Int?,
    var windowSize: Int?,
    var windowScaleFactor: Int?,
    var inBuiltDNS: Boolean,
    var inBuiltDNSIP: String?,
    var inBuiltDNSPort: Int?,
    var doh: Boolean,
    var dohServer: String?,
    var desyncAttacks: Boolean,
    var desyncZeroAttack: DesyncZeroAttack?,
    var desyncFirstAttack: DesyncFirstAttack?,
    var default: Boolean = false
) {
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        if (default)
            stringBuilder.append("--profile default")
        else
            stringBuilder.append("--profile \"$name\"")
        bufferSize?.let { stringBuilder.append(" --buffer-size \"$it\"") }
        splitPosition?.let { stringBuilder.append(" --split-position \"$it\"") }
        if (wrongSeq)
            stringBuilder.append(" --wrong-seq")
        if (autoTtl)
            stringBuilder.append(" --auto-ttl \"1-4-10\"")
        fakePacketsTtl?.let { stringBuilder.append(" --ttl \"$it\"") }
        windowSize?.let { stringBuilder.append(" --wsize \"$it\"") }
        windowScaleFactor?.let { stringBuilder.append(" --wsfactor \"$it\"") }
        if (inBuiltDNS)
            stringBuilder.append(" --builtin-dns")
        inBuiltDNSIP?.let { stringBuilder.append(" --builtin-dns-ip \"$it\"") }
        inBuiltDNSPort?.let { stringBuilder.append(" --builtin-dns-port \"$it\"") }
        if (doh)
            stringBuilder.append(" --doh")
        dohServer?.let { stringBuilder.append(" --doh-server \"$it\"") }
        if(splitAtSni)
            stringBuilder.append(" --split-at-sni")
        if(desyncAttacks) {
            stringBuilder.append(" --desync-attacks ${desyncZeroAttack?.enumToString() ?: ""}")
            desyncFirstAttack?.let {
                if (desyncZeroAttack != null) stringBuilder.append(',')
                stringBuilder.append(it.enumToString())
            }
        }
        return stringBuilder.toString()
    }

    private fun DesyncZeroAttack.enumToString() = zeroEnumNames[ordinal]

    private fun DesyncFirstAttack.enumToString() = firstEnumNames[ordinal]

    companion object {
        private val zeroEnumNames = listOf("fake", "rst", "rstack")
        private val firstEnumNames = listOf("disorder", "disorder_fake", "split", "split_fake")
    }
}
