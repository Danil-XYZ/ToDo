package com.example.todo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.Date


class TodoAdapter(val list: List<TodoModel>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(){

    private var listener: TodoAdapterInterface? = null

    fun setListener(listener: TodoAdapterInterface){
        this.listener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todo, parent, false),
            listener
        )
    }

    override fun getItemCount() = list.size

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class TodoViewHolder(itemView: View, listener: TodoAdapterInterface?) : RecyclerView.ViewHolder(itemView){

        val binding = ItemTodoBinding.bind(itemView)

        val listener = listener




        fun bind(todoModel: TodoModel) = with(binding) {

            editTask.setOnClickListener {
                Log.e("test", "1editTask $position")
                listener?.onEditItemClicked(todoModel , position)
            }

            if (todoModel.isFinished == 1) {
                editTask.visibility = View.GONE
            }
            var color = itemView.resources.getColor(R.color.primaryColor)

            when(todoModel.category) {
                "Low" -> color = itemView.resources.getColor(R.color.yellow_400)
                "Medium" -> color = itemView.resources.getColor(R.color.orange_400)
                "High" -> color = itemView.resources.getColor(R.color.deep_orange_400)
                "Supreme" -> color = itemView.resources.getColor(R.color.colorAccent)
            }

            viewColorTag.setBackgroundColor(color)
            txtShowTitle.text = todoModel.title
            txtShowTask.text = todoModel.description
            txtShowCategory.text = todoModel.category

            if (todoModel.description.isEmpty()) {
                Log.e("test", "")
                txtShowTask.visibility = View.GONE
            } else {
                txtShowTask.visibility = View.VISIBLE
                txtShowTask.setOnClickListener {
                    // Изменение состояния сворачивания/разворачивания
                    if (txtShowTask.maxLines == Integer.MAX_VALUE) {
                        // Если текст развернут, сворачиваем его
                        txtShowTask.maxLines = 1
                    } else {
                        // Если текст свернут, разворачиваем его
                        txtShowTask.maxLines = Integer.MAX_VALUE
                    }
                }
            }

            if (todoModel.date.toInt() != 0) {
                txtShowDate.visibility = View.VISIBLE
                textDate.visibility = View.VISIBLE
                updateDate(todoModel.date)
                if (todoModel.time.toInt() != 0) {
                    txtShowTime.visibility = View.VISIBLE
                    textTime.visibility = View.VISIBLE
                    updateTime(todoModel.time)
                }
                else {
                    txtShowTime.visibility = View.GONE
                    textTime.visibility = View.GONE
                }
            }
            else {
                txtShowDate.visibility = View.GONE
                txtShowTime.visibility = View.GONE
                textDate.visibility = View.GONE
                textTime.visibility = View.GONE
            }

            if (todoModel.category == "None") {
                txtShowCategory.visibility = View.GONE
                textCategory.visibility = View.GONE
            }
            else {
                txtShowCategory.visibility = View.VISIBLE
                textCategory.visibility = View.VISIBLE
            }


        }

        private fun updateTime(time: Long) {
            val sdf = SimpleDateFormat("h:mm a")
            binding.txtShowTime.text = sdf.format(Date(time))
        }

        private fun updateDate(time: Long) {
            val sdf = SimpleDateFormat("EEE, d MMM yyyy")
            binding.txtShowDate.text = sdf.format(Date(time))
        }
    }

    interface TodoAdapterInterface{
        fun onEditItemClicked(todoModel: TodoModel, position: Int)
    }
}
