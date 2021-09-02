package com.example.simpleshareduse

class Scores {
    private var userName: String? = null
    private var userScore = 0

    fun getUserName(): String? {
        return userName
    }

    fun setUserName(userName: String?) {
        this.userName = userName
    }

    fun getUserScore(): Int {
        return userScore
    }

    fun setUserScore(userScore: Int) {
        this.userScore = userScore
    }

    override fun toString(): String {
        return "Scores{" +
                "userName='" + userName + '\'' +
                ", userScore=" + userScore +
                '}'
    }
}