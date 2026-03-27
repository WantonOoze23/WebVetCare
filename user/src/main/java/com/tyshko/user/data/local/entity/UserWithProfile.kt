package com.tyshko.user.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithProfile(
    @Embedded val user: UserEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val doctorProfile: DoctorProfileEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val patientProfile: PatientProfileEntity?
)