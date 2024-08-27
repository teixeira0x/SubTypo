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

package com.teixeira.subtitles.subtitle.models;

/**
 * @author Felipe Teixeira
 */
public class SyntaxError {

  private String message;
  private int line;

  public SyntaxError(String message, int line) {
    this.message = message;
    this.line = line;
  }

  public String getMessage() {
    return this.message;
  }

  public int getLine() {
    return this.line;
  }

  @Override
  public String toString() {
    return "Error[message=" + message + ", line=" + line + "]";
  }
}
