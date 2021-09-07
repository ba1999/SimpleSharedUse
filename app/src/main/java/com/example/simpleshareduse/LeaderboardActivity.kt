package com.example.simpleshareduse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.NonNull
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import java.util.ArrayList

class LeaderboardActivity : AppCompatActivity() {

    private val lvScoreSingle : ListView by lazy { findViewById(R.id.lvSingleLeader) }
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }

    private lateinit var listScore: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        db.collection(Constants.LEADERBOARD)
            .orderBy(Constants.USERSCORE, Query.Direction.DESCENDING)
            .limit(Constants.HIGHSCORELIMIT.toLong())
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    return@EventListener
                }
                updateListOnChange(value!!)
            })

    }

    private fun updateListOnChange(value: QuerySnapshot) {
        listScore = ArrayList()
        for (documentSnapshot in value) {
            val highscore = documentSnapshot.toObject(Scores::class.java)
            listScore.add(highscore.toString())
        }
        adapter = ArrayAdapter(
            this@LeaderboardActivity,
            android.R.layout.simple_list_item_1, listScore
        )
        lvScoreSingle.adapter = adapter
    }
}