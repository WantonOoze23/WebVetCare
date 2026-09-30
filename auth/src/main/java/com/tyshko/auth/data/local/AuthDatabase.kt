package com.tyshko.auth.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.tyshko.auth.data.local.dao.AuthDao
import com.tyshko.auth.data.local.entity.AuthCredentialEntity

@Database(
    entities = [AuthCredentialEntity::class],
    version = 1,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao
}