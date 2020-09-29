package com.example.springboot;

public class Quiz {

	/*
	 * qustion : 問題分 answer : 回答
	 */
	private String question;
	private boolean answer;

	public Quiz(String question, boolean answer) {
		this.question = question;
		this.answer = answer;
	}

	public String getQuestion() {
		return question;
	}

	public boolean isAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		// true = "〇”、false = "×"
		String marubatu = answer ? "〇" : "×";
		return question + " " + marubatu;
	}

	public static Quiz fromString(String line) {
		String question = line.substring(0, line.length() - 2);
		boolean answer = line.endsWith("〇");

		return new Quiz(question, answer);
	}
}
