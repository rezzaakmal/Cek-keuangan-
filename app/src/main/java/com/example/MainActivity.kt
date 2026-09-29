package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.HomeScreen
import com.example.ui.TransactionViewModel
import com.example.ui.TransactionViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.transactionDao())
        TransactionViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
