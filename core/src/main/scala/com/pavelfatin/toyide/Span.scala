/*
 * Copyright (C) 2011 Pavel Fatin <http://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.toyide

/** A text span combines a text substring with its interval within a parent text.
  *
  * @param source     the (entire) parent text
  * @param interval   the interval to select a substring
  */
case class Span(source: CharSequence, interval: Interval) extends IntervalLike {
  def begin : Int = interval.begin
  def end   : Int = interval.end

  /** The text denoted by `source.subSequence(begin, end)`. */
  def text: String = source.subSequence(begin, end).toString

  def leftEdge: Span = Span(source, begin, begin)
}

object Span {
  def apply(source: CharSequence, begin: Int, end: Int): Span = Span(source, Interval(begin, end))
}