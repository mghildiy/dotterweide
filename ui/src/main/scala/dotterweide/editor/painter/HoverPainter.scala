/*
 *  HoverPainter.scala
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
import java.awt.{Graphics2D, Rectangle}

import dotterweide.Interval
import dotterweide.editor.{HoverChange, Styling}
import dotterweide.node.ReferenceNode

/** A no-op painter that collects terminal hovers as decorations (blue underlined text). */
private class HoverPainter(context: PainterContext) extends AbstractPainter(context) with Decorator {
  def id = "hover"

  def layer: Int = Painter.LayerHover

  private def mkAttributes() =
    Map(
      TextAttribute.FOREGROUND  -> styling(Styling.Hover),
      TextAttribute.UNDERLINE   -> TextAttribute.UNDERLINE_ON
    )

  private[this] var hoverAttributes = mkAttributes()

  styling.onChange {
    hoverAttributes = mkAttributes()
  }

  terminal.onChange {
    case HoverChange(_, before, now) =>
      before.foreach(offset => hoverInterval(offset).foreach(notifyObservers))
      now   .foreach(offset => hoverInterval(offset).foreach(notifyObservers))
    case _ =>
  }

  private def hoverInterval(offset: Int): Option[Interval] = {
    data.structure.flatMap(_.elements.find(node =>
      node.isInstanceOf[ReferenceNode] && node.span.includes(offset))).map(_.span.interval)
  }

  def paint(g: Graphics2D, bounds: Rectangle): Unit = ()

  override def decorations: Map[Interval, Map[TextAttribute, Any]] =
    terminal.hover.flatMap(hoverInterval)
      .map(interval => (interval, hoverAttributes)).toMap
}
