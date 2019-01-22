package app.sonderful.wheeliebackflipper

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import tornadofx.*

class MainView : View("Wheelie Backflipper") {
    private val controller : MainController by inject()
    private var miceListView: ListView<String> by singleAssign()

    init {
        UacElevator.run()
    }

    override val root = vbox {
        style {
            spacing = 5.px
            padding = box(20.px)
        }
        label("Mice detected in registry")
        miceListView = listview(controller.values) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            style {
                prefHeight = 200.px
                prefWidth = 250.px
            }
            tooltip("Hold control or shift to select multiple mice")
        }
        button {
            label("Invert Scroll")
            style {
                spacing = 5.px
            }
            action {
                Platform.runLater {
                    controller.invertScroll(miceListView.selectionModel.selectedIndices)
                }
            }
        }
    }
}

class MainController : Controller() {
    private val flipFlopWheelKey = "FlipFlopWheel"
    private val hidPath = "SYSTEM\\CurrentControlSet\\Enum\\HID"
    private val mice = mutableListOf<Mouse>()

    val values: ObservableList<String> = FXCollections.observableArrayList()

    init {
        updateMouseSelection()
    }

    fun invertScroll(selectedIndices: ObservableList<Int>) {
        selectedIndices.forEach { index: Int ->
            Advapi32Util.registrySetIntValue(
                HKEY_LOCAL_MACHINE, mice[index].registryKey, flipFlopWheelKey, 1, WinNT.KEY_WRITE)
        }
    }

    private fun updateMouseSelection() {
        Advapi32Util.registryGetKeys(HKEY_LOCAL_MACHINE, hidPath).forEach { hid: String? ->
            Advapi32Util.registryGetKeys(HKEY_LOCAL_MACHINE, "$hidPath\\$hid").forEach { vid: String ->
                val params = Advapi32Util.registryGetValues(HKEY_LOCAL_MACHINE,
                    "$hidPath\\$hid\\$vid\\Device Parameters", WinNT.KEY_READ)
                params.forEach { key, value ->
                    if (key == flipFlopWheelKey) {
                        val flipped = (value == 1)
                        mice.add(
                            Mouse(
                                flipped,
                                vid,
                                "$hidPath\\$hid\\$vid\\Device Parameters"
                            )
                        )
                        values.add(vid)
                    }
                }
            }
        }
    }
}

class Launcher : App(MainView::class)