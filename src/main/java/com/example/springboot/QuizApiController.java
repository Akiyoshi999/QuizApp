package com.example.springboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("page")
public class QuizApiController {

	private List<Quiz> quizzes = new ArrayList<>();
	private List<ResultQuiz> resultQuizs = new ArrayList<>();
	private QuizFileDao quizFileDao = new QuizFileDao();
	private DBContoroller dbCon = new DBContoroller();
	private Logger logger = Logger.getLogger(QuizApiController.class);
	Map<Integer, Boolean> answerInfo = new HashMap<Integer, Boolean>();

	/*
	 * クイズデータをランダムに1件取得する
	 */
	@GetMapping("/quiz")
	public String quiz(Model model) {
		logger.info("回答していないクイズをランダムに1件取得 [STAR]");
		int i;
		do {
			i = new Random().nextInt(quizzes.size());
		} while (answerInfo.keySet().contains(i));

		model.addAttribute("quiz", quizzes.get(i));
		logger.debug("index = " + i + "quizzes = " + quizzes.get(i));
		logger.info("回答していないクイズをランダムに1件取得 [END]");
		return "quiz";
	}

	/*
	 * クイズのホーム画面
	 */

	@GetMapping("/show")
	public String show() {
		logger.info("ホーム画面表示");
		// quizzes.clear();
		return "list";
	}

	/*
	 * テキストファイルからクイズ情報を読み込む
	 */
	@GetMapping("/load")
	public String load() {
		logger.info("テキストファイルからデータを読み込み [START]");
		try {
			quizzes = quizFileDao.read();
			logger.trace("quizzes = " + quizzes);
			logger.info("テキストファイルからデータを読み込み [END]");
			return "File load SUCCES";
		} catch (IOException e) {
			logger.error(e);
			return "File load FAILURE";
		}
	}

	/*
	 * クイズに回答する
	 */
	@GetMapping("/answer")
	public String answer(Model model, @RequestParam String question, boolean answer) {
		logger.info("クイズの回答処理 [START]");
		// 指定したquestionを登録済みのクイズから検索する
		int count = 0;
		for (Quiz quiz : quizzes) {
			if (quiz.getQuestion().equals(question)) {
				// 指定されたquestionが見つかったら 回答を格納する
				answerInfo.put(count, answer);
				logger.debug("answeInfo = " + answerInfo);
				if (quizzes.size() == answerInfo.size()) {
					logger.debug("クイズの回答処理 [END] 全問回答");
					return "redirect:/page/finish";
				}
				logger.info("クイズの回答処理 [END] " + answerInfo.size() + "/" + quizzes.size() + "件");
				return "redirect:/page/quiz";
			}
			count++;
		}

		logger.error("クイズの回答処理 [ERROR] question:" + question + "見つかりません");
		return "問題がありません";

	}

	/*
	 * クイズを一時中断し、回答したクイズをDBに登録する
	 */
	@GetMapping("/suspend")
	public String suspend(RedirectAttributes attributes) {
		logger.info("一時中断処理 [START]");
		List<String> suspendSQL = new ArrayList<String>();

		dbCon.initialization("temporary_answer");
		// 回答したインデックスを繰り返す
		for (Integer index : answerInfo.keySet()) {
			Quiz quiz = quizzes.get(index);
			suspendSQL.add("INSERT INTO temporary_answer VALUES(" + index + ",'" + quiz.getQuestion() + "'" + ","
					+ answerInfo.get(index) + ")");
		}
		dbCon.multiUpdate(suspendSQL);
		attributes.addFlashAttribute("succesMessage", "回答を一時保存しました");
		logger.debug("実行SQL :" + suspendSQL);
		logger.info("一時中断処理 [END]");

		return "redirect:/page/show";
	}

	/*
	 * DBの「quiz_list」を初期化し、テキストファイルからクイズを読み込み 「quiz_list」に登録する
	 */
	@GetMapping("/start")
	public String start(RedirectAttributes attributes) {
		List<String> loadSQL = new ArrayList<String>();
		logger.info("DBにクイズデータを登録 [START]");
		quizzes.clear();
		// quiz_listテーブルの初期化
		dbCon.initialization("quiz_list");
		try {
			quizzes = quizFileDao.read();
			logger.debug("quizzes = " + quizzes);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			attributes.addFlashAttribute("errorMessage", "DBの登録に失敗しました");
			return "redirect:/page/show";
		}

		// テキストファイルから読み込んだクイズを
		// DBのquiz_listに登録する
		logger.trace("DBにクイズデータを登録[START] quizzes =" + quizzes);
		int i = 0;
		for (Quiz quiz : quizzes) {
			loadSQL.add("INSERT INTO quiz_list VALUES(" + i + ",'" + quiz.getQuestion() + "'" + "," + quiz.isAnswer()
					+ ")");
			i++;
		}
		dbCon.multiUpdate(loadSQL);
		quizzes = dbCon.quizLoad();
		logger.debug("quizzes = " + quizzes);
		logger.debug("DBにクイズデータを登録 [END]");
		return "redirect:/page/quiz";
	}

	/*
	 * 一時回答のクイズをDBから読み込む
	 */

	@GetMapping("/restart")
	public String restart() {
		logger.info("DBから一時回答のクイズデータの読込 [START]");
		answerInfo.clear();
		answerInfo.putAll(dbCon.answerLoad("temporary_answer"));
		logger.debug("answerInfo = " + answerInfo);
		logger.info("DBから一時回答のクイズデータの読込 [END]");
		return "redirect:/page/quiz";
	}

	/*
	 * 全問クイズを回答した際の、得点計算処理
	 */
	@GetMapping("/finish")
	public String finish(Model model) {
		Map<Integer, Boolean> trueAnswer = new HashMap<Integer, Boolean>();
		List<Integer> index = new ArrayList<>(answerInfo.keySet());

		double score;
		double correctAnswers = 0;
		resultQuizs.clear();

		logger.info("得点計算処理 [START]");
		trueAnswer.putAll(dbCon.answerLoad("quiz_list"));
		for (Integer ind : index) {
			ResultQuiz result = new ResultQuiz(quizzes.get(ind).getQuestion(), trueAnswer.get(ind),
					answerInfo.get(ind));
			resultQuizs.add(result);
			if (answerInfo.get(ind).equals(trueAnswer.get(ind))) {
				correctAnswers++;
			}
		}
		score = Math.round(correctAnswers / trueAnswer.size() * 100);

		model.addAttribute("scoreResult", (int) score + "/100点 でした！また挑戦してね！");
		model.addAttribute("resultQuiz", resultQuizs);
		logger.debug("trueAnswer = " + trueAnswer + ", answerInfo = " + answerInfo + ", result = " + resultQuizs);

		// 回答内容をクリアする
		answerInfo.clear();
		dbCon.initialization("temporary_answer");
		logger.info("得点計算処理 [END]");
		return "result";

	}

}
