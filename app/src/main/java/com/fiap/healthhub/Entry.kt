package com.fiap.healthhub
import android.os.Parcel
import android.os.Parcelable

data class Entry(
    val clinicalStatusCode: String,
    val recordedDate: String,
    val noteText: String,
    val abatDate: String,
    val verificationCode: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(clinicalStatusCode)
        parcel.writeString(recordedDate)
        parcel.writeString(noteText)
        parcel.writeString(abatDate)
        parcel.writeString(verificationCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Entry> {
        override fun createFromParcel(parcel: Parcel): Entry {
            return Entry(parcel)
        }

        override fun newArray(size: Int): Array<Entry?> {
            return arrayOfNulls(size)
        }
    }
}
