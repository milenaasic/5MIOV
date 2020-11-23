package app.adinfinitum.ello.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "e1_and_call_verif_enabled_countries_table")
data class E1andCallVerificationEnabledCountries (

    @PrimaryKey(autoGenerate = true)
    var id:Long= 0L,

    @ColumnInfo(name="e1_enabled_countries")
    val e1EnabledCountries:String,

    @ColumnInfo(name= "call_verification_enabled_countries")
    val callVerificationEnabledCountries: String,

)
