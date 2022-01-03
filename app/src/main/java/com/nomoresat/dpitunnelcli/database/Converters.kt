package com.nomoresat.dpitunnelcli.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun toDesyncZeroAttack(value: String?) = value?.let { enumValueOf<DesyncZeroAttack>(it) }

    @TypeConverter
    fun fromDesyncZeroAttack(value: DesyncZeroAttack?) = value?.name

    @TypeConverter
    fun toDesyncFirstAttack(value: String?) = value?.let { enumValueOf<DesyncFirstAttack>(it) }

    @TypeConverter
    fun fromDesyncFirstAttack(value: DesyncFirstAttack?) = value?.name
}