package com.example.simpleshareduse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.NonNull
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
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
            .get()
            .addOnCompleteListener { task ->
                    if(task.isSuccessful)
                    {
                        updateList(task)
                    }
            }


    }

    private fun updateList(task: Task<QuerySnapshot>) {
        listScore = ArrayList<String>()
        for (documentSnapshot in task.result!!) {
            val highscore = documentSnapshot.toObject(Scores::class.java)
            listScore.add(highscore.toString())
        }
        adapter = ArrayAdapter<String>(
            this@LeaderboardActivity,
            android.R.layout.simple_list_item_1, listScore
        )
        lvScoreSingle.adapter = adapter
    }
}