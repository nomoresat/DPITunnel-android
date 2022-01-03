package com.nomoresat.dpitunnelcli.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "profiles_table")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "enabled") var enabled: Boolean,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "buffer_size") val bufferSize: Int?,
    @ColumnInfo(name = "split_position") val splitPosition: Int?,
    @ColumnInfo(name = "split_at_sni") val splitAtSni: Boolean,
    @ColumnInfo(name = "wrong_seq") val wrongSeq: Boolean,
    @ColumnInfo(name = "auto_ttl") var autoTtl: Boolean,
    @ColumnInfo(name = "fake_packets_ttl") val fakePacketsTtl: Int?,
    @ColumnInfo(name = "window_size") val windowSize: Int?,
    @ColumnInfo(name = "window_scale_factor") val windowScaleFactor: Int?,
    @ColumnInfo(name = "inbuilt_dns") val inBuiltDNS: Boolean,
    @ColumnInfo(name = "inbuilt_dns_ip") val inBuiltDNSIP: String?,
    @ColumnInfo(name = "inbuilt_dns_port") val inBuiltDNSPort: Int?,
    @ColumnInfo(name = "doh") val doh: Boolean,
    @ColumnInfo(name = "doh_server") val dohServer: String?,
    @ColumnInfo(name = "desync_attacks") val desyncAttacks: Boolean,
    @ColumnInfo(name = "desync_zero_attack") val desyncZeroAttack: DesyncZeroAttack?,
    @ColumnInfo(name = "desync_first_attack") val desyncFirstAttack: DesyncFirstAttack?
)
