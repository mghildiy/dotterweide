/*
 *  StopAction.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

/*
 * Original code copyright 2018 Pavel Fatin, https://pavelfatin.com
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package dotterweide.ide.action

import dotterweide.Output
import dotterweide.ide.Launcher
import javax.swing.KeyStroke

import scala.swing.Action

class StopAction(title0: String, mnemonic0: Char, shortcut: String,
                 launcher: Launcher, output: Output) extends Action(title0) {
  mnemonic = mnemonic0

  accelerator = Some(KeyStroke.getKeyStroke(shortcut))

  enabled = false

  launcher.onChange {
    enabled = launcher.active
  }

  def apply(): Unit = {
    launcher.stop()
    output.print("\nStopped")
  }
}