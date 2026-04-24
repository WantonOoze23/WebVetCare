package com.tyshko.user.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tyshko.user.data.local.dao.UserDao
import com.tyshko.user.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase(){

    abstract fun userDao(): UserDao

}