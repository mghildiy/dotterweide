/*
 *  Controller.scala
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

import java.awt.event.{KeyEvent, MouseEvent}

import dotterweide.editor.{ActionProcessor, EditorActions}

/** The instance that processes key and mouse events. */
trait Controller extends ActionProcessor {
  def processKeyPressed   (e: KeyEvent  ): Unit
  def processKeyTyped     (e: KeyEvent  ): Unit

  def processMousePressed (e: MouseEvent): Unit
  def processMouseDragged (e: MouseEvent): Unit
  def processMouseMoved   (e: MouseEvent): Unit

  def actions: EditorActions
}