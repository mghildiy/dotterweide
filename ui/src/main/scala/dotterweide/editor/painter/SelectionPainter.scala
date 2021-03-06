/*
 *  SelectionPainter.scala
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

import java.awt.font.TextAttribute
import java.awt.{Color, Graphics2D, Rectangle}

import dotterweide.Interval
import dotterweide.editor.{SelectionChange, Styling}

/** Paints the terminal's `selection` and collects them as decorations. */
private class SelectionPainter(context: PainterContext) extends AbstractPainter(context) with Decorator {
  def id = "selection"

  def layer: Int = Painter.LayerSelection

  terminal.onChange {
    case SelectionChange(_, before, now) =>
      before.foreach(notifyObservers)
      now   .foreach(notifyObservers)
    case _ =>
  }

  override def paint(g: Graphics2D, bounds: Rectangle): Unit = {
    // XXX TODO inefficient
    val rectangles = terminal.selection.toSeq.flatMap(rectanglesOf)
      .map(_.intersection(bounds)).filterNot(_.isEmpty)

    if (rectangles.nonEmpty) {
      g.setColor(styling(Styling.SelectionBackground))
      rectangles.foreach(fill(g, _))
    }
  }

  // XXX TODO cache and smart update
  override def decorations: Map[Interval, Map[TextAttribute, Color]] = terminal.selection
    .map(interval => (interval, Map(TextAttribute.FOREGROUND -> styling(Styling.SelectionForeground)))).toMap
}
