package com.example.surveyformlab4

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val answersFileName = "survey_answers.txt"

    private lateinit var questions: Array<String>
    private lateinit var answers: MutableList<String>
    private lateinit var progressView: TextView
    private lateinit var questionView: TextView
    private lateinit var answerInput: EditText
    private lateinit var messageView: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var savedAnswersView: TextView
    private lateinit var filePathView: TextView
    private lateinit var clearFileButton: Button

    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        questions = resources.getStringArray(R.array.survey_questions)
        answers = MutableList(questions.size) { "" }

        window.statusBarColor = getColor(R.color.primary_dark)
        buildInterface()
        renderQuestion()
        refreshSavedAnswers()
    }

    private fun buildInterface() {
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(getColor(R.color.background))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        val titleView = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextColor(getColor(R.color.text_primary))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitleView = TextView(this).apply {
            text = getString(R.string.app_subtitle)
            setTextColor(getColor(R.color.text_secondary))
            textSize = 15f
            setPadding(0, dp(4), 0, dp(18))
        }

        val questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundResource(R.drawable.card_background)
            elevation = dp(2).toFloat()
        }

        progressView = TextView(this).apply {
            setTextColor(getColor(R.color.primary))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }

        questionView = TextView(this).apply {
            setTextColor(getColor(R.color.text_primary))
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(8), 0, dp(12))
        }

        answerInput = EditText(this).apply {
            setBackgroundResource(R.drawable.input_background)
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
            hint = getString(R.string.answer_hint)
            minLines = 4
            maxLines = 6
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_DONE
            textSize = 16f
        }

        messageView = TextView(this).apply {
            setTextColor(getColor(R.color.text_secondary))
            textSize = 14f
            setPadding(0, dp(10), 0, 0)
        }

        previousButton = createButton(getString(R.string.previous), primary = false).apply {
            setOnClickListener { goToPreviousQuestion() }
        }

        nextButton = createButton(getString(R.string.next), primary = true).apply {
            setOnClickListener { goToNextQuestionOrSave() }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, 0)
        }

        buttonRow.addView(
            previousButton,
            LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(8)
            }
        )
        buttonRow.addView(
            nextButton,
            LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginStart = dp(8)
            }
        )

        questionCard.addView(progressView)
        questionCard.addView(questionView)
        questionCard.addView(answerInput)
        questionCard.addView(messageView)
        questionCard.addView(buttonRow)

        val resetButton = createButton(getString(R.string.reset_form), primary = false).apply {
            setOnClickListener { confirmResetForm() }
        }

        val savedCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundResource(R.drawable.card_background)
            elevation = dp(2).toFloat()
        }

        val savedTitleView = TextView(this).apply {
            text = getString(R.string.saved_answers_title)
            setTextColor(getColor(R.color.text_primary))
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
        }

        filePathView = TextView(this).apply {
            setTextColor(getColor(R.color.text_secondary))
            textSize = 13f
            setPadding(0, dp(6), 0, dp(10))
        }

        savedAnswersView = TextView(this).apply {
            setTextColor(getColor(R.color.text_primary))
            textSize = 14f
            setLineSpacing(dp(2).toFloat(), 1f)
        }

        val refreshButton = createButton(getString(R.string.refresh), primary = false).apply {
            setOnClickListener { refreshSavedAnswers() }
        }

        clearFileButton = createButton(getString(R.string.clear_file), primary = false).apply {
            setOnClickListener { confirmClearFile() }
        }

        val savedButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, 0)
        }
        savedButtonsRow.addView(
            refreshButton,
            LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(8)
            }
        )
        savedButtonsRow.addView(
            clearFileButton,
            LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginStart = dp(8)
            }
        )

        savedCard.addView(savedTitleView)
        savedCard.addView(filePathView)
        savedCard.addView(savedAnswersView)
        savedCard.addView(savedButtonsRow)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(questionCard, cardLayoutParams())
        container.addView(resetButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(48)
        ).apply {
            topMargin = dp(12)
        })
        container.addView(savedCard, cardLayoutParams(topMargin = dp(18)))

        scrollView.addView(container)
        setContentView(scrollView)
    }

    private fun renderQuestion() {
        progressView.text = getString(
            R.string.question_progress,
            currentQuestionIndex + 1,
            questions.size
        )
        questionView.text = questions[currentQuestionIndex]
        answerInput.setText(answers[currentQuestionIndex])
        answerInput.setSelection(answerInput.text.length)
        previousButton.isEnabled = currentQuestionIndex > 0
        nextButton.text = if (currentQuestionIndex == questions.lastIndex) {
            getString(R.string.save)
        } else {
            getString(R.string.next)
        }
    }

    private fun goToPreviousQuestion() {
        answers[currentQuestionIndex] = answerInput.text.toString().trim()
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            messageView.text = ""
            renderQuestion()
        }
    }

    private fun goToNextQuestionOrSave() {
        if (!saveCurrentAnswer()) return

        if (currentQuestionIndex < questions.lastIndex) {
            currentQuestionIndex++
            messageView.text = ""
            renderQuestion()
            return
        }

        val savedFile = appendAnswersToFile()
        Toast.makeText(this, getString(R.string.saved_success), Toast.LENGTH_SHORT).show()
        messageView.setTextColor(getColor(R.color.primary))
        messageView.text = "${getString(R.string.saved_success)} ${savedFile.name}"
        resetCurrentForm()
        refreshSavedAnswers()
    }

    private fun saveCurrentAnswer(): Boolean {
        val answer = answerInput.text.toString().trim()
        if (answer.isEmpty()) {
            messageView.setTextColor(getColor(R.color.error))
            messageView.text = getString(R.string.empty_answer_error)
            return false
        }

        answers[currentQuestionIndex] = answer
        return true
    }

    private fun appendAnswersToFile(): File {
        val file = answersFile()
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val surveyText = buildString {
            appendLine(getString(R.string.submission_header, formatter.format(Date())))
            questions.forEachIndexed { index, question ->
                appendLine("${index + 1}. $question")
                appendLine(getString(R.string.answer_label, answers[index]))
            }
            appendLine("-----")
            appendLine()
        }

        file.appendText(surveyText, Charsets.UTF_8)
        return file
    }

    private fun refreshSavedAnswers() {
        val file = answersFile()
        filePathView.text = "${getString(R.string.file_path_prefix)} ${file.absolutePath}"

        if (!file.exists() || file.length() == 0L) {
            savedAnswersView.text = getString(R.string.no_saved_answers)
            clearFileButton.isEnabled = false
            return
        }

        val text = file.readText(Charsets.UTF_8)
        savedAnswersView.text = if (text.length > 5000) {
            text.takeLast(5000)
        } else {
            text
        }
        clearFileButton.isEnabled = true
    }

    private fun confirmResetForm() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_form_title))
            .setMessage(getString(R.string.clear_form_message))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                resetCurrentForm()
                messageView.text = ""
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmClearFile() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_file_title))
            .setMessage(getString(R.string.clear_file_message))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                answersFile().writeText("", Charsets.UTF_8)
                refreshSavedAnswers()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun resetCurrentForm() {
        answers = MutableList(questions.size) { "" }
        currentQuestionIndex = 0
        renderQuestion()
    }

    private fun answersFile(): File = File(filesDir, answersFileName)

    private fun createButton(text: String, primary: Boolean): Button {
        return Button(this).apply {
            this.text = text
            setAllCaps(false)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            minHeight = 0
            minWidth = 0
            setPadding(dp(8), 0, dp(8), 0)
            setTextColor(getColor(if (primary) R.color.surface else R.color.primary))
            setBackgroundResource(if (primary) R.drawable.button_primary else R.drawable.button_secondary)
        }
    }

    private fun cardLayoutParams(topMargin: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.topMargin = topMargin
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }
}
