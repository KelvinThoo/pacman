package epfl.pacman
package interface

import swing._
import Swing._
import event.{ButtonClicked, ValueChanged}
import maze.MVC
import editor.ScalaPane
import behaviour.Behavior
import java.awt.{Font, Color, Insets}

trait GUIs { this: MVC =>

  class PacmanApp extends SimpleSwingApplication {

    // not used right now, but this is what they should be...
    //  val width = 10 + Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width + 10
    //  val height = 10 + view.height + 10

    val code = new ScalaPane()
    code.background = Color.BLACK
    code.peer.setCaretColor(Color.WHITE)
    code.text = Behavior.defaultBehavior
    code.keywords ++= Settings.keywords
    code.preferredSize = (Settings.codeTextWidth, 0)
    code.notifyUpdate()

    val runButton = new Button("Compiler nouveu Code!")

    val simpleMode = new RadioButton("Mode simple")
    simpleMode.foreground = Color.WHITE
    simpleMode.selected = true
    val advancedMode = new RadioButton("Mode avancé")
    advancedMode.foreground = Color.WHITE
    val modeGroup = new ButtonGroup(simpleMode, advancedMode)

    val pauseButton = new Button("Pause...")

    val resetButton = new Button("Redémarrer...")

    val statusDisplay = new Component {
      preferredSize = (200, 10)

      override def paintComponent(g: Graphics2D) {
        import java.awt.RenderingHints.{KEY_ANTIALIASING, VALUE_ANTIALIAS_ON}
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        
        g.setColor(Color.WHITE)
        g.fillArc(50, 50, 100, 100, 0, 300)
      }
    }


    def top = new MainFrame {
      title = "Scala Pacman"
      background = Color.BLACK

      contents = new GridBagPanel {
        import GridBagPanel._
        val c = new Constraints

        val left = new GridBagPanel {
          val c = new Constraints

          c.fill = Fill.Horizontal
          c.gridx = 0
          c.gridy = 0
          c.insets = new Insets(10, 10, 15, 10)
          val codeLabel = new Label("Code de PacMan")
          codeLabel.xAlignment = Alignment.Left
          codeLabel.foreground = Color.WHITE
          codeLabel.font = new Font(codeLabel.font.getName, Font.BOLD, 18)
          layout(codeLabel) = c

          c.fill = Fill.Vertical
          c.gridx = 0
          c.gridy = 1
          c.weighty = 1.0
          c.insets = new Insets(5, 10, 5, 10) // top, left, bottom, right
          layout(code) = c

          c.fill = Fill.None
          c.gridx = 0
          c.gridy = 2
          c.weighty = 0.0
          c.insets = new Insets(5, 10, 10, 10)
          layout(runButton) = c
        }
        left.border = new javax.swing.border.LineBorder(Color.GRAY)

        c.fill = Fill.Vertical
        c.gridx = 0
        c.gridy = 0
        c.insets = new Insets(10, 10, 10, 10)
        layout(left) = c

        c.fill = Fill.None
        c.gridx = 1
        c.gridy = 0
        c.insets = new Insets(10, 10, 10, 10)
        layout(view) = c

        val right = new GridBagPanel {
          val c = new Constraints

          c.fill = Fill.Horizontal
          c.gridx = 0
          c.gridy = 0
          c.insets = new Insets(10, 10, 5, 10) // top, left, bottom, right
          layout(simpleMode) = c

          c.gridx = 0
          c.gridy = 1
          c.insets = new Insets(0, 10, 10, 10)
          layout(advancedMode) = c

          c.gridx = 0
          c.gridy = 2
          c.insets = new Insets(10, 10, 5, 10)
          layout(pauseButton) = c

          c.gridx = 0
          c.gridy = 3
          c.insets = new Insets(5, 10, 5, 10)
          layout(resetButton) = c

          c.fill = Fill.Both
          c.gridx = 0
          c.gridy = 4
          c.weighty = 1.0
          c.insets = new Insets(5, 10, 10, 10)
          layout(statusDisplay) = c
        }
        right.border = new javax.swing.border.LineBorder(Color.GRAY)

        c.fill = Fill.Vertical
        c.gridx = 2
        c.gridy = 0
        c.insets = new Insets(10, 10, 10, 10)
        layout(right) = c

      }


      listenTo(runButton, pauseButton, resetButton, simpleMode, advancedMode)
      reactions += {
        case ButtonClicked(`runButton`) =>

          // if model.gameOver: do a reset first

          controller ! Pause("Code en Charge...")
          compiler.compile(code.text)
          code.requestFocus()
          pause()
          lock()

        case ButtonClicked(`pauseButton`) =>

          // if model.gameOver: do nothing

          if (model.paused) {
            resume()
            controller ! Resume
          } else {
            pause()
            controller ! Pause()
          }

        case ButtonClicked(`resetButton`) =>
          reset(true)

        case ButtonClicked(`simpleMode`) | ButtonClicked(`advancedMode`) =>
          reset(false)
      }


      maximize()
    }

 /*   def update() {
      val locked = model.State != Loading
      runButton.enabled = locked

      pauseButton.text =
        if (model.state == Paused) "Continuer..."
        else "Pause..."
      pauseButton.enabled = locked

      resetButton.enabled = locked

      simpleMode.enabled = locked
      advancedMode.enabled = locked
    }
*/
    def pause() {
      pauseButton.text = "Continuer..."
    }

    def resume() {
      pauseButton.text = "Pause..."
    }

    def reset(hard: Boolean) {
      if (hard || model.simpleMode != simpleMode.selected) {
        pauseButton.text = "Pause..."
        controller ! Reset(simpleMode.selected)
      }
    }

    def lock() {
      runButton.enabled = false
      pauseButton.enabled = false
      resetButton.enabled = false
    }

    def unlock() {
      runButton.enabled = true
      pauseButton.enabled = true
      resetButton.enabled = true
    }


    def setErrors(errorLines: collection.Set[Int]) {
      val lines = code.lines
      for ((line, i) <- lines.zipWithIndex) {
        // i is 0-based, line numbers start at 1
        if (errorLines contains (i+1))
          line.highlight
      }
      code.repaint()
    }
  }
}

