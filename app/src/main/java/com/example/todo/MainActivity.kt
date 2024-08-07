package com.example.todo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TodoAdapter.TodoAdapterInterface {

    lateinit var binding: ActivityMainBinding
    var list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(list)
    val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Подключение ActionBar
        setSupportActionBar(binding.toolbar)

        // Установка слушателя для адаптера
        adapter.setListener(this)

        // Подключение adapter к ReciclerView
        binding.todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        initSwipe()

    }



    override fun onResume() {
        super.onResume()
        try {
            db.todoDao().getTask().observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    list.clear()
                    list.addAll(it)
                    val categoryOrder = arrayListOf("Supreme", "High", "Medium", "Low", "None")
                    list.sortWith( compareBy<TodoModel> { categoryOrder.indexOf(it.category) }.
                    thenBy { it.date }.
                    thenBy { it.time })

                } else {
                    list.clear()
                }
                adapter.notifyDataSetChanged()
            })
        } catch (e: Exception) {
            Log.e("test", e.message.toString())
        }
    }



    fun initSwipe() {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch(Dispatchers.IO) {
                        Log.e("test", "${adapter.getItemId(position)}")
                        db.todoDao().deleteTask(adapter.getItemId(position))

                        runOnUiThread {
                            try {
                                adapter.notifyDataSetChanged()
                            } catch (e: Exception) {
                                Log.e("test", "${e.message.toString()}")
                            }
                        }
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.todoDao().finishTask(adapter.getItemId(position))
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView

                    val paint = Paint()
                    val icon: Bitmap

                    if (dX > 0) {

                        icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_check_white_png)

                        paint.color = Color.parseColor("#388E3C")

                        canvas.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                        )

                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat(),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )


                    } else {
                        icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_delete_white_png)

                        paint.color = Color.parseColor("#D32F2F")

                        canvas.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )

                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }

                    viewHolder.itemView.translationX = dX


                } else {
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.todoRv)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                //displayTodo()
                onResume()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                //displayTodo()
                onResume()
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    displayTodo(newText)
                }
                else {
                    onResume()
                }
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    fun displayTodo(newText: String = "") {
        db.todoDao().getTask().observe(this, Observer {
            if (it.isNotEmpty()) {
                list.clear()
                list.addAll(
                    it.filter { todo ->
                        todo.title.contains(newText, true)
                    }
                )
                adapter.notifyDataSetChanged()
            }
        })
    }

    fun openNewTask(view: View) {
        startActivity(Intent(this, TaskActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> {
                Log.e("test", "history clicked")
                try {
                    startActivity(Intent(this, HistoryActivity::class.java))
                } catch (e: Exception) {
                    Log.e("test", "${e.message.toString()}")
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEditItemClicked(todoModel: TodoModel, position: Int) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("todoModel", todoModel)
        startActivity(intent)

    }
}