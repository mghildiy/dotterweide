/*
 *  Redo.scala
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

package dotterweide.editor.controller

import dotterweide.document.Document
import dotterweide.editor.{Action, History, Terminal}

private class Redo(document: Document, terminal: Terminal, history: History) extends Action with Repeater {
  repeat(document, terminal)

  def keys: Seq[String] = List("shift ctrl pressed Z")

  override def enabled: Boolean = history.canRedo

  def apply(): Unit =
    history.redo()
}