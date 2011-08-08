package modules

import swing._
import event.ButtonClicked

object userInterface extends SimpleSwingApplication{
    def top = new MainFrame {
      title = "Work Distributor"

      val workLabel = new Label() {
        text = "Select Work:"
      }

      val collectSchema = new CheckBox("collect Schema")
      val collectInstance = new CheckBox("collect Instance")

      val factorLabel = new Label() {
        text = "Enter factor:"
      }

      val factorField = new TextField() {
        columns = 10
      }

      val strategyLabel = new Label(){
        text = "Select Worker Strategy"
      }

      val strategy = new ButtonGroup
      val serial = new RadioButton("Serial")
      val parallel = new RadioButton("Parallel")
      val other = new RadioButton("Other")
      val resources = new RadioButton("According to Resources")
      val strategyRadios = List(serial, parallel, other, resources)
      strategy.buttons ++= strategyRadios
      strategy.select(serial)

      val otherStrategyLabel = new Label(){
        text = "Enter Number of Workers"
      }

      val otherStrategyField = new TextField() {
        columns = 10
      }

      val submitButton = new Button() {
      text = "Submit"
      }

      contents =  new BoxPanel(Orientation.Vertical){
        contents += workLabel
        contents += collectSchema
        contents += collectInstance

        contents += factorLabel
        contents += factorField

        contents += strategyLabel
        contents ++= strategyRadios

        contents += otherStrategyLabel
        contents += otherStrategyField

        contents += submitButton
      }

      listenTo(submitButton)
      var nClicks = 0
      reactions += {
        case ButtonClicked(b) =>

          var strategySelected = ""
          strategy.selected.get match {
            case `serial` =>  strategySelected = "SERIAL"
            case `parallel` =>  strategySelected = "PARALLEL"
            case `other` => strategySelected = "OTHER"
          }

          nClicks += 1
          submitButton.text = "Sent: " + nClicks

//          Supervisor.run(collectSchema.selected, collectInstance.selected, factorField.text.toInt, strategySelected, otherStrategyField.text.toInt)

          collectSchema.selected = false
      }
    }
  }