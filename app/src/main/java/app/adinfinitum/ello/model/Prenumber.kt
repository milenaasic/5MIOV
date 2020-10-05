package app.adinfinitum.ello.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.adinfinitum.ello.utils.EMPTY_E1_PRENUMBER

@Entity(tableName = "e1_prenumber_table")
data class E1Prenumber (

    @PrimaryKey(autoGenerate = true)
    var id:Int=0,

    @ColumnInfo(name = "e1prenumber")
    val prenumber: String= EMPTY_E1_PRENUMBER,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long=0L

)