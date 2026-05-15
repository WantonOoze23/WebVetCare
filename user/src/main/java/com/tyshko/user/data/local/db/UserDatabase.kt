package com.tyshko.user.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Імпортуйте це
import com.tyshko.user.data.local.dao.UserDao
import com.tyshko.user.data.local.entity.UserEntity
import com.tyshko.user.data.local.entity.DoctorProfileEntity // Додайте імпорт
import com.tyshko.user.data.local.entity.PatientProfileEntity // Додайте імпорт

@Database(
    entities = [
        UserEntity::class,
        DoctorProfileEntity::class,
        PatientProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}