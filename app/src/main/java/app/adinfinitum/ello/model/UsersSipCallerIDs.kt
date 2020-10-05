package app.adinfinitum.ello.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_sipcallerIDs_table")
data class UsersSipCallerIDs (

    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name= "userSipCallerId")
    val userSipCallerID: String

)
