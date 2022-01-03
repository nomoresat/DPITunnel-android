package com.nomoresat.dpitunnelcli.domain.entities

data class ConfiguredProfile(
    var splitAtSni: Boolean,
    var wrongSeq: Boolean,
    var autoTtl: Boolean,
    var fakePacketsTtl: Int?,
    var windowSize: Int?,
    var windowScaleFactor: Int?,
    var desyncAttacks: Boolean,
    var desyncZeroAttack: DesyncZeroAttack?,
    var desyncFirstAttack: DesyncFirstAttack?
)