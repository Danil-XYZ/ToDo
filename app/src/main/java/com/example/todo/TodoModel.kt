package com.example.todo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Мы делаем список для каждой задачи
@Entity
data class TodoModel(
    var title:String,
    var description:String,
    var category: String,
    var date:Long = 0,
    var time:Long = 0,
    var isFinished : Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
) : Serializable
