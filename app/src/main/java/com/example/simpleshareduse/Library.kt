package com.example.simpleshareduse

class Library {
    private var question: String? = null
    private var answer: String? = null

    fun getQuestion(): String? {
        return question
    }

    fun setQuestion(question: String?) {
        this.question = question
    }

    fun getAnswer(): String? {
        return answer
    }

    fun setAnswer(answer: String?) {
        this.answer = answer
    }

    override fun toString(): String {
        return "Library{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}'
    }
}