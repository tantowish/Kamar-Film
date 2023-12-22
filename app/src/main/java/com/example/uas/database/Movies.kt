package com.example.uas.database

import android.os.Parcel
import android.os.Parcelable

data class Movies(
    var id: String = "",
    var imagePath: String = "",
    var title: String = "",
    var year: String = "",
    var description: String = "",
    var rating: String = "",
    var isTrending: Boolean = false
): Parcelable {
    // agar bisa di edit
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(imagePath)
        parcel.writeString(title)
        parcel.writeString(year)
        parcel.writeString(description)
        parcel.writeString(rating)
        parcel.writeString(isTrending.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Movies> {
        override fun createFromParcel(parcel: Parcel): Movies {
            return Movies(parcel)
        }

        override fun newArray(size: Int): Array<Movies?> {
            return arrayOfNulls(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        imagePath = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        year = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        rating = parcel.readString() ?: ""
    )
}
