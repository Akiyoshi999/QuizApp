package com.example.springboot;

public class ResultQuiz {

	/*
	 * qustion : 問題分 answer : 回答
	 */
	private String question;
	private boolean correct;
	private boolean answer;

	public ResultQuiz(String question, boolean correct, boolean answer) {
		this.question = question;
		this.correct = correct;
		this.answer = answer;
	}

	public String getQuestion() {
		return question;
	}

	public boolean isCorrect() {
		return correct;
	}

	public boolean isAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		// 条件演算子、trueなら"〇"、falseなら"×"
		return question + " " + correct + " " + answer;
	}

}
