/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.domain.subtitle.format

import com.teixeira0x.subtypo.domain.model.Cue
import com.teixeira0x.subtypo.utils.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.utils.TimeUtils.getMilliseconds
import org.junit.Assert.assertEquals
import org.junit.Test

private val SUBRIP_TEXT_1 =
  """
1
00:00:00,000 --> 00:00:21,807
First cue

2
00:00:22,800 --> 00:00:40,000
Second cue
Second cue second line
"""
    .trimIndent()
    .trim()

private val SUBRIP_TEXT_2 =
  """

1
00:00:00,000 --> 00:00:21,807
First cue





2
00:00:22,800 --> 00:00:40,000
Second cue
Second cue second line


"""

private val CUE_LIST =
  listOf(
    Cue(
      startTime = "00:00:00,000".getMilliseconds(),
      endTime = "00:00:21,807".getMilliseconds(),
      text = "First cue",
    ),
    Cue(
      startTime = "00:00:22,800".getMilliseconds(),
      endTime = "00:00:40,000".getMilliseconds(),
      text = "Second cue\nSecond cue second line",
    ),
  )

class SRTFormatTest {

  @Test
  fun `Test the cue list parser for format text`() {
    assertEquals(SUBRIP_TEXT_1, SRTFormat.toText(CUE_LIST))
  }

  @Test
  fun `Test the text parser for cue list`() {
    val cueListTest1 = SRTFormat.parseText(SUBRIP_TEXT_1)
    val cueListTest2 = SRTFormat.parseText(SUBRIP_TEXT_2)

    listCueListAssert(cueListTest1)
    listCueListAssert(cueListTest2)
  }

  private fun listCueListAssert(cues: List<Cue>) {
    assertEquals(2, cues.size)

    val cue1 = cues[0]

    assertEquals("00:00:00,000", cue1.startTime.getFormattedTime())
    assertEquals("00:00:21,807", cue1.endTime.getFormattedTime())
    assertEquals("First cue", cue1.text)

    val cue2 = cues[1]

    assertEquals("00:00:22,800", cue2.startTime.getFormattedTime())
    assertEquals("00:00:40,000", cue2.endTime.getFormattedTime())
    assertEquals("Second cue\nSecond cue second line", cue2.text)
  }
}
