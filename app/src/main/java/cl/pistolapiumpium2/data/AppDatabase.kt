package cl.pistolapiumpium2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executor

@Database(entities = [AppConfig::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gun_app_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        // --- MÉTODOS PARA PRUEBAS ---

        fun getTestDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Un executor que corre todo en el hilo actual, de forma síncrona.
                val executor = Executor { it.run() }

                val instance = Room.inMemoryDatabaseBuilder(
                    context,
                    AppDatabase::class.java
                )
                // SOLUCIÓN: Obliga a Room a usar nuestro executor síncrono para consultas y transacciones.
                .setQueryExecutor(executor)
                .setTransactionExecutor(executor)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeTestDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
