/*
 *  ActionProcessor.scala
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

package dotterweide.editor

import dotterweide.ObservableEvents

/** Implemented in the `ui` module as `Controller`. */
class ActionProcessor extends ObservableEvents[ActionProcessorEvent]

sealed trait ActionProcessorEvent

case class ActionStarted(immediate: Boolean) extends ActionProcessorEvent

case object ActionFinished extends ActionProcessorEvent
