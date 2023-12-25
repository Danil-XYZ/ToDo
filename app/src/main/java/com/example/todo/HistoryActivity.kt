package com.example.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityHistoryBinding
    var list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(list)
    val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Подключение adapter к ReciclerView
        binding.todoRv.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }

        try {
            db.todoDao().getCompletedTask().observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    list.clear()
                    list.addAll(it)

                    adapter.notifyDataSetChanged()
                } else {
                    list.clear()
                    adapter.notifyDataSetChanged()
                }
            })
        } catch (e: Exception) {
            Log.e("test", e.message.toString())
        }

    }


}