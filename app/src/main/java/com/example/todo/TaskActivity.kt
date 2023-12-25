package com.example.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.example.todo.databinding.ActivityTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

private lateinit var binding: ActivityTaskBinding

const val DB_NAME = "todo.db"

class TaskActivity : AppCompatActivity(), View.OnClickListener {

    // Лейблы для задач
    private val labels = arrayListOf("None", "Low", "Medium", "High", "Supreme")

    lateinit var calendar: Calendar

    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    lateinit var todoModel: TodoModel

    var finalDate: Long = 0L
    var finalTime: Long = 0L
    val db by lazy { AppDatabase.getDatabase(this) }
    var newTask: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Устанавливаются слушатели нажатий
        binding.dateEdt.setOnClickListener(this)
        binding.timeEdt.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)

        setUpSpinner()

        val intent = intent
        if (intent.hasExtra("todoModel")) {
            todoModel = intent.getSerializableExtra("todoModel") as TodoModel
            newTask = false



            binding.titleEdt.setText(todoModel.title)
            binding.descriptionEdt.setText(todoModel.description)
            binding.spinnerCategory.setSelection(labels.indexOf(todoModel.category))

            if (todoModel.date.toInt() != 0) {
                finalDate = todoModel.date
                var sdf = SimpleDateFormat("EEE, d MMM yyyy")
                binding.dateEdt.setText(sdf.format(Date(todoModel.date)))
                finalDate = todoModel.time
                if (todoModel.time.toInt() != 0) {
                    finalTime = todoModel.time
                    binding.timeInptLay.visibility = View.VISIBLE
                    val sdf = SimpleDateFormat("h:mm a")
                    binding.timeEdt.setText(sdf.format(Date(todoModel.time)))
                    finalTime = todoModel.time
                }
            }
        }
    }

    private fun setUpSpinner() {

        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.spinnerCategory.adapter = adapter
    }

    // Переопределяемый метод, унаследованный от View.OnClickListener
    override fun onClick(v: View) {
        //При нажатии кнопки в Task activity->
        when (v.id) {
            R.id.dateEdt -> {
                setDateListener()
            }

            R.id.timeEdt -> {
                setTimeListener()
            }

            R.id.saveBtn -> {
                saveTodo()
            }
        }
    }

    private fun setDateListener() {
        calendar = Calendar.getInstance()

        dateSetListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDate()

            }
        // Установка datePicker диалога
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDate() {
        val myformat = "EEE, d MMM yyyy"
        val simpleDateFormat = SimpleDateFormat(myformat)
        finalDate = calendar.time.time
        binding.dateEdt.setText(simpleDateFormat.format(calendar.time))

        binding.timeInptLay.visibility = View.VISIBLE

    }


    private fun setTimeListener() {
        calendar = Calendar.getInstance()

        timeSetListener =
            TimePickerDialog.OnTimeSetListener() { _: TimePicker, hourOfDay: Int, min: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, min)
                updateTime()
            }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    private fun updateTime() {
        val myformat = "h:mm a"
        val sdf = SimpleDateFormat(myformat)
        finalTime = calendar.time.time
        binding.timeEdt.setText(sdf.format(calendar.time))
    }


    private fun saveTodo() {
        if (binding.titleInpLay.editText?.text.toString().trim() == ""){
            Toast.makeText(getApplicationContext(), "Title can't be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (newTask) {
            val category = binding.spinnerCategory.selectedItem.toString()
            val title = binding.titleInpLay.editText?.text.toString()
            val description = binding.taskInpLay.editText?.text.toString()

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {

                    return@withContext db.todoDao().insertTask(
                        TodoModel(
                            title,
                            description,
                            category,
                            finalDate,
                            finalTime
                        )
                    )

                }
                finish()
            }
        } else {
            val category = binding.spinnerCategory.selectedItem.toString()
            val title = binding.titleInpLay.editText?.text.toString()
            val description = binding.taskInpLay.editText?.text.toString()
            val isFinished = todoModel.isFinished
            val id = todoModel.id
            Log.e("test", "$title $description")

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    return@withContext db.todoDao().updateTask(
                        TodoModel(
                            title,
                            description,
                            category,
                            finalDate,
                            finalTime,
                            isFinished,
                            id
                        )
                    )
                }
                finish()
            }
        }
    }


}