/*
 *  CaretPainter.scala
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

package dotterweide.editor.painter

import java.awt.{Graphics2D, Rectangle}

import dotterweide.editor.{CaretMode, CaretMovement, CaretVisibilityChanged, Styling}

// XXX TODO --- support overwrite mode (block cursor) without XOR paint mode

/** Paints the cursor position as a vertical line. */
private class CaretPainter(context: PainterContext) extends AbstractPainter(context) {
  def id = "caret"

  def layer: Int = Painter.LayerCaret

  terminal.onChange {
    case CaretMovement(_, before, now) =>
      notifyObservers(caretRectangleAt(before ))
      notifyObservers(caretRectangleAt(now    ))

    case CaretMode(_, _) =>
      notifyObservers(caretRectangleAt(terminal.offset, overwrite = true))  // maximum dirty rectangle

    case _ =>
  }

  canvas.onChange {
    case CaretVisibilityChanged(_) =>
      notifyObservers(caretRectangleAt(terminal.offset))
    case _ =>
  }

  override def paint(g: Graphics2D, bounds: Rectangle): Unit =
    if (canvas.caretVisible) {
      val pos       = terminal.offset
      val caretRect = caretRectangleAt(pos).intersection(bounds)

      if (!caretRect.isEmpty) {
        g.setColor(styling(Styling.CaretForeground))
        val ovr = terminal.overwriteMode
        // XXX TODO --- this a quick hack until we have better
        // integration with TextPainter to actually paint the
        // text character under the cursor in the specified color
        if (ovr) g.setXORMode(styling(Styling.CaretComplement))
        fill(g, caretRect)
        if (ovr) g.setPaintMode()
      }
    }
}
