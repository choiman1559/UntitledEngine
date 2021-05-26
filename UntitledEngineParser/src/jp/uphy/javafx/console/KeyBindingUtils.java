package jp.uphy.javafx.console;

import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


/**
 * @author Yuhi Ishikura
 */
class KeyBindingUtils {

  static void installEmacsKeyBinding(TextInputControl textInputControl) {
    textInputControl.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown()) {
        switch (e.getCode()) {
          case F:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));
            e.consume();
            break;
          case B:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT, false, false, false, false));
            e.consume();
            break;
          case N:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
            e.consume();
            break;
          case P:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
            e.consume();
            break;
          case E:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.END, false, false, false, false));
            e.consume();
            break;
          case A:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.HOME, false, false, false, false));
            e.consume();
            break;
          case K:
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.END, true, false, false, false));
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.END, true, false, false, false));
            textInputControl.copy();
            textInputControl.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DELETE, true, false, false, false));
            e.consume();
            break;
          case Y:
            textInputControl.paste();
            e.consume();
            break;
        }
      } else if (e.isAltDown()) {
        if (e.getCode() == KeyCode.W) {
          textInputControl.copy();
          e.consume();
        }
      }
    });
  }

}
